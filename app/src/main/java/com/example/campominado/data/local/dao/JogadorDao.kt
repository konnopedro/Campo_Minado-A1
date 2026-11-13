package com.example.campominado.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.campominado.data.local.entity.JogadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JogadorDao {

    @Query("SELECT * FROM jogadores ORDER BY nome ASC")
    fun listarTodos(): Flow<List<JogadorEntity>>

    @Query("SELECT * FROM jogadores WHERE id = :id")
    fun observarPorId(id: Long): Flow<JogadorEntity?>

    @Query("SELECT * FROM jogadores WHERE nome = :nome LIMIT 1")
    suspend fun buscarPorNome(nome: String): JogadorEntity?

    @Insert
    suspend fun inserir(jogador: JogadorEntity): Long

    @Update
    suspend fun atualizar(jogador: JogadorEntity)

    @Delete
    suspend fun excluir(jogador: JogadorEntity)
}
