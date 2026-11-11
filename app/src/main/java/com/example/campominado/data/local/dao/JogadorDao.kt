package com.example.campominado.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.campominado.data.local.entity.JogadorEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object: a "lista de comandos" que podemos fazer na tabela jogadores.
 * O Room lê as anotações e gera o código SQL de verdade na compilação.
 */
@Dao
interface JogadorDao {

    // READ reativo: retorna Flow. Sempre que a tabela mudar, o Flow emite a lista nova
    // e a tela atualiza sozinha (o ViewModel coleta esse Flow).
    @Query("SELECT * FROM jogadores ORDER BY nome ASC")
    fun listarTodos(): Flow<List<JogadorEntity>>

    // Observa um jogador específico (usado para saber o nível desbloqueado em tempo real).
    @Query("SELECT * FROM jogadores WHERE id = :id")
    fun observarPorId(id: Long): Flow<JogadorEntity?>

    // Busca única (não reativa) para o Login verificar se o nome já existe.
    @Query("SELECT * FROM jogadores WHERE nome = :nome LIMIT 1")
    suspend fun buscarPorNome(nome: String): JogadorEntity?

    // CREATE: devolve o id gerado.
    @Insert
    suspend fun inserir(jogador: JogadorEntity): Long

    // UPDATE: encontra a linha pelo id e substitui os outros campos.
    @Update
    suspend fun atualizar(jogador: JogadorEntity)

    // DELETE: remove pelo id. As partidas dele somem junto (CASCADE).
    @Delete
    suspend fun excluir(jogador: JogadorEntity)
}
