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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campominado.logic.Casa
import com.example.campominado.logic.Tabuleiro

private val CasaMadeira = Color(0xFF805333)
private val CasaAberta = Color(0xFFD5B58D)
private val CasaGalho = Color(0xFF9B4F32)
private val CasaPlaca = Color(0xFF477D91)
private val TextoCasa = Color(0xFF2A1B12)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapaCampo(
    tabuleiro: Tabuleiro,
    modifier: Modifier = Modifier,
    revelarGalhos: Boolean = false,
    onTocar: ((Int, Int) -> Unit)? = null,
    onTocarLongo: ((Int, Int) -> Unit)? = null
) {
    val tamanhoFonte = if (tabuleiro.tamanho >= 9) 13.sp else 18.sp
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (linha in 0 until tabuleiro.tamanho) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (coluna in 0 until tabuleiro.tamanho) {
                    CasaDoMapa(
                        casa = tabuleiro.casa(linha, coluna),
                        revelarGalhos = revelarGalhos,
                        tamanhoFonte = tamanhoFonte,
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
    val mostrarGalho = casa.temGalho && (casa.aberta || revelarGalhos)
    val fundo = when {
        casa.aberta && casa.temGalho -> CasaGalho
        casa.aberta -> CasaAberta
        mostrarGalho -> CasaAberta
        else -> CasaMadeira
    }
    val texto = when {
        mostrarGalho -> "🌿"
        casa.aberta && casa.vizinhos > 0 -> casa.vizinhos.toString()
        !casa.aberta && casa.placa -> "⚑"
        else -> ""
    }
    val corTexto = when {
        !casa.aberta && casa.placa -> CasaPlaca
        casa.vizinhos == 1 -> Color(0xFF477D91)
        casa.vizinhos == 2 -> Color(0xFF477044)
        casa.vizinhos >= 3 -> Color(0xFF9B4F32)
        else -> TextoCasa
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(5.dp))
            .background(fundo)
            .then(
                if (onTocar != null) {
                    Modifier.combinedClickable(onClick = onTocar, onLongClick = onTocarLongo)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, color = corTexto, fontSize = tamanhoFonte, fontWeight = FontWeight.Bold)
    }
}
