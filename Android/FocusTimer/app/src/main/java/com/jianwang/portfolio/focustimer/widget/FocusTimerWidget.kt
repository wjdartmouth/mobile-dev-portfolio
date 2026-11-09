package com.jianwang.portfolio.focustimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class FocusTimerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }
}

@Composable
private fun WidgetContent() {
    val phase = WidgetStateHolder.phase.replace('_', ' ')
    val time = WidgetStateHolder.timeDisplay
    val running = if (WidgetStateHolder.isRunning) "Running" else "Paused"
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1B4D3E)))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Focus Timer", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
        Text(
            text = time,
            style = TextStyle(color = ColorProvider(Color.White), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        )
        Text(text = "$phase · $running", style = TextStyle(color = ColorProvider(Color(0xFFE8A838)), fontSize = 14.sp))
    }
}

class FocusTimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusTimerWidget()
}
