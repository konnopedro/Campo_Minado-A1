package com.example.campominado.logic

/**
 * Uma casa da Praça. Imutável: para "mudar" uma casa criamos uma cópia (copy),
 * assim o Compose percebe que o estado mudou e redesenha a tela.
 */
data class Casa(
    val temGalho: Boolean = false,   // true = galho caído (a "mina")
    val vizinhos: Int = 0,           // quantos galhos há nas 8 casas ao redor
    val aberta: Boolean = false,
    val placa: Boolean = false       // placa de aviso = bandeira
)

enum class EstadoPartida { JOGANDO, VITORIA, DERROTA }

/**
 * O mapa inteiro. Sempre quadrado (tamanho x tamanho).
 * As casas ficam numa lista única: a casa (linha, coluna) está no índice linha * tamanho + coluna.
 *
 * galhosSorteados = false até o primeiro toque: os galhos só são sorteados depois dele,
 * para o primeiro toque ser sempre seguro.
 */
data class Tabuleiro(
    val tamanho: Int,
    val totalGalhos: Int,
    val casas: List<Casa>,
    val galhosSorteados: Boolean = false,
    val estado: EstadoPartida = EstadoPartida.JOGANDO
) {
    fun indice(linha: Int, coluna: Int) = linha * tamanho + coluna

    fun casa(linha: Int, coluna: Int): Casa = casas[indice(linha, coluna)]

    // Contador mostrado na tela: galhos que ainda "faltam" achar, considerando as placas postas.
    val galhosRestantes: Int
        get() = totalGalhos - casas.count { it.placa }

    val casasAbertas: Int
        get() = casas.count { it.aberta && !it.temGalho }
}
