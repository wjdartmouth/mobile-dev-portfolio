import SwiftUI
import MapKit

struct JournalMapView: View {
    @Bindable var viewModel: JournalListViewModel
    @State private var position: MapCameraPosition = .userLocation(fallback: .automatic)
    @State private var selected: JournalEntry?

    private var pinned: [JournalEntry] {
        viewModel.entries.filter(\.hasCoordinate)
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Map(position: $position) {
                    UserAnnotation()
                    ForEach(pinned, id: \.id) { entry in
                        if let coord = entry.coordinate {
                            Annotation(entry.title, coordinate: coord) {
                                Button {
                                    selected = entry
                                } label: {
                                    VStack(spacing: 2) {
                                        if let image = ImageStore.image(from: entry.photoData) {
                                            Image(uiImage: image)
                                                .resizable()
                                                .scaledToFill()
                                                .frame(width: 36, height: 36)
                                                .clipShape(Circle())
                                                .overlay(Circle().stroke(.white, lineWidth: 2))
                                        } else {
                                            Image(systemName: "mappin.circle.fill")
                                                .font(.title)
                                                .foregroundStyle(Color.accentColor)
                                        }
                                    }
                                }
                                .accessibilityLabel(entry.title)
                            }
                        }
                    }
                }
                .mapControls {
                    MapUserLocationButton()
                    MapCompass()
                    MapPitchToggle()
                }

                if pinned.isEmpty {
                    VStack {
                        Spacer()
                        Text("Entries with a location appear as pins here.")
                            .font(.footnote)
                            .padding(10)
                            .background(.ultraThinMaterial, in: Capsule())
                            .padding()
                    }
                }
            }
            .navigationTitle("Map")
            .sheet(item: $selected) { entry in
                NavigationStack {
                    EntryDetailView(entry: entry)
                }
                .presentationDetents([.medium, .large])
            }
            .onAppear { viewModel.reload() }
        }
    }
}

struct EntryDetailView: View {
    let entry: JournalEntry

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if let image = ImageStore.image(from: entry.photoData) {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFit()
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                Text(entry.title).font(.title.bold())
                if let place = entry.placeName {
                    Label(place, systemImage: "mappin.and.ellipse")
                        .foregroundStyle(.secondary)
                }
                Text(entry.note)
                    .font(.body)
                Text(entry.createdAt, format: .dateTime.day().month().year().hour().minute())
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .navigationTitle("Entry")
        .navigationBarTitleDisplayMode(.inline)
    }
}
