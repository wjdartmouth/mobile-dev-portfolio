package com.jianwang.portfolio.syncnotes.data

import com.jianwang.portfolio.syncnotes.sync.AccountSyncStatus
import com.jianwang.portfolio.syncnotes.sync.NoteSyncing
import com.jianwang.portfolio.syncnotes.sync.RemoteNote
import com.jianwang.portfolio.syncnotes.sync.SyncMode
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val dao: NoteDao,
    private val syncFactory: (SyncMode) -> NoteSyncing
) {
    fun observe(): Flow<List<NoteEntity>> = dao.observeActive()

    private var sync: NoteSyncing = syncFactory(SyncMode.LOCAL_ONLY)
    private var lastSyncAt: Long? = null

    fun switchMode(mode: SyncMode) {
        sync = syncFactory(mode)
    }

    suspend fun accountStatus(): AccountSyncStatus = sync.accountStatus()

    suspend fun upsert(title: String, body: String, existingId: String?, pinned: Boolean): NoteEntity {
        val now = System.currentTimeMillis()
        val existing = existingId?.let { dao.get(it) }
        val note = NoteEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = title.ifBlank { "Untitled" },
            body = body,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            isPinned = pinned,
            revision = (existing?.revision ?: 0) + 1,
            isDeleted = false,
            remoteId = existing?.remoteId ?: existing?.id
        )
        dao.upsert(note)
        sync.push(note.toRemote())
        return note
    }

    suspend fun delete(note: NoteEntity) {
        val deleted = note.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis(),
            revision = note.revision + 1
        )
        dao.upsert(deleted)
        sync.delete(note.id)
    }

    suspend fun togglePin(note: NoteEntity) {
        upsert(note.title, note.body, note.id, !note.isPinned)
    }

    /**
     * Pull remote changes and merge with last-write-wins by revision (then updatedAt).
     */
    suspend fun syncNow(): AccountSyncStatus {
        val status = sync.accountStatus()
        if (status != AccountSyncStatus.AVAILABLE && sync.mode == SyncMode.FIRESTORE) return status
        val remote = sync.fetchChanges(lastSyncAt)
        remote.forEach { r ->
            val local = dao.get(r.id)
            if (local == null || r.revision > local.revision ||
                (r.revision == local.revision && r.updatedAt > local.updatedAt)
            ) {
                dao.upsert(
                    NoteEntity(
                        id = r.id,
                        title = r.title,
                        body = r.body,
                        createdAt = r.createdAt,
                        updatedAt = r.updatedAt,
                        isPinned = r.isPinned,
                        revision = r.revision,
                        isDeleted = r.isDeleted,
                        remoteId = r.id
                    )
                )
            }
        }
        // Push local winners
        dao.all().forEach { local ->
            sync.push(local.toRemote())
        }
        lastSyncAt = System.currentTimeMillis()
        return status
    }

    private fun NoteEntity.toRemote() = RemoteNote(
        id = id, title = title, body = body, createdAt = createdAt,
        updatedAt = updatedAt, isPinned = isPinned, revision = revision, isDeleted = isDeleted
    )
}
