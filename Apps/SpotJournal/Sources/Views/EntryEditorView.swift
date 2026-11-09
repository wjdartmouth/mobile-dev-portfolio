import SwiftUI
import PhotosUI
import SwiftData

struct EntryEditorView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: EntryEditorViewModel

    init(locationService: LocationService, entry: JournalEntry?) {
        _viewModel = State(initialValue: EntryEditorViewModel(entry: entry, locationService: locationService))
    }

    var body: some View {
        NavigationStack {
            Form {
                photoSection
                Section("Details") {
                    TextField("Title", text: $viewModel.title)
                        .accessibilityLabel("Entry title")
                    TextField("Short note", text: $viewModel.note, axis: .vertical)
                        .lineLimit(3...8)
                }
                locationSection
                if let error = viewModel.errorMessage {
                    Section {
                        Text(error).foregroundStyle(.red).font(.footnote)
                    }
                }
            }
            .navigationTitle(viewModel.isEditing ? "Edit Spot" : "New Spot")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        do {
                            try viewModel.save(into: modelContext)
                            dismiss()
                        } catch {
                            viewModel.errorMessage = error.localizedDescription
                        }
                    }
                    .disabled(!viewModel.canSave || viewModel.isSaving)
                }
            }
            .sheet(isPresented: $viewModel.showCamera) {
                CameraPicker { image in
                    if let image { viewModel.setCameraImage(image) }
                }
                .ignoresSafeArea()
            }
            .onChange(of: viewModel.selectedItem) { _, _ in
                Task { await viewModel.loadPhoto() }
            }
        }
    }

    private var photoSection: some View {
        Section("Photo") {
            if let image = ImageStore.image(from: viewModel.photoData) {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(maxHeight: 220)
                    .clipped()
                    .listRowInsets(EdgeInsets())
                Button("Remove photo", role: .destructive, action: viewModel.clearPhoto)
            }
            PhotosPicker(selection: $viewModel.selectedItem, matching: .images) {
                Label("Choose from Library", systemImage: "photo.on.rectangle")
            }
            Button {
                viewModel.showCamera = true
            } label: {
                Label("Take Photo", systemImage: "camera")
            }
        }
    }

    private var locationSection: some View {
        Section("Location") {
            if let place = viewModel.placeName {
                Label(place, systemImage: "mappin.circle.fill")
                if let lat = viewModel.latitude, let lon = viewModel.longitude {
                    Text(String(format: "%.5f, %.5f", lat, lon))
                        .font(.caption.monospacedDigit())
                        .foregroundStyle(.secondary)
                }
                Button("Clear location", role: .destructive, action: viewModel.clearLocation)
            } else {
                Text("No pin yet")
                    .foregroundStyle(.secondary)
            }
            Button {
                Task { await viewModel.captureLocation() }
            } label: {
                if viewModel.isLocating {
                    ProgressView()
                } else {
                    Label("Use Current Location", systemImage: "location.fill")
                }
            }
            .disabled(viewModel.isLocating)
        }
    }
}
