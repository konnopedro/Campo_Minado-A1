package com.example.campominado.ui.navigation

import com.example.campominado.ui.viewmodel.Perfil

object Rotas {
    const val LOGIN = "login"
    const val NIVEIS = "niveis"
    const val JOGO = "jogo"
    const val RESULTADO = "resultado"
    const val RECORDES = "recordes"
    const val HISTORICO = "historico"
    const val ADMIN_NIVEIS = "admin_niveis"
    const val ADMIN_EDITAR = "admin_editar"
}

data class ItemBarra(val rota: String, val rotulo: String, val icone: String)

fun itensDaBarra(perfil: Perfil): List<ItemBarra> = when (perfil) {
    Perfil.JOGADOR -> listOf(
        ItemBarra(Rotas.NIVEIS, "Níveis", "🗺"),
        ItemBarra(Rotas.RECORDES, "Recordes", "🏆"),
        ItemBarra(Rotas.HISTORICO, "Histórico", "📋")
    )
    Perfil.ADMIN -> listOf(
        ItemBarra(Rotas.ADMIN_NIVEIS, "Níveis (Admin)", "⚙"),
        ItemBarra(Rotas.RECORDES, "Recordes", "🏆")
    )
    Perfil.NENHUM -> emptyList()
}
