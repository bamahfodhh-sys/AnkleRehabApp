package com.sanad.anklerehab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F4EE),
    onPrimaryContainer = Color(0xFF003D36),
    secondary = Color(0xFF455A64),
    background = Color(0xFFF7F9F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EFED),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF72DACB),
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005047),
    background = Color(0xFF101413),
    surface = Color(0xFF171C1A),
    surfaceVariant = Color(0xFF27312E),
    error = Color(0xFFFFB4AB)
)

@Composable
fun AnkleRehabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
