package com.jianwang.portfolio.syncnotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jianwang.portfolio.syncnotes.data.NoteEntity
import com.jianwang.portfolio.syncnotes.sync.SyncMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesRoot(vm: NotesViewModel = viewModel()) {
    val nav = rememberNavController()
    val notes by vm.notes.collectAsState()
    val mode by vm.mode.collectAsState()
    val status by vm.status.collectAsState()
    val message by vm.message.collectAsState()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snack.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snack) }) { padding ->
        NavHost(nav, startDestination = "list", modifier = Modifier.padding(padding)) {
            composable("list") {
                ListScreen(
                    notes, mode, status.title,
                    onMode = vm::setMode,
                    onSync = vm::syncNow,
                    onAdd = { vm.startNew(); nav.navigate("edit") },
                    onOpen = { vm.edit(it); nav.navigate("edit") },
                    onDelete = vm::delete,
                    onPin = vm::togglePin
                )
            }
            composable("edit") {
                EditorScreen(vm, onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListScreen(
    notes: List<NoteEntity>,
    mode: SyncMode,
    statusTitle: String,
    onMode: (SyncMode) -> Unit,
    onSync: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onPin: (NoteEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Notes") },
                actions = {
                    IconButton(onClick = onSync) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync now")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "New note")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (mode == SyncMode.LOCAL_ONLY) Icons.Default.CloudOff else Icons.Default.Cloud,
                            contentDescription = null
                        )
                        Text(statusTitle, fontWeight = FontWeight.Medium,
                            modifier = Modifier.semantics { contentDescription = "Sync status $statusTitle" })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = mode == SyncMode.LOCAL_ONLY,
                            onClick = { onMode(SyncMode.LOCAL_ONLY) },
                            label = { Text("Local only") }
                        )
                        FilterChip(
                            selected = mode == SyncMode.FIRESTORE,
                            onClick = { onMode(SyncMode.FIRESTORE) },
                            label = { Text("Firestore") }
                        )
                    }
                    Text(
                        "Room always persists locally. Firestore sync is optional and disabled until google-services.json is added.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(note, onOpen, onDelete, onPin)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onOpen: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onPin: (NoteEntity) -> Unit
) {
    val fmt = remember { SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth().clickable { onOpen(note) }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(note.title, fontWeight = FontWeight.SemiBold)
                Text(note.body.take(80), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Text("rev ${note.revision} · ${fmt.format(Date(note.updatedAt))}", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = { onPin(note) }) {
                Icon(Icons.Default.PushPin, contentDescription = if (note.isPinned) "Unpin" else "Pin",
                    tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDelete(note) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${note.title}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(vm: NotesViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val editor by vm.editor.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopAppBar(
            title = { Text(if (editor.id == null) "New note" else "Edit note") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Close") } },
            actions = {
                TextButton(onClick = vm::togglePinEditor) { Text(if (editor.pinned) "Unpin" else "Pin") }
                TextButton(onClick = { vm.save(onSaved) }) { Text("Save") }
            }
        )
        OutlinedTextField(editor.title, vm::updateTitle, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(editor.body, vm::updateBody, label = { Text("Body") }, modifier = Modifier.fillMaxWidth().height(240.dp))
    }
}
