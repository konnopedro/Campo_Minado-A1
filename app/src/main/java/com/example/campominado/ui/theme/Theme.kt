package com.example.campominado.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de madeira inspirada no Tun Tun Clock.
private val Fundo = Color(0xFF17120F)
private val Superficie = Color(0xFF261D17)
private val Madeira = Color(0xFFC58B55)
private val MadeiraClara = Color(0xFFE0B17A)
private val Texto = Color(0xFFF6EBDD)
private val TextoSecundario = Color(0xFFBCA18A)
private val Borda = Color(0xFF493427)
private val Erro = Color(0xFFD98262)

private val CampoMinadoColors = darkColorScheme(
    primary = Madeira,
    onPrimary = Fundo,
    primaryContainer = Color(0xFF493427),
    onPrimaryContainer = Texto,
    secondary = MadeiraClara,
    onSecondary = Fundo,
    secondaryContainer = Color(0xFF3A2A20),
    onSecondaryContainer = Texto,
    tertiary = Color(0xFFB87943),
    onTertiary = Fundo,
    error = Erro,
    onError = Fundo,
    background = Fundo,
    onBackground = Texto,
    surface = Superficie,
    onSurface = Texto,
    surfaceVariant = Color(0xFF34251C),
    onSurfaceVariant = TextoSecundario,
    outline = Borda
)

@Composable
fun CampoMinadoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CampoMinadoColors,
        typography = Typography,
        content = content
    )
}
