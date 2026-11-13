package com.example.campominado.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.campominado.data.local.entity.PartidaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDao {

    @Query("SELECT * FROM partidas WHERE jogadorId = :jogadorId ORDER BY data DESC")
    fun listarPorJogador(jogadorId: Long): Flow<List<PartidaEntity>>

    @Query("SELECT * FROM partidas WHERE venceu = 1")
    fun listarVitorias(): Flow<List<PartidaEntity>>

    @Insert
    suspend fun inserir(partida: PartidaEntity): Long

    @Update
    suspend fun atualizar(partida: PartidaEntity)

    @Delete
    suspend fun excluir(partida: PartidaEntity)
}
