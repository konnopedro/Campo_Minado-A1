package com.example.tungclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tungclock.data.FusoHorario
import com.example.tungclock.data.repository.FusoHorarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FusoViewModel(private val repository: FusoHorarioRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FusoUiState())
    val uiState: StateFlow<FusoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.buscarTodos().collect { fusos ->
                    _uiState.update { it.copy(fusos = fusos, carregando = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(carregando = false, erro = e.message ?: "Erro ao buscar fusos") }
            }
        }
    }

    fun iniciarEdicao(fuso: FusoHorario) {
        _uiState.update { it.copy(fusoEmEdicao = fuso.id, cidadeEdicao = fuso.cidade, paisEdicao = fuso.pais, erro = null) }
    }

    fun alterarCidade(valor: String) {
        _uiState.update { it.copy(cidadeEdicao = valor) }
    }

    fun alterarPais(valor: String) {
        _uiState.update { it.copy(paisEdicao = valor) }
    }

    fun cancelarEdicao() {
        _uiState.update { it.copy(fusoEmEdicao = null, cidadeEdicao = "", paisEdicao = "") }
    }

    fun adicionarFuso(fuso: FusoHorario, aoSalvar: () -> Unit = {}) = executar(aoSalvar) { repository.inserir(fuso) }
    fun atualizarFuso(fuso: FusoHorario, aoSalvar: () -> Unit = {}) = executar(aoSalvar) { repository.atualizar(fuso) }
    fun removerFuso(fuso: FusoHorario, aoExcluir: () -> Unit = {}) = executar(aoExcluir) { repository.excluir(fuso) }

    private fun executar(aoConcluir: () -> Unit = {}, operacao: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operacao()
                _uiState.update { it.copy(erro = null) }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message ?: "Erro ao salvar fuso") }
            }
        }
    }
}
