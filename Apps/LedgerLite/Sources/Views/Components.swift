import SwiftUI
import Charts
import UIKit

struct ExpenseRow: View {
    let expense: Expense

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle().fill((expense.category?.color ?? .secondary).opacity(0.2))
                Image(systemName: expense.category?.symbol ?? "questionmark")
                    .foregroundStyle(expense.category?.color ?? .secondary)
            }
            .frame(width: 40, height: 40)

            VStack(alignment: .leading, spacing: 2) {
                Text(expense.title)
                    .font(.body.weight(.medium))
                    .foregroundStyle(.primary)
                Text(expense.date, format: .dateTime.month().day().year())
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Text(MoneyFormat.string(from: expense.amount))
                .font(.body.monospacedDigit().weight(.semibold))
                .foregroundStyle(.primary)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(expense.title), \(MoneyFormat.string(from: expense.amount)), \(expense.category?.name ?? "Uncategorized")")
    }
}

struct FilterChip: View {
    let title: String
    var selected: Bool
    var color: Color = .accentColor
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption.weight(.semibold))
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(
                    Capsule().fill(selected ? color.opacity(0.25) : Color.secondary.opacity(0.12))
                )
                .overlay(
                    Capsule().stroke(selected ? color : .clear, lineWidth: 1.5)
                )
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}

struct CategoryChartView: View {
    let totals: [CategoryTotal]

    var body: some View {
        Chart(totals) { item in
            SectorMark(
                angle: .value("Total", item.total),
                innerRadius: .ratio(0.55),
                angularInset: 1.5
            )
            .foregroundStyle(Color(hex: item.colorHex))
            .cornerRadius(4)
        }
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    var items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
