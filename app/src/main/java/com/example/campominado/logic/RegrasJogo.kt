package com.example.campominado.logic

import kotlin.random.Random

/**
 * Regras do Campo Minado em Kotlin puro (nada de Android aqui).
 * Todas as funções recebem um Tabuleiro e devolvem um Tabuleiro NOVO; o original não é alterado.
 */
object RegrasJogo {

    /** Mapa vazio, ainda sem galhos (eles são sorteados no primeiro toque). */
    fun criarTabuleiro(tamanho: Int, totalGalhos: Int): Tabuleiro =
        Tabuleiro(tamanho, totalGalhos, List(tamanho * tamanho) { Casa() })

    /** Abre uma casa. É o coração do jogo. */
    fun abrir(
        tabuleiro: Tabuleiro,
        linha: Int,
        coluna: Int,
        random: Random = Random.Default
    ): Tabuleiro {
        if (tabuleiro.estado != EstadoPartida.JOGANDO) return tabuleiro

        val casaTocada = tabuleiro.casa(linha, coluna)
        // Casa já aberta ou com placa: o toque não faz nada (a placa protege a casa).
        if (casaTocada.aberta || casaTocada.placa) return tabuleiro

        // Primeiro toque: agora sim sorteia os galhos, longe da casa tocada.
        val base = if (tabuleiro.galhosSorteados) tabuleiro
        else sortearGalhos(tabuleiro, linha, coluna, random)

        val casa = base.casa(linha, coluna)

        // Abriu um galho: derrota na hora (sem vidas).
        if (casa.temGalho) {
            val casas = base.casas.toMutableList()
            casas[base.indice(linha, coluna)] = casa.copy(aberta = true)
            return base.copy(casas = casas, estado = EstadoPartida.DERROTA)
        }

        val aberto = abrirComCascata(base, linha, coluna)
        return aberto.copy(estado = if (venceu(aberto)) EstadoPartida.VITORIA else EstadoPartida.JOGANDO)
    }

    /** Põe ou tira a placa de aviso. Casa já aberta não aceita placa. */
    fun alternarPlaca(tabuleiro: Tabuleiro, linha: Int, coluna: Int): Tabuleiro {
        if (tabuleiro.estado != EstadoPartida.JOGANDO) return tabuleiro
        val casa = tabuleiro.casa(linha, coluna)
        if (casa.aberta) return tabuleiro

        val casas = tabuleiro.casas.toMutableList()
        casas[tabuleiro.indice(linha, coluna)] = casa.copy(placa = !casa.placa)
        return tabuleiro.copy(casas = casas)
    }

    /** Vitória: todas as casas SEM galho estão abertas. */
    fun venceu(tabuleiro: Tabuleiro): Boolean =
        tabuleiro.galhosSorteados && tabuleiro.casas.all { it.temGalho || it.aberta }

    // As 8 casas ao redor (ou menos, nas bordas do mapa), como pares (linha, coluna).
    fun vizinhas(tamanho: Int, linha: Int, coluna: Int): List<Pair<Int, Int>> {
        val lista = mutableListOf<Pair<Int, Int>>()
        for (dl in -1..1) {
            for (dc in -1..1) {
                if (dl == 0 && dc == 0) continue          // a própria casa não conta
                val l = linha + dl
                val c = coluna + dc
                if (l in 0 until tamanho && c in 0 until tamanho) lista.add(Pair(l, c))
            }
        }
        return lista
    }

    // Sorteia os galhos fora da casa tocada e das suas vizinhas, depois conta os vizinhos de cada casa.
    private fun sortearGalhos(tabuleiro: Tabuleiro, linha: Int, coluna: Int, random: Random): Tabuleiro {
        val tamanho = tabuleiro.tamanho
        val proibidas = vizinhas(tamanho, linha, coluna).map { tabuleiro.indice(it.first, it.second) } +
            tabuleiro.indice(linha, coluna)

        // Índices permitidos, embaralhados; os primeiros "totalGalhos" viram galhos.
        val sorteados = tabuleiro.casas.indices
            .filter { it !in proibidas }
            .shuffled(random)
            .take(tabuleiro.totalGalhos)
            .toSet()

        // 1º passo: marca quem tem galho.
        val comGalhos = tabuleiro.casas.mapIndexed { i, casa -> casa.copy(temGalho = i in sorteados) }

        // 2º passo: conta os galhos vizinhos de cada casa.
        val comContagem = comGalhos.mapIndexed { i, casa ->
            val l = i / tamanho
            val c = i % tamanho
            val qtd = vizinhas(tamanho, l, c).count { (vl, vc) -> comGalhos[vl * tamanho + vc].temGalho }
            casa.copy(vizinhos = qtd)
        }
        return tabuleiro.copy(casas = comContagem, galhosSorteados = true)
    }

    // Abre a casa; se ela tem 0 galhos ao redor, abre as vizinhas em cascata.
    // BFS (busca em largura) com uma fila: abre a casa, e se for 0 coloca as vizinhas na fila.
    private fun abrirComCascata(tabuleiro: Tabuleiro, linha: Int, coluna: Int): Tabuleiro {
        val tamanho = tabuleiro.tamanho
        val casas = tabuleiro.casas.toMutableList()
        val fila = ArrayDeque<Pair<Int, Int>>()

        casas[tabuleiro.indice(linha, coluna)] = casas[tabuleiro.indice(linha, coluna)].copy(aberta = true)
        fila.addLast(Pair(linha, coluna))

        while (fila.isNotEmpty()) {
            val (l, c) = fila.removeFirst()
            // Só continua a cascata a partir de casas com 0 galhos ao redor.
            if (casas[l * tamanho + c].vizinhos != 0) continue

            for ((vl, vc) in vizinhas(tamanho, l, c)) {
                val i = vl * tamanho + vc
                val v = casas[i]
                // Não abre quem já está aberto nem quem tem placa.
                if (v.aberta || v.placa) continue
                casas[i] = v.copy(aberta = true)
                fila.addLast(Pair(vl, vc))
            }
        }
        return tabuleiro.copy(casas = casas)
    }
}
