package com.example.campominado.logic

data class Casa(
    val temGalho: Boolean = false,
    val vizinhos: Int = 0,
    val aberta: Boolean = false,
    val placa: Boolean = false
)

enum class EstadoPartida { JOGANDO, VITORIA, DERROTA }

data class Tabuleiro(
    val tamanho: Int,
    val totalGalhos: Int,
    val casas: List<Casa>,
    val galhosSorteados: Boolean = false,
    val estado: EstadoPartida = EstadoPartida.JOGANDO
) {
    fun indice(linha: Int, coluna: Int) = linha * tamanho + coluna

    fun casa(linha: Int, coluna: Int): Casa = casas[indice(linha, coluna)]

    val galhosRestantes: Int
        get() = totalGalhos - casas.count { it.placa }

    val casasAbertas: Int
        get() = casas.count { it.aberta && !it.temGalho }
}
