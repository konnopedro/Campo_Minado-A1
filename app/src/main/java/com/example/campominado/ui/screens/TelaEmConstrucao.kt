package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tela PROVISÓRIA: ocupa o lugar das telas que ainda não foram feitas, para a navegação
 * poder ser testada. Cada tela real (Login, Níveis, Recordes...) substitui uma destas
 * no AppNavigation e esta função some quando todas estiverem prontas.
 *
 * @param acoes botões de teste: texto + o que fazer ao clicar.
 */
@Composable
fun TelaEmConstrucao(
    titulo: String,
    acoes: List<Pair<String, () -> Unit>> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Tela em construção", style = MaterialTheme.typography.bodyMedium)
        acoes.forEach { (texto, aoClicar) ->
            Button(onClick = aoClicar, modifier = Modifier.fillMaxWidth()) { Text(texto) }
        }
    }
}
