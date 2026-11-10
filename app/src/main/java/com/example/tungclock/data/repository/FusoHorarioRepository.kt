package com.example.tungclock.data.repository

import com.example.tungclock.data.FusoHorario
import com.example.tungclock.data.FusoHorarioDao
import kotlinx.coroutines.flow.Flow

/**
 * Centraliza o acesso aos fusos horários persistidos pelo Room.
 * A interface nunca deve acessar o DAO diretamente.
 */
class FusoHorarioRepository(private val dao: FusoHorarioDao) {
    fun buscarTodos(): Flow<List<FusoHorario>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): FusoHorario? = dao.buscarPorId(id)

    suspend fun inserir(fuso: FusoHorario): Long = dao.inserir(fuso)

    suspend fun atualizar(fuso: FusoHorario) = dao.atualizar(fuso)

    suspend fun excluir(fuso: FusoHorario) = dao.excluir(fuso)
}
