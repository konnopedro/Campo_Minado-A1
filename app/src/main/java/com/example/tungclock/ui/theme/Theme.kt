package com.example.tungclock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val TunTunDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC58B55),
    onPrimary = Color(0xFF21160F),
    secondary = Color(0xFFE0B17A),
    onSecondary = Color(0xFF2A1B12),
    tertiary = Color(0xFF9FB79A),
    background = Color(0xFF17120F),
    onBackground = Color(0xFFF6EBDD),
    surface = Color(0xFF261D17),
    onSurface = Color(0xFFF6EBDD),
    surfaceVariant = Color(0xFF38291F),
    onSurfaceVariant = Color(0xFFD0BBA7),
    outline = Color(0xFF6E5140)
)

@Composable
fun TunTunClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TunTunDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun TungOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFFF6EBDD),
            unfocusedTextColor = Color(0xFFF6EBDD),
            focusedLabelColor = Color(0xFFE0B17A),
            unfocusedLabelColor = Color(0xFFBCA18A),
            focusedBorderColor = Color(0xFFC58B55),
            unfocusedBorderColor = Color(0xFF6E5140),
            cursorColor = Color(0xFFE0B17A)
        )
    )
}
