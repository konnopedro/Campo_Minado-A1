package com.example.tungclock.viewmodel

import com.example.tungclock.data.FusoHorario

data class FusoUiState(
    val fusos: List<FusoHorario> = emptyList(),
    val carregando: Boolean = true,
    val erro: String? = null,
    val cidadeEdicao: String = "",
    val paisEdicao: String = "",
    val fusoEmEdicao: Int? = null
)
