package com.example.campominado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.repository.JogoRepository
import com.example.campominado.data.repository.Resultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Quem está usando o app agora. As telas e a Bottom Bar mudam conforme este valor. */
enum class Perfil { NENHUM, JOGADOR, ADMIN }

/**
 * Tudo que as telas de Login (e o resto do app) precisam saber sobre a sessão.
 * É um data class imutável: o ViewModel troca o estado inteiro por uma cópia (copy).
 */
data class SessaoUiState(
    val perfil: Perfil = Perfil.NENHUM,
    val jogadorId: Long? = null,
    val jogadorNome: String = "",
    val nivelMaximoDesbloqueado: Int = 1,
    val jogadoresSalvos: List<JogadorEntity> = emptyList(),  // lista mostrada no Login
    val erro: String? = null
) {
    val ehAdmin: Boolean get() = perfil == Perfil.ADMIN
    val ehJogador: Boolean get() = perfil == Perfil.JOGADOR
}

/**
 * Cuida do Login e de quem está logado (Jogador ou Admin).
 * Não fala com o banco: pede tudo ao JogoRepository.
 */
class SessaoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SessaoUiState())
    val uiState: StateFlow<SessaoUiState> = _uiState.asStateFlow()

    // Coleta do jogador logado; guardamos o Job para cancelar ao trocar de jogador ou sair.
    private var jobJogador: Job? = null

    init {
        // Lista de jogadores salvos: o Flow do Room atualiza a tela sozinho.
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

    /** Entra como Jogador. Se o nome não existe, cadastra; se existe, usa o perfil salvo. */
    fun entrarComoJogador(nome: String) {
        viewModelScope.launch {
            when (val resultado = repository.entrarOuCadastrar(nome)) {
                is Resultado.Sucesso -> iniciarSessaoDoJogador(resultado.dado)
                is Resultado.Erro -> _uiState.update { it.copy(erro = resultado.mensagem) }
            }
        }
    }

    /** Admin simulado: o PDF não exige login de verdade, então usuário e senha são fixos. */
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
        // Mantém a lista de jogadores salvos; zera o resto.
        _uiState.update { SessaoUiState(jogadoresSalvos = it.jogadoresSalvos) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    // Passa a observar o jogador no banco: se o nível desbloqueado ou o nome mudarem
    // (ao vencer uma partida, ao renomear), a sessão atualiza sozinha.
    private fun iniciarSessaoDoJogador(id: Long) {
        _uiState.update { it.copy(perfil = Perfil.JOGADOR, jogadorId = id, erro = null) }
        jobJogador?.cancel()
        jobJogador = viewModelScope.launch {
            repository.observarJogador(id).collect { resultado ->
                when (resultado) {
                    is Resultado.Sucesso -> {
                        val jogador = resultado.dado
                        if (jogador == null) {
                            sair()  // o perfil foi excluído
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
