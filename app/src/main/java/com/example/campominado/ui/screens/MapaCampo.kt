package com.example.campominado.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campominado.logic.Casa
import com.example.campominado.logic.Tabuleiro

// Cores do mapa (paleta do CLAUDE.md).
private val CorCasaFechada = Color(0xFF6FA563)   // grama
private val CorCasaAberta = Color(0xFFE8DFC9)    // calçada
private val CorGalho = Color(0xFFB4541A)         // alerta
private val CorPlaca = Color(0xFF2D5F8B)         // azul
private val CorTexto = Color(0xFF1D2A1F)
private val CorNumero2 = Color(0xFF2E5E3A)

/**
 * Desenha o mapa N x N. Não guarda estado nenhum: recebe o Tabuleiro e avisa os toques.
 * É reutilizável: a tela de Resultado também usa, com revelarGalhos = true e sem toques.
 *
 * @param revelarGalhos mostra todos os galhos (usado quando a partida acabou).
 * @param onTocar toque curto numa casa (linha, coluna). Se for null, o mapa não reage a toques.
 * @param onTocarLongo toque longo numa casa (coloca a placa).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapaCampo(
    tabuleiro: Tabuleiro,
    modifier: Modifier = Modifier,
    revelarGalhos: Boolean = false,
    onTocar: ((Int, Int) -> Unit)? = null,
    onTocarLongo: ((Int, Int) -> Unit)? = null
) {
    // Mapas maiores têm casas menores, então o texto diminui um pouco.
    val tamanhoFonte = if (tabuleiro.tamanho >= 9) 13.sp else 18.sp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (linha in 0 until tabuleiro.tamanho) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (coluna in 0 until tabuleiro.tamanho) {
                    CasaDoMapa(
                        casa = tabuleiro.casa(linha, coluna),
                        revelarGalhos = revelarGalhos,
                        tamanhoFonte = tamanhoFonte,
                        // Se o mapa aceita toques, cada casa avisa com a sua (linha, coluna).
                        onTocar = if (onTocar != null) ({ onTocar(linha, coluna) }) else null,
                        onTocarLongo = if (onTocarLongo != null) ({ onTocarLongo(linha, coluna) }) else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.CasaDoMapa(
    casa: Casa,
    revelarGalhos: Boolean,
    tamanhoFonte: androidx.compose.ui.unit.TextUnit,
    onTocar: (() -> Unit)?,
    onTocarLongo: (() -> Unit)?
) {
    // O que a casa mostra e com qual cor de fundo.
    val mostrarGalho = casa.temGalho && (casa.aberta || revelarGalhos)
    val fundo = when {
        casa.aberta && casa.temGalho -> CorGalho       // o galho que o jogador abriu
        casa.aberta -> CorCasaAberta
        mostrarGalho -> CorCasaAberta                  // galho revelado no fim da partida
        else -> CorCasaFechada
    }
    val texto = when {
        mostrarGalho -> "🌿"
        casa.aberta && casa.vizinhos > 0 -> casa.vizinhos.toString()
        !casa.aberta && casa.placa -> "⚑"
        else -> ""
    }
    val corTexto = when {
        !casa.aberta && casa.placa -> CorPlaca
        casa.vizinhos == 1 -> CorPlaca
        casa.vizinhos == 2 -> CorNumero2
        casa.vizinhos >= 3 -> CorGalho
        else -> CorTexto
    }

    Box(
        modifier = Modifier
            .weight(1f)                 // todas as casas da linha dividem a largura igualmente
            .aspectRatio(1f)            // e ficam quadradas
            .clip(RoundedCornerShape(4.dp))
            .background(fundo)
            .then(
                if (onTocar != null) {
                    Modifier.combinedClickable(
                        onClick = onTocar,
                        onLongClick = onTocarLongo
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, color = corTexto, fontSize = tamanhoFonte, fontWeight = FontWeight.Bold)
    }
}
