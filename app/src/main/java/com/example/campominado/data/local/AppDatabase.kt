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

/**
 * O banco de dados do app. Lista as entidades (tabelas) e entrega os DAOs.
 * version = 2: sempre que uma entidade muda, a versão sobe e precisamos de uma Migration.
 * (v1 -> v2: a tabela partidas ganhou a coluna "anotacao".)
 */
@Database(
    entities = [JogadorEntity::class, PartidaEntity::class],
    version = 2,
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
                ).addMigrations(MIGRACAO_1_2).build().also { instancia = it }
            }
        }

        // Quem já tinha o app com o banco v1 mantém jogadores e partidas:
        // só acrescentamos a coluna nova, com texto vazio nas partidas antigas.
        private val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE partidas ADD COLUMN anotacao TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
