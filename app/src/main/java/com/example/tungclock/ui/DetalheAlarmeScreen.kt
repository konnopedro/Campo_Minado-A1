package com.example.tungclock.ui

import com.example.tungclock.data.Alarme
import com.example.tungclock.data.FusoHorario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FundoDetalheAlarme = Color(0xFF17120F)
private val MadeiraDetalheAlarme = Color(0xFFC58B55)
private val MadeiraClaraDetalheAlarme = Color(0xFFE0B17A)
private val TextoDetalheAlarme = Color(0xFFF6EBDD)
private val TextoSecundarioDetalheAlarme = Color(0xFFBCA18A)
private val CardDetalheAlarme = Color(0xFF261D17)
private val BordaDetalheAlarme = Color(0xFF493427)

private val diasDaSemana = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")


private fun formatarEntradaHorario(entrada: TextFieldValue): TextFieldValue {
    val cursorOriginal = entrada.selection.start.coerceIn(0, entrada.text.length)
    val digitosAntesDoCursor = entrada.text.take(cursorOriginal).count { it.isDigit() }
    val digitos = entrada.text.filter { it.isDigit() }.take(4)
    val formatado = if (digitos.length <= 2) digitos else digitos.take(2) + ":" + digitos.drop(2)
    val cursorFormatado = if (digitosAntesDoCursor <= 2) digitosAntesDoCursor else digitosAntesDoCursor + 1
    return TextFieldValue(formatado, selection = TextRange(cursorFormatado.coerceIn(0, formatado.length)))
}

private fun diasSelecionadosDoTexto(texto: String): Set<String> {
    val valor = texto.lowercase()
    if (valor.contains("todos os dias")) return diasDaSemana.toSet()
    if (valor.contains("segunda a sexta")) return diasDaSemana.take(5).toSet()
    return diasDaSemana.filter { dia ->
        valor.contains(dia.lowercase()) ||
            (dia == "Segunda" && valor.contains("seg")) ||
            (dia == "Terça" && valor.contains("ter")) ||
            (dia == "Quarta" && valor.contains("qua")) ||
            (dia == "Quinta" && valor.contains("qui")) ||
            (dia == "Sábado" && valor.contains("sáb")) ||
            (dia == "Domingo" && valor.contains("dom"))
    }.toSet()
}

private fun textoDosDiasSelecionados(selecionados: Set<String>): String {
    if (selecionados.isEmpty()) return "Uma vez"
    if (selecionados.size == 7) return "Todos os dias"
    if (selecionados == diasDaSemana.take(5).toSet()) return "Segunda a sexta"
    return diasDaSemana.filter { it in selecionados }.joinToString(", ")
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DetalheAlarmeScreen(
    id: Int,
    alarmes: List<Alarme>,
    viewModel: com.example.tungclock.viewmodel.AlarmeViewModel,
    onVoltar: () -> Unit
) {

    val alarme = alarmes.firstOrNull { it.id == id }
    val carregou = !viewModel.uiState.value.carregando

    if (!carregou || alarme == null) {
        Scaffold(
            containerColor = FundoDetalheAlarme,
            topBar = {
                TopAppBar(
                    title = { Text("Detalhes do alarme") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FundoDetalheAlarme,
                        titleContentColor = TextoDetalheAlarme,
                        navigationIconContentColor = TextoDetalheAlarme
                    )
                )
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (carregou) "Não foi possível encontrar este alarme." else "Carregando alarme…",
                    color = TextoDetalheAlarme
                )
            }
        }
        return
    }

    val alarmeAtual = alarme!!
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(id) {
        viewModel.iniciarEdicao(alarmeAtual, diasSelecionadosDoTexto(alarmeAtual.dias))
    }
    val horario = state.horarioEdicao
    val nome = state.nomeEdicao
    val diasSelecionados = state.diasEdicao
    val ativo = state.ativoEdicao
    var erro by remember(id) { mutableStateOf("") }

    Scaffold(
        containerColor = FundoDetalheAlarme,
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do alarme", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FundoDetalheAlarme,
                    titleContentColor = TextoDetalheAlarme,
                    navigationIconContentColor = TextoDetalheAlarme
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(FundoDetalheAlarme)
                .padding(innerPadding).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text("EDITANDO ALARME", color = MadeiraClaraDetalheAlarme, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = horario,
                onValueChange = { entrada ->
                    viewModel.alterarHorarioEdicao(formatarEntradaHorario(entrada))
                    erro = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Horário (HH:mm)") },
                placeholder = { Text("07:30") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoDetalheAlarme,
                    unfocusedTextColor = TextoDetalheAlarme,
                    focusedLabelColor = MadeiraClaraDetalheAlarme,
                    unfocusedLabelColor = TextoSecundarioDetalheAlarme,
                    cursorColor = MadeiraClaraDetalheAlarme,
                    focusedBorderColor = MadeiraDetalheAlarme,
                    unfocusedBorderColor = BordaDetalheAlarme
                )
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { viewModel.alterarNomeEdicao(it); erro = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do alarme") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoDetalheAlarme,
                    unfocusedTextColor = TextoDetalheAlarme,
                    focusedLabelColor = MadeiraClaraDetalheAlarme,
                    unfocusedLabelColor = TextoSecundarioDetalheAlarme,
                    cursorColor = MadeiraClaraDetalheAlarme,
                    focusedBorderColor = MadeiraDetalheAlarme,
                    unfocusedBorderColor = BordaDetalheAlarme
                )
            )
            Spacer(Modifier.height(20.dp))

            Text("Repetir", color = TextoDetalheAlarme, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                if (diasSelecionados.isEmpty()) "Não repetir" else "Selecione os dias desejados",
                color = TextoSecundarioDetalheAlarme,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                diasDaSemana.chunked(4).forEach { linha ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        linha.forEach { dia ->
                            val selecionado = dia in diasSelecionados
                            FilterChip(
                                selected = selecionado,
                                onClick = {
                                    viewModel.alterarDiaEdicao(dia)
                                    erro = ""
                                },
                                label = { Text(dia.take(3), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MadeiraDetalheAlarme,
                                    selectedLabelColor = FundoDetalheAlarme,
                                    containerColor = CardDetalheAlarme,
                                    labelColor = TextoDetalheAlarme
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Alarme ativo", color = TextoDetalheAlarme, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (ativo) "O alarme está ligado" else "O alarme está desligado",
                        color = TextoSecundarioDetalheAlarme,
                        fontSize = 12.sp
                    )
                }
                Switch(checked = ativo, onCheckedChange = { viewModel.alterarAtivoEdicao(it) })
            }

            if (erro.isNotBlank() || state.erro != null) {
                Spacer(Modifier.height(12.dp))
                Text(if (erro.isNotBlank()) erro else state.erro.orEmpty(), color = Color(0xFFE18A75), fontSize = 12.sp)
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    val horarioValido = horario.text.matches(Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$"))
                    if (!horarioValido) {
                        erro = "Digite um horário válido entre 00:00 e 23:59."
                    } else if (nome.isBlank()) {
                        erro = "Digite um nome para o alarme."
                    } else {
                        val atualizado = alarmeAtual.copy(
                            horario = horario.text,
                            nome = nome.trim(),
                            dias = textoDosDiasSelecionados(diasSelecionados),
                            ativo = ativo
                        )
                        viewModel.atualizarAlarme(atualizado, aoSalvar = onVoltar)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Você pode selecionar mais de um dia. Sem dias selecionados, o alarme fica configurado para uma única vez.",
                color = TextoSecundarioDetalheAlarme,
                fontSize = 11.sp
            )
        }
    }
}
