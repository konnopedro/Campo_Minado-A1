package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campominado.ui.theme.CampoMinadoTheme

/**
 * Resultado visual de uma partida. Recebe apenas os dados necessários e callbacks;
 * não acessa ViewModel nem contém regras para determinar vitória ou derrota.
 */
@Composable
fun TelaResultado(
    venceu: Boolean,
    nivel: Int,
    tempoSegundos: Int,
    onJogarNovamente: () -> Unit,
    onProximoNivel: () -> Unit,
    onVoltarNiveis: () -> Unit,
    modifier: Modifier = Modifier,
    temProximoNivel: Boolean = true   // false no nível 15: não há próximo
) {
    val corDestaque = if (venceu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val tempoFormatado = "%02d:%02d".format(tempoSegundos / 60, tempoSegundos % 60)

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (venceu) "🏆" else "🌿", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (venceu) "Parabéns!" else "Fim de jogo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = corDestaque
        )
        Text(
            text = if (venceu) "Você limpou o caminho dos galhos!" else "Um galho foi encontrado. Tente novamente!",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Nível $nivel", style = MaterialTheme.typography.titleMedium)
                Text(tempoFormatado, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text("Tempo da partida", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(onClick = onJogarNovamente, modifier = Modifier.fillMaxWidth()) {
            Text("Jogar novamente")
        }
        if (venceu && temProximoNivel) {
            Button(onClick = onProximoNivel, modifier = Modifier.fillMaxWidth()) {
                Text("Próximo nível")
            }
        }
        OutlinedButton(onClick = onVoltarNiveis, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar aos níveis")
        }
    }
}

@Preview(showBackground = true, name = "Vitória")
@Composable
private fun TelaResultadoVitoriaPreview() {
    CampoMinadoTheme {
        TelaResultado(
            venceu = true,
            nivel = 4,
            tempoSegundos = 93,
            onJogarNovamente = {},
            onProximoNivel = {},
            onVoltarNiveis = {}
        )
    }
}

@Preview(showBackground = true, name = "Derrota")
@Composable
private fun TelaResultadoDerrotaPreview() {
    CampoMinadoTheme {
        TelaResultado(
            venceu = false,
            nivel = 4,
            tempoSegundos = 41,
            onJogarNovamente = {},
            onProximoNivel = {},
            onVoltarNiveis = {}
        )
    }
}
