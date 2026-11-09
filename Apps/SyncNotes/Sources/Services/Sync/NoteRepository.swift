import Foundation
import SwiftData

/// Orchestrates local SwiftData notes with an optional CloudKit (or local) sync backend.
/// Conflict policy: last-writer-wins by `revision`, then `updatedAt`.
@MainActor
@Observable
final class NoteRepository {
    private(set) var accountStatus: AccountSyncStatus = .checking
    private(set) var activeMode: SyncMode = .localOnly
    private(set) var lastSyncAt: Date?
    private(set) var syncMessage: String?

    private var syncService: any NoteSyncing
    private let cloudService: CloudKitNoteSyncService
    private let localService: LocalNoteSyncService
    private var modelContext: ModelContext?

    init() {
        cloudService = CloudKitNoteSyncService()
        localService = LocalNoteSyncService()
        syncService = localService
    }

    func attach(context: ModelContext) {
        modelContext = context
    }

    func refreshAccountAndMode() async {
        accountStatus = .checking
        let status = await cloudService.accountStatus()
        accountStatus = status
        if status.isCloudUsable {
            syncService = cloudService
            activeMode = .cloudKit
            syncMessage = "Using CloudKit private database."
        } else {
            syncService = localService
            activeMode = .localOnly
            syncMessage = "CloudKit unavailable — working in local fallback mode."
        }
    }

    func forceLocalMode() {
        syncService = localService
        activeMode = .localOnly
        syncMessage = "Forced local fallback mode."
    }

    func save(_ note: Note) async {
        note.updatedAt = .now
        note.revision += 1
        if note.cloudRecordName == nil {
            note.cloudRecordName = note.id.uuidString
        }
        try? modelContext?.save()
        await push(note)
    }

    func softDelete(_ note: Note) async {
        note.isDeleted = true
        note.updatedAt = .now
        note.revision += 1
        try? modelContext?.save()
        await push(note)
        modelContext?.delete(note)
        try? modelContext?.save()
    }

    func syncNow() async {
        await refreshAccountAndMode()
        guard let modelContext else { return }

        // Push local notes
        let descriptor = FetchDescriptor<Note>()
        let locals = ((try? modelContext.fetch(descriptor)) ?? []).filter { !$0.isDeleted }
        for note in locals {
            await push(note)
        }

        // Pull remote
        do {
            let remote = try await syncService.fetchChanges(since: lastSyncAt)
            merge(remote, into: modelContext)
            lastSyncAt = .now
            syncMessage = "Synced \(remote.count) remote change(s)."
        } catch {
            syncMessage = "Sync failed: \(error.localizedDescription)"
        }
    }

    private func push(_ note: Note) async {
        guard let recordName = note.cloudRecordName ?? Optional(note.id.uuidString) else { return }
        note.cloudRecordName = recordName
        let payload = CloudNotePayload(
            id: note.id,
            title: note.title,
            body: note.body,
            createdAt: note.createdAt,
            updatedAt: note.updatedAt,
            isPinned: note.isPinned,
            revision: note.revision,
            isDeleted: note.isDeleted,
            recordName: recordName
        )
        do {
            try await syncService.push(payload)
        } catch {
            syncMessage = "Push deferred: \(error.localizedDescription)"
        }
    }

    private func merge(_ remote: [CloudNotePayload], into context: ModelContext) {
        for payload in remote {
            let targetID = payload.id
            var descriptor = FetchDescriptor<Note>(
                predicate: #Predicate { $0.id == targetID }
            )
            descriptor.fetchLimit = 1
            let existing = try? context.fetch(descriptor).first

            if let existing {
                if shouldPreferRemote(payload, over: existing) {
                    apply(payload, to: existing)
                    if payload.isDeleted {
                        context.delete(existing)
                    }
                }
            } else if !payload.isDeleted {
                let note = Note(title: payload.title, body: payload.body, createdAt: payload.createdAt, isPinned: payload.isPinned)
                note.id = payload.id
                note.updatedAt = payload.updatedAt
                note.revision = payload.revision
                note.cloudRecordName = payload.recordName
                note.isDeleted = payload.isDeleted
                context.insert(note)
            }
        }
        try? context.save()
    }

    private func shouldPreferRemote(_ remote: CloudNotePayload, over local: Note) -> Bool {
        if remote.revision != local.revision {
            return remote.revision > local.revision
        }
        return remote.updatedAt > local.updatedAt
    }

    private func apply(_ payload: CloudNotePayload, to note: Note) {
        note.title = payload.title
        note.body = payload.body
        note.updatedAt = payload.updatedAt
        note.isPinned = payload.isPinned
        note.revision = payload.revision
        note.cloudRecordName = payload.recordName
        note.isDeleted = payload.isDeleted
    }
}
