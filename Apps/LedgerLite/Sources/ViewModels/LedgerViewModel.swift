import Foundation
import SwiftData
import SwiftUI

enum ExpenseFilter: String, CaseIterable, Identifiable {
    case all
    case thisMonth
    case last7Days

    var id: String { rawValue }

    var label: String {
        switch self {
        case .all: return "All"
        case .thisMonth: return "This month"
        case .last7Days: return "Last 7 days"
        }
    }
}

struct CategoryTotal: Identifiable {
    var id: String { name }
    var name: String
    var symbol: String
    var colorHex: String
    var total: Double
}

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

        let cal = Calendar.current
        let now = Date()
        let filteredByDate: [Expense] = all.filter { expense in
            switch filter {
            case .all: return true
            case .thisMonth:
                return cal.isDate(expense.date, equalTo: now, toGranularity: .month)
            case .last7Days:
                guard let start = cal.date(byAdding: .day, value: -7, to: now) else { return true }
                return expense.date >= start
            }
        }

        let filteredByCategory: [Expense]
        if let selectedCategoryID {
            filteredByCategory = filteredByDate.filter { $0.category?.id == selectedCategoryID }
        } else {
            filteredByCategory = filteredByDate
        }

        let q = searchText.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if q.isEmpty {
            expenses = filteredByCategory
        } else {
            expenses = filteredByCategory.filter {
                $0.title.lowercased().contains(q)
                    || $0.note.lowercased().contains(q)
                    || ($0.category?.name.lowercased().contains(q) ?? false)
            }
        }

        totalAmount = expenses.reduce(0) { $0 + $1.amount }

        var buckets: [String: CategoryTotal] = [:]
        for expense in expenses {
            let name = expense.category?.name ?? "Uncategorized"
            let symbol = expense.category?.symbol ?? "questionmark.circle"
            let hex = expense.category?.colorHex ?? "#95A5A6"
            var bucket = buckets[name] ?? CategoryTotal(name: name, symbol: symbol, colorHex: hex, total: 0)
            bucket.total += expense.amount
            buckets[name] = bucket
        }
        categoryTotals = buckets.values.sorted { $0.total > $1.total }
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
