import Foundation

enum CSVExporter {
    static func export(expenses: [Expense]) -> URL? {
        var lines = ["id,date,title,amount,category,note"]
        let dateFormatter = ISO8601DateFormatter()
        dateFormatter.formatOptions = [.withFullDate]

        for expense in expenses.sorted(by: { $0.date > $1.date }) {
            let cols: [String] = [
                expense.id.uuidString,
                dateFormatter.string(from: expense.date),
                escape(expense.title),
                String(format: "%.2f", expense.amount),
                escape(expense.category?.name ?? "Uncategorized"),
                escape(expense.note)
            ]
            lines.append(cols.joined(separator: ","))
        }

        let csv = lines.joined(separator: "\n")
        let url = FileManager.default.temporaryDirectory
            .appendingPathComponent("ledger-lite-\(Int(Date().timeIntervalSince1970)).csv")
        do {
            try csv.data(using: .utf8)?.write(to: url)
            return url
        } catch {
            return nil
        }
    }

    private static func escape(_ value: String) -> String {
        if value.contains(",") || value.contains("\"") || value.contains("\n") {
            return "\"\(value.replacingOccurrences(of: "\"", with: "\"\""))\""
        }
        return value
    }
}
