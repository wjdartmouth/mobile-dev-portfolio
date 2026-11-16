import Foundation

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

struct CategoryTotal: Identifiable, Equatable {
    var id: String { name }
    var name: String
    var symbol: String
    var colorHex: String
    var total: Double
}

struct LedgerEntry: Equatable {
    var id: String
    var title: String
    var amount: Double
    var date: Date
    var note: String
    var categoryID: String?
    var categoryName: String?
    var categorySymbol: String?
    var categoryColorHex: String?
}

struct LedgerSummary: Equatable {
    var entries: [LedgerEntry]
    var totalAmount: Double
    var categoryTotals: [CategoryTotal]
}

enum LedgerAnalytics {
    static func summarize(
        entries: [LedgerEntry],
        filter: ExpenseFilter,
        selectedCategoryID: String?,
        searchText: String,
        now: Date,
        calendar: Calendar = .current
    ) -> LedgerSummary {
        let filtered = applyFilters(
            entries: entries,
            filter: filter,
            selectedCategoryID: selectedCategoryID,
            searchText: searchText,
            now: now,
            calendar: calendar
        )
        return LedgerSummary(
            entries: filtered,
            totalAmount: filtered.reduce(0) { $0 + $1.amount },
            categoryTotals: categoryTotals(for: filtered)
        )
    }

    static func applyFilters(
        entries: [LedgerEntry],
        filter: ExpenseFilter,
        selectedCategoryID: String?,
        searchText: String,
        now: Date,
        calendar: Calendar = .current
    ) -> [LedgerEntry] {
        let filteredByDate = entries.filter { entry in
            switch filter {
            case .all:
                return true
            case .thisMonth:
                return calendar.isDate(entry.date, equalTo: now, toGranularity: .month)
            case .last7Days:
                guard let start = calendar.date(byAdding: .day, value: -7, to: now) else { return true }
                return entry.date >= start
            }
        }

        let filteredByCategory: [LedgerEntry]
        if let selectedCategoryID {
            filteredByCategory = filteredByDate.filter { $0.categoryID == selectedCategoryID }
        } else {
            filteredByCategory = filteredByDate
        }

        let query = searchText.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if query.isEmpty {
            return filteredByCategory
        }
        return filteredByCategory.filter {
            $0.title.lowercased().contains(query)
                || $0.note.lowercased().contains(query)
                || ($0.categoryName?.lowercased().contains(query) ?? false)
        }
    }

    static func categoryTotals(for entries: [LedgerEntry]) -> [CategoryTotal] {
        var buckets: [String: CategoryTotal] = [:]
        for entry in entries {
            let name = entry.categoryName ?? "Uncategorized"
            let symbol = entry.categorySymbol ?? "questionmark.circle"
            let hex = entry.categoryColorHex ?? "#95A5A6"
            var bucket = buckets[name] ?? CategoryTotal(name: name, symbol: symbol, colorHex: hex, total: 0)
            bucket.total += entry.amount
            buckets[name] = bucket
        }
        return buckets.values.sorted { $0.total > $1.total }
    }
}

enum LedgerCSV {
    static func render(_ entries: [LedgerEntry], formatDate: (Date) -> String) -> String {
        var lines = ["id,date,title,amount,category,note"]
        for entry in entries.sorted(by: { $0.date > $1.date }) {
            let cols = [
                entry.id,
                formatDate(entry.date),
                escape(entry.title),
                String(format: "%.2f", entry.amount),
                escape(entry.categoryName ?? "Uncategorized"),
                escape(entry.note)
            ]
            lines.append(cols.joined(separator: ","))
        }
        return lines.joined(separator: "\n")
    }

    static func escape(_ value: String) -> String {
        if value.contains(",") || value.contains("\"") || value.contains("\n") {
            return "\"\(value.replacingOccurrences(of: "\"", with: "\"\""))\""
        }
        return value
    }
}
