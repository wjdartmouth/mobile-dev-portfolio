package com.jianwang.portfolio.syncnotes

import android.app.Application
import com.jianwang.portfolio.syncnotes.data.AppDatabase
import com.jianwang.portfolio.syncnotes.data.NoteRepository
import com.jianwang.portfolio.syncnotes.data.SettingsStore
import com.jianwang.portfolio.syncnotes.sync.FirestoreNoteSyncService
import com.jianwang.portfolio.syncnotes.sync.LocalNoteSyncService
import com.jianwang.portfolio.syncnotes.sync.NoteSyncing
import com.jianwang.portfolio.syncnotes.sync.SyncMode

class SyncNotesApp : Application() {
    lateinit var database: AppDatabase private set
    lateinit var settings: SettingsStore private set
    lateinit var repository: NoteRepository private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        settings = SettingsStore(this)
        repository = NoteRepository(database.notes(), ::createSync)
    }

    private fun createSync(mode: SyncMode): NoteSyncing = when (mode) {
        SyncMode.LOCAL_ONLY -> LocalNoteSyncService()
        SyncMode.FIRESTORE -> {
            if (BuildConfig.FIREBASE_SYNC_ENABLED) FirestoreNoteSyncService()
            else LocalNoteSyncService()
        }
    }
}
