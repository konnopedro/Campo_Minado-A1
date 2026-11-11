package com.example.campominado.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.campominado.data.local.entity.PartidaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDao {

    // Histórico de um jogador, da mais recente para a mais antiga.
    @Query("SELECT * FROM partidas WHERE jogadorId = :jogadorId ORDER BY data DESC")
    fun listarPorJogador(jogadorId: Long): Flow<List<PartidaEntity>>

    // Todas as vitórias de todos os jogadores: base da tela de Recordes.
    // O filtro por tamanho de mapa e a ordenação por tempo ficam no ViewModel
    // (filter / sortedBy), como o PDF exige.
    @Query("SELECT * FROM partidas WHERE venceu = 1")
    fun listarVitorias(): Flow<List<PartidaEntity>>

    @Insert
    suspend fun inserir(partida: PartidaEntity): Long

    @Delete
    suspend fun excluir(partida: PartidaEntity)
}
