import Foundation
import SwiftData
import SwiftUI

struct DayStat: Identifiable {
    let id: Date
    let date: Date
    let focusMinutes: Int
    let completedSessions: Int
}

@MainActor
@Observable
final class HistoryViewModel {
    private(set) var sessions: [FocusSession] = []
    private(set) var dayStats: [DayStat] = []
    private(set) var totalFocusMinutes: Int = 0
    private(set) var streakDays: Int = 0

    private var modelContext: ModelContext?

    func attach(context: ModelContext) {
        modelContext = context
        reload()
    }

    func reload() {
        guard let modelContext else { return }
        let descriptor = FetchDescriptor<FocusSession>(
            sortBy: [SortDescriptor(\.completedAt, order: .reverse)]
        )
        sessions = (try? modelContext.fetch(descriptor)) ?? []
        rebuildStats()
    }

    func delete(_ session: FocusSession) {
        guard let modelContext else { return }
        modelContext.delete(session)
        try? modelContext.save()
        reload()
    }

    func clearAll() {
        guard let modelContext else { return }
        for s in sessions { modelContext.delete(s) }
        try? modelContext.save()
        reload()
    }

    private func rebuildStats() {
        let cal = Calendar.current
        var buckets: [Date: (minutes: Int, count: Int)] = [:]

        for session in sessions where session.phase == .focus && session.wasCompleted {
            let day = cal.startOfDay(for: session.completedAt)
            var bucket = buckets[day] ?? (0, 0)
            bucket.minutes += max(1, session.actualSeconds / 60)
            bucket.count += 1
            buckets[day] = bucket
        }

        dayStats = buckets.keys.sorted().map { day in
            let b = buckets[day]!
            return DayStat(id: day, date: day, focusMinutes: b.minutes, completedSessions: b.count)
        }

        totalFocusMinutes = dayStats.reduce(0) { $0 + $1.focusMinutes }

        // Streak: consecutive days ending today or yesterday with ≥1 completed focus
        var streak = 0
        var cursor = cal.startOfDay(for: .now)
        if buckets[cursor] == nil {
            cursor = cal.date(byAdding: .day, value: -1, to: cursor) ?? cursor
        }
        while buckets[cursor] != nil {
            streak += 1
            guard let prev = cal.date(byAdding: .day, value: -1, to: cursor) else { break }
            cursor = prev
        }
        streakDays = streak
    }
}
