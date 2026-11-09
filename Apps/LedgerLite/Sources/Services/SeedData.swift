import Foundation
import SwiftData

enum SeedData {
    static func ensureCategories(in context: ModelContext) {
        let descriptor = FetchDescriptor<ExpenseCategory>()
        let existing = (try? context.fetch(descriptor)) ?? []
        guard existing.isEmpty else { return }

        for (index, item) in ExpenseCategory.defaults.enumerated() {
            context.insert(ExpenseCategory(name: item.0, symbol: item.1, colorHex: item.2, sortOrder: index))
        }
        try? context.save()
    }
}
