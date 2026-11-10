package com.example.tungclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarmes")
data class Alarme(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val horario: String,
    val nome: String,
    val dias: String,
    var ativo: Boolean
)
