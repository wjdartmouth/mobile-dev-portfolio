import SwiftUI
import SwiftData

struct NotesRootView: View {
    @Environment(NoteRepository.self) private var repository
    @Environment(\.modelContext) private var modelContext
    @State private var listVM = NotesListViewModel()
    @State private var path = NavigationPath()
    @State private var selected: Note?

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                if listVM.notes.isEmpty {
                    ContentUnavailableView(
                        "No notes yet",
                        systemImage: "note.text",
                        description: Text("Create a note. It stays on-device and syncs when iCloud is available.")
                    )
                } else {
                    List {
                        accountBanner
                        ForEach(listVM.notes, id: \.id) { note in
                            NavigationLink(value: note.id) {
                                NoteRow(note: note)
                            }
                            .swipeActions {
                                Button(role: .destructive) {
                                    Task {
                                        await repository.softDelete(note)
                                        listVM.reload()
                                    }
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                                Button {
                                    note.isPinned.toggle()
                                    Task {
                                        await repository.save(note)
                                        listVM.reload()
                                    }
                                } label: {
                                    Label(note.isPinned ? "Unpin" : "Pin", systemImage: note.isPinned ? "pin.slash" : "pin")
                                }
                                .tint(.orange)
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Sync Notes")
            .searchable(text: $listVM.searchText, prompt: "Search notes")
            .onChange(of: listVM.searchText) { _, _ in listVM.reload() }
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        Task { await repository.syncNow(); listVM.reload() }
                    } label: {
                        Image(systemName: "arrow.triangle.2.circlepath")
                    }
                    .accessibilityLabel("Sync now")
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        let note = listVM.createNote()
                        path.append(note.id)
                    } label: {
                        Image(systemName: "square.and.pencil")
                    }
                    .accessibilityLabel("New note")
                }
            }
            .navigationDestination(for: UUID.self) { id in
                NoteEditorView(noteID: id)
            }
            .safeAreaInset(edge: .bottom) {
                if listVM.notes.isEmpty {
                    accountBanner
                        .padding()
                }
            }
        }
        .task {
            repository.attach(context: modelContext)
            listVM.attach(context: modelContext)
            await repository.refreshAccountAndMode()
        }
    }

    private var accountBanner: some View {
        VStack(alignment: .leading, spacing: 6) {
            Label(repository.accountStatus.title, systemImage: repository.accountStatus.systemImage)
                .font(.subheadline.weight(.semibold))
            Text(modeCopy)
                .font(.caption)
                .foregroundStyle(.secondary)
            if let message = repository.syncMessage {
                Text(message)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            if let last = repository.lastSyncAt {
                Text("Last sync \(last.formatted(date: .abbreviated, time: .shortened))")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 12).fill(Color(.secondarySystemBackground)))
        .accessibilityElement(children: .combine)
    }

    private var modeCopy: String {
        switch repository.activeMode {
        case .cloudKit:
            return "Mode: CloudKit — private DB, last-writer-wins by revision."
        case .localOnly:
            return "Mode: Local fallback — identical API, no remote writes."
        }
    }
}

struct NoteRow: View {
    let note: Note

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            if note.isPinned {
                Image(systemName: "pin.fill")
                    .font(.caption)
                    .foregroundStyle(.orange)
                    .accessibilityLabel("Pinned")
            }
            VStack(alignment: .leading, spacing: 4) {
                Text(note.title.isEmpty ? "Untitled" : note.title)
                    .font(.headline)
                    .lineLimit(1)
                Text(note.preview)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(2)
                Text(note.updatedAt, format: .dateTime.month().day().hour().minute())
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
        }
        .accessibilityElement(children: .combine)
    }
}
