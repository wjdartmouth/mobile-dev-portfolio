package com.jianwang.portfolio.spotjournal.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jianwang.portfolio.spotjournal.SpotJournalApp
import com.jianwang.portfolio.spotjournal.data.JournalEntry
import com.jianwang.portfolio.spotjournal.data.LocationHelper
import com.jianwang.portfolio.spotjournal.data.PhotoStore
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditorState(
    val id: String? = null,
    val title: String = "",
    val note: String = "",
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeLabel: String? = null,
    val locating: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SpotJournalApp).database.journal()
    private val photos = PhotoStore(app)
    private val location = LocationHelper(app)

    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query.asStateFlow()

    val entries = query.flatMapLatest { q ->
        if (q.isBlank()) dao.observeAll() else dao.search(q.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editor = MutableStateFlow(EditorState())
    val editor: StateFlow<EditorState> = _editor.asStateFlow()

    fun setQuery(q: String) { query.value = q }

    fun startNew() {
        _editor.value = EditorState()
        captureLocation()
    }

    fun edit(entry: JournalEntry) {
        _editor.value = EditorState(
            id = entry.id,
            title = entry.title,
            note = entry.note,
            photoPath = entry.photoPath,
            latitude = entry.latitude,
            longitude = entry.longitude,
            placeLabel = entry.placeLabel
        )
    }

    fun updateTitle(v: String) { _editor.value = _editor.value.copy(title = v) }
    fun updateNote(v: String) { _editor.value = _editor.value.copy(note = v) }

    fun onPickedPhoto(uri: Uri) {
        viewModelScope.launch {
            val path = photos.persistFromUri(uri)
            _editor.value = _editor.value.copy(photoPath = path)
        }
    }

    fun onCapturedPhoto(filePath: String) {
        _editor.value = _editor.value.copy(photoPath = filePath)
    }

    fun createCaptureUri(): Pair<java.io.File, Uri> = photos.createCaptureUri()

    fun captureLocation() {
        viewModelScope.launch {
            _editor.value = _editor.value.copy(locating = true, error = null)
            val loc = location.current()
            _editor.value = if (loc != null) {
                _editor.value.copy(
                    locating = false,
                    latitude = loc.lat,
                    longitude = loc.lng,
                    placeLabel = loc.label
                )
            } else {
                _editor.value.copy(locating = false, error = "Location unavailable. Grant permission or try again.")
            }
        }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val e = _editor.value
            if (e.title.isBlank() && e.note.isBlank() && e.photoPath == null) {
                _editor.value = e.copy(error = "Add a title, note, or photo.")
                return@launch
            }
            _editor.value = e.copy(saving = true)
            val now = System.currentTimeMillis()
            val id = e.id ?: UUID.randomUUID().toString()
            val existing = e.id?.let { dao.get(it) }
            dao.upsert(
                JournalEntry(
                    id = id,
                    title = e.title.ifBlank { "Untitled spot" },
                    note = e.note,
                    photoPath = e.photoPath,
                    latitude = e.latitude,
                    longitude = e.longitude,
                    placeLabel = e.placeLabel,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
            _editor.value = e.copy(saving = false)
            onDone()
        }
    }

    fun delete(entry: JournalEntry) {
        viewModelScope.launch {
            photos.delete(entry.photoPath)
            dao.delete(entry)
        }
    }

    fun hasLocationPermission(): Boolean = location.hasPermission()
}
