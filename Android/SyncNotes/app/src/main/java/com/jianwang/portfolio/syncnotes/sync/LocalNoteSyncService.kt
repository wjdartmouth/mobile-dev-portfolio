package com.jianwang.portfolio.syncnotes.sync

class LocalNoteSyncService : NoteSyncing {
    override val mode: SyncMode = SyncMode.LOCAL_ONLY
    override suspend fun accountStatus(): AccountSyncStatus = AccountSyncStatus.DISABLED
    override suspend fun push(note: RemoteNote) { /* no-op local */ }
    override suspend fun fetchChanges(since: Long?): List<RemoteNote> = emptyList()
    override suspend fun delete(id: String) { /* no-op */ }
}
