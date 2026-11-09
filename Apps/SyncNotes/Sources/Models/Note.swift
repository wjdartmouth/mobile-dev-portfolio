import Foundation
import SwiftData

@Model
final class Note {
    @Attribute(.unique) var id: UUID
    var title: String
    var body: String
    var createdAt: Date
    var updatedAt: Date
    var isPinned: Bool
    /// CloudKit record name once mirrored; nil while local-only.
    var cloudRecordName: String?
    /// Monotonic revision used for last-writer-wins conflict policy.
    var revision: Int
    var isDeleted: Bool

    init(
        title: String = "",
        body: String = "",
        createdAt: Date = .now,
        isPinned: Bool = false
    ) {
        self.id = UUID()
        self.title = title
        self.body = body
        self.createdAt = createdAt
        self.updatedAt = createdAt
        self.isPinned = isPinned
        self.cloudRecordName = nil
        self.revision = 1
        self.isDeleted = false
    }

    var preview: String {
        let trimmed = body.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty { return "No additional text" }
        return trimmed
    }
}
