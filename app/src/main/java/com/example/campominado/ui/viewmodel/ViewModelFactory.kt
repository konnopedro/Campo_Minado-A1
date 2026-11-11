package com.example.campominado.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.campominado.data.local.AppDatabase
import com.example.campominado.data.remote.NiveisRemoteDataSource
import com.example.campominado.data.repository.JogoRepository

/**
 * Os ViewModels precisam receber o JogoRepository no construtor, mas o Android só sabe criar
 * ViewModels sem parâmetros. Esta fábrica ensina o Android a criá-los com o Repository (injeção).
 *
 * Uso numa tela: viewModel<JogoViewModel>(factory = factory)
 */
class ViewModelFactory(private val repository: JogoRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SessaoViewModel::class.java) -> SessaoViewModel(repository)
            modelClass.isAssignableFrom(JogoViewModel::class.java) -> JogoViewModel(repository)
            modelClass.isAssignableFrom(HistoricoViewModel::class.java) -> HistoricoViewModel(repository)
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> AdminViewModel(repository)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        } as T
    }

    companion object {
        /** Monta o Repository (Room + Firestore) e a fábrica. Chamado uma vez, na MainActivity. */
        fun criar(context: Context): ViewModelFactory {
            val banco = AppDatabase.getInstance(context)
            val repository = JogoRepository(
                jogadorDao = banco.jogadorDao(),
                partidaDao = banco.partidaDao(),
                niveisRemoto = NiveisRemoteDataSource()
            )
            return ViewModelFactory(repository)
        }
    }
}
