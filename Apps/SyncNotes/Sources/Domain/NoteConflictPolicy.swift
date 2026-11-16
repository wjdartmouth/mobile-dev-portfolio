import Foundation

struct NoteVersion: Equatable {
    var revision: Int
    var updatedAt: Date
    var isDeleted: Bool
}

enum NoteConflictPolicy {
    /// Last-writer-wins. A higher revision wins. When revisions match, the later
    /// `updatedAt` wins. Equal timestamps keep the local note.
    ///
    /// `isDeleted` does not override that order. A deleted remote is applied
    /// only when this returns true; the repository then removes the local note.
    static func shouldPreferRemote(_ remote: NoteVersion, over local: NoteVersion) -> Bool {
        if remote.revision != local.revision {
            return remote.revision > local.revision
        }
        return remote.updatedAt > local.updatedAt
    }
}
