package com.example.campominado.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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

    indices = [Index("jogadorId")]
)
data class PartidaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val jogadorId: Long,

    val nivel: Int,
    val tamanhoMapa: Int,
    val totalGalhos: Int,
    val casasAbertas: Int,
    val venceu: Boolean,
    val tempoSegundos: Int,
    val data: Long = System.currentTimeMillis(),

    @ColumnInfo(defaultValue = "")
    val anotacao: String = ""
)
