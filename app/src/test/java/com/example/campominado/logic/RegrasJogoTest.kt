package com.example.campominado.logic

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegrasJogoTest {

    private val random = Random(42)

    @Test
    fun primeiroToqueNuncaTemGalhoNemVizinhoComGalho() {
        repeat(50) { semente ->
            val t = RegrasJogo.abrir(RegrasJogo.criarTabuleiro(6, 9), 2, 3, Random(semente))
            assertEquals(9, t.casas.count { it.temGalho })
            assertFalse(t.casa(2, 3).temGalho)
            assertEquals(0, t.casa(2, 3).vizinhos)
            assertTrue(t.casa(2, 3).aberta)
        }
    }

    @Test
    fun cascataAbreVizinhasDeCasaZero() {
        val t = RegrasJogo.abrir(RegrasJogo.criarTabuleiro(6, 4), 0, 0, random)

        assertTrue(t.casas.count { it.aberta } >= 4)
    }

    @Test
    fun abrirGalhoEncerraComDerrota() {
        val inicial = RegrasJogo.abrir(RegrasJogo.criarTabuleiro(6, 4), 0, 0, random)
        val indiceGalho = inicial.casas.indexOfFirst { it.temGalho }
        val t = RegrasJogo.abrir(inicial, indiceGalho / 6, indiceGalho % 6, random)
        assertEquals(EstadoPartida.DERROTA, t.estado)
    }

    @Test
    fun placaImpedeAberturaEAlteraContador() {
        val t0 = RegrasJogo.criarTabuleiro(6, 4)
        val t1 = RegrasJogo.alternarPlaca(t0, 1, 1)
        assertEquals(3, t1.galhosRestantes)
        val t2 = RegrasJogo.abrir(t1, 1, 1, random)
        assertFalse(t2.casa(1, 1).aberta)
        assertEquals(4, RegrasJogo.alternarPlaca(t1, 1, 1).galhosRestantes)
    }

    @Test
    fun abrirTodasAsCasasSeguras_dahVitoria() {
        var t = RegrasJogo.abrir(RegrasJogo.criarTabuleiro(6, 4), 0, 0, random)
        for (i in t.casas.indices) {
            if (!t.casas[i].temGalho) t = RegrasJogo.abrir(t, i / 6, i % 6, random)
        }
        assertEquals(EstadoPartida.VITORIA, t.estado)
    }

    @Test
    fun vizinhasNoCantoSaoTres() {
        assertEquals(3, RegrasJogo.vizinhas(6, 0, 0).size)
        assertEquals(8, RegrasJogo.vizinhas(6, 2, 2).size)
    }

    @Test
    fun niveisPadraoCobremOs15NiveisDentroDoLimite() {
        assertEquals(15, NiveisPadrao.todos.size)
        assertTrue(NiveisPadrao.todos.all { it.galhos <= NiveisPadrao.maximoDeGalhos(it.tamanhoMapa) })
    }
}
