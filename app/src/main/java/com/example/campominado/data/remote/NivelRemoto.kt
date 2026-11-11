package com.example.campominado.data.remote

/**
 * Um nível como está guardado no Firestore (documento da coleção "niveis").
 *
 * O tamanho do mapa é fixo por nível, mas fica aqui também para o app ter tudo num lugar só.
 * O Admin pode mudar "galhos" (de 1 até 25% das casas) e "ativo".
 */
data class NivelRemoto(
    val nivel: Int,          // 1 a 15 (também é o id do documento)
    val tamanhoMapa: Int,    // 6 a 10
    val galhos: Int,
    val ativo: Boolean = true
)
