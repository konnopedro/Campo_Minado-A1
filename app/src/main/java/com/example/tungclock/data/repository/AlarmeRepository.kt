package com.example.tungclock.data.repository

import com.example.tungclock.data.Alarme
import com.example.tungclock.data.AlarmeDao
import kotlinx.coroutines.flow.Flow

/**
 * Centraliza o acesso aos alarmes persistidos pelo Room.
 * A interface nunca deve acessar o DAO diretamente.
 */
class AlarmeRepository(private val dao: AlarmeDao) {
    fun buscarTodos(): Flow<List<Alarme>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Alarme? = dao.buscarPorId(id)

    suspend fun inserir(alarme: Alarme): Long = dao.inserir(alarme)

    suspend fun atualizar(alarme: Alarme) = dao.atualizar(alarme)

    suspend fun excluir(alarme: Alarme) = dao.excluir(alarme)
}
