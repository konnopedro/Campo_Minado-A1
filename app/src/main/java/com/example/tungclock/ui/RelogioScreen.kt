package com.example.tungclock.ui

import com.example.tungclock.data.Alarme
import com.example.tungclock.data.FusoHorario

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Fundo = Color(0xFF17120F)
private val CardFundo = Color(0xFF261D17)
private val Madeira = Color(0xFFC58B55)
private val MadeiraClara = Color(0xFFE0B17A)
private val Texto = Color(0xFFF6EBDD)
private val TextoSecundario = Color(0xFFBCA18A)
private val Borda = Color(0xFF493427)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RelogioScreen(
    paddingValues: PaddingValues,
    fusos: List<FusoHorario>,
    onAbrirAlarmes: () -> Unit,
    onAbrirFusos: () -> Unit,
    onAbrirDetalhesFuso: (Int) -> Unit
) {
    var horaAtual by remember { mutableStateOf(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))) }
    LaunchedEffect(Unit) {
        while (true) {
            horaAtual = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            delay(1000)
        }
    }
    val hora = horaAtual.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val data = horaAtual.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")))
        .replaceFirstChar { it.titlecase(Locale("pt", "BR")) }

    Column(
        modifier = Modifier.fillMaxSize().background(Fundo).padding(paddingValues)
            .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("TUN TUN", color = MadeiraClara, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("CLOCK", color = Texto, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            }
            Card(shape = RoundedCornerShape(50), colors = CardDefaults.cardColors(containerColor = Color(0xFF283328)), border = BorderStroke(1.dp, Color(0xFF52664B))) {
                Text("●  AO VIVO", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color(0xFFB4D1A6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Seu tempo, do seu jeito.", color = Texto, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(data, color = TextoSecundario, fontSize = 14.sp)
        }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = CardFundo), border = BorderStroke(1.dp, Borda)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("HORÁRIO DE BRASÍLIA", color = MadeiraClara, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(hora, color = Texto, fontSize = 43.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp, textAlign = TextAlign.Center)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("🕑", fontSize = 14.sp)
                    Spacer(Modifier.width(7.dp))
                    Text("São Paulo, Brasil  ·  UTC−03:00", color = TextoSecundario, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Relógios mundiais", color = Texto, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("A hora em outras cidades", color = TextoSecundario, fontSize = 12.sp)
                }
                IconButton(onClick = onAbrirFusos) { Icon(Icons.Default.Add, contentDescription = "Adicionar fuso horário", tint = MadeiraClara) }
            }

            if (fusos.isEmpty()) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardFundo), border = BorderStroke(1.dp, Borda)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nenhuma cidade adicionada", color = Texto, fontWeight = FontWeight.SemiBold)
                        Text("Adicione um fuso para acompanhar outros horários.", color = TextoSecundario, fontSize = 13.sp)
                        FilledTonalButton(onClick = onAbrirFusos) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Adicionar cidade")
                        }
                    }
                }
            } else {
                fusos.forEach { fuso ->
                    var horaFuso by remember(fuso.id) { mutableStateOf(ZonedDateTime.now(ZoneId.of(fuso.zona))) }
                    LaunchedEffect(fuso.zona) {
                        while (true) {
                            horaFuso = ZonedDateTime.now(ZoneId.of(fuso.zona))
                            delay(1000)
                        }
                    }
                    Card(onClick = { onAbrirDetalhesFuso(fuso.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardFundo), border = BorderStroke(1.dp, Borda)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(fuso.cidade, color = Texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(fuso.pais, color = TextoSecundario, fontSize = 12.sp)
                                Text("UTC" + (if (fuso.offset >= 0) "+" else "") + fuso.offset, color = MadeiraClara, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(horaFuso.format(DateTimeFormatter.ofPattern("HH:mm")), color = Texto, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                                Text(horaFuso.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale("pt", "BR"))), color = TextoSecundario, fontSize = 11.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("›", color = Madeira, fontSize = 25.sp)
                        }
                    }
                }
                androidx.compose.material3.TextButton(onClick = onAbrirFusos, modifier = Modifier.fillMaxWidth()) {
                    Text("Gerenciar cidades", color = MadeiraClara)
                    Spacer(Modifier.width(6.dp))
                    Text("›", color = MadeiraClara, fontSize = 25.sp)
                }
            }
        }
    }
}
