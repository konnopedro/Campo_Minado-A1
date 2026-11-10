package com.example.tungclock.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fusos_horarios")
data class FusoHorario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cidade: String,
    val pais: String,
    val zona: String,
    @ColumnInfo(name = "utc_offset")
    val offset: Int
)
