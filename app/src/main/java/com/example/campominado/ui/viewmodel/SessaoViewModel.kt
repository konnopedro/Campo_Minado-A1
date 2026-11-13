package com.example.campominado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.data.repository.JogoRepository
import com.example.campominado.data.repository.Resultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Perfil { NENHUM, JOGADOR, ADMIN }

data class SessaoUiState(
    val perfil: Perfil = Perfil.NENHUM,
    val jogadorId: Long? = null,
    val jogadorNome: String = "",
    val nivelMaximoDesbloqueado: Int = 1,
    val jogadoresSalvos: List<JogadorEntity> = emptyList(),
    val erro: String? = null,

    val niveis: List<NivelRemoto> = emptyList(),
    val carregandoNiveis: Boolean = false,
    val avisoNiveis: String? = null
) {
    val ehAdmin: Boolean get() = perfil == Perfil.ADMIN
    val ehJogador: Boolean get() = perfil == Perfil.JOGADOR
}

class SessaoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SessaoUiState())
    val uiState: StateFlow<SessaoUiState> = _uiState.asStateFlow()

    private var jobJogador: Job? = null

    init {

        viewModelScope.launch {
            repository.listarJogadores().collect { resultado ->
                when (resultado) {
                    is Resultado.Sucesso ->
                        _uiState.update { it.copy(jogadoresSalvos = resultado.dado) }
                    is Resultado.Erro ->
                        _uiState.update { it.copy(erro = resultado.mensagem) }
                }
            }
        }
    }

    fun entrarComoJogador(nome: String) {
        viewModelScope.launch {
            when (val resultado = repository.entrarOuCadastrar(nome)) {
                is Resultado.Sucesso -> iniciarSessaoDoJogador(resultado.dado)
                is Resultado.Erro -> _uiState.update { it.copy(erro = resultado.mensagem) }
            }
        }
    }

    fun entrarComoAdmin(usuario: String, senha: String) {
        if (usuario.trim() == USUARIO_ADMIN && senha == SENHA_ADMIN) {
            jobJogador?.cancel()
            _uiState.update { it.copy(perfil = Perfil.ADMIN, jogadorId = null, jogadorNome = "Administrador", erro = null) }
        } else {
            _uiState.update { it.copy(erro = "Usuário ou senha do administrador incorretos.") }
        }
    }

    fun sair() {
        jobJogador?.cancel()

        _uiState.update { SessaoUiState(jogadoresSalvos = it.jogadoresSalvos) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    fun carregarNiveis() {
        _uiState.update { it.copy(carregandoNiveis = true) }
        viewModelScope.launch {
            val carregados = repository.carregarNiveis()
            _uiState.update {
                it.copy(niveis = carregados.niveis, carregandoNiveis = false, avisoNiveis = carregados.erro)
            }
        }
    }

    private fun iniciarSessaoDoJogador(id: Long) {
        _uiState.update { it.copy(perfil = Perfil.JOGADOR, jogadorId = id, erro = null) }
        jobJogador?.cancel()
        jobJogador = viewModelScope.launch {
            repository.observarJogador(id).collect { resultado ->
                when (resultado) {
                    is Resultado.Sucesso -> {
                        val jogador = resultado.dado
                        if (jogador == null) {
                            sair()
                        } else {
                            _uiState.update {
                                it.copy(
                                    jogadorNome = jogador.nome,
                                    nivelMaximoDesbloqueado = jogador.nivelMaximoDesbloqueado
                                )
                            }
                        }
                    }
                    is Resultado.Erro -> _uiState.update { it.copy(erro = resultado.mensagem) }
                }
            }
        }
    }

    companion object {
        const val USUARIO_ADMIN = "admin"
        const val SENHA_ADMIN = "admin123"
    }
}
