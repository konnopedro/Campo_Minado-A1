package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.logic.NiveisPadrao
import com.example.campominado.ui.theme.CampoMinadoTheme

/**
 * Mostra os 15 níveis em cartões agrupados pelo tamanho do mapa.
 * A tela recebe somente dados e callbacks; desbloqueio e melhores tempos vêm de fora.
 */
@Composable
fun TelaNiveis(
    niveis: List<NivelRemoto>,
    nivelMaximoDesbloqueado: Int,
    melhoresTempos: Map<Int, Int>,
    onSelecionarNivel: (Int) -> Unit,
    carregando: Boolean = false,
    erro: String? = null,
    modifier: Modifier = Modifier
) {
    val grupos = niveis.sortedBy { it.nivel }.groupBy { it.tamanhoMapa }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Escolha um nível", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Você desbloqueou até o nível $nivelMaximoDesbloqueado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (erro != null) {
            Text(erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (carregando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            grupos.forEach { (tamanho, lista) ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Mapa ${tamanho}×$tamanho",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                items(lista, key = { it.nivel }) { nivel ->
                    val desbloqueado = nivel.nivel <= nivelMaximoDesbloqueado && nivel.ativo
                    CartaoNivel(
                        nivel = nivel,
                        desbloqueado = desbloqueado,
                        melhorTempo = melhoresTempos[nivel.nivel],
                        onClick = { if (desbloqueado) onSelecionarNivel(nivel.nivel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoNivel(
    nivel: NivelRemoto,
    desbloqueado: Boolean,
    melhorTempo: Int?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = desbloqueado,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (desbloqueado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("Nível ${nivel.nivel}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(if (desbloqueado) "🌳" else "🔒", style = MaterialTheme.typography.titleLarge)
            Text(
                text = melhorTempo?.let { "★ ${formatarTempo(it)}" } ?: "—:—",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!nivel.ativo) {
                Text("Indisponível", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatarTempo(segundos: Int): String = "%02d:%02d".format(segundos / 60, segundos % 60)

@Preview(showBackground = true, name = "Seleção de níveis")
@Composable
private fun TelaNiveisPreview() {
    val niveis = NiveisPadrao.todos.map { NivelRemoto(it.nivel, it.tamanhoMapa, it.galhos) }
    CampoMinadoTheme {
        TelaNiveis(
            niveis = niveis,
            nivelMaximoDesbloqueado = 6,
            melhoresTempos = mapOf(1 to 83, 2 to 112, 4 to 95),
            onSelecionarNivel = {}
        )
    }
}
