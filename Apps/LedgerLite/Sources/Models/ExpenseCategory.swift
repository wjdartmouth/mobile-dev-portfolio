import Foundation
import SwiftData
import SwiftUI

@Model
final class ExpenseCategory {
    @Attribute(.unique) var id: UUID
    var name: String
    var symbol: String
    var colorHex: String
    var sortOrder: Int
    @Relationship(deleteRule: .nullify, inverse: \Expense.category)
    var expenses: [Expense]

    init(name: String, symbol: String, colorHex: String, sortOrder: Int = 0) {
        self.id = UUID()
        self.name = name
        self.symbol = symbol
        self.colorHex = colorHex
        self.sortOrder = sortOrder
        self.expenses = []
    }

    var color: Color { Color(hex: colorHex) }

    static let defaults: [(String, String, String)] = [
        ("Food", "fork.knife", "#FF6B6B"),
        ("Transport", "car.fill", "#4D96FF"),
        ("Shopping", "bag.fill", "#FFD93D"),
        ("Bills", "doc.text.fill", "#6BCB77"),
        ("Health", "heart.fill", "#FF8AAE"),
        ("Entertainment", "film.fill", "#9B59B6"),
        ("Other", "ellipsis.circle.fill", "#95A5A6")
    ]
}

@Model
final class Expense {
    @Attribute(.unique) var id: UUID
    var title: String
    var amount: Double
    var date: Date
    var note: String
    var createdAt: Date
    var category: ExpenseCategory?

    init(
        title: String,
        amount: Double,
        date: Date = .now,
        note: String = "",
        category: ExpenseCategory? = nil
    ) {
        self.id = UUID()
        self.title = title
        self.amount = amount
        self.date = date
        self.note = note
        self.createdAt = .now
        self.category = category
    }
}

extension Color {
    init(hex: String) {
        let cleaned = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: cleaned).scanHexInt64(&int)
        let r, g, b: UInt64
        switch cleaned.count {
        case 6:
            (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        default:
            (r, g, b) = (150, 150, 150)
        }
        self.init(.sRGB, red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255, opacity: 1)
    }
}
