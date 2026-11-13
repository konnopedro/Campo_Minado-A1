package com.example.campominado.data.remote

import com.example.campominado.logic.NiveisPadrao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NiveisRemoteDataSource(

    private val firestore: Lazy<FirebaseFirestore> = lazy { FirebaseFirestore.getInstance() }
) {

    private val colecao get() = firestore.value.collection(COLECAO)

    suspend fun buscarNiveis(): List<NivelRemoto> = withContext(Dispatchers.IO) {
        colecao.get(Source.SERVER).await().documents.mapNotNull { doc ->

            val nivel = doc.getLong("nivel")?.toInt() ?: return@mapNotNull null
            val tamanho = doc.getLong("tamanhoMapa")?.toInt() ?: return@mapNotNull null
            val galhos = doc.getLong("galhos")?.toInt() ?: return@mapNotNull null
            NivelRemoto(nivel, tamanho, galhos, doc.getBoolean("ativo") ?: true)
        }.sortedBy { it.nivel }
    }

    suspend fun salvarNivel(nivel: NivelRemoto) = withContext(Dispatchers.IO) {
        val dados = mapOf(
            "nivel" to nivel.nivel,
            "tamanhoMapa" to nivel.tamanhoMapa,
            "galhos" to nivel.galhos,
            "ativo" to nivel.ativo
        )
        colecao.document(nivel.nivel.toString()).set(dados).await()
    }

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
