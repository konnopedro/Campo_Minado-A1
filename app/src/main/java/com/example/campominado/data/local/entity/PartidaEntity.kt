package com.example.campominado.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabela "partidas": o resultado de cada partida jogada.
 * É a base do Histórico e dos Recordes (melhor tempo por nível).
 *
 * A chave estrangeira liga a partida ao jogador que jogou.
 * onDelete = CASCADE: se o jogador for excluído, as partidas dele também são.
 */
@Entity(
    tableName = "partidas",
    foreignKeys = [
        ForeignKey(
            entity = JogadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["jogadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Índice na coluna da chave estrangeira: deixa as buscas por jogador mais rápidas
    // e evita um aviso do Room na compilação.
    indices = [Index("jogadorId")]
)
data class PartidaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val jogadorId: Long,

    val nivel: Int,            // 1 a 15
    val tamanhoMapa: Int,      // 6 = mapa 6x6, 10 = mapa 10x10
    val totalGalhos: Int,      // quantidade de galhos nesse nível
    val casasAbertas: Int,     // quantas casas seguras o jogador abriu
    val venceu: Boolean,       // Room guarda como 0 ou 1
    val tempoSegundos: Int,
    val data: Long = System.currentTimeMillis()
)
