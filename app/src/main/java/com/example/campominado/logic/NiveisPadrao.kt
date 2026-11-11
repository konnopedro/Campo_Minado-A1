package com.example.campominado.logic

/** Configuração de um nível: tamanho do mapa (fixo) e quantidade de galhos (o Admin pode mudar). */
data class ConfigNivel(val nivel: Int, val tamanhoMapa: Int, val galhos: Int)

/**
 * Valores padrão dos 15 níveis. São usados quando o Firestore não está disponível
 * (sem internet) ou ainda não tem a configuração.
 */
object NiveisPadrao {

    val todos: List<ConfigNivel> = listOf(
        ConfigNivel(1, 6, 4), ConfigNivel(2, 6, 5), ConfigNivel(3, 6, 6),
        ConfigNivel(4, 7, 7), ConfigNivel(5, 7, 8), ConfigNivel(6, 7, 9),
        ConfigNivel(7, 8, 10), ConfigNivel(8, 8, 11), ConfigNivel(9, 8, 12),
        ConfigNivel(10, 9, 13), ConfigNivel(11, 9, 14), ConfigNivel(12, 9, 15),
        ConfigNivel(13, 10, 16), ConfigNivel(14, 10, 18), ConfigNivel(15, 10, 20)
    )

    fun buscar(nivel: Int): ConfigNivel? = todos.firstOrNull { it.nivel == nivel }

    // Regra do Admin: de 1 galho até 25% das casas do mapa.
    fun maximoDeGalhos(tamanhoMapa: Int): Int = tamanhoMapa * tamanhoMapa / 4
}
