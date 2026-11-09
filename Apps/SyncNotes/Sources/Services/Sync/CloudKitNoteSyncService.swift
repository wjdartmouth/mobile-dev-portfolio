import Foundation
import CloudKit

/// Production CloudKit mirror. Requires an Apple Developer team, the
/// `iCloud.com.jianwang.portfolio.syncnotes` container, and a signed-in iCloud account.
actor CloudKitNoteSyncService: NoteSyncing {
    nonisolated var mode: SyncMode { .cloudKit }

    private let container: CKContainer
    private let database: CKDatabase
    private let recordType = "Note"

    init(containerIdentifier: String = "iCloud.com.jianwang.portfolio.syncnotes") {
        container = CKContainer(identifier: containerIdentifier)
        database = container.privateCloudDatabase
    }

    func accountStatus() async -> AccountSyncStatus {
        do {
            let status = try await container.accountStatus()
            switch status {
            case .available: return .available
            case .noAccount: return .noAccount
            case .restricted: return .restricted
            case .couldNotDetermine: return .couldNotDetermine
            case .temporarilyUnavailable: return .temporarilyUnavailable
            @unknown default: return .couldNotDetermine
            }
        } catch {
            return .failed(error.localizedDescription)
        }
    }

    func push(_ note: CloudNotePayload) async throws {
        let recordID = CKRecord.ID(recordName: note.recordName)
        let record: CKRecord
        if let existing = try? await database.record(for: recordID) {
            record = existing
        } else {
            record = CKRecord(recordType: recordType, recordID: recordID)
        }

        record["id"] = note.id.uuidString as CKRecordValue
        record["title"] = note.title as CKRecordValue
        record["body"] = note.body as CKRecordValue
        record["createdAt"] = note.createdAt as CKRecordValue
        record["updatedAt"] = note.updatedAt as CKRecordValue
        record["isPinned"] = (note.isPinned ? 1 : 0) as CKRecordValue
        record["revision"] = note.revision as CKRecordValue
        record["isDeleted"] = (note.isDeleted ? 1 : 0) as CKRecordValue

        _ = try await database.save(record)
    }

    func fetchChanges(since cursor: Date?) async throws -> [CloudNotePayload] {
        let predicate: NSPredicate
        if let cursor {
            predicate = NSPredicate(format: "updatedAt > %@", cursor as NSDate)
        } else {
            predicate = NSPredicate(value: true)
        }
        let query = CKQuery(recordType: recordType, predicate: predicate)
        query.sortDescriptors = [NSSortDescriptor(key: "updatedAt", ascending: false)]

        let (results, _) = try await database.records(matching: query)
        var payloads: [CloudNotePayload] = []
        for (_, result) in results {
            if let record = try? result.get(), let payload = Self.payload(from: record) {
                payloads.append(payload)
            }
        }
        return payloads
    }

    func delete(recordName: String) async throws {
        let id = CKRecord.ID(recordName: recordName)
        _ = try await database.deleteRecord(withID: id)
    }

    private static func payload(from record: CKRecord) -> CloudNotePayload? {
        guard
            let idString = record["id"] as? String,
            let id = UUID(uuidString: idString),
            let title = record["title"] as? String,
            let body = record["body"] as? String,
            let createdAt = record["createdAt"] as? Date,
            let updatedAt = record["updatedAt"] as? Date
        else { return nil }

        let isPinned = ((record["isPinned"] as? Int) ?? 0) == 1
        let revision = (record["revision"] as? Int) ?? 1
        let isDeleted = ((record["isDeleted"] as? Int) ?? 0) == 1

        return CloudNotePayload(
            id: id,
            title: title,
            body: body,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isPinned: isPinned,
            revision: revision,
            isDeleted: isDeleted,
            recordName: record.recordID.recordName
        )
    }
}
