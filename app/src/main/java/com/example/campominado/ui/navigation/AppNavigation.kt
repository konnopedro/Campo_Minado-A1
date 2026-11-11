package com.example.campominado.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campominado.ui.screens.TelaEmConstrucao
import com.example.campominado.ui.screens.TelaJogo
import com.example.campominado.ui.viewmodel.AdminViewModel
import com.example.campominado.ui.viewmodel.HistoricoViewModel
import com.example.campominado.ui.viewmodel.JogoViewModel
import com.example.campominado.ui.viewmodel.Perfil
import com.example.campominado.ui.viewmodel.SessaoViewModel
import com.example.campominado.ui.viewmodel.ViewModelFactory

/**
 * Esqueleto do app: cria os ViewModels, a Bottom Bar (que muda conforme o perfil) e o NavHost.
 *
 * Os ViewModels são criados aqui, no nível da Activity, e compartilhados entre as telas
 * (por exemplo, o JogoViewModel é o mesmo na tela do Jogo e na tela de Resultado).
 *
 * As telas marcadas com TelaEmConstrucao são provisórias e serão trocadas pelas reais.
 */
@Composable
fun AppNavigation(factory: ViewModelFactory) {
    val navController = rememberNavController()

    val sessaoViewModel: SessaoViewModel = viewModel(factory = factory)
    val jogoViewModel: JogoViewModel = viewModel(factory = factory)
    val historicoViewModel: HistoricoViewModel = viewModel(factory = factory)
    val adminViewModel: AdminViewModel = viewModel(factory = factory)

    val sessao by sessaoViewModel.uiState.collectAsState()
    val jogo by jogoViewModel.uiState.collectAsState()

    // Sempre que o perfil muda (entrou, saiu), a navegação vai para a tela inicial dele.
    // O popUpTo(0) limpa o histórico de telas, para o botão Voltar não reabrir o Login.
    LaunchedEffect(sessao.perfil, sessao.jogadorId) {
        val destino = when (sessao.perfil) {
            Perfil.NENHUM -> Rotas.LOGIN
            Perfil.JOGADOR -> Rotas.NIVEIS
            Perfil.ADMIN -> Rotas.ADMIN_NIVEIS
        }
        if (sessao.perfil == Perfil.JOGADOR && sessao.jogadorId != null) {
            historicoViewModel.definirJogador(sessao.jogadorId!!)
        }
        navController.navigate(destino) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Quando a partida termina, vai para o Resultado (e tira o Jogo da pilha de telas).
    LaunchedEffect(jogo.terminou) {
        if (jogo.terminou) {
            navController.navigate(Rotas.RESULTADO) {
                popUpTo(Rotas.JOGO) { inclusive = true }
            }
        }
    }

    // A Bottom Bar só aparece nas telas "principais" (não no Login, Jogo, Resultado nem Editar).
    val rotaAtual = navController.currentBackStackEntryAsState().value?.destination?.route
    val itensBarra = itensDaBarra(sessao.perfil)
    val mostrarBarra = itensBarra.any { it.rota == rotaAtual }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    itensBarra.forEach { item ->
                        NavigationBarItem(
                            selected = rotaAtual == item.rota,
                            onClick = {
                                navController.navigate(item.rota) {
                                    // Evita empilhar a mesma tela várias vezes ao trocar de aba.
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.icone) },
                            label = { Text(item.rotulo) }
                        )
                    }
                }
            }
        }
    ) { espaco ->
        NavHost(
            navController = navController,
            startDestination = Rotas.LOGIN,
            modifier = Modifier.padding(espaco)
        ) {
            composable(Rotas.LOGIN) {
                // TODO (Pessoa 2): trocar pela TelaLogin real.
                TelaEmConstrucao(
                    titulo = "Login",
                    acoes = listOf(
                        "Entrar como Jogador (teste)" to { sessaoViewModel.entrarComoJogador("Jogador") },
                        "Entrar como Admin (teste)" to { sessaoViewModel.entrarComoAdmin("admin", "admin123") }
                    )
                )
            }

            composable(Rotas.NIVEIS) {
                // TODO (Pessoa 3): trocar pela TelaNiveis real.
                TelaEmConstrucao(
                    titulo = "Níveis",
                    acoes = listOf(
                        "Jogar nível 1 (teste)" to {
                            // Prepara a partida ANTES de navegar, para o Jogo já abrir com o mapa pronto.
                            jogoViewModel.iniciar(1, sessao.jogadorId ?: 0)
                            navController.navigate(Rotas.JOGO)
                        },
                        "Sair" to { sessaoViewModel.sair() }
                    )
                )
            }

            composable(Rotas.JOGO) {
                TelaJogo(
                    estado = jogo,
                    onTocar = jogoViewModel::aoTocar,
                    onTocarLongo = jogoViewModel::aoTocarLongo,
                    onAlternarModo = jogoViewModel::alternarModo,
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable(Rotas.RESULTADO) {
                // TODO (Pessoa 2): trocar pela TelaResultado real (pode usar o MapaCampo).
                TelaEmConstrucao(
                    titulo = if (jogo.venceu) "Vitória!" else "Fim de jogo",
                    acoes = buildList {
                        add("Jogar de novo" to {
                            jogoViewModel.jogarDeNovo()
                            navController.navigate(Rotas.JOGO) { popUpTo(Rotas.RESULTADO) { inclusive = true } }
                        })
                        if (jogo.venceu && jogo.temProximoNivel) {
                            add("Próximo nível" to {
                                jogoViewModel.irParaProximoNivel()
                                navController.navigate(Rotas.JOGO) { popUpTo(Rotas.RESULTADO) { inclusive = true } }
                            })
                        }
                        add("Voltar aos níveis" to {
                            navController.navigate(Rotas.NIVEIS) { popUpTo(Rotas.RESULTADO) { inclusive = true } }
                        })
                    }
                )
            }

            composable(Rotas.RECORDES) {
                // TODO (Pessoa 3): trocar pela TelaRecordes real.
                TelaEmConstrucao(titulo = "Recordes")
            }

            composable(Rotas.HISTORICO) {
                // TODO (Pessoa 3): trocar pela TelaHistorico real.
                TelaEmConstrucao(titulo = "Histórico e Perfil")
            }

            composable(Rotas.ADMIN_NIVEIS) {
                // TODO (item 7): Painel do Admin.
                TelaEmConstrucao(
                    titulo = "Painel do Admin",
                    acoes = listOf("Sair" to { sessaoViewModel.sair() })
                )
            }

            composable(Rotas.ADMIN_EDITAR) {
                // TODO (item 7): Editar nível.
                TelaEmConstrucao(titulo = "Editar nível")
            }
        }
    }
}
