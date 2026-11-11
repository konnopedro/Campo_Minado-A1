package com.example.campominado.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.logic.NiveisPadrao
import com.example.campominado.ui.theme.CampoMinadoTheme
import com.example.campominado.ui.viewmodel.AdminUiState

private val VerdePraca = Color(0xFF2E5E3A)
private val Alerta = Color(0xFFB4541A)

/**
 * Painel do Admin: lista os 15 níveis (vindos do Firestore) com filtro por tamanho de mapa.
 * Tocar num nível abre a tela de edição. Só recebe estado e avisa cliques (sem ViewModel).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAdmin(
    estado: AdminUiState,
    onFiltrarMapa: (Int?) -> Unit,
    onEditar: (NivelRemoto) -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Painel do Admin", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VerdePraca)
            OutlinedButton(onClick = onSair) { Text("Sair") }
        }

        if (estado.erro != null) {
            Text(text = estado.erro, color = Alerta)
        }

        // Filtro por tamanho de mapa: "Todos" + um botão para cada tamanho.
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = estado.filtroMapa == null,
                onClick = { onFiltrarMapa(null) },
                label = { Text("Todos") }
            )
            estado.tamanhosDeMapa.forEach { tamanho ->
                FilterChip(
                    selected = estado.filtroMapa == tamanho,
                    onClick = { onFiltrarMapa(tamanho) },
                    label = { Text("${tamanho}×$tamanho") }
                )
            }
        }

        if (estado.carregando) {
            CircularProgressIndicator()
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(estado.niveisFiltrados, key = { it.nivel }) { nivel ->
                CartaoDoNivel(nivel = nivel, onClick = { onEditar(nivel) })
            }
        }
    }
}

@Composable
private fun CartaoDoNivel(nivel: NivelRemoto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (nivel.ativo) Color(0xFFDCE8D6) else Color(0xFFE8DFC9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Nível ${nivel.nivel}", fontWeight = FontWeight.Bold, color = Color(0xFF1D2A1F))
                Text(
                    "Mapa ${nivel.tamanhoMapa}×${nivel.tamanhoMapa} · ${nivel.galhos} galhos",
                    color = Color(0xFF1D2A1F)
                )
            }
            Text(
                text = if (nivel.ativo) "Ativo" else "Desativado",
                color = if (nivel.ativo) VerdePraca else Alerta,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TelaAdminPreview() {
    val niveis = NiveisPadrao.todos.map {
        NivelRemoto(it.nivel, it.tamanhoMapa, it.galhos, ativo = it.nivel != 3)
    }
    CampoMinadoTheme {
        TelaAdmin(
            estado = AdminUiState(niveis = niveis, filtroMapa = 6),
            onFiltrarMapa = {},
            onEditar = {},
            onSair = {}
        )
    }
}
