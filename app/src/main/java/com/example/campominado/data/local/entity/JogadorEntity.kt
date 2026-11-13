package com.example.campominado.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jogadores")
data class JogadorEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nome: String,

    val nivelMaximoDesbloqueado: Int = 1,

    val dataCriacao: Long = System.currentTimeMillis()
)
