package com.jianwang.portfolio.syncnotes.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SyncNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme())
            darkColorScheme(primary = Color(0xFF7E57C2), secondary = Color(0xFFFFB74D))
        else lightColorScheme(primary = Color(0xFF5E35B1), secondary = Color(0xFFFB8C00), background = Color(0xFFF7F4FB)),
        content = content
    )
}
