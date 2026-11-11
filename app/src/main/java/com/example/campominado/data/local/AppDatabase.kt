package com.example.campominado.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campominado.data.local.dao.JogadorDao
import com.example.campominado.data.local.dao.PartidaDao
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.local.entity.PartidaEntity

/**
 * O banco de dados do app. Lista as entidades (tabelas) e entrega os DAOs.
 * version = 1: se mudarmos alguma entidade depois, a versão precisa subir.
 */
@Database(
    entities = [JogadorEntity::class, PartidaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jogadorDao(): JogadorDao
    abstract fun partidaDao(): PartidaDao

    companion object {
        // Singleton: o app inteiro usa UMA única instância do banco.
        // @Volatile garante que todas as threads enxerguem o valor mais recente.
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // Se já existe, devolve. Se não, cria dentro de um bloco synchronized,
            // para duas threads não criarem dois bancos ao mesmo tempo.
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campo_minado.db"
                ).build().also { instancia = it }
            }
        }
    }
}
