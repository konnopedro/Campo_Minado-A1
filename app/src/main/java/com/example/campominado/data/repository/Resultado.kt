package com.example.campominado.data.repository

/**
 * Embrulho para o retorno do Repository: ou deu certo (Sucesso, com o dado),
 * ou deu errado (Erro, com uma mensagem já pronta para mostrar ao usuário).
 *
 * Assim o Repository nunca deixa uma exceção "vazar" para o ViewModel:
 * ele captura com try-catch e entrega o erro tratado, que o ViewModel
 * coloca no UiState para a tela exibir.
 */
sealed class Resultado<out T> {
    data class Sucesso<T>(val dado: T) : Resultado<T>()
    data class Erro(val mensagem: String) : Resultado<Nothing>()
}
