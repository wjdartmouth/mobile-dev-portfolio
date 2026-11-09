package com.jianwang.portfolio.syncnotes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jianwang.portfolio.syncnotes.sync.SyncMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("sync_notes_settings")

class SettingsStore(private val context: Context) {
    private val modeKey = stringPreferencesKey("sync_mode")

    val syncMode: Flow<SyncMode> = context.dataStore.data.map {
        runCatching { SyncMode.valueOf(it[modeKey] ?: SyncMode.LOCAL_ONLY.name) }.getOrDefault(SyncMode.LOCAL_ONLY)
    }

    suspend fun setSyncMode(mode: SyncMode) {
        context.dataStore.edit { it[modeKey] = mode.name }
    }
}
