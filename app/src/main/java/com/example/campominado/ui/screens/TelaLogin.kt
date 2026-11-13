package com.example.campominado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campominado.ui.theme.CampoMinadoTheme

@Composable
fun TelaLogin(
    jogadores: List<String>,
    onEntrarComoJogador: (String) -> Unit,
    onEntrarComoAdmin: (usuario: String, senha: String) -> Unit,
    modifier: Modifier = Modifier,
    erro: String? = null
) {
    var jogadorSelecionado by remember(jogadores) { mutableStateOf(jogadores.firstOrNull().orEmpty()) }
    var novoJogador by remember { mutableStateOf("") }
    var usuarioAdmin by remember { mutableStateOf("") }
    var senhaAdmin by remember { mutableStateOf("") }
    var modoAdmin by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌳", style = MaterialTheme.typography.displayMedium)
        Text("Campo Minado", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Praça Osório • Curitiba", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        if (erro != null) {
            Text(erro, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { modoAdmin = false }, modifier = Modifier.weight(1f)) {
                        Text("Jogador")
                    }
                    OutlinedButton(onClick = { modoAdmin = true }, modifier = Modifier.weight(1f)) {
                        Text("Administrador")
                    }
                }

                if (modoAdmin) {
                    Text("Acesso do administrador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = usuarioAdmin,
                        onValueChange = { usuarioAdmin = it },
                        label = { Text("Usuário") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = senhaAdmin,
                        onValueChange = { senhaAdmin = it },
                        label = { Text("Senha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onEntrarComoAdmin(usuarioAdmin.trim(), senhaAdmin) },
                        enabled = usuarioAdmin.isNotBlank() && senhaAdmin.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Entrar como administrador") }
                } else {
                    Text("Quem vai jogar?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (jogadores.isEmpty()) {
                        Text("Ainda não há jogadores cadastrados. Crie o primeiro perfil abaixo.")
                    } else {
                        LazyColumn(modifier = Modifier.height(140.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(jogadores) { nome ->
                                OutlinedButton(
                                    onClick = { jogadorSelecionado = nome },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (jogadorSelecionado == nome) "✓  $nome" else nome)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = novoJogador,
                        onValueChange = { novoJogador = it },
                        label = { Text("Ou digite um nome novo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onEntrarComoJogador(novoJogador.trim().ifBlank { jogadorSelecionado }) },
                        enabled = novoJogador.isNotBlank() || jogadorSelecionado.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Começar a jogar") }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Encontre os galhos, coloque placas e avance pelos 15 níveis.", style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun TelaLoginPreview() {
    CampoMinadoTheme {
        TelaLogin(
            jogadores = listOf("Daniel", "Pedro", "Ana"),
            onEntrarComoJogador = {},
            onEntrarComoAdmin = { _, _ -> }
        )
    }
}
