import SwiftUI
import SwiftData

struct JournalListView: View {
    @Bindable var viewModel: JournalListViewModel
    var locationService: LocationService
    @Environment(\.modelContext) private var modelContext
    @State private var showEditor = false
    @State private var editingEntry: JournalEntry?

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.entries.isEmpty {
                    ContentUnavailableView(
                        "No spots yet",
                        systemImage: "mappin.and.ellipse",
                        description: Text("Capture a photo, a short note, and pin it on the map.")
                    )
                } else {
                    List {
                        ForEach(viewModel.entries, id: \.id) { entry in
                            Button {
                                editingEntry = entry
                            } label: {
                                EntryRow(entry: entry)
                            }
                            .buttonStyle(.plain)
                            .swipeActions {
                                Button(role: .destructive) {
                                    viewModel.delete(entry)
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Spot Journal")
            .searchable(text: $viewModel.searchText, prompt: "Search notes or places")
            .onChange(of: viewModel.searchText) { _, _ in viewModel.reload() }
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Menu {
                        Picker("Sort", selection: $viewModel.sort) {
                            ForEach(JournalSort.allCases) { sort in
                                Text(sort.label).tag(sort)
                            }
                        }
                    } label: {
                        Image(systemName: "arrow.up.arrow.down")
                    }
                    .onChange(of: viewModel.sort) { _, _ in viewModel.reload() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        editingEntry = nil
                        showEditor = true
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel("New entry")
                }
            }
            .sheet(isPresented: $showEditor, onDismiss: { viewModel.reload() }) {
                EntryEditorView(locationService: locationService, entry: nil)
            }
            .sheet(item: $editingEntry, onDismiss: { viewModel.reload() }) { entry in
                EntryEditorView(locationService: locationService, entry: entry)
            }
        }
    }
}

extension JournalEntry: Identifiable {}

struct EntryRow: View {
    let entry: JournalEntry

    var body: some View {
        HStack(spacing: 12) {
            Group {
                if let image = ImageStore.image(from: entry.photoData) {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                } else {
                    ZStack {
                        Color.secondary.opacity(0.15)
                        Image(systemName: "photo")
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .frame(width: 64, height: 64)
            .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 4) {
                Text(entry.title)
                    .font(.headline)
                    .foregroundStyle(.primary)
                    .lineLimit(1)
                if !entry.note.isEmpty {
                    Text(entry.note)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                }
                HStack(spacing: 6) {
                    if let place = entry.placeName {
                        Label(place, systemImage: "mappin")
                    }
                    Text(entry.createdAt, style: .date)
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
        }
        .accessibilityElement(children: .combine)
    }
}
