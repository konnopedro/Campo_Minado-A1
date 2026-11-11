package com.example.campominado.data.remote

import com.example.campominado.logic.NiveisPadrao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Único lugar do app que conversa com o Firestore.
 * Coleção "niveis": um documento por nível, com o número do nível como id ("1" a "15").
 *
 * Esta classe NÃO trata erros: se faltar internet ou o Firebase não estiver configurado,
 * ela lança exceção. Quem captura (try-catch) é o JogoRepository, como o professor pede.
 */
class NiveisRemoteDataSource(
    // Só é buscado quando precisa (lazy): se o Firebase não estiver configurado,
    // o erro acontece dentro da chamada (onde o Repository captura) e não ao criar o objeto.
    private val firestore: Lazy<FirebaseFirestore> = lazy { FirebaseFirestore.getInstance() }
) {

    private val colecao get() = firestore.value.collection(COLECAO)

    // READ: traz os níveis salvos, ordenados pelo número.
    suspend fun buscarNiveis(): List<NivelRemoto> =
        colecao.get().await().documents.mapNotNull { doc ->
            // Lemos campo a campo; se um documento estiver incompleto, ele é ignorado.
            val nivel = doc.getLong("nivel")?.toInt() ?: return@mapNotNull null
            val tamanho = doc.getLong("tamanhoMapa")?.toInt() ?: return@mapNotNull null
            val galhos = doc.getLong("galhos")?.toInt() ?: return@mapNotNull null
            NivelRemoto(nivel, tamanho, galhos, doc.getBoolean("ativo") ?: true)
        }.sortedBy { it.nivel }

    // UPDATE (Admin): grava um nível. O id do documento é o número do nível,
    // então salvar de novo o mesmo nível apenas substitui os dados.
    suspend fun salvarNivel(nivel: NivelRemoto) {
        val dados = mapOf(
            "nivel" to nivel.nivel,
            "tamanhoMapa" to nivel.tamanhoMapa,
            "galhos" to nivel.galhos,
            "ativo" to nivel.ativo
        )
        colecao.document(nivel.nivel.toString()).set(dados).await()
    }

    // Cria os 15 níveis com os valores padrão, mas só se a coleção estiver vazia.
    // Assim o Admin já encontra os níveis no Firestore na primeira vez que abrir o painel.
    suspend fun criarNiveisPadraoSeVazio() {
        if (colecao.limit(1).get().await().isEmpty) {
            NiveisPadrao.todos.forEach {
                salvarNivel(NivelRemoto(it.nivel, it.tamanhoMapa, it.galhos, ativo = true))
            }
        }
    }

    companion object {
        const val COLECAO = "niveis"
    }
}
