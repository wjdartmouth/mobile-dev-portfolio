package com.jianwang.portfolio.ledgerlite.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme())
            darkColorScheme(primary = Color(0xFF6AA84F), secondary = Color(0xFFF6B26B))
        else lightColorScheme(primary = Color(0xFF38761D), secondary = Color(0xFFE69138), background = Color(0xFFF8F6F1)),
        content = content
    )
}
