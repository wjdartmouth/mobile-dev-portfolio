import Foundation
import CloudKit

enum SyncMode: String, Codable {
    case localOnly
    case cloudKit
}

enum AccountSyncStatus: Equatable {
    case checking
    case available
    case noAccount
    case restricted
    case couldNotDetermine
    case temporarilyUnavailable
    case failed(String)

    var title: String {
        switch self {
        case .checking: return "Checking iCloud…"
        case .available: return "iCloud Ready"
        case .noAccount: return "No iCloud Account"
        case .restricted: return "iCloud Restricted"
        case .couldNotDetermine: return "Status Unknown"
        case .temporarilyUnavailable: return "Temporarily Unavailable"
        case .failed(let message): return message
        }
    }

    var systemImage: String {
        switch self {
        case .available: return "checkmark.icloud.fill"
        case .checking: return "icloud"
        default: return "xmark.icloud.fill"
        }
    }

    var isCloudUsable: Bool {
        if case .available = self { return true }
        return false
    }
}

struct CloudNotePayload: Identifiable, Hashable, Sendable {
    var id: UUID
    var title: String
    var body: String
    var createdAt: Date
    var updatedAt: Date
    var isPinned: Bool
    var revision: Int
    var isDeleted: Bool
    var recordName: String
}

protocol NoteSyncing: AnyObject, Sendable {
    var mode: SyncMode { get }
    func accountStatus() async -> AccountSyncStatus
    func push(_ note: CloudNotePayload) async throws
    func fetchChanges(since cursor: Date?) async throws -> [CloudNotePayload]
    func delete(recordName: String) async throws
}
