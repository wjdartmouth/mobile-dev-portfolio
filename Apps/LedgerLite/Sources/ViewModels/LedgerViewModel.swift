import Foundation
import SwiftData
import SwiftUI

@MainActor
@Observable
final class LedgerViewModel {
    var searchText = ""
    var filter: ExpenseFilter = .thisMonth
    var selectedCategoryID: UUID?
    private(set) var expenses: [Expense] = []
    private(set) var categories: [ExpenseCategory] = []
    private(set) var categoryTotals: [CategoryTotal] = []
    private(set) var totalAmount: Double = 0

    private var modelContext: ModelContext?

    func attach(context: ModelContext) {
        modelContext = context
        SeedData.ensureCategories(in: context)
        reload()
    }

    func reload() {
        guard let modelContext else { return }
        categories = ((try? modelContext.fetch(FetchDescriptor<ExpenseCategory>(
            sortBy: [SortDescriptor(\.sortOrder)]
        ))) ?? [])

        let all = ((try? modelContext.fetch(FetchDescriptor<Expense>(
            sortBy: [SortDescriptor(\.date, order: .reverse)]
        ))) ?? [])

        let summary = LedgerAnalytics.summarize(
            entries: all.map { $0.ledgerEntry() },
            filter: filter,
            selectedCategoryID: selectedCategoryID?.uuidString,
            searchText: searchText,
            now: Date(),
            calendar: Calendar.current
        )
        let kept = Set(summary.entries.map(\.id))
        expenses = all.filter { kept.contains($0.id.uuidString) }
        totalAmount = summary.totalAmount
        categoryTotals = summary.categoryTotals
    }

    func delete(_ expense: Expense) {
        guard let modelContext else { return }
        modelContext.delete(expense)
        try? modelContext.save()
        reload()
    }

    func exportCSV() -> URL? {
        // Export the currently filtered list for transparency
        CSVExporter.export(expenses: expenses)
    }
}
