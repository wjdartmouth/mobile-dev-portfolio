import SwiftUI
import SwiftData

@main
struct LedgerLiteApp: App {
    private let container: ModelContainer

    init() {
        let schema = Schema([Expense.self, ExpenseCategory.self])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        do {
            container = try ModelContainer(for: schema, configurations: [config])
        } catch {
            fatalError("ModelContainer failed: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            LedgerRootView()
                .modelContainer(container)
        }
    }
}
