package com.example.campominado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campominado.data.local.entity.PartidaEntity
import com.example.campominado.data.repository.JogoRepository
import com.example.campominado.data.repository.Resultado
import com.example.campominado.logic.EstadoPartida
import com.example.campominado.logic.RegrasJogo
import com.example.campominado.logic.Tabuleiro
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado das telas Jogo e Resultado. */
data class JogoUiState(
    val carregando: Boolean = false,
    val nivel: Int = 1,
    val tabuleiro: Tabuleiro? = null,     // null enquanto o nível não foi carregado
    val segundos: Int = 0,                // cronômetro
    val modoPlaca: Boolean = false,       // false = botão em "Abrir"; true = em "Placa"
    val partidaSalva: Boolean = false,
    val temProximoNivel: Boolean = false,
    val erro: String? = null
) {
    val estado: EstadoPartida get() = tabuleiro?.estado ?: EstadoPartida.JOGANDO
    val terminou: Boolean get() = estado != EstadoPartida.JOGANDO
    val venceu: Boolean get() = estado == EstadoPartida.VITORIA
}

/**
 * Controla uma partida: carrega o nível, recebe os toques, conta o tempo e salva o resultado.
 * As regras em si (cascata, vitória...) ficam em RegrasJogo, em Kotlin puro.
 */
class JogoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(JogoUiState())
    val uiState: StateFlow<JogoUiState> = _uiState.asStateFlow()

    private var jogadorId: Long = 0
    private var jobCronometro: Job? = null

    /** Começa (ou recomeça) um nível. Busca os galhos no Firestore, com os valores padrão de reserva. */
    fun iniciar(nivel: Int, jogadorId: Long) {
        this.jogadorId = jogadorId
        pararCronometro()
        _uiState.value = JogoUiState(carregando = true, nivel = nivel)

        viewModelScope.launch {
            val carregados = repository.carregarNiveis()
            val config = carregados.niveis.firstOrNull { it.nivel == nivel }

            if (config == null || !config.ativo) {
                _uiState.value = JogoUiState(nivel = nivel, erro = "Este nível não está disponível.")
                return@launch
            }
            _uiState.value = JogoUiState(
                nivel = nivel,
                tabuleiro = RegrasJogo.criarTabuleiro(config.tamanhoMapa, config.galhos),
                temProximoNivel = nivel < JogoRepository.NIVEL_MAXIMO,
                // Se o Firestore falhou, o aviso aparece, mas dá para jogar com os valores padrão.
                erro = carregados.erro
            )
        }
    }

    fun jogarDeNovo() = iniciar(_uiState.value.nivel, jogadorId)

    fun irParaProximoNivel() = iniciar(_uiState.value.nivel + 1, jogadorId)

    /** O botão "Abrir / Placa" troca entre os dois modos. */
    fun alternarModo() {
        _uiState.update { it.copy(modoPlaca = !it.modoPlaca) }
    }

    /** Toque curto numa casa: abre ou põe placa, conforme o modo escolhido no botão. */
    fun aoTocar(linha: Int, coluna: Int) {
        val atual = _uiState.value
        val tabuleiro = atual.tabuleiro ?: return
        val novo = if (atual.modoPlaca) RegrasJogo.alternarPlaca(tabuleiro, linha, coluna)
        else RegrasJogo.abrir(tabuleiro, linha, coluna)
        aplicar(novo)
    }

    /** Toque longo: sempre põe/tira a placa, qualquer que seja o modo. */
    fun aoTocarLongo(linha: Int, coluna: Int) {
        val tabuleiro = _uiState.value.tabuleiro ?: return
        aplicar(RegrasJogo.alternarPlaca(tabuleiro, linha, coluna))
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    private fun aplicar(novo: Tabuleiro) {
        val antes = _uiState.value.tabuleiro
        if (novo === antes) return                    // o toque não mudou nada
        _uiState.update { it.copy(tabuleiro = novo) }

        // O cronômetro começa no primeiro toque que abre uma casa (quando os galhos são sorteados).
        if (antes?.galhosSorteados == false && novo.galhosSorteados) iniciarCronometro()

        if (novo.estado != EstadoPartida.JOGANDO) {
            pararCronometro()
            salvarPartida(novo)
        }
    }

    private fun iniciarCronometro() {
        jobCronometro?.cancel()
        jobCronometro = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(segundos = it.segundos + 1) }
            }
        }
    }

    private fun pararCronometro() {
        jobCronometro?.cancel()
        jobCronometro = null
    }

    // Grava no Room (Histórico e Recordes). Se o jogador venceu, o Repository desbloqueia o próximo nível.
    private fun salvarPartida(tabuleiro: Tabuleiro) {
        val atual = _uiState.value
        if (atual.partidaSalva) return                // evita salvar duas vezes
        _uiState.update { it.copy(partidaSalva = true) }

        viewModelScope.launch {
            val partida = PartidaEntity(
                jogadorId = jogadorId,
                nivel = atual.nivel,
                tamanhoMapa = tabuleiro.tamanho,
                totalGalhos = tabuleiro.totalGalhos,
                casasAbertas = tabuleiro.casasAbertas,
                venceu = tabuleiro.estado == EstadoPartida.VITORIA,
                tempoSegundos = atual.segundos
            )
            val resultado = repository.registrarPartida(partida)
            if (resultado is Resultado.Erro) {
                _uiState.update { it.copy(erro = resultado.mensagem) }
            }
        }
    }

    // Se a tela sair, o cronômetro não pode continuar rodando.
    override fun onCleared() {
        pararCronometro()
    }
}
