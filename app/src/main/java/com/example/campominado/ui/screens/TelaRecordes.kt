package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campominado.data.local.entity.PartidaEntity
import com.example.campominado.ui.theme.CampoMinadoTheme
import com.example.campominado.ui.viewmodel.RecordeItem

@Composable
fun TelaRecordes(
    recordes: List<RecordeItem>,
    filtroMapa: Int?,
    tamanhosDeMapa: List<Int>,
    onFiltrarMapa: (Int?) -> Unit,
    onExcluirPartida: ((PartidaEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Recordes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Melhor tempo de cada jogador em cada nível.", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = filtroMapa == null, onClick = { onFiltrarMapa(null) }, label = { Text("Todos") })
            tamanhosDeMapa.forEach { tamanho ->
                FilterChip(
                    selected = filtroMapa == tamanho,
                    onClick = { onFiltrarMapa(tamanho) },
                    label = { Text("${tamanho}×$tamanho") }
                )
            }
        }

        if (recordes.isEmpty()) {
            Text(
                "Ainda não há recordes para este filtro. Vença uma partida para aparecer aqui!",
                modifier = Modifier.padding(top = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(recordes, key = { _, item -> item.partida.id }) { indice, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when (indice) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "#${indice + 1}" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(item.nomeJogador, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Nível ${item.partida.nivel} · Mapa ${item.partida.tamanhoMapa}×${item.partida.tamanhoMapa}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatarTempo(item.partida.tempoSegundos), fontWeight = FontWeight.Bold)
                                if (onExcluirPartida != null) {
                                    OutlinedButton(onClick = { onExcluirPartida(item.partida) }) {
                                        Text("Excluir")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatarTempo(segundos: Int): String = "%02d:%02d".format(segundos / 60, segundos % 60)

@Preview(showBackground = true)
@Composable
private fun TelaRecordesPreview() {
    val partidas = listOf(
        RecordeItem(PartidaEntity(id = 1, jogadorId = 1, nivel = 3, tamanhoMapa = 6, totalGalhos = 6, casasAbertas = 30, venceu = true, tempoSegundos = 72), "Ana"),
        RecordeItem(PartidaEntity(id = 2, jogadorId = 2, nivel = 5, tamanhoMapa = 7, totalGalhos = 8, casasAbertas = 41, venceu = true, tempoSegundos = 95), "Pedro")
    )
    CampoMinadoTheme {
        TelaRecordes(partidas, null, listOf(6, 7, 8, 9, 10), {}, {})
    }
}
