package com.jianwang.portfolio.syncnotes.sync

enum class SyncMode { LOCAL_ONLY, FIRESTORE }

enum class AccountSyncStatus {
    CHECKING, AVAILABLE, NO_ACCOUNT, DISABLED, FAILED;

    val title: String
        get() = when (this) {
            CHECKING -> "Checking…"
            AVAILABLE -> "Cloud ready"
            NO_ACCOUNT -> "Sign-in required"
            DISABLED -> "Local only"
            FAILED -> "Sync unavailable"
        }
}

data class RemoteNote(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val revision: Int,
    val isDeleted: Boolean
)

interface NoteSyncing {
    val mode: SyncMode
    suspend fun accountStatus(): AccountSyncStatus
    suspend fun push(note: RemoteNote)
    suspend fun fetchChanges(since: Long?): List<RemoteNote>
    suspend fun delete(id: String)
}
