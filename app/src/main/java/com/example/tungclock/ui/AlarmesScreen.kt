package com.example.tungclock.ui

import com.example.tungclock.data.Alarme
import com.example.tungclock.data.FusoHorario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Fundo = Color(0xFF17120F)
private val CardFundo = Color(0xFF261D17)
private val Madeira = Color(0xFFC58B55)
private val MadeiraClara = Color(0xFFE0B17A)
private val Texto = Color(0xFFF6EBDD)
private val TextoSecundario = Color(0xFFBCA18A)
private val Borda = Color(0xFF493427)
private val DiasSemana = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

private fun textoRepeticao(dias: Set<String>): String = when {
    dias.isEmpty() -> "Uma vez"
    dias.size == 7 -> "Todos os dias"
    dias == DiasSemana.take(5).toSet() -> "Segunda a sexta"
    else -> DiasSemana.filter { it in dias }.joinToString(", ")
}

private fun formatarEntradaHorario(entrada: TextFieldValue): TextFieldValue {
    val cursorOriginal = entrada.selection.start.coerceIn(0, entrada.text.length)
    val digitosAntesDoCursor = entrada.text.take(cursorOriginal).count { it.isDigit() }
    val digitos = entrada.text.filter { it.isDigit() }.take(4)
    val formatado = if (digitos.length <= 2) digitos else digitos.take(2) + ":" + digitos.drop(2)
    val cursorFormatado = if (digitosAntesDoCursor <= 2) digitosAntesDoCursor else digitosAntesDoCursor + 1
    return TextFieldValue(formatado, selection = TextRange(cursorFormatado.coerceIn(0, formatado.length)))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmesScreen(
    paddingValues: PaddingValues,
    alarmes: List<Alarme>,
    viewModel: com.example.tungclock.viewmodel.AlarmeViewModel,
    onVoltar: () -> Unit,
    onAbrirDetalhes: (Int) -> Unit
) {
    var mostrarNovoAlarme by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()
    val novoHorario = state.horarioFormulario
    val novoNome = state.nomeFormulario
    val novosDias = state.diasFormulario
    var erro by remember { mutableStateOf("") }
    var alarmeParaExcluir by remember { mutableStateOf<Alarme?>(null) }


    Scaffold(
        modifier = Modifier.padding(paddingValues),
        containerColor = Fundo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.reiniciarFormulario()
                    erro = ""
                    mostrarNovoAlarme = true
                },
                containerColor = MadeiraClara,
                contentColor = Fundo,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar alarme")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Fundo)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TUN TUN CLOCK · ALARMES",
                        color = Madeira,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Alarmes",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Sua rotina",
                        color = Texto,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Toque em um alarme para editar os detalhes.",
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )
                }
            }

            if (state.erro != null) {
                item {
                    Text(state.erro.orEmpty(), color = Color(0xFFE18A75), fontSize = 12.sp)
                }
            }

            if (alarmes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardFundo
                        ),
                        border = BorderStroke(1.dp, Borda)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "◷",
                                color = MadeiraClara,
                                fontSize = 36.sp
                            )
                            Text(
                                "Nenhum alarme por enquanto",
                                color = Texto,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Use o botão + para criar seu primeiro horário.",
                                color = TextoSecundario,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {



                    items(items = alarmes, key = { it.id }) { alarme ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onAbrirDetalhes(alarme.id) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = CardFundo),
                            border = BorderStroke(1.dp, if (alarme.ativo) Color(0xFF765137) else Borda)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(alarme.horario, color = if (alarme.ativo) Texto else TextoSecundario, fontSize = 34.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
                                    Text(alarme.nome, color = Texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(alarme.dias, color = TextoSecundario, fontSize = 12.sp)
                                    Text(if (alarme.ativo) "ATIVO" else "DESATIVADO", color = if (alarme.ativo) MadeiraClara else TextoSecundario, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Switch(
                                        checked = alarme.ativo,
                                        onCheckedChange = { ativo ->
                                            val atualizado = alarme.copy(ativo = ativo)
                                            viewModel.atualizarAlarme(atualizado)
                                        }
                                    )
                                    IconButton(onClick = { alarmeParaExcluir = alarme }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir alarme", tint = Color(0xFFE18A75))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    if (mostrarNovoAlarme) {
        AlertDialog(
            onDismissRequest = { mostrarNovoAlarme = false },
            containerColor = CardFundo,
            titleContentColor = Texto,
            textContentColor = TextoSecundario,
            title = { Text("Novo alarme", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Defina quando você quer ser lembrado.", color = TextoSecundario, fontSize = 13.sp)
                    OutlinedTextField(
                        value = novoHorario,
                        onValueChange = { viewModel.alterarHorarioFormulario(formatarEntradaHorario(it)) },
                        label = { Text("Horário (HH:mm)") },
                        placeholder = { Text("07:30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Texto, unfocusedTextColor = Texto, focusedLabelColor = MadeiraClara, unfocusedLabelColor = TextoSecundario, focusedBorderColor = Madeira, unfocusedBorderColor = Borda, cursorColor = MadeiraClara)
                    )
                    OutlinedTextField(
                        value = novoNome,
                        onValueChange = { viewModel.alterarNomeFormulario(it) },
                        label = { Text("Nome do alarme") },
                        placeholder = { Text("Acordar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Texto, unfocusedTextColor = Texto, focusedLabelColor = MadeiraClara, unfocusedLabelColor = TextoSecundario, focusedBorderColor = Madeira, unfocusedBorderColor = Borda, cursorColor = MadeiraClara)
                    )
                    Text("Repetir", color = Texto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(textoRepeticao(novosDias), color = TextoSecundario, fontSize = 12.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DiasSemana.chunked(4).forEach { linha ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                linha.forEach { dia ->
                                    val selecionado = dia in novosDias
                                    FilterChip(
                                        selected = selecionado,
                                        onClick = { viewModel.alterarDiaFormulario(dia) },
                                        label = { Text(dia, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MadeiraClara,
                                            selectedLabelColor = Fundo,
                                            containerColor = CardFundo,
                                            labelColor = Texto
                                        )
                                    )
                                }
                            }
                        }
                    }
                    if (erro.isNotBlank() || state.erro != null) Text(if (erro.isNotBlank()) erro else state.erro.orEmpty(), color = Color(0xFFE18A75), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val horarioValido = novoHorario.text.matches(Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$"))
                    if (!horarioValido) {
                        erro = "Use um horário válido, como 07:30."
                    } else if (novoNome.isBlank()) {
                        erro = "Digite um nome para o alarme."
                    } else {
                        val novoAlarme = Alarme(horario = novoHorario.text, nome = novoNome.trim(), dias = textoRepeticao(novosDias), ativo = true)
                        viewModel.adicionarAlarme(novoAlarme, aoSalvar = { mostrarNovoAlarme = false })
                    }
                }) { Text("Salvar", color = MadeiraClara) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarNovoAlarme = false }) { Text("Cancelar", color = TextoSecundario) }
            }
        )
    }

    alarmeParaExcluir?.let { alarme ->
        AlertDialog(
            onDismissRequest = { alarmeParaExcluir = null },
            containerColor = CardFundo,
            title = { Text("Excluir alarme?", color = Texto) },
            text = {
                Text(
                    "O alarme “" + alarme.nome + "” será removido permanentemente.",
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removerAlarme(alarme, aoExcluir = { alarmeParaExcluir = null })
                }) { Text("Excluir", color = Color(0xFFE18A75)) }
            },
            dismissButton = {
                TextButton(onClick = { alarmeParaExcluir = null }) {
                    Text(
                        "Cancelar",
                        color = TextoSecundario
                    )
                }
            }
        )
    }
}
