package com.jianwang.portfolio.pulseweather.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PulseWeatherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme())
            darkColorScheme(primary = Color(0xFF5B9BD5), secondary = Color(0xFFFFC857))
        else lightColorScheme(primary = Color(0xFF2E6EB5), secondary = Color(0xFFE39B2D), background = Color(0xFFF3F7FB)),
        content = content
    )
}
