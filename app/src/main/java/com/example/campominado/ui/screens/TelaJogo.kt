package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.campominado.logic.RegrasJogo
import com.example.campominado.ui.theme.CampoMinadoTheme
import com.example.campominado.ui.viewmodel.JogoUiState
import kotlin.random.Random

private val VerdePraca = Color(0xFF2E5E3A)
private val Alerta = Color(0xFFB4541A)

/**
 * Tela do Jogo. Não conhece o ViewModel: recebe o estado e avisa o que o usuário fez.
 * Quem liga uma coisa na outra é a navegação (JogoViewModel -> esta tela).
 *
 * Mostra: galhos restantes, cronômetro, o mapa e o botão Abrir / Placa.
 * A casa também aceita toque longo para colocar a placa.
 */
@Composable
fun TelaJogo(
    estado: JogoUiState,
    onTocar: (linha: Int, coluna: Int) -> Unit,
    onTocarLongo: (linha: Int, coluna: Int) -> Unit,
    onAlternarModo: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nível ${estado.nivel}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VerdePraca
        )

        // Aviso de erro (por exemplo: sem internet, usando os níveis padrão).
        if (estado.erro != null) {
            Text(text = estado.erro, color = Alerta)
        }

        val tabuleiro = estado.tabuleiro
        when {
            estado.carregando -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
            }

            tabuleiro == null -> {
                // Não carregou (nível desativado, por exemplo): só dá para voltar.
                OutlinedButton(onClick = onVoltar) { Text("Voltar") }
            }

            else -> {
                // Barra de informações: galhos que faltam e tempo.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🌿 Galhos: ${tabuleiro.galhosRestantes}", fontSize = 18.sp)
                    Text("⏱ ${formatarTempo(estado.segundos)}", fontSize = 18.sp)
                }

                MapaCampo(
                    tabuleiro = tabuleiro,
                    revelarGalhos = estado.terminou,
                    onTocar = if (estado.terminou) null else onTocar,
                    onTocarLongo = if (estado.terminou) null else onTocarLongo
                )

                // Botão Abrir / Placa: mostra o modo atual e troca ao clicar.
                Button(
                    onClick = onAlternarModo,
                    enabled = !estado.terminou,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (estado.modoPlaca) Color(0xFF2D5F8B) else VerdePraca
                    )
                ) {
                    Text(if (estado.modoPlaca) "⚑ Modo: Placa (toque para Abrir)" else "Modo: Abrir (toque para Placa)")
                }
                Text(
                    text = "Dica: toque longo numa casa também coloca a placa.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// 75 segundos -> "01:15"
private fun formatarTempo(segundos: Int): String =
    "%02d:%02d".format(segundos / 60, segundos % 60)

@Preview(showBackground = true)
@Composable
private fun TelaJogoPreview() {
    // Mapa de exemplo: 6x6, com um toque já dado para aparecer casas abertas.
    val tabuleiro = RegrasJogo.abrir(RegrasJogo.criarTabuleiro(6, 5), 0, 0, Random(1))
    CampoMinadoTheme {
        TelaJogo(
            estado = JogoUiState(nivel = 2, tabuleiro = tabuleiro, segundos = 75),
            onTocar = { _, _ -> },
            onTocarLongo = { _, _ -> },
            onAlternarModo = {},
            onVoltar = {}
        )
    }
}
