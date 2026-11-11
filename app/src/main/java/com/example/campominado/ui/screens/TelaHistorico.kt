package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campominado.data.local.entity.PartidaEntity
import com.example.campominado.ui.theme.CampoMinadoTheme
import com.example.campominado.ui.viewmodel.FiltroResultado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Histórico de partidas e ações do perfil do jogador atual. */
@Composable
fun TelaHistorico(
    jogadorNome: String,
    partidas: List<PartidaEntity>,
    filtroResultado: FiltroResultado,
    erro: String?,
    onFiltrarResultado: (FiltroResultado) -> Unit,
    onExcluirPartida: (PartidaEntity) -> Unit,
    onRenomearPerfil: (String) -> Unit,
    onExcluirPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarRenomear by remember { mutableStateOf(false) }
    var mostrarConfirmacaoPerfil by remember { mutableStateOf(false) }
    var partidaParaExcluir by remember { mutableStateOf<PartidaEntity?>(null) }
    var novoNome by remember(jogadorNome) { mutableStateOf(jogadorNome) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Histórico e Perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Perfil do jogador", style = MaterialTheme.typography.labelLarge)
                Text(jogadorNome.ifBlank { "Jogador" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { novoNome = jogadorNome; mostrarRenomear = true }) {
                        Text("Renomear")
                    }
                    OutlinedButton(onClick = { mostrarConfirmacaoPerfil = true }) {
                        Text("Excluir perfil")
                    }
                }
            }
        }

        Text("Partidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroResultado.values().forEach { filtro ->
                FilterChip(
                    selected = filtroResultado == filtro,
                    onClick = { onFiltrarResultado(filtro) },
                    label = { Text(filtro.rotulo()) }
                )
            }
        }
        if (erro != null) {
            Text(erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (partidas.isEmpty()) {
            Text(
                "Nenhuma partida encontrada para este filtro.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(partidas, key = { it.id }) { partida ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    if (partida.venceu) "🏆 Vitória · Nível ${partida.nivel}" else "🌿 Derrota · Nível ${partida.nivel}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Mapa ${partida.tamanhoMapa}×${partida.tamanhoMapa} · ${formatarTempo(partida.tempoSegundos)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date(partida.data)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { partidaParaExcluir = partida }) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }

    if (mostrarRenomear) {
        AlertDialog(
            onDismissRequest = { mostrarRenomear = false },
            title = { Text("Renomear perfil") },
            text = {
                OutlinedTextField(
                    value = novoNome,
                    onValueChange = { novoNome = it },
                    label = { Text("Novo nome") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onRenomearPerfil(novoNome); mostrarRenomear = false },
                    enabled = novoNome.isNotBlank() && novoNome.trim() != jogadorNome
                ) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { mostrarRenomear = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarConfirmacaoPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoPerfil = false },
            title = { Text("Excluir perfil?") },
            text = { Text("O perfil e todo o histórico de partidas serão excluídos permanentemente.") },
            confirmButton = {
                TextButton(onClick = { onExcluirPerfil(); mostrarConfirmacaoPerfil = false }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacaoPerfil = false }) { Text("Cancelar") } }
        )
    }

    partidaParaExcluir?.let { partida ->
        AlertDialog(
            onDismissRequest = { partidaParaExcluir = null },
            title = { Text("Excluir partida?") },
            text = { Text("Esta partida será removida do histórico.") },
            confirmButton = {
                TextButton(onClick = { onExcluirPartida(partida); partidaParaExcluir = null }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { partidaParaExcluir = null }) { Text("Cancelar") } }
        )
    }
}

private fun FiltroResultado.rotulo(): String = when (this) {
    FiltroResultado.TODAS -> "Todas"
    FiltroResultado.VITORIAS -> "Vitórias"
    FiltroResultado.DERROTAS -> "Derrotas"
}

private fun formatarTempo(segundos: Int): String = "%02d:%02d".format(segundos / 60, segundos % 60)

@Preview(showBackground = true)
@Composable
private fun TelaHistoricoPreview() {
    val partidas = listOf(
        PartidaEntity(id = 1, jogadorId = 1, nivel = 2, tamanhoMapa = 6, totalGalhos = 5, casasAbertas = 31, venceu = true, tempoSegundos = 82),
        PartidaEntity(id = 2, jogadorId = 1, nivel = 3, tamanhoMapa = 6, totalGalhos = 6, casasAbertas = 12, venceu = false, tempoSegundos = 37)
    )
    CampoMinadoTheme {
        TelaHistorico(
            jogadorNome = "Daniel",
            partidas = partidas,
            filtroResultado = FiltroResultado.TODAS,
            erro = null,
            onFiltrarResultado = {},
            onExcluirPartida = {},
            onRenomearPerfil = {},
            onExcluirPerfil = {}
        )
    }
}
