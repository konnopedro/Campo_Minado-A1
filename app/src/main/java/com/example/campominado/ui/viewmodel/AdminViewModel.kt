package com.example.campominado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.data.repository.JogoRepository
import com.example.campominado.data.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado do Painel do Admin e da tela Editar nível. */
data class AdminUiState(
    val carregando: Boolean = false,
    val niveis: List<NivelRemoto> = emptyList(),      // os 15 níveis
    val filtroMapa: Int? = null,                      // null = todos os tamanhos
    val tamanhosDeMapa: List<Int> = listOf(6, 7, 8, 9, 10),
    val nivelEmEdicao: NivelRemoto? = null,           // nível aberto na tela Editar
    val salvando: Boolean = false,                    // true enquanto espera o Firestore confirmar
    val salvoComSucesso: Boolean = false,             // a tela Editar volta quando vira true
    val erro: String? = null
) {
    // A lista que o painel mostra: filtrada por tamanho de mapa, se houver filtro.
    val niveisFiltrados: List<NivelRemoto>
        get() = niveis.filter { filtroMapa == null || it.tamanhoMapa == filtroMapa }
}

/** Cuida das telas do Admin. Só o Admin chega aqui (a navegação esconde isso do Jogador). */
class AdminViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    /** Carrega os níveis do Firestore. Na primeira vez cria os 15 níveis padrão no servidor. */
    fun carregar() {
        _uiState.update { it.copy(carregando = true) }
        viewModelScope.launch {
            val carregados = repository.carregarNiveis(criarSeVazio = true)
            _uiState.update { it.copy(carregando = false, niveis = carregados.niveis, erro = carregados.erro) }
        }
    }

    fun filtrarMapa(tamanho: Int?) {
        _uiState.update { it.copy(filtroMapa = tamanho) }
    }

    /** Abre a tela Editar com o nível escolhido. */
    fun editarNivel(nivel: NivelRemoto) {
        _uiState.update { it.copy(nivelEmEdicao = nivel, salvoComSucesso = false, erro = null) }
    }

    /** Salva a mudança no Firestore. O Repository valida o limite de galhos (1 até 25% das casas). */
    fun salvarNivel(galhos: Int, ativo: Boolean) {
        val nivel = _uiState.value.nivelEmEdicao ?: return
        if (_uiState.value.salvando) return              // ignora toque duplo no Salvar
        _uiState.update { it.copy(salvando = true, erro = null) }
        viewModelScope.launch {
            val atualizado = nivel.copy(galhos = galhos, ativo = ativo)
            when (val resultado = repository.salvarNivel(atualizado)) {
                is Resultado.Sucesso -> _uiState.update { estado ->
                    estado.copy(
                        // Troca só o nível editado na lista, sem precisar recarregar tudo.
                        niveis = estado.niveis.map { if (it.nivel == atualizado.nivel) atualizado else it },
                        salvando = false,
                        salvoComSucesso = true,
                        erro = null
                    )
                }
                is Resultado.Erro -> _uiState.update { it.copy(salvando = false, erro = resultado.mensagem) }
            }
        }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }
}
