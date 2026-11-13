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

data class JogoUiState(
    val carregando: Boolean = false,
    val nivel: Int = 1,
    val tabuleiro: Tabuleiro? = null,
    val segundos: Int = 0,
    val modoPlaca: Boolean = false,
    val partidaSalva: Boolean = false,
    val temProximoNivel: Boolean = false,
    val erro: String? = null
) {
    val estado: EstadoPartida get() = tabuleiro?.estado ?: EstadoPartida.JOGANDO
    val terminou: Boolean get() = estado != EstadoPartida.JOGANDO
    val venceu: Boolean get() = estado == EstadoPartida.VITORIA
}

class JogoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(JogoUiState())
    val uiState: StateFlow<JogoUiState> = _uiState.asStateFlow()

    private var jogadorId: Long = 0
    private var jobCronometro: Job? = null

    fun iniciar(nivel: Int, jogadorId: Long) {
        this.jogadorId = jogadorId
        pararCronometro()
        _uiState.value = JogoUiState(carregando = true, nivel = nivel)

        viewModelScope.launch {
            val carregados = repository.niveisParaJogar()
            val config = carregados.niveis.firstOrNull { it.nivel == nivel }

            if (config == null || !config.ativo) {
                _uiState.value = JogoUiState(nivel = nivel, erro = "Este nível não está disponível.")
                return@launch
            }
            _uiState.value = JogoUiState(
                nivel = nivel,
                tabuleiro = RegrasJogo.criarTabuleiro(config.tamanhoMapa, config.galhos),
                temProximoNivel = nivel < JogoRepository.NIVEL_MAXIMO,

                erro = carregados.erro
            )
        }
    }

    fun jogarDeNovo() = iniciar(_uiState.value.nivel, jogadorId)

    fun irParaProximoNivel() = iniciar(_uiState.value.nivel + 1, jogadorId)

    fun alternarModo() {
        _uiState.update { it.copy(modoPlaca = !it.modoPlaca) }
    }

    fun aoTocar(linha: Int, coluna: Int) {
        val atual = _uiState.value
        val tabuleiro = atual.tabuleiro ?: return
        val novo = if (atual.modoPlaca) RegrasJogo.alternarPlaca(tabuleiro, linha, coluna)
        else RegrasJogo.abrir(tabuleiro, linha, coluna)
        aplicar(novo)
    }

    fun aoTocarLongo(linha: Int, coluna: Int) {
        val tabuleiro = _uiState.value.tabuleiro ?: return
        aplicar(RegrasJogo.alternarPlaca(tabuleiro, linha, coluna))
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    private fun aplicar(novo: Tabuleiro) {
        val antes = _uiState.value.tabuleiro
        if (novo === antes) return
        _uiState.update { it.copy(tabuleiro = novo) }

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

    private fun salvarPartida(tabuleiro: Tabuleiro) {
        val atual = _uiState.value
        if (atual.partidaSalva) return
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

    override fun onCleared() {
        pararCronometro()
    }
}
