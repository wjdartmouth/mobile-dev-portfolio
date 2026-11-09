package com.jianwang.portfolio.spotjournal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF0F6E6E)
private val Sand = Color(0xFFD9A066)

@Composable
fun SpotJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme(primary = Teal, secondary = Sand)
        else lightColorScheme(primary = Teal, secondary = Sand, background = Color(0xFFF4F1EA)),
        content = content
    )
}
