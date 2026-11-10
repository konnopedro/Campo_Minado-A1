
package com.example.tungclock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmeDao {
    @Insert
    suspend fun inserir(alarme: Alarme): Long

    @Query("SELECT * FROM alarmes ORDER BY id ASC")
    fun buscarTodos(): Flow<List<Alarme>>

    @Query("SELECT * FROM alarmes WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Alarme?

    @Update
    suspend fun atualizar(alarme: Alarme)

    @Delete
    suspend fun excluir(alarme: Alarme)
}
