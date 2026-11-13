package com.example.campominado.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarDefaults
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
import com.example.campominado.ui.screens.TelaAdmin
import com.example.campominado.ui.screens.TelaEditarNivel
import com.example.campominado.ui.screens.TelaHistorico
import com.example.campominado.ui.screens.TelaJogo
import com.example.campominado.ui.screens.TelaLogin
import com.example.campominado.ui.screens.TelaNiveis
import com.example.campominado.ui.screens.TelaRecordes
import com.example.campominado.ui.screens.TelaResultado
import com.example.campominado.ui.viewmodel.AdminViewModel
import com.example.campominado.ui.viewmodel.HistoricoViewModel
import com.example.campominado.ui.viewmodel.JogoViewModel
import com.example.campominado.ui.viewmodel.Perfil
import com.example.campominado.ui.viewmodel.SessaoViewModel
import com.example.campominado.ui.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val sessaoViewModel: SessaoViewModel = viewModel(factory = factory)
    val jogoViewModel: JogoViewModel = viewModel(factory = factory)
    val historicoViewModel: HistoricoViewModel = viewModel(factory = factory)
    val adminViewModel: AdminViewModel = viewModel(factory = factory)

    val sessao by sessaoViewModel.uiState.collectAsState()
    val jogo by jogoViewModel.uiState.collectAsState()
    val historico by historicoViewModel.uiState.collectAsState()
    val admin by adminViewModel.uiState.collectAsState()

    LaunchedEffect(sessao.perfil, sessao.jogadorId) {
        if (navController.currentBackStackEntry == null) return@LaunchedEffect
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

    LaunchedEffect(jogo.terminou) {
        if (jogo.terminou) {
            navController.navigate(Rotas.RESULTADO) {
                popUpTo(Rotas.JOGO) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val rotaAtual = navController.currentBackStackEntryAsState().value?.destination?.route
    val itensBarra = itensDaBarra(sessao.perfil)
    val mostrarBarra = itensBarra.any { it.rota == rotaAtual }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    itensBarra.forEach { item ->
                        NavigationBarItem(
                            selected = rotaAtual == item.rota,
                            onClick = {
                                navController.navigate(item.rota) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.icone) },
                            label = { Text(item.rotulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                TelaLogin(
                    jogadores = sessao.jogadoresSalvos.map { it.nome },
                    onEntrarComoJogador = sessaoViewModel::entrarComoJogador,
                    onEntrarComoAdmin = sessaoViewModel::entrarComoAdmin,
                    erro = sessao.erro
                )
            }
            composable(Rotas.NIVEIS) {
                LaunchedEffect(Unit) { sessaoViewModel.carregarNiveis() }
                TelaNiveis(
                    niveis = sessao.niveis,
                    nivelMaximoDesbloqueado = sessao.nivelMaximoDesbloqueado,
                    melhoresTempos = historico.melhoresTempos,
                    onSelecionarNivel = { nivel ->
                        jogoViewModel.iniciar(nivel, sessao.jogadorId ?: 0)
                        navController.navigate(Rotas.JOGO)
                    },
                    carregando = sessao.carregandoNiveis,
                    erro = sessao.avisoNiveis
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
                TelaResultado(
                    venceu = jogo.venceu,
                    nivel = jogo.nivel,
                    tempoSegundos = jogo.segundos,
                    temProximoNivel = jogo.temProximoNivel,
                    onJogarNovamente = {
                        jogoViewModel.jogarDeNovo()
                        navController.navigate(Rotas.JOGO) { popUpTo(Rotas.RESULTADO) { inclusive = true } }
                    },
                    onProximoNivel = {
                        jogoViewModel.irParaProximoNivel()
                        navController.navigate(Rotas.JOGO) { popUpTo(Rotas.RESULTADO) { inclusive = true } }
                    },
                    onVoltarNiveis = { navController.popBackStack(Rotas.NIVEIS, inclusive = false) }
                )
            }
            composable(Rotas.RECORDES) {
                TelaRecordes(
                    recordes = historico.recordes,
                    filtroMapa = historico.filtroMapa,
                    tamanhosDeMapa = historico.tamanhosDeMapa,
                    onFiltrarMapa = historicoViewModel::filtrarMapa,
                    onExcluirPartida = if (sessao.ehAdmin) historicoViewModel::excluirPartida else null
                )
            }
            composable(Rotas.HISTORICO) {
                TelaHistorico(
                    jogadorNome = historico.jogadorNome,
                    partidas = historico.partidas,
                    filtroResultado = historico.filtroResultado,
                    erro = historico.erro,
                    onFiltrarResultado = historicoViewModel::filtrarResultado,
                    onExcluirPartida = historicoViewModel::excluirPartida,
                    onAnotarPartida = historicoViewModel::anotarPartida,
                    onRenomearPerfil = historicoViewModel::renomearPerfil,
                    onExcluirPerfil = historicoViewModel::excluirPerfil,
                    onSair = sessaoViewModel::sair
                )
            }
            composable(Rotas.ADMIN_NIVEIS) {
                LaunchedEffect(Unit) { if (admin.niveis.isEmpty()) adminViewModel.carregar() }
                TelaAdmin(
                    estado = admin,
                    onFiltrarMapa = adminViewModel::filtrarMapa,
                    onEditar = { nivel ->
                        adminViewModel.editarNivel(nivel)
                        navController.navigate(Rotas.ADMIN_EDITAR)
                    },
                    onSair = { sessaoViewModel.sair() }
                )
            }
            composable(Rotas.ADMIN_EDITAR) {
                LaunchedEffect(admin.salvoComSucesso) {
                    if (admin.salvoComSucesso) navController.popBackStack()
                }
                val nivelEmEdicao = admin.nivelEmEdicao
                if (nivelEmEdicao != null) {
                    TelaEditarNivel(
                        nivel = nivelEmEdicao,
                        erro = admin.erro,
                        salvando = admin.salvando,
                        onSalvar = adminViewModel::salvarNivel,
                        onCancelar = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
