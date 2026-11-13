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

enum class FiltroResultado { TODAS, VITORIAS, DERROTAS }

data class RecordeItem(val partida: PartidaEntity, val nomeJogador: String)

data class HistoricoUiState(
    val jogadorNome: String = "",

    val partidas: List<PartidaEntity> = emptyList(),
    val filtroResultado: FiltroResultado = FiltroResultado.TODAS,

    val recordes: List<RecordeItem> = emptyList(),
    val filtroMapa: Int? = null,
    val tamanhosDeMapa: List<Int> = listOf(6, 7, 8, 9, 10),

    val melhoresTempos: Map<Int, Int> = emptyMap(),
    val erro: String? = null
)

class HistoricoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoUiState())
    val uiState: StateFlow<HistoricoUiState> = _uiState.asStateFlow()

    private var partidasBrutas: List<PartidaEntity> = emptyList()
    private var vitoriasBrutas: List<PartidaEntity> = emptyList()
    private var jogadores: List<JogadorEntity> = emptyList()
    private var jogadorAtual: JogadorEntity? = null
    private var jobJogador: Job? = null

    init {

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

    fun definirJogador(jogadorId: Long) {
        jobJogador?.cancel()

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

    fun anotarPartida(partida: PartidaEntity, anotacao: String) {
        viewModelScope.launch { mostrarErro(repository.atualizarPartida(partida.copy(anotacao = anotacao.trim()))) }
    }

    fun renomearPerfil(novoNome: String) {
        val jogador = jogadorAtual ?: return
        viewModelScope.launch { mostrarErro(repository.renomearJogador(jogador, novoNome)) }
    }

    fun excluirPerfil() {
        val jogador = jogadorAtual ?: return
        viewModelScope.launch { mostrarErro(repository.excluirJogador(jogador)) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    private fun mostrarErro(resultado: Resultado<*>) {
        if (resultado is Resultado.Erro) _uiState.update { it.copy(erro = resultado.mensagem) }
    }

    private fun recalcular() {
        val estado = _uiState.value

        val partidas = partidasBrutas.filter { p ->
            when (estado.filtroResultado) {
                FiltroResultado.TODAS -> true
                FiltroResultado.VITORIAS -> p.venceu
                FiltroResultado.DERROTAS -> !p.venceu
            }
        }

        val nomes = jogadores.associate { it.id to it.nome }
        val recordes = vitoriasBrutas
            .groupBy { it.jogadorId to it.nivel }
            .map { (_, partidasDoNivel) -> partidasDoNivel.minBy { it.tempoSegundos } }
            .filter { estado.filtroMapa == null || it.tamanhoMapa == estado.filtroMapa }
            .sortedBy { it.tempoSegundos }
            .map { RecordeItem(it, nomes[it.jogadorId] ?: "Jogador removido") }

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
