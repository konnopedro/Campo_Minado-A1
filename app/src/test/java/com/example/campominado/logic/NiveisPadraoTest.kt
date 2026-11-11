package com.example.campominado.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NiveisPadraoTest {

    @Test
    fun possuiExatamente15NiveisEmOrdem() {
        assertEquals(15, NiveisPadrao.todos.size)
        assertEquals((1..15).toList(), NiveisPadrao.todos.map { it.nivel })
    }

    @Test
    fun tamanhosDosMapasCorrespondemAsFaixasDefinidas() {
        val tamanhosEsperados = listOf(
            6, 6, 6,
            7, 7, 7,
            8, 8, 8,
            9, 9, 9,
            10, 10, 10
        )
        assertEquals(tamanhosEsperados, NiveisPadrao.todos.map { it.tamanhoMapa })
    }

    @Test
    fun todosOsNiveisRespeitamOLimiteDe25PorCentoDeGalhos() {
        NiveisPadrao.todos.forEach { nivel ->
            val maximo = NiveisPadrao.maximoDeGalhos(nivel.tamanhoMapa)
            assertTrue(
                "Nível ${nivel.nivel} excede o limite de 25%",
                nivel.galhos in 1..maximo
            )
        }
    }

    @Test
    fun cadaNivelTemQuantidadePositivaDeGalhos() {
        assertTrue(NiveisPadrao.todos.all { it.galhos > 0 })
    }

    @Test
    fun buscaRetornaNivelExistenteENullParaNivelInvalido() {
        assertNotNull(NiveisPadrao.buscar(1))
        assertNotNull(NiveisPadrao.buscar(15))
        assertEquals(null, NiveisPadrao.buscar(0))
        assertEquals(null, NiveisPadrao.buscar(16))
    }

    @Test
    fun limiteDeGalhosEhUmQuartoDasCasasDoMapa() {
        assertEquals(9, NiveisPadrao.maximoDeGalhos(6))
        assertEquals(12, NiveisPadrao.maximoDeGalhos(7))
        assertEquals(16, NiveisPadrao.maximoDeGalhos(8))
        assertEquals(20, NiveisPadrao.maximoDeGalhos(9))
        assertEquals(25, NiveisPadrao.maximoDeGalhos(10))
    }
}
