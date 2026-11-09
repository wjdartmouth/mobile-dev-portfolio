import SwiftUI
import SwiftData

struct LedgerRootView: View {
    @State private var viewModel = LedgerViewModel()
    @Environment(\.modelContext) private var modelContext
    @State private var showForm = false
    @State private var editing: Expense?
    @State private var exportURL: URL?
    @State private var showShare = false

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.expenses.isEmpty && viewModel.searchText.isEmpty {
                    ContentUnavailableView(
                        "No expenses",
                        systemImage: "tray",
                        description: Text("Add your first expense to see totals and charts.")
                    )
                } else {
                    List {
                        summarySection
                        chartSection
                        filterSection
                        expenseSection
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Ledger Lite")
            .searchable(text: $viewModel.searchText, prompt: "Search expenses")
            .onChange(of: viewModel.searchText) { _, _ in viewModel.reload() }
            .onChange(of: viewModel.filter) { _, _ in viewModel.reload() }
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        if let url = viewModel.exportCSV() {
                            exportURL = url
                            showShare = true
                        }
                    } label: {
                        Label("Export CSV", systemImage: "square.and.arrow.up")
                    }
                    .disabled(viewModel.expenses.isEmpty)
                    .accessibilityHint("Exports the currently filtered expenses as a CSV file")
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        editing = nil
                        showForm = true
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel("Add expense")
                }
            }
            .sheet(isPresented: $showForm, onDismiss: { viewModel.reload() }) {
                ExpenseFormView(categories: viewModel.categories, expense: nil)
            }
            .sheet(item: $editing, onDismiss: { viewModel.reload() }) { expense in
                ExpenseFormView(categories: viewModel.categories, expense: expense)
            }
            .sheet(isPresented: $showShare) {
                if let exportURL {
                    ShareSheet(items: [exportURL])
                }
            }
            .onAppear { viewModel.attach(context: modelContext) }
        }
    }

    private var summarySection: some View {
        Section {
            VStack(alignment: .leading, spacing: 8) {
                Text("Total")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .accessibilityHidden(true)
                Text(MoneyFormat.string(from: viewModel.totalAmount))
                    .font(.largeTitle.bold())
                    .accessibilityLabel("Total \(MoneyFormat.string(from: viewModel.totalAmount))")
                Text("\(viewModel.expenses.count) expenses · \(viewModel.filter.label)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.vertical, 4)
        }
    }

    private var chartSection: some View {
        Section("By category") {
            if viewModel.categoryTotals.isEmpty {
                Text("No data for this filter.")
                    .foregroundStyle(.secondary)
            } else {
                CategoryChartView(totals: viewModel.categoryTotals)
                    .frame(height: 200)
                    .accessibilityLabel("Spending by category chart")
                ForEach(viewModel.categoryTotals) { item in
                    HStack {
                        Image(systemName: item.symbol)
                            .foregroundStyle(Color(hex: item.colorHex))
                            .frame(width: 24)
                        Text(item.name)
                        Spacer()
                        Text(MoneyFormat.string(from: item.total))
                            .fontWeight(.medium)
                    }
                    .accessibilityElement(children: .combine)
                }
            }
        }
    }

    private var filterSection: some View {
        Section {
            Picker("Range", selection: $viewModel.filter) {
                ForEach(ExpenseFilter.allCases) { f in
                    Text(f.label).tag(f)
                }
            }
            .pickerStyle(.segmented)
            .accessibilityLabel("Date range filter")

            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    FilterChip(title: "All categories", selected: viewModel.selectedCategoryID == nil) {
                        viewModel.selectedCategoryID = nil
                        viewModel.reload()
                    }
                    ForEach(viewModel.categories, id: \.id) { category in
                        FilterChip(
                            title: category.name,
                            selected: viewModel.selectedCategoryID == category.id,
                            color: category.color
                        ) {
                            viewModel.selectedCategoryID = category.id
                            viewModel.reload()
                        }
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }

    private var expenseSection: some View {
        Section("Expenses") {
            ForEach(viewModel.expenses, id: \.id) { expense in
                Button {
                    editing = expense
                } label: {
                    ExpenseRow(expense: expense)
                }
                .buttonStyle(.plain)
                .swipeActions {
                    Button(role: .destructive) {
                        viewModel.delete(expense)
                    } label: {
                        Label("Delete", systemImage: "trash")
                    }
                }
            }
        }
    }
}

extension Expense: Identifiable {}
