import Foundation
import SwiftData
import SwiftUI

@MainActor
@Observable
final class NotesListViewModel {
    var searchText = ""
    private(set) var notes: [Note] = []

    private var modelContext: ModelContext?

    func attach(context: ModelContext) {
        modelContext = context
        reload()
    }

    func reload() {
        guard let modelContext else { return }
        var descriptor = FetchDescriptor<Note>(
            predicate: #Predicate { $0.isDeleted == false },
            sortBy: [
                SortDescriptor(\.isPinned, order: .reverse),
                SortDescriptor(\.updatedAt, order: .reverse)
            ]
        )
        let all = (try? modelContext.fetch(descriptor)) ?? []
        let q = searchText.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if q.isEmpty {
            notes = all
        } else {
            notes = all.filter {
                $0.title.lowercased().contains(q) || $0.body.lowercased().contains(q)
            }
        }
    }

    func createNote() -> Note {
        let note = Note(title: "", body: "")
        modelContext?.insert(note)
        try? modelContext?.save()
        reload()
        return note
    }
}
