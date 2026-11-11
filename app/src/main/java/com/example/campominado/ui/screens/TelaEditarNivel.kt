package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.logic.NiveisPadrao
import com.example.campominado.ui.theme.CampoMinadoTheme

private val VerdePraca = Color(0xFF2E5E3A)
private val Alerta = Color(0xFFB4541A)

/**
 * Editar nível (Admin): muda a quantidade de galhos e se o nível está ativo.
 * O tamanho do mapa é fixo e só aparece para consulta.
 *
 * Os valores digitados ficam guardados aqui na tela (remember) até o Admin tocar em Salvar;
 * só então são enviados ao ViewModel.
 */
@Composable
fun TelaEditarNivel(
    nivel: NivelRemoto,
    erro: String?,
    onSalvar: (galhos: Int, ativo: Boolean) -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // remember(nivel): se outro nível for aberto, os campos recomeçam com os valores dele.
    var galhosTexto by remember(nivel) { mutableStateOf(nivel.galhos.toString()) }
    var ativo by remember(nivel) { mutableStateOf(nivel.ativo) }

    val maximo = NiveisPadrao.maximoDeGalhos(nivel.tamanhoMapa)
    val galhos = galhosTexto.toIntOrNull()   // null se estiver vazio ou não for número

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Editar nível ${nivel.nivel}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VerdePraca)
        Text("Mapa ${nivel.tamanhoMapa}×${nivel.tamanhoMapa} (tamanho fixo)")

        OutlinedTextField(
            value = galhosTexto,
            onValueChange = { galhosTexto = it.filter { c -> c.isDigit() } },  // aceita só dígitos
            label = { Text("Galhos (1 a $maximo)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = galhos == null || galhos !in 1..maximo,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nível ativo")
            Switch(checked = ativo, onCheckedChange = { ativo = it })
        }

        // Erro vindo do ViewModel (limite de galhos, falha de conexão ao salvar...).
        if (erro != null) {
            Text(text = erro, color = Alerta)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(
                onClick = { if (galhos != null) onSalvar(galhos, ativo) },
                enabled = galhos != null,
                modifier = Modifier.weight(1f)
            ) { Text("Salvar") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TelaEditarNivelPreview() {
    CampoMinadoTheme {
        TelaEditarNivel(
            nivel = NivelRemoto(nivel = 5, tamanhoMapa = 7, galhos = 8),
            erro = null,
            onSalvar = { _, _ -> },
            onCancelar = {}
        )
    }
}
