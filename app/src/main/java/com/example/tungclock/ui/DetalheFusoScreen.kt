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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private val Fundo = Color(0xFF17120F)
private val CardFundo = Color(0xFF261D17)
private val Roxo = Color(0xFFC58B55)
private val Azul = Color(0xFFE0B17A)
private val Texto = Color.White
private val TextoSecundario = Color(0xFFBCA18A)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetalheFusoScreen(
    id: Int,
    fusos: List<FusoHorario>,
    viewModel: com.example.tungclock.viewmodel.FusoViewModel,
    onVoltar: () -> Unit
) {

    val fuso = fusos.firstOrNull { it.id == id }

    if (fuso == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Fundo)
                .padding(20.dp)
        ) {
            Text(
                text = "Carregando fuso horário...",
                color = Texto
            )
            Button(onClick = onVoltar) {
                Text("Voltar")
            }
        }
        return
    }

    val fusoAtual = fuso ?: return

    val state by viewModel.uiState.collectAsState()
    val editando = state.fusoEmEdicao == fusoAtual.id
    val cidadeEditada = state.cidadeEdicao
    val paisEditado = state.paisEdicao
    var erroEdicao by remember(fusoAtual.id) { mutableStateOf("") }

    var agora by remember {
        mutableStateOf(
            ZonedDateTime.now(
                ZoneId.of(fusoAtual.zona)
            )
        )
    }

    LaunchedEffect(fusoAtual.zona) {

        while (true) {

            agora = ZonedDateTime.now(
                ZoneId.of(fusoAtual.zona)
            )

            delay(1000)
        }
    }

    val horaFormatada = agora.format(
        DateTimeFormatter.ofPattern("HH:mm:ss")
    )

    val agoraSaoPaulo = ZonedDateTime.now(
        ZoneId.of("America/Sao_Paulo")
    )

    val diferencaMinutos = (
        agora.offset.totalSeconds -
        agoraSaoPaulo.offset.totalSeconds
    ) / 60

    val textoDiferenca = when {
        diferencaMinutos == 0 -> "Mesmo horário de São Paulo"
        else -> {
            val adiantado = diferencaMinutos > 0
            val minutos = kotlin.math.abs(diferencaMinutos)
            val horas = minutos / 60
            val resto = minutos % 60
            val intervalo = if (resto == 0) "${horas}h" else "${horas}h ${resto}min"
            "${intervalo} ${if (adiantado) "à frente" else "atrás"} de São Paulo"
        }
    }

    Scaffold(
        containerColor = Fundo,
        topBar = {

            TopAppBar(
                title = {
                    Text("Detalhes do Fuso")
                },
                navigationIcon = {

                    IconButton(
                        onClick = onVoltar
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Fundo,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Fundo)
                .padding(innerPadding)
                .padding(20.dp)
        ) {

            Text(
                text = fusoAtual.cidade.uppercase(),
                color = Roxo,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = fusoAtual.pais,
                color = TextoSecundario,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (editando) {
                OutlinedTextField(
                    value = cidadeEditada,
                    onValueChange = { viewModel.alterarCidade(it) },
                    label = { Text("Nome da cidade") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = paisEditado,
                    onValueChange = { viewModel.alterarPais(it) },
                    label = { Text("País") },
                    singleLine = true
                )
                if (erroEdicao.isNotBlank() || state.erro != null) Text(if (erroEdicao.isNotBlank()) erroEdicao else state.erro.orEmpty(), color = Color(0xFFE18A75))
                Button(onClick = {
                    if (cidadeEditada.isBlank() || paisEditado.isBlank()) {
                        erroEdicao = "Preencha cidade e país."
                    } else {
                        viewModel.atualizarFuso(
                            fusoAtual.copy(cidade = cidadeEditada.trim(), pais = paisEditado.trim()),
                            aoSalvar = {
                                viewModel.cancelarEdicao()
                                erroEdicao = ""
                            }
                        )
                    }
                }) { Text("Salvar alterações") }
                Button(onClick = {
                    viewModel.cancelarEdicao()
                    erroEdicao = ""
                }) { Text("Cancelar") }
            } else {
                Button(onClick = { viewModel.iniciarEdicao(fusoAtual) }) { Text("Editar informações") }
            }

            Spacer(
                modifier = Modifier.height(35.dp)
            )

            Text(
                text = "HORÁRIO ATUAL",
                color = TextoSecundario,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = horaFormatada,
                color = Texto,
                fontSize = 38.sp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "UTC${agora.offset.id.let { if (it == "Z") "+00:00" else it }}",
                color = Azul,
                fontSize = 13.sp
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier
                        .background(CardFundo)
                        .padding(18.dp)
                ) {

                    Text(
                        text = "COMPARAÇÃO",
                        color = Roxo,
                        fontSize = 12.sp
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column {

                            Text(
                                text = "São Paulo",
                                color = Texto,
                                fontSize = 13.sp
                            )

                            Text(
                                text = agoraSaoPaulo.format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                                ),
                                color = TextoSecundario,
                                fontSize = 20.sp
                            )
                        }

                        Column {

                            Text(
                                text = fusoAtual.cidade,
                                color = Texto,
                                fontSize = 13.sp
                            )

                            Text(
                                text = agora.format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                                ),
                                color = TextoSecundario,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )

                    Text(
                        text = textoDiferenca,
                        color = Azul,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text(
                text = "A diferença acima considera os offsets atuais das cidades, incluindo mudanças de horário de verão.",
                color = TextoSecundario,
                fontSize = 11.sp
            )
        }
    }
}
