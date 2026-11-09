import SwiftUI
import SwiftData

struct NoteEditorView: View {
    let noteID: UUID
    @Environment(NoteRepository.self) private var repository
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var title: String = ""
    @State private var bodyText: String = ""
    @State private var note: Note?
    @State private var saveTask: Task<Void, Never>?

    var body: some View {
        Group {
            if note != nil {
                Form {
                    TextField("Title", text: $title)
                        .font(.title2.weight(.semibold))
                        .accessibilityLabel("Note title")
                    TextField("Start writing…", text: $bodyText, axis: .vertical)
                        .lineLimit(10...30)
                        .accessibilityLabel("Note body")
                    Section("Sync metadata") {
                        if let note {
                            LabeledContent("Revision", value: "\(note.revision)")
                            LabeledContent("Record", value: note.cloudRecordName ?? "—")
                            LabeledContent("Mode", value: repository.activeMode.rawValue)
                        }
                    }
                }
            } else {
                ContentUnavailableView("Note missing", systemImage: "questionmark.folder")
            }
        }
        .navigationTitle("Edit")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: load)
        .onChange(of: title) { _, _ in scheduleSave() }
        .onChange(of: bodyText) { _, _ in scheduleSave() }
        .onDisappear {
            saveTask?.cancel()
            persistNow()
        }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    persistNow()
                } label: {
                    Image(systemName: "checkmark.circle")
                }
                .accessibilityLabel("Save note")
            }
        }
    }

    private func load() {
        var descriptor = FetchDescriptor<Note>(
            predicate: #Predicate { $0.id == noteID }
        )
        descriptor.fetchLimit = 1
        note = try? modelContext.fetch(descriptor).first
        title = note?.title ?? ""
        bodyText = note?.body ?? ""
    }

    private func scheduleSave() {
        saveTask?.cancel()
        saveTask = Task {
            try? await Task.sleep(nanoseconds: 450_000_000)
            guard !Task.isCancelled else { return }
            persistNow()
        }
    }

    private func persistNow() {
        guard let note else { return }
        note.title = title
        note.body = bodyText
        Task {
            await repository.save(note)
        }
    }
}
