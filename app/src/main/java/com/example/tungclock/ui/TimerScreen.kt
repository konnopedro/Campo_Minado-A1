package com.example.tungclock.ui

import com.example.tungclock.data.Alarme
import com.example.tungclock.data.FusoHorario

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable

private val Fundo = Color(0xFF17120F)
private val CardFundo = Color(0xFF261D17)
private val Roxo = Color(0xFFC58B55)
private val Texto = Color.White
private val TextoSecundario = Color(0xFFBCA18A)

@Composable
fun TimerScreen(
    paddingValues: PaddingValues
) {

    val context = LocalContext.current

    var minutos by rememberSaveable {
        mutableStateOf("15")
    }

    var segundos by rememberSaveable {
        mutableStateOf(0)
    }

    var rodando by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(rodando) {

        while (rodando) {

            delay(1000)

            if (
                minutos.toIntOrNull() == 0 &&
                segundos == 0
            ) {

                rodando = false

                Toast.makeText(
                    context,
                    "Timer finalizado!",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (segundos > 0) {

                segundos--

            } else {

                minutos = (
                        (minutos.toIntOrNull() ?: 1) - 1
                        ).toString()

                segundos = 59
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "TUN TUN CLOCK · TIMER",
            color = Roxo,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text = "Timer",
            color = Texto,
            fontSize = 20.sp
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardFundo)
                    .padding(25.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "%02d:%02d".format(
                        minutos.toIntOrNull() ?: 0,
                        segundos
                    ),
                    color = Texto,
                    fontSize = 32.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "minutos : segundos",
                    color = TextoSecundario,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Text(
            text = "ATALHOS",
            color = Texto,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    minutos = "5"
                    segundos = 0
                }
            ) {
                Text("5 MIN")
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Button(
                onClick = {
                    minutos = "10"
                    segundos = 0
                }
            ) {
                Text("10 MIN")
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Button(
                onClick = {
                    minutos = "15"
                    segundos = 0
                }
            ) {
                Text("15 MIN")
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedTextField(
            value = minutos,
            onValueChange = {
                if (it.all { caractere ->
                        caractere.isDigit()
                    }) {
                    minutos = it
                }
            },
            label = {
                Text("Minutos")
            }
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                rodando = !rodando
            }
        ) {

            Text(
                if (rodando)
                    "PAUSAR"
                else
                    "▶ INICIAR"
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = if (rodando)
                "Timer em andamento"
            else
                "Escolha uma duração",
            color = TextoSecundario,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
