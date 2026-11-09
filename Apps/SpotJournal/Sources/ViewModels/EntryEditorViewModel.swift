import Foundation
import SwiftUI
import SwiftData
import PhotosUI
import CoreLocation

@MainActor
@Observable
final class EntryEditorViewModel {
    var title: String = ""
    var note: String = ""
    var photoData: Data?
    var latitude: Double?
    var longitude: Double?
    var placeName: String?
    var selectedItem: PhotosPickerItem?
    var isSaving = false
    var isLocating = false
    var errorMessage: String?
    var showCamera = false

    private var existing: JournalEntry?
    private let locationService: LocationService

    var canSave: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var isEditing: Bool { existing != nil }

    init(entry: JournalEntry? = nil, locationService: LocationService) {
        self.existing = entry
        self.locationService = locationService
        if let entry {
            title = entry.title
            note = entry.note
            photoData = entry.photoData
            latitude = entry.latitude
            longitude = entry.longitude
            placeName = entry.placeName
        }
    }

    func loadPhoto() async {
        guard let selectedItem else { return }
        do {
            if let data = try await selectedItem.loadTransferable(type: Data.self),
               let image = UIImage(data: data) {
                photoData = ImageStore.jpegData(from: image)
            }
        } catch {
            errorMessage = "Could not load the selected photo."
        }
    }

    func setCameraImage(_ image: UIImage) {
        photoData = ImageStore.jpegData(from: image)
    }

    func clearPhoto() {
        photoData = nil
        selectedItem = nil
    }

    func captureLocation() async {
        isLocating = true
        errorMessage = nil
        defer { isLocating = false }
        do {
            let location = try await locationService.currentLocation()
            latitude = location.coordinate.latitude
            longitude = location.coordinate.longitude
            placeName = locationService.placeName
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func clearLocation() {
        latitude = nil
        longitude = nil
        placeName = nil
    }

    func save(into context: ModelContext) throws {
        guard canSave else {
            errorMessage = "Please add a title."
            throw ValidationError.emptyTitle
        }
        isSaving = true
        defer { isSaving = false }

        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedNote = note.trimmingCharacters(in: .whitespacesAndNewlines)

        if let existing {
            existing.title = trimmedTitle
            existing.note = trimmedNote
            existing.photoData = photoData
            existing.latitude = latitude
            existing.longitude = longitude
            existing.placeName = placeName
            existing.updatedAt = .now
        } else {
            let entry = JournalEntry(
                title: trimmedTitle,
                note: trimmedNote,
                latitude: latitude,
                longitude: longitude,
                placeName: placeName,
                photoData: photoData
            )
            context.insert(entry)
        }
        try context.save()
    }

    enum ValidationError: LocalizedError {
        case emptyTitle
        var errorDescription: String? { "Title is required." }
    }
}
