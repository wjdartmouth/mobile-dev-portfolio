import Foundation
import SwiftData
import SwiftUI

enum JournalSort: String, CaseIterable, Identifiable {
    case newest
    case oldest
    case title

    var id: String { rawValue }

    var label: String {
        switch self {
        case .newest: return "Newest"
        case .oldest: return "Oldest"
        case .title: return "Title"
        }
    }
}

@MainActor
@Observable
final class JournalListViewModel {
    var searchText = ""
    var sort: JournalSort = .newest
    private(set) var entries: [JournalEntry] = []

    private var modelContext: ModelContext?

    func attach(context: ModelContext) {
        modelContext = context
        reload()
    }

    func reload() {
        guard let modelContext else { return }
        var descriptor = FetchDescriptor<JournalEntry>()
        switch sort {
        case .newest:
            descriptor.sortBy = [SortDescriptor(\.createdAt, order: .reverse)]
        case .oldest:
            descriptor.sortBy = [SortDescriptor(\.createdAt, order: .forward)]
        case .title:
            descriptor.sortBy = [SortDescriptor(\.title, order: .forward)]
        }
        let all = (try? modelContext.fetch(descriptor)) ?? []
        let q = searchText.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if q.isEmpty {
            entries = all
        } else {
            entries = all.filter {
                $0.title.lowercased().contains(q)
                    || $0.note.lowercased().contains(q)
                    || ($0.placeName?.lowercased().contains(q) ?? false)
            }
        }
    }

    func delete(_ entry: JournalEntry) {
        guard let modelContext else { return }
        modelContext.delete(entry)
        try? modelContext.save()
        reload()
    }
}
