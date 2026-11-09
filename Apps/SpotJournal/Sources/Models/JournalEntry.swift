import Foundation
import SwiftData
import CoreLocation

@Model
final class JournalEntry {
    @Attribute(.unique) var id: UUID
    var title: String
    var note: String
    var createdAt: Date
    var updatedAt: Date
    var latitude: Double?
    var longitude: Double?
    var placeName: String?
    @Attribute(.externalStorage) var photoData: Data?

    var hasCoordinate: Bool { latitude != nil && longitude != nil }

    var coordinate: CLLocationCoordinate2D? {
        guard let latitude, let longitude else { return nil }
        return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }

    init(
        title: String,
        note: String,
        createdAt: Date = .now,
        latitude: Double? = nil,
        longitude: Double? = nil,
        placeName: String? = nil,
        photoData: Data? = nil
    ) {
        self.id = UUID()
        self.title = title
        self.note = note
        self.createdAt = createdAt
        self.updatedAt = createdAt
        self.latitude = latitude
        self.longitude = longitude
        self.placeName = placeName
        self.photoData = photoData
    }
}
