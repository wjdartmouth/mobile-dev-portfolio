package com.jianwang.portfolio.syncnotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jianwang.portfolio.syncnotes.SyncNotesApp
import com.jianwang.portfolio.syncnotes.data.NoteEntity
import com.jianwang.portfolio.syncnotes.sync.AccountSyncStatus
import com.jianwang.portfolio.syncnotes.sync.SyncMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditorUi(
    val id: String? = null,
    val title: String = "",
    val body: String = "",
    val pinned: Boolean = false
)

class NotesViewModel(app: Application) : AndroidViewModel(app) {
    private val syncApp = app as SyncNotesApp
    private val repo = syncApp.repository
    private val settings = syncApp.settings

    val notes = repo.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mode = MutableStateFlow(SyncMode.LOCAL_ONLY)
    val mode: StateFlow<SyncMode> = _mode.asStateFlow()

    private val _status = MutableStateFlow(AccountSyncStatus.DISABLED)
    val status: StateFlow<AccountSyncStatus> = _status.asStateFlow()

    private val _editor = MutableStateFlow(EditorUi())
    val editor: StateFlow<EditorUi> = _editor.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            settings.syncMode.collect { m ->
                _mode.value = m
                repo.switchMode(m)
                refreshStatus()
            }
        }
    }

    fun setMode(mode: SyncMode) {
        viewModelScope.launch {
            settings.setSyncMode(mode)
            if (mode == SyncMode.FIRESTORE && !com.jianwang.portfolio.syncnotes.BuildConfig.FIREBASE_SYNC_ENABLED) {
                _message.value = "Firebase is not enabled in this build. App stays in local-only mode. See README."
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch { _status.value = repo.accountStatus() }
    }

    fun syncNow() {
        viewModelScope.launch {
            _status.value = AccountSyncStatus.CHECKING
            _status.value = repo.syncNow()
            _message.value = "Sync finished (${_status.value.title})"
        }
    }

    fun startNew() { _editor.value = EditorUi() }
    fun edit(note: NoteEntity) {
        _editor.value = EditorUi(note.id, note.title, note.body, note.isPinned)
    }
    fun updateTitle(v: String) { _editor.value = _editor.value.copy(title = v) }
    fun updateBody(v: String) { _editor.value = _editor.value.copy(body = v) }
    fun togglePinEditor() { _editor.value = _editor.value.copy(pinned = !_editor.value.pinned) }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val e = _editor.value
            repo.upsert(e.title, e.body, e.id, e.pinned)
            onDone()
        }
    }

    fun delete(note: NoteEntity) { viewModelScope.launch { repo.delete(note) } }
    fun togglePin(note: NoteEntity) { viewModelScope.launch { repo.togglePin(note) } }
    fun clearMessage() { _message.value = null }
}
