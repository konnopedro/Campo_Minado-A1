package com.example.campominado.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CampoMinadoColors = darkColorScheme(
    primary = Color(0xFF527A46),
    secondary = Color(0xFFB6C99B),
    background = Color(0xFF172018),
    surface = Color(0xFF202B21),
    onPrimary = Color.White,
    onSecondary = Color(0xFF172018),
    onBackground = Color(0xFFEAF0E5),
    onSurface = Color(0xFFEAF0E5)
)

@Composable
fun CampoMinadoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CampoMinadoColors,
        typography = Typography,
        content = content
    )
}
