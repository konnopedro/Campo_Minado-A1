package com.example.campominado.data.remote

data class NivelRemoto(
    val nivel: Int,
    val tamanhoMapa: Int,
    val galhos: Int,
    val ativo: Boolean = true
)
