package com.example.campominado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.local.entity.PartidaEntity
import com.example.campominado.data.repository.JogoRepository
import com.example.campominado.data.repository.Resultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filtro do Histórico. */
enum class FiltroResultado { TODAS, VITORIAS, DERROTAS }

/** Uma linha da tela de Recordes: a melhor partida de um jogador num nível. */
data class RecordeItem(val partida: PartidaEntity, val nomeJogador: String)

/** Estado das telas Recordes, Histórico e Perfil (e dos tempos mostrados na tela de Níveis). */
data class HistoricoUiState(
    val jogadorNome: String = "",
    // Histórico (já filtrado)
    val partidas: List<PartidaEntity> = emptyList(),
    val filtroResultado: FiltroResultado = FiltroResultado.TODAS,
    // Recordes (já filtrados e ordenados)
    val recordes: List<RecordeItem> = emptyList(),
    val filtroMapa: Int? = null,                     // null = todos os tamanhos
    val tamanhosDeMapa: List<Int> = listOf(6, 7, 8, 9, 10),
    // Melhor tempo do jogador logado em cada nível (nível -> segundos), para a tela de Níveis
    val melhoresTempos: Map<Int, Int> = emptyMap(),
    val erro: String? = null
)

/**
 * Cuida do Histórico, dos Recordes e do Perfil.
 * Os filtros e a ordenação são feitos AQUI, com filter / map / sortedBy e lambdas (exigência do professor);
 * o Room só entrega as listas "cruas".
 */
class HistoricoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoUiState())
    val uiState: StateFlow<HistoricoUiState> = _uiState.asStateFlow()

    // Listas cruas vindas do Room. Sempre que uma muda (ou um filtro muda), recalculamos o estado.
    private var partidasBrutas: List<PartidaEntity> = emptyList()
    private var vitoriasBrutas: List<PartidaEntity> = emptyList()
    private var jogadores: List<JogadorEntity> = emptyList()
    private var jogadorAtual: JogadorEntity? = null
    private var jobJogador: Job? = null

    init {
        // Recordes dependem de todas as vitórias e dos nomes dos jogadores.
        viewModelScope.launch {
            repository.listarVitorias().collect { r ->
                if (r is Resultado.Sucesso) { vitoriasBrutas = r.dado; recalcular() } else mostrarErro(r)
            }
        }
        viewModelScope.launch {
            repository.listarJogadores().collect { r ->
                if (r is Resultado.Sucesso) { jogadores = r.dado; recalcular() } else mostrarErro(r)
            }
        }
    }

    /** Chamado quando um jogador entra: passa a observar o histórico e o perfil dele. */
    fun definirJogador(jogadorId: Long) {
        jobJogador?.cancel()
        // Limpa os dados do jogador anterior, para não aparecerem por um instante na tela do novo.
        partidasBrutas = emptyList()
        jogadorAtual = null
        recalcular()
        jobJogador = viewModelScope.launch {
            launch {
                repository.listarPartidasDoJogador(jogadorId).collect { r ->
                    if (r is Resultado.Sucesso) { partidasBrutas = r.dado; recalcular() } else mostrarErro(r)
                }
            }
            repository.observarJogador(jogadorId).collect { r ->
                if (r is Resultado.Sucesso) { jogadorAtual = r.dado; recalcular() } else mostrarErro(r)
            }
        }
    }

    fun filtrarResultado(filtro: FiltroResultado) {
        _uiState.update { it.copy(filtroResultado = filtro) }
        recalcular()
    }

    fun filtrarMapa(tamanho: Int?) {
        _uiState.update { it.copy(filtroMapa = tamanho) }
        recalcular()
    }

    fun excluirPartida(partida: PartidaEntity) {
        viewModelScope.launch { mostrarErro(repository.excluirPartida(partida)) }
    }

    fun renomearPerfil(novoNome: String) {
        val jogador = jogadorAtual ?: return
        viewModelScope.launch { mostrarErro(repository.renomearJogador(jogador, novoNome)) }
    }

    /** Excluir o perfil apaga também as partidas (CASCADE). A SessaoViewModel percebe e faz o logout. */
    fun excluirPerfil() {
        val jogador = jogadorAtual ?: return
        viewModelScope.launch { mostrarErro(repository.excluirJogador(jogador)) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    // Só mostra algo se o resultado for um erro (sucesso não faz nada).
    private fun mostrarErro(resultado: Resultado<*>) {
        if (resultado is Resultado.Erro) _uiState.update { it.copy(erro = resultado.mensagem) }
    }

    // Refaz tudo que a tela mostra a partir das listas cruas e dos filtros atuais.
    private fun recalcular() {
        val estado = _uiState.value

        // Histórico: filtra vitórias/derrotas (a lista já vem da mais recente para a mais antiga).
        val partidas = partidasBrutas.filter { p ->
            when (estado.filtroResultado) {
                FiltroResultado.TODAS -> true
                FiltroResultado.VITORIAS -> p.venceu
                FiltroResultado.DERROTAS -> !p.venceu
            }
        }

        // Recordes: melhor tempo de cada jogador em cada nível, filtrado por mapa, do mais rápido ao mais lento.
        val nomes = jogadores.associate { it.id to it.nome }
        val recordes = vitoriasBrutas
            .groupBy { it.jogadorId to it.nivel }
            .map { (_, partidasDoNivel) -> partidasDoNivel.minBy { it.tempoSegundos } }
            .filter { estado.filtroMapa == null || it.tamanhoMapa == estado.filtroMapa }
            .sortedBy { it.tempoSegundos }
            .map { RecordeItem(it, nomes[it.jogadorId] ?: "Jogador removido") }

        // Melhor tempo do jogador logado em cada nível (para a tela de Níveis).
        val idAtual = jogadorAtual?.id
        val melhores = vitoriasBrutas
            .filter { it.jogadorId == idAtual }
            .groupBy { it.nivel }
            .mapValues { (_, lista) -> lista.minOf { it.tempoSegundos } }

        _uiState.update {
            it.copy(
                jogadorNome = jogadorAtual?.nome ?: "",
                partidas = partidas,
                recordes = recordes,
                melhoresTempos = melhores
            )
        }
    }
}
