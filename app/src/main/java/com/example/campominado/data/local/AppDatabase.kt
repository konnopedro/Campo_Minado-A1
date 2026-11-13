package com.example.campominado.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.campominado.data.local.dao.JogadorDao
import com.example.campominado.data.local.dao.PartidaDao
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.local.entity.PartidaEntity

@Database(
    entities = [JogadorEntity::class, PartidaEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jogadorDao(): JogadorDao
    abstract fun partidaDao(): PartidaDao

    companion object {

        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {

            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campo_minado.db"
                ).addMigrations(MIGRACAO_1_2).build().also { instancia = it }
            }
        }

        private val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE partidas ADD COLUMN anotacao TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
