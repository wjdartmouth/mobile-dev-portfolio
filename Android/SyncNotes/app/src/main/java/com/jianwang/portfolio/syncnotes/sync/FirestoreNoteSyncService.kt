package com.jianwang.portfolio.syncnotes.sync

/**
 * Optional Firebase Firestore implementation.
 *
 * The app ships with FIREBASE_SYNC_ENABLED=false and no google-services.json,
 * so this class never talks to the network unless you enable Firebase
 * (see README). When disabled it behaves like local-only.
 *
 * Enabling steps (documented in README):
 * 1. Add google-services.json
 * 2. Apply Google Services Gradle plugin
 * 3. Uncomment Firebase deps
 * 4. Set buildConfigField FIREBASE_SYNC_ENABLED=true
 * 5. Replace the stub body below with real Firestore calls
 */
class FirestoreNoteSyncService : NoteSyncing {
    override val mode: SyncMode = SyncMode.FIRESTORE

    override suspend fun accountStatus(): AccountSyncStatus {
        // When Firebase Auth is wired, return AVAILABLE / NO_ACCOUNT based on currentUser.
        return if (com.jianwang.portfolio.syncnotes.BuildConfig.FIREBASE_SYNC_ENABLED) {
            AccountSyncStatus.NO_ACCOUNT
        } else {
            AccountSyncStatus.DISABLED
        }
    }

    override suspend fun push(note: RemoteNote) {
        if (!com.jianwang.portfolio.syncnotes.BuildConfig.FIREBASE_SYNC_ENABLED) return
        // Firestore: collection("notes").document(note.id).set(mapOf(...))
        // Last-write-wins: only write if remote.revision < note.revision OR remote.updatedAt < note.updatedAt
    }

    override suspend fun fetchChanges(since: Long?): List<RemoteNote> {
        if (!com.jianwang.portfolio.syncnotes.BuildConfig.FIREBASE_SYNC_ENABLED) return emptyList()
        // Firestore query where updatedAt > since
        return emptyList()
    }

    override suspend fun delete(id: String) {
        if (!com.jianwang.portfolio.syncnotes.BuildConfig.FIREBASE_SYNC_ENABLED) return
        // Soft-delete: set isDeleted=true, bump revision
    }
}
