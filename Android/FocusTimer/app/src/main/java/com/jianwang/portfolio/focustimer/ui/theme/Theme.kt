package com.jianwang.portfolio.focustimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1B4D3E)
private val GreenLight = Color(0xFF2E7D5B)
private val Accent = Color(0xFFE8A838)

private val Light = lightColorScheme(
    primary = Green,
    secondary = Accent,
    tertiary = GreenLight,
    background = Color(0xFFF7F5F0),
    surface = Color.White
)
private val Dark = darkColorScheme(
    primary = GreenLight,
    secondary = Accent,
    tertiary = Green,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun FocusTimerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        content = content
    )
}
