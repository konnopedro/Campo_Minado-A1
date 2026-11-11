package com.example.campominado.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta aprovada para a ambientação da Praça Osório.
private val VerdePraca = Color(0xFF2E5E3A)
private val VerdeClaro = Color(0xFFDCE8D6)
private val Grama = Color(0xFF6FA563)
private val Calcada = Color(0xFFE8DFC9)
private val Fundo = Color(0xFFF2F4EC)
private val Texto = Color(0xFF1D2A1F)
private val Alerta = Color(0xFFB4541A)
private val Azul = Color(0xFF2D5F8B)

private val CampoMinadoColors = lightColorScheme(
    primary = VerdePraca,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = Texto,
    secondary = Grama,
    onSecondary = Color.White,
    secondaryContainer = Calcada,
    onSecondaryContainer = Texto,
    tertiary = Azul,
    onTertiary = Color.White,
    error = Alerta,
    onError = Color.White,
    background = Fundo,
    onBackground = Texto,
    surface = Color.White,
    onSurface = Texto,
    surfaceVariant = VerdeClaro,
    onSurfaceVariant = Texto,
    outline = VerdePraca.copy(alpha = 0.55f)
)

@Composable
fun CampoMinadoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CampoMinadoColors,
        typography = Typography,
        content = content
    )
}
