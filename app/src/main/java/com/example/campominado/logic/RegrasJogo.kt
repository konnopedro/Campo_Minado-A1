package com.example.campominado.logic

import kotlin.random.Random

object RegrasJogo {

    fun criarTabuleiro(tamanho: Int, totalGalhos: Int): Tabuleiro =
        Tabuleiro(tamanho, totalGalhos, List(tamanho * tamanho) { Casa() })

    fun abrir(
        tabuleiro: Tabuleiro,
        linha: Int,
        coluna: Int,
        random: Random = Random.Default
    ): Tabuleiro {
        if (tabuleiro.estado != EstadoPartida.JOGANDO) return tabuleiro

        val casaTocada = tabuleiro.casa(linha, coluna)

        if (casaTocada.aberta || casaTocada.placa) return tabuleiro

        val base = if (tabuleiro.galhosSorteados) tabuleiro
        else sortearGalhos(tabuleiro, linha, coluna, random)

        val casa = base.casa(linha, coluna)

        if (casa.temGalho) {
            val casas = base.casas.toMutableList()
            casas[base.indice(linha, coluna)] = casa.copy(aberta = true)
            return base.copy(casas = casas, estado = EstadoPartida.DERROTA)
        }

        val aberto = abrirComCascata(base, linha, coluna)
        return aberto.copy(estado = if (venceu(aberto)) EstadoPartida.VITORIA else EstadoPartida.JOGANDO)
    }

    fun alternarPlaca(tabuleiro: Tabuleiro, linha: Int, coluna: Int): Tabuleiro {
        if (tabuleiro.estado != EstadoPartida.JOGANDO) return tabuleiro
        val casa = tabuleiro.casa(linha, coluna)
        if (casa.aberta) return tabuleiro

        val casas = tabuleiro.casas.toMutableList()
        casas[tabuleiro.indice(linha, coluna)] = casa.copy(placa = !casa.placa)
        return tabuleiro.copy(casas = casas)
    }

    fun venceu(tabuleiro: Tabuleiro): Boolean =
        tabuleiro.galhosSorteados && tabuleiro.casas.all { it.temGalho || it.aberta }

    fun vizinhas(tamanho: Int, linha: Int, coluna: Int): List<Pair<Int, Int>> {
        val lista = mutableListOf<Pair<Int, Int>>()
        for (dl in -1..1) {
            for (dc in -1..1) {
                if (dl == 0 && dc == 0) continue
                val l = linha + dl
                val c = coluna + dc
                if (l in 0 until tamanho && c in 0 until tamanho) lista.add(Pair(l, c))
            }
        }
        return lista
    }

    private fun sortearGalhos(tabuleiro: Tabuleiro, linha: Int, coluna: Int, random: Random): Tabuleiro {
        val tamanho = tabuleiro.tamanho
        val proibidas = vizinhas(tamanho, linha, coluna).map { tabuleiro.indice(it.first, it.second) } +
            tabuleiro.indice(linha, coluna)

        val sorteados = tabuleiro.casas.indices
            .filter { it !in proibidas }
            .shuffled(random)
            .take(tabuleiro.totalGalhos)
            .toSet()

        val comGalhos = tabuleiro.casas.mapIndexed { i, casa -> casa.copy(temGalho = i in sorteados) }

        val comContagem = comGalhos.mapIndexed { i, casa ->
            val l = i / tamanho
            val c = i % tamanho
            val qtd = vizinhas(tamanho, l, c).count { (vl, vc) -> comGalhos[vl * tamanho + vc].temGalho }
            casa.copy(vizinhos = qtd)
        }
        return tabuleiro.copy(casas = comContagem, galhosSorteados = true)
    }

    private fun abrirComCascata(tabuleiro: Tabuleiro, linha: Int, coluna: Int): Tabuleiro {
        val tamanho = tabuleiro.tamanho
        val casas = tabuleiro.casas.toMutableList()
        val fila = ArrayDeque<Pair<Int, Int>>()

        casas[tabuleiro.indice(linha, coluna)] = casas[tabuleiro.indice(linha, coluna)].copy(aberta = true)
        fila.addLast(Pair(linha, coluna))

        while (fila.isNotEmpty()) {
            val (l, c) = fila.removeFirst()

            if (casas[l * tamanho + c].vizinhos != 0) continue

            for ((vl, vc) in vizinhas(tamanho, l, c)) {
                val i = vl * tamanho + vc
                val v = casas[i]

                if (v.aberta || v.placa) continue
                casas[i] = v.copy(aberta = true)
                fila.addLast(Pair(vl, vc))
            }
        }
        return tabuleiro.copy(casas = casas)
    }
}
