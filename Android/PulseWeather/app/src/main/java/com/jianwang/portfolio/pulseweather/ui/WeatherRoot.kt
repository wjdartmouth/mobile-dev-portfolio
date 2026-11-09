package com.jianwang.portfolio.pulseweather.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jianwang.portfolio.pulseweather.data.HourlyPoint
import com.jianwang.portfolio.pulseweather.data.WeatherBundle
import com.jianwang.portfolio.pulseweather.data.weatherLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherRoot(vm: WeatherViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    val query by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.any { g -> g }) vm.useDeviceLocation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pulse Weather") },
                actions = {
                    IconButton(onClick = vm::toggleUnit) {
                        Icon(Icons.Default.Thermostat, contentDescription = "Toggle Celsius Fahrenheit")
                    }
                    IconButton(onClick = {
                        if (vm.hasLocationPermission()) vm.useDeviceLocation()
                        else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }) { Icon(Icons.Default.MyLocation, contentDescription = "Use device location") }
                    IconButton(onClick = vm::refresh) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::setQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search city") },
                    singleLine = true
                )
                IconButton(onClick = vm::search) { Icon(Icons.Default.Search, contentDescription = "Search") }
            }
            if (results.isNotEmpty()) {
                LazyColumn(Modifier.height(160.dp)) {
                    items(results) { g ->
                        Text(
                            g.display,
                            modifier = Modifier.fillMaxWidth().clickable { vm.selectCity(g) }.padding(12.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            when (val state = ui) {
                WeatherUi.Idle -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Search a city or use your location.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                WeatherUi.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is WeatherUi.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                is WeatherUi.Ready -> WeatherDashboard(state.bundle, state.useF)
            }
        }
    }
}

@Composable
private fun WeatherDashboard(bundle: WeatherBundle, useF: Boolean) {
    fun Double.display(): String {
        val v = if (useF) this * 9 / 5 + 32 else this
        val unit = if (useF) "°F" else "°C"
        return "${v.toInt()}$unit"
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(bundle.placeName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (bundle.fromCache) Text("Offline cache", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Text(bundle.currentTempC.display(), fontSize = 56.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { contentDescription = "Current temperature ${bundle.currentTempC.display()}" })
                    Text(weatherLabel(bundle.weatherCode), style = MaterialTheme.typography.titleMedium)
                    Text("Wind ${bundle.windspeed.toInt()} km/h · Updated ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(bundle.fetchedAt))}")
                }
            }
        }
        item {
            Text("Next 24 hours", style = MaterialTheme.typography.titleMedium)
            HourlyChart(bundle.hourly, useF)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bundle.hourly.take(12)) { h ->
                    Card {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(h.time.takeLast(5))
                            Text(h.tempC.let { if (useF) it * 9 / 5 + 32 else it }.toInt().toString() + "°")
                        }
                    }
                }
            }
        }
        item {
            Text("7-day forecast", style = MaterialTheme.typography.titleMedium)
            bundle.daily.forEach { d ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(d.date)
                    Text(weatherLabel(d.code))
                    Text("${d.minC.let { if (useF) it * 9 / 5 + 32 else it }.toInt()}° / ${d.maxC.let { if (useF) it * 9 / 5 + 32 else it }.toInt()}°")
                }
            }
        }
    }
}

@Composable
private fun HourlyChart(points: List<HourlyPoint>, useF: Boolean) {
    if (points.isEmpty()) return
    val temps = points.map { if (useF) it.tempC * 9 / 5 + 32 else it.tempC }
    val minT = temps.minOrNull() ?: 0.0
    val maxT = temps.maxOrNull() ?: 1.0
    val span = max(1.0, maxT - minT)
    val line = MaterialTheme.colorScheme.primary
    Card(Modifier.fillMaxWidth().height(140.dp)) {
        Canvas(Modifier.fillMaxSize().padding(12.dp).semantics { contentDescription = "Hourly temperature chart" }) {
            val path = Path()
            points.indices.forEach { i ->
                val x = size.width * i / max(1, points.lastIndex)
                val y = size.height * (1f - ((temps[i] - minT) / span).toFloat())
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = line, style = Stroke(width = 4f, cap = StrokeCap.Round))
            points.indices.forEach { i ->
                val x = size.width * i / max(1, points.lastIndex)
                val y = size.height * (1f - ((temps[i] - minT) / span).toFloat())
                drawCircle(line, radius = 5f, center = Offset(x, y))
            }
        }
    }
}
