import Foundation

extension Expense {
    func ledgerEntry() -> LedgerEntry {
        LedgerEntry(
            id: id.uuidString,
            title: title,
            amount: amount,
            date: date,
            note: note,
            categoryID: category?.id.uuidString,
            categoryName: category?.name,
            categorySymbol: category?.symbol,
            categoryColorHex: category?.colorHex
        )
    }
}
