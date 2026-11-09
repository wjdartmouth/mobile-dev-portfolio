import SwiftUI
import SwiftData

@main
struct SyncNotesApp: App {
    private let container: ModelContainer
    @State private var repository = NoteRepository()

    init() {
        let schema = Schema([Note.self])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        do {
            container = try ModelContainer(for: schema, configurations: [config])
        } catch {
            fatalError("ModelContainer failed: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            NotesRootView()
                .environment(repository)
                .modelContainer(container)
        }
    }
}
