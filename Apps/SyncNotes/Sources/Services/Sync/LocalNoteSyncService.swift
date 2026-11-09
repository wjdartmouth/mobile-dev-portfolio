import Foundation

/// Local fallback used when CloudKit is unavailable (no Apple ID / signing / container).
/// Keeps the sync API identical so the UI and merge logic stay exercise-ready.
actor LocalNoteSyncService: NoteSyncing {
    nonisolated var mode: SyncMode { .localOnly }

    private var store: [String: CloudNotePayload] = [:]

    func accountStatus() async -> AccountSyncStatus {
        .noAccount
    }

    func push(_ note: CloudNotePayload) async throws {
        store[note.recordName] = note
    }

    func fetchChanges(since cursor: Date?) async throws -> [CloudNotePayload] {
        let values = Array(store.values)
        guard let cursor else { return values }
        return values.filter { $0.updatedAt > cursor }
    }

    func delete(recordName: String) async throws {
        store[recordName] = nil
    }
}
