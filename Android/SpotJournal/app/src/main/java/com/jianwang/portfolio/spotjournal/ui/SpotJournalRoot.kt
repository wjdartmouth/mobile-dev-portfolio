package com.jianwang.portfolio.spotjournal.ui

import android.Manifest
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.jianwang.portfolio.spotjournal.data.JournalEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun SpotJournalRoot(vm: JournalViewModel = viewModel()) {
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route ?: "list"
    val entries by vm.entries.collectAsState()
    val query by vm.searchQuery.collectAsState()

    Scaffold(
        bottomBar = {
            if (route == "list" || route == "map") {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == "list",
                        onClick = { nav.navigate("list") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.List, contentDescription = "List") },
                        label = { Text("List") }
                    )
                    NavigationBarItem(
                        selected = route == "map",
                        onClick = { nav.navigate("map") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Map") }
                    )
                }
            }
        },
        floatingActionButton = {
            if (route == "list" || route == "map") {
                FloatingActionButton(onClick = {
                    vm.startNew()
                    nav.navigate("edit/new")
                }) { Icon(Icons.Default.Add, contentDescription = "New entry") }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "list", modifier = Modifier.padding(padding)) {
            composable("list") {
                ListScreen(entries, query, onQuery = vm::setQuery, onOpen = {
                    vm.edit(it); nav.navigate("edit/${it.id}")
                }, onDelete = vm::delete)
            }
            composable("map") {
                MapScreen(entries) { entry ->
                    vm.edit(entry); nav.navigate("edit/${entry.id}")
                }
            }
            composable(
                "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                EditorScreen(vm, onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListScreen(
    entries: List<JournalEntry>,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (JournalEntry) -> Unit,
    onDelete: (JournalEntry) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Spot Journal") })
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search notes, places…") },
            singleLine = true
        )
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No spots yet. Tap + to capture a place.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries, key = { it.id }) { entry ->
                    EntryCard(entry, onOpen, onDelete)
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, onOpen: (JournalEntry) -> Unit, onDelete: (JournalEntry) -> Unit) {
    val fmt = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth().clickable { onOpen(entry) }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (entry.photoPath != null) {
                AsyncImage(
                    model = File(entry.photoPath),
                    contentDescription = "Photo for ${entry.title}",
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.SemiBold)
                if (entry.placeLabel != null) Text(entry.placeLabel!!, style = MaterialTheme.typography.bodySmall)
                Text(fmt.format(Date(entry.createdAt)), style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = { onDelete(entry) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${entry.title}")
            }
        }
    }
}

@Composable
private fun MapScreen(entries: List<JournalEntry>, onOpen: (JournalEntry) -> Unit) {
    val withCoords = entries.filter { it.latitude != null && it.longitude != null }
    Column(Modifier.fillMaxSize()) {
        Text("Map", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        if (withCoords.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Entries with location appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            OsmMap(withCoords, Modifier.weight(1f).fillMaxWidth())
            LazyColumn(Modifier.height(160.dp), contentPadding = PaddingValues(8.dp)) {
                items(withCoords) { e ->
                    Text(
                        "${e.title} · ${e.placeLabel ?: "${e.latitude}, ${e.longitude}"}",
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(e) }.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OsmMap(entries: List<JournalEntry>, modifier: Modifier = Modifier) {
    val html = remember(entries) { buildLeafletHtml(entries) }
    AndroidView(
        modifier = modifier.semantics { contentDescription = "OpenStreetMap with ${entries.size} pins" },
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://leafletjs.com/", html, "text/html", "UTF-8", null)
            }
        },
        update = { it.loadDataWithBaseURL("https://leafletjs.com/", html, "text/html", "UTF-8", null) }
    )
}

private fun buildLeafletHtml(entries: List<JournalEntry>): String {
    val arr = JSONArray()
    entries.forEach { e ->
        arr.put(JSONObject().apply {
            put("lat", e.latitude)
            put("lng", e.longitude)
            put("title", e.title)
            put("label", e.placeLabel ?: "")
        })
    }
    val center = entries.first()
    return """
<!DOCTYPE html><html><head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>html,body,#map{height:100%;margin:0}</style>
</head><body><div id="map"></div><script>
var map = L.map('map').setView([${center.latitude}, ${center.longitude}], 12);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19, attribution: '&copy; OpenStreetMap'
}).addTo(map);
var pins = $arr;
pins.forEach(function(p){
  L.marker([p.lat,p.lng]).addTo(map).bindPopup('<b>'+p.title+'</b><br/>'+p.label);
});
if (pins.length > 1) {
  var g = pins.map(function(p){return [p.lat,p.lng];});
  map.fitBounds(g, {padding:[24,24]});
}
</script></body></html>
""".trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(vm: JournalViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val state by vm.editor.collectAsState()
    val context = LocalContext.current
    var capturePath by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) vm.captureLocation()
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.onPickedPhoto(uri)
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) capturePath?.let { vm.onCapturedPhoto(it) }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (state.id == null) "New spot" else "Edit spot") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Close") } },
            actions = {
                TextButton(onClick = { vm.save(onSaved) }, enabled = !state.saving) {
                    Text(if (state.saving) "Saving…" else "Save")
                }
            }
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(state.title, vm::updateTitle, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(state.note, vm::updateNote, label = { Text("Note") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { photoPicker.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Gallery")
                }
                Button(onClick = {
                    val (file, uri) = vm.createCaptureUri()
                    capturePath = file.absolutePath
                    takePicture.launch(uri)
                }) {
                    Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(6.dp)); Text("Camera")
                }
            }
            if (state.photoPath != null) {
                AsyncImage(
                    model = File(state.photoPath!!),
                    contentDescription = "Selected photo",
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (vm.hasLocationPermission()) vm.captureLocation()
                    else permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }) { Icon(Icons.Default.MyLocation, contentDescription = "Capture location") }
                if (state.locating) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(state.placeLabel ?: state.latitude?.let { "${"%.4f".format(it)}, ${"%.4f".format(state.longitude)}" } ?: "No location yet")
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
