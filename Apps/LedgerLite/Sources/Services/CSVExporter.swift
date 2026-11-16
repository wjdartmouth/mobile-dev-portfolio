import Foundation

enum CSVExporter {
    static func export(expenses: [Expense]) -> URL? {
        let dateFormatter = ISO8601DateFormatter()
        dateFormatter.formatOptions = [.withFullDate]
        let csv = LedgerCSV.render(expenses.map { $0.ledgerEntry() }) { date in
            dateFormatter.string(from: date)
        }
        let url = FileManager.default.temporaryDirectory
            .appendingPathComponent("ledger-lite-\(Int(Date().timeIntervalSince1970)).csv")
        do {
            try csv.data(using: .utf8)?.write(to: url)
            return url
        } catch {
            return nil
        }
    }
}
