package com.example.campominado.ui.navigation

import com.example.campominado.ui.viewmodel.Perfil

/** Nomes das telas ("rotas") usados pelo NavHost. Ficam num lugar só para não errar o texto. */
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

/** Um botão da Bottom Navigation Bar. */
data class ItemBarra(val rota: String, val rotulo: String, val icone: String)

/**
 * A barra é diferente para cada perfil (exigência do professor: telas e funções
 * mostradas ou ocultadas conforme o estado no ViewModel).
 *  - Jogador: Níveis, Recordes, Histórico
 *  - Admin:   Níveis (Admin), Recordes
 */
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
