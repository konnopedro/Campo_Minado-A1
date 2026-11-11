package com.example.campominado.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabela "jogadores": cada perfil cadastrado neste aparelho.
 *
 * CRUD completo nesta entidade:
 *  - Create: cadastrar um jogador novo no Login
 *  - Read:   listar jogadores (Flow) no Login e no Admin
 *  - Update: renomear o jogador e desbloquear o próximo nível ao vencer
 *  - Delete: excluir o perfil (as partidas dele são apagadas junto, ver PartidaEntity)
 */
@Entity(tableName = "jogadores")
data class JogadorEntity(
    // autoGenerate = true: o Room cria o id sozinho (1, 2, 3...) ao inserir.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nome: String,

    // Começa no nível 1. Ao vencer o nível N, vira N + 1 (máximo 15).
    val nivelMaximoDesbloqueado: Int = 1,

    // Data de criação em milissegundos (System.currentTimeMillis()).
    val dataCriacao: Long = System.currentTimeMillis()
)
