package com.example.tungclock

import com.example.tungclock.data.*
import com.example.tungclock.ui.*

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private val Fundo = Color(0xFF17120F)
private val Divisoria = Color(0xFF493427)
private val Madeira = Color(0xFFC58B55)
private val TextoBranco = Color(0xFFF6EBDD)
private val TextoInativo = Color(0xFFBCA18A)
private val Indicador = Color(0xFF493023)

object Rotas {
    const val RELOGIO = "relogio"
    const val CRONOMETRO = "cronometro"
    const val TIMER = "timer"
    const val ALARMES = "alarmes"
    const val DETALHE_ALARME = "detalhe_alarme/{id}"
    const val FUSOS = "fusos"
    const val DETALHE_FUSO = "detalhe_fuso/{id}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val alarmeRepository = remember(database) { com.example.tungclock.data.repository.AlarmeRepository(database.alarmeDao()) }
    val fusoRepository = remember(database) { com.example.tungclock.data.repository.FusoHorarioRepository(database.fusoHorarioDao()) }
    val alarmeViewModel: com.example.tungclock.viewmodel.AlarmeViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.tungclock.viewmodel.AlarmeViewModelFactory(alarmeRepository))
    val fusoViewModel: com.example.tungclock.viewmodel.FusoViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.tungclock.viewmodel.FusoViewModelFactory(fusoRepository))
    val alarmeState by alarmeViewModel.uiState.collectAsState()
    val fusoState by fusoViewModel.uiState.collectAsState()
    val alarmes = alarmeState.alarmes
    val fusos = fusoState.fusos

    val mostrarBarra = rotaAtual == Rotas.RELOGIO || rotaAtual == Rotas.ALARMES || rotaAtual == Rotas.CRONOMETRO || rotaAtual == Rotas.TIMER

    Scaffold(
        containerColor = Fundo,
        bottomBar = {
            if (mostrarBarra) {
                Column(modifier = Modifier.background(Fundo)) {
                    Spacer(
                        modifier = Modifier.fillMaxWidth().height(1.dp).background(Divisoria)
                    )
                    NavigationBar(
                        containerColor = Fundo,
                        contentColor = TextoBranco,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = rotaAtual == Rotas.RELOGIO,
                            onClick = { navController.navigate(Rotas.RELOGIO) { popUpTo(Rotas.RELOGIO) { inclusive = false }; launchSingleTop = true } },
                            icon = { Text("🕑") },
                            label = { Text("Relógio") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Madeira, selectedTextColor = Madeira, unselectedIconColor = TextoBranco, unselectedTextColor = TextoInativo, indicatorColor = Indicador)
                        )
                        NavigationBarItem(
                            selected = rotaAtual == Rotas.ALARMES,
                            onClick = { navController.navigate(Rotas.ALARMES) { launchSingleTop = true } },
                            icon = { Text("⏰") },
                            label = { Text("Alarmes") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Madeira, selectedTextColor = Madeira, unselectedIconColor = TextoBranco, unselectedTextColor = TextoInativo, indicatorColor = Indicador)
                        )
                        NavigationBarItem(
                            selected = rotaAtual == Rotas.CRONOMETRO,
                            onClick = { navController.navigate(Rotas.CRONOMETRO) { launchSingleTop = true } },
                            icon = { Text("⏱️") },
                            label = { Text("Cronômetro") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Madeira, selectedTextColor = Madeira, unselectedIconColor = TextoBranco, unselectedTextColor = TextoInativo, indicatorColor = Indicador)
                        )
                        NavigationBarItem(
                            selected = rotaAtual == Rotas.TIMER,
                            onClick = { navController.navigate(Rotas.TIMER) { launchSingleTop = true } },
                            icon = { Text("⏲️") },
                            label = { Text("Timer") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Madeira, selectedTextColor = Madeira, unselectedIconColor = TextoBranco, unselectedTextColor = TextoInativo, indicatorColor = Indicador)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Rotas.RELOGIO) {
            composable(Rotas.RELOGIO) {
                RelogioScreen(
                    paddingValues = paddingValues,
                    fusos = fusos,
                    onAbrirAlarmes = { navController.navigate(Rotas.ALARMES) },
                    onAbrirFusos = { navController.navigate(Rotas.FUSOS) },
                    onAbrirDetalhesFuso = { id -> navController.navigate("detalhe_fuso/$id") }
                )
            }
            composable(Rotas.ALARMES) {
                AlarmesScreen(
                    paddingValues = paddingValues,
                    alarmes = alarmes,
                    viewModel = alarmeViewModel,
                    onVoltar = { navController.navigate(Rotas.RELOGIO) { launchSingleTop = true } },
                    onAbrirDetalhes = { id -> navController.navigate("detalhe_alarme/$id") }
                )
            }
            composable(Rotas.CRONOMETRO) { CronometroScreen(paddingValues = paddingValues) }
            composable(Rotas.TIMER) { TimerScreen(paddingValues = paddingValues) }
            composable(Rotas.FUSOS) {
                FusosScreen(
                    paddingValues = paddingValues,
                    fusos = fusos,
                    viewModel = fusoViewModel,
                    onVoltar = { navController.popBackStack() },
                    onAbrirDetalhes = { id -> navController.navigate("detalhe_fuso/$id") }
                )
            }
            composable(
                route = Rotas.DETALHE_ALARME,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                DetalheAlarmeScreen(
                    id = entry.arguments?.getInt("id") ?: -1,
                    alarmes = alarmes,
                    viewModel = alarmeViewModel,
                    onVoltar = { navController.popBackStack() }
                )
            }
            composable(
                route = Rotas.DETALHE_FUSO,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                DetalheFusoScreen(
                    id = entry.arguments?.getInt("id") ?: -1,
                    fusos = fusos,
                    viewModel = fusoViewModel,
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}
