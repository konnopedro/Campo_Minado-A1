package com.example.campominado.data.remote

import com.example.campominado.logic.NiveisPadrao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    // Source.SERVER: pergunta direto ao servidor. Se não houver conexão, dá erro logo
    // (e o Repository usa os valores padrão), em vez de ficar esperando ou devolver um cache vazio.
    // withContext(Dispatchers.IO): o Firestore é iniciado e usado fora da thread da tela,
    // para a interface não engasgar na primeira vez que o app fala com o servidor.
    suspend fun buscarNiveis(): List<NivelRemoto> = withContext(Dispatchers.IO) {
        colecao.get(Source.SERVER).await().documents.mapNotNull { doc ->
            // Lemos campo a campo; se um documento estiver incompleto, ele é ignorado.
            val nivel = doc.getLong("nivel")?.toInt() ?: return@mapNotNull null
            val tamanho = doc.getLong("tamanhoMapa")?.toInt() ?: return@mapNotNull null
            val galhos = doc.getLong("galhos")?.toInt() ?: return@mapNotNull null
            NivelRemoto(nivel, tamanho, galhos, doc.getBoolean("ativo") ?: true)
        }.sortedBy { it.nivel }
    }

    // UPDATE (Admin): grava um nível. O id do documento é o número do nível,
    // então salvar de novo o mesmo nível apenas substitui os dados.
    suspend fun salvarNivel(nivel: NivelRemoto) = withContext(Dispatchers.IO) {
        val dados = mapOf(
            "nivel" to nivel.nivel,
            "tamanhoMapa" to nivel.tamanhoMapa,
            "galhos" to nivel.galhos,
            "ativo" to nivel.ativo
        )
        colecao.document(nivel.nivel.toString()).set(dados).await()
    }

    // Cria os 15 níveis com os valores padrão (o Repository só chama se a coleção estiver vazia).
    // Usamos um "batch": as 15 gravações vão juntas, numa única ida ao servidor,
    // em vez de 15 idas seguidas (que deixavam o painel do Admin lento na primeira vez).
    suspend fun criarNiveisPadrao() = withContext(Dispatchers.IO) {
        val lote = firestore.value.batch()
        NiveisPadrao.todos.forEach {
            val dados = mapOf(
                "nivel" to it.nivel,
                "tamanhoMapa" to it.tamanhoMapa,
                "galhos" to it.galhos,
                "ativo" to true
            )
            lote.set(colecao.document(it.nivel.toString()), dados)
        }
        lote.commit().await()
    }

    companion object {
        const val COLECAO = "niveis"
    }
}
