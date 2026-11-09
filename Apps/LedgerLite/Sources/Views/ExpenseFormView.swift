import SwiftUI
import SwiftData

struct ExpenseFormView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: ExpenseFormViewModel
    let categories: [ExpenseCategory]

    init(categories: [ExpenseCategory], expense: Expense?) {
        self.categories = categories
        _viewModel = State(initialValue: ExpenseFormViewModel(expense: expense, defaultCategory: categories.first))
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Expense") {
                    TextField("Title", text: $viewModel.title)
                        .textInputAutocapitalization(.sentences)
                        .accessibilityLabel("Expense title")
                    TextField("Amount", text: $viewModel.amountText)
                        .keyboardType(.decimalPad)
                        .accessibilityLabel("Amount")
                    DatePicker("Date", selection: $viewModel.date, displayedComponents: .date)
                    TextField("Note", text: $viewModel.note, axis: .vertical)
                        .lineLimit(2...4)
                }

                Section("Category") {
                    if categories.isEmpty {
                        Text("No categories available.")
                    } else {
                        Picker("Category", selection: $viewModel.selectedCategory) {
                            ForEach(categories, id: \.id) { category in
                                Label(category.name, systemImage: category.symbol).tag(Optional(category))
                            }
                        }
                        .accessibilityLabel("Category picker")
                    }
                }

                if let message = viewModel.validationMessage {
                    Section {
                        Text(message)
                            .foregroundStyle(.red)
                            .font(.footnote)
                            .accessibilityLabel("Validation error: \(message)")
                    }
                }
            }
            .navigationTitle(viewModel.isEditing ? "Edit Expense" : "New Expense")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        if viewModel.save(into: modelContext) {
                            dismiss()
                        }
                    }
                    .accessibilityHint("Validates the form and saves the expense")
                }
            }
        }
    }
}
