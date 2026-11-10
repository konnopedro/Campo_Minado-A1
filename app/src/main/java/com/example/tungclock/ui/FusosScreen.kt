package com.example.tungclock.ui

import com.example.tungclock.data.Alarme
import com.example.tungclock.data.FusoHorario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val Fundo = Color(0xFF17120F)
private val CardFundo = Color(0xFF261D17)
private val Madeira = Color(0xFFC58B55)
private val MadeiraClara = Color(0xFFE0B17A)
private val Texto = Color(0xFFF6EBDD)
private val TextoSecundario = Color(0xFFBCA18A)
private val Borda = Color(0xFF493427)

private data class CidadeDisponivel(val cidade: String, val pais: String, val zona: String)

private val cidadesDisponiveis = listOf(
    CidadeDisponivel("Tóquio", "Japão", "Asia/Tokyo"),
    CidadeDisponivel("Seul", "Coreia do Sul", "Asia/Seoul"),
    CidadeDisponivel("Pequim", "China", "Asia/Shanghai"),
    CidadeDisponivel("Dubai", "Emirados Árabes Unidos", "Asia/Dubai"),
    CidadeDisponivel("Mumbai", "Índia", "Asia/Kolkata"),
    CidadeDisponivel("Singapura", "Singapura", "Asia/Singapore"),
    CidadeDisponivel("Sydney", "Austrália", "Australia/Sydney"),
    CidadeDisponivel("Paris", "França", "Europe/Paris"),
    CidadeDisponivel("Berlim", "Alemanha", "Europe/Berlin"),
    CidadeDisponivel("Lisboa", "Portugal", "Europe/Lisbon"),
    CidadeDisponivel("Roma", "Itália", "Europe/Rome"),
    CidadeDisponivel("Cairo", "Egito", "Africa/Cairo"),
    CidadeDisponivel("Los Angeles", "Estados Unidos", "America/Los_Angeles"),
    CidadeDisponivel("Chicago", "Estados Unidos", "America/Chicago"),
    CidadeDisponivel("Toronto", "Canadá", "America/Toronto"),
    CidadeDisponivel("Cidade do México", "México", "America/Mexico_City"),
    CidadeDisponivel("Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires"),
    CidadeDisponivel("Santiago", "Chile", "America/Santiago")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FusosScreen(
    paddingValues: PaddingValues,
    fusos: List<FusoHorario>,
    viewModel: com.example.tungclock.viewmodel.FusoViewModel,
    onVoltar: () -> Unit,
    onAbrirDetalhes: (Int) -> Unit
) {
    var mostrarCidades by remember { mutableStateOf(false) }
    var fusoParaExcluir by remember { mutableStateOf<FusoHorario?>(null) }
    var erro by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()


    Scaffold(
        modifier = Modifier.padding(paddingValues),
        containerColor = Fundo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Fundo).padding(innerPadding).padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TUN TUN CLOCK · FUSOS", color = Madeira, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text("Relógios mundiais", color = Color.White, fontSize = 20.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onVoltar) {
                    Text("← Voltar", color = TextoSecundario)
                }
                TextButton(onClick = { mostrarCidades = true; erro = "" }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MadeiraClara)
                    Text("Adicionar cidade", color = MadeiraClara)
                }
            }

            Text("Acompanhe o horário em qualquer lugar do mundo.", color = TextoSecundario, fontSize = 13.sp)
            if (state.erro != null) Text(state.erro.orEmpty(), color = Color(0xFFE18A75), fontSize = 12.sp)
            if (fusos.isEmpty()) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardFundo), border = BorderStroke(1.dp, Borda)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("◎", color = MadeiraClara, fontSize = 28.sp)
                        Text("Nenhuma cidade adicionada", color = Texto, fontWeight = FontWeight.SemiBold)
                        Text("Escolha uma cidade para começar sua lista de relógios mundiais.", color = TextoSecundario, fontSize = 13.sp)
                        TextButton(onClick = { mostrarCidades = true }) { Text("Escolher cidade", color = MadeiraClara) }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = fusos, key = { it.id }) { fuso ->
                        var agora by remember(fuso.id) { mutableStateOf(ZonedDateTime.now(ZoneId.of(fuso.zona))) }
                        androidx.compose.runtime.LaunchedEffect(fuso.zona) {
                            while (true) {
                                agora = ZonedDateTime.now(ZoneId.of(fuso.zona))
                                kotlinx.coroutines.delay(1000)
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onAbrirDetalhes(fuso.id) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = CardFundo),
                            border = BorderStroke(1.dp, Borda)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(start = 17.dp, top = 16.dp, bottom = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🕑", modifier = Modifier.padding(end = 13.dp), fontSize = 16.sp)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(fuso.cidade, color = Texto, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                    Text(fuso.pais, color = TextoSecundario, fontSize = 12.sp)
                                    Text("UTC" + (if (fuso.offset >= 0) "+" else "") + fuso.offset, color = MadeiraClara, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(agora.format(DateTimeFormatter.ofPattern("HH:mm")), color = Texto, fontSize = 25.sp, fontWeight = FontWeight.Light)
                                    Text(agora.format(DateTimeFormatter.ofPattern("EEE, d MMM")), color = TextoSecundario, fontSize = 11.sp)
                                }
                                IconButton(onClick = { fusoParaExcluir = fuso }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir cidade", tint = Color(0xFFE18A75))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarCidades) {
        AlertDialog(
            onDismissRequest = { mostrarCidades = false },
            containerColor = CardFundo,
            title = { Text("Adicionar cidade", color = Texto, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Escolha uma cidade da lista. O fuso será configurado automaticamente.", color = TextoSecundario, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(360.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(cidadesDisponiveis.filter { cidade -> fusos.none { it.zona == cidade.zona } }) { cidade ->
                            TextButton(
                                onClick = {
                                    try {
                                        val zoneId = ZoneId.of(cidade.zona)
                                        val offset = zoneId.rules.getOffset(Instant.now()).totalSeconds / 3600
                                        val novo = FusoHorario(cidade = cidade.cidade, pais = cidade.pais, zona = cidade.zona, offset = offset)
                                        viewModel.adicionarFuso(novo, aoSalvar = { mostrarCidades = false })
                                    } catch (e: Exception) {
                                        erro = "Não foi possível adicionar essa cidade."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(cidade.cidade, color = Texto, fontWeight = FontWeight.SemiBold)
                                    Text(cidade.pais, color = TextoSecundario, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    if (erro.isNotBlank()) Text(erro, color = Color(0xFFE18A75), fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = { mostrarCidades = false }) { Text("Fechar", color = MadeiraClara) } }
        )
    }

    fusoParaExcluir?.let { fuso ->
        AlertDialog(
            onDismissRequest = { fusoParaExcluir = null },
            containerColor = CardFundo,
            title = { Text("Remover cidade?", color = Texto) },
            text = { Text("“" + fuso.cidade + "” será removida dos seus relógios mundiais.", color = TextoSecundario) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removerFuso(fuso, aoExcluir = { fusoParaExcluir = null })
                }) { Text("Remover", color = Color(0xFFE18A75)) }
            },
            dismissButton = { TextButton(onClick = { fusoParaExcluir = null }) { Text("Cancelar", color = TextoSecundario) } }
        )
    }
}
