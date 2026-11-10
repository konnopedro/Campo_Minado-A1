package com.example.tungclock.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tungclock.data.Alarme
import com.example.tungclock.data.repository.AlarmeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmeViewModel(private val repository: AlarmeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AlarmeUiState())
    val uiState: StateFlow<AlarmeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.buscarTodos().collect { alarmes ->
                    _uiState.update { it.copy(alarmes = alarmes, carregando = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(carregando = false, erro = e.message ?: "Erro ao buscar alarmes") }
            }
        }
    }

    fun reiniciarFormulario() {
        _uiState.update { it.copy(
            horarioFormulario = TextFieldValue("07:00"), nomeFormulario = "",
            diasFormulario = setOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"),
            erro = null
        ) }
    }

    fun alterarHorarioFormulario(valor: TextFieldValue) {
        _uiState.update { it.copy(horarioFormulario = valor) }
    }

    fun alterarNomeFormulario(valor: String) {
        _uiState.update { it.copy(nomeFormulario = valor) }
    }

    fun alterarDiaFormulario(dia: String) {
        _uiState.update {
            it.copy(diasFormulario = if (dia in it.diasFormulario) it.diasFormulario - dia else it.diasFormulario + dia)
        }
    }

    fun iniciarEdicao(alarme: Alarme, dias: Set<String>) {
        _uiState.update {
            it.copy(
                alarmeEmEdicao = alarme.id,
                horarioEdicao = TextFieldValue(alarme.horario),
                nomeEdicao = alarme.nome,
                diasEdicao = dias,
                ativoEdicao = alarme.ativo
            )
        }
    }

    fun alterarHorarioEdicao(valor: TextFieldValue) {
        _uiState.update { it.copy(horarioEdicao = valor) }
    }

    fun alterarNomeEdicao(valor: String) {
        _uiState.update { it.copy(nomeEdicao = valor) }
    }

    fun alterarDiaEdicao(dia: String) {
        _uiState.update {
            it.copy(diasEdicao = if (dia in it.diasEdicao) it.diasEdicao - dia else it.diasEdicao + dia)
        }
    }

    fun alterarAtivoEdicao(valor: Boolean) {
        _uiState.update { it.copy(ativoEdicao = valor) }
    }

    fun adicionarAlarme(alarme: Alarme, aoSalvar: () -> Unit = {}) = executar(aoSalvar) { repository.inserir(alarme) }
    fun atualizarAlarme(alarme: Alarme, aoSalvar: () -> Unit = {}) = executar(aoSalvar) { repository.atualizar(alarme) }
    fun removerAlarme(alarme: Alarme, aoExcluir: () -> Unit = {}) = executar(aoExcluir) { repository.excluir(alarme) }

    private fun executar(aoConcluir: () -> Unit = {}, operacao: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operacao()
                _uiState.update { it.copy(erro = null) }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message ?: "Erro ao salvar alarme") }
            }
        }
    }
}
