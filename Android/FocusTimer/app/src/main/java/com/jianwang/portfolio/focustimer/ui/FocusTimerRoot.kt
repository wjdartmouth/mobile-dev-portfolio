package com.jianwang.portfolio.focustimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jianwang.portfolio.focustimer.data.FocusSession
import com.jianwang.portfolio.focustimer.data.TimerPhase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun FocusTimerRoot(vm: TimerViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "timer"
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = route == "timer",
                    onClick = { nav.navigate("timer") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                    label = { Text("Timer") }
                )
                NavigationBarItem(
                    selected = route == "history",
                    onClick = { nav.navigate("history") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = route == "settings",
                    onClick = { nav.navigate("settings") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "timer", modifier = Modifier.padding(padding)) {
            composable("timer") { TimerScreen(vm) }
            composable("history") { HistoryScreen(vm) }
            composable("settings") { SettingsScreen(vm) }
        }
    }
}

@Composable
private fun TimerScreen(vm: TimerViewModel) {
    val state by vm.ui.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (state.phase) {
                TimerPhase.FOCUS -> "Focus"
                TimerPhase.SHORT_BREAK -> "Short Break"
                TimerPhase.LONG_BREAK -> "Long Break"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${state.completedFocusCount} focus sessions today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
            val track = MaterialTheme.colorScheme.surfaceVariant
            val progressColor = MaterialTheme.colorScheme.primary
            Canvas(Modifier.fillMaxSize().semantics { contentDescription = "Timer progress ${ (state.progress * 100).toInt() } percent" }) {
                val stroke = 18.dp.toPx()
                drawArc(
                    color = track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                    size = Size(size.minDimension, size.minDimension)
                )
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * state.progress,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                    size = Size(size.minDimension, size.minDimension)
                )
            }
            Text(
                text = state.timeDisplay,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { contentDescription = "Remaining time ${state.timeDisplay}" }
            )
        }
        Spacer(Modifier.height(36.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedIconButton(onClick = vm::reset, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset timer")
            }
            FilledIconButton(onClick = vm::toggle, modifier = Modifier.size(72.dp)) {
                Icon(
                    if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }
            OutlinedIconButton(onClick = vm::skip, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Skip phase")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(vm: TimerViewModel) {
    val sessions by vm.history.collectAsState()
    val focusSessions = sessions.filter { it.phase == "FOCUS" && it.wasCompleted }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("History") })
        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sessions yet. Start a focus timer!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            DailyChart(focusSessions)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions, key = { it.id }) { SessionRow(it) }
            }
        }
    }
}

@Composable
private fun DailyChart(sessions: List<FocusSession>) {
    val byDay: List<Pair<String, Int>> = remember(sessions) {
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        val map = linkedMapOf<String, Int>()
        sessions.takeLast(40).forEach { s ->
            val key = fmt.format(Date(s.completedAt))
            map[key] = (map[key] ?: 0) + 1
        }
        map.entries.toList().takeLast(7).map { it.key to it.value }
    }
    if (byDay.isEmpty()) return
    val maxCount = max(1, byDay.maxOf { it.second })
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Focus sessions (recent)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                byDay.forEach { pair ->
                    val day = pair.first
                    val count = pair.second
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val barColor = MaterialTheme.colorScheme.primary
                        val barHeight = (90f * count / maxCount).coerceAtLeast(4f).dp
                        Canvas(Modifier.size(width = 28.dp, height = barHeight)) {
                            drawRect(barColor)
                        }
                        Text(day, style = MaterialTheme.typography.labelSmall)
                        Text("$count", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: FocusSession) {
    val fmt = remember { SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(session.phase.replace('_', ' '), fontWeight = FontWeight.Medium)
                Text(fmt.format(Date(session.completedAt)), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "${session.completedSeconds / 60}m",
                color = if (session.wasCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(vm: TimerViewModel) {
    val state by vm.ui.collectAsState()
    val s = state.settings
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Settings") })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                DurationSlider("Focus (minutes)", s.focusMinutes, 5..60) {
                    vm.updateSettings { cur -> cur.copy(focusMinutes = it) }
                }
            }
            item {
                DurationSlider("Short break", s.shortBreakMinutes, 1..30) {
                    vm.updateSettings { cur -> cur.copy(shortBreakMinutes = it) }
                }
            }
            item {
                DurationSlider("Long break", s.longBreakMinutes, 5..45) {
                    vm.updateSettings { cur -> cur.copy(longBreakMinutes = it) }
                }
            }
            item {
                DurationSlider("Sessions until long break", s.sessionsUntilLongBreak, 2..8) {
                    vm.updateSettings { cur -> cur.copy(sessionsUntilLongBreak = it) }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-start next phase")
                    Switch(checked = s.autoStartNext, onCheckedChange = { v -> vm.updateSettings { it.copy(autoStartNext = v) } })
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Haptics")
                    Switch(checked = s.hapticsEnabled, onCheckedChange = { v -> vm.updateSettings { it.copy(hapticsEnabled = v) } })
                }
            }
        }
    }
}

@Composable
private fun DurationSlider(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    var local by remember(value) { mutableIntStateOf(value) }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text("$local")
        }
        Slider(
            value = local.toFloat(),
            onValueChange = { local = it.toInt() },
            onValueChangeFinished = { onChange(local) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}
