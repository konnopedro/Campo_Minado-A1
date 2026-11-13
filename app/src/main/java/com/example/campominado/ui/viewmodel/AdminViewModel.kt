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

data class AdminUiState(
    val carregando: Boolean = false,
    val niveis: List<NivelRemoto> = emptyList(),
    val filtroMapa: Int? = null,
    val tamanhosDeMapa: List<Int> = listOf(6, 7, 8, 9, 10),
    val nivelEmEdicao: NivelRemoto? = null,
    val salvando: Boolean = false,
    val salvoComSucesso: Boolean = false,
    val erro: String? = null
) {

    val niveisFiltrados: List<NivelRemoto>
        get() = niveis.filter { filtroMapa == null || it.tamanhoMapa == filtroMapa }
}

class AdminViewModel(private val repository: JogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

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

    fun editarNivel(nivel: NivelRemoto) {
        _uiState.update { it.copy(nivelEmEdicao = nivel, salvoComSucesso = false, erro = null) }
    }

    fun salvarNivel(galhos: Int, ativo: Boolean) {
        val nivel = _uiState.value.nivelEmEdicao ?: return
        if (_uiState.value.salvando) return
        _uiState.update { it.copy(salvando = true, erro = null) }
        viewModelScope.launch {
            val atualizado = nivel.copy(galhos = galhos, ativo = ativo)
            when (val resultado = repository.salvarNivel(atualizado)) {
                is Resultado.Sucesso -> _uiState.update { estado ->
                    estado.copy(

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
