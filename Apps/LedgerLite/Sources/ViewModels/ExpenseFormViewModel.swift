import Foundation
import SwiftData

@MainActor
@Observable
final class ExpenseFormViewModel {
    var title = ""
    var amountText = ""
    var date = Date()
    var note = ""
    var selectedCategory: ExpenseCategory?
    var validationMessage: String?

    private var existing: Expense?

    var isEditing: Bool { existing != nil }

    init(expense: Expense? = nil, defaultCategory: ExpenseCategory? = nil) {
        existing = expense
        if let expense {
            title = expense.title
            amountText = String(format: "%.2f", expense.amount)
            date = expense.date
            note = expense.note
            selectedCategory = expense.category
        } else {
            selectedCategory = defaultCategory
        }
    }

    func validate() -> Double? {
        validationMessage = nil
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            validationMessage = "Title is required."
            return nil
        }
        let normalized = amountText.replacingOccurrences(of: ",", with: ".")
        guard let amount = Double(normalized), amount > 0, amount < 1_000_000_000 else {
            validationMessage = "Enter a valid amount greater than 0."
            return nil
        }
        return amount
    }

    func save(into context: ModelContext) -> Bool {
        guard let amount = validate() else { return false }
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedNote = note.trimmingCharacters(in: .whitespacesAndNewlines)

        if let existing {
            existing.title = trimmedTitle
            existing.amount = amount
            existing.date = date
            existing.note = trimmedNote
            existing.category = selectedCategory
        } else {
            context.insert(
                Expense(
                    title: trimmedTitle,
                    amount: amount,
                    date: date,
                    note: trimmedNote,
                    category: selectedCategory
                )
            )
        }
        do {
            try context.save()
            return true
        } catch {
            validationMessage = "Couldn't save: \(error.localizedDescription)"
            return false
        }
    }
}
