import Foundation
import CoreLocation

enum LocationError: LocalizedError {
    case denied
    case unavailable
    case timeout

    var errorDescription: String? {
        switch self {
        case .denied: return "Location access is denied. Enable it in Settings."
        case .unavailable: return "Location is currently unavailable."
        case .timeout: return "Timed out waiting for a location fix."
        }
    }
}

@MainActor
final class LocationService: NSObject, ObservableObject {
    @Published private(set) var authorization: CLAuthorizationStatus
    @Published private(set) var lastLocation: CLLocation?
    @Published private(set) var placeName: String?

    private let manager = CLLocationManager()
    private var continuation: CheckedContinuation<CLLocation, Error>?

    override init() {
        authorization = manager.authorizationStatus
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func requestPermission() {
        manager.requestWhenInUseAuthorization()
    }

    func currentLocation() async throws -> CLLocation {
        authorization = manager.authorizationStatus
        switch authorization {
        case .notDetermined:
            requestPermission()
            try await Task.sleep(nanoseconds: 600_000_000)
            return try await currentLocation()
        case .denied, .restricted:
            throw LocationError.denied
        default:
            break
        }

        if let last = manager.location, abs(last.timestamp.timeIntervalSinceNow) < 30 {
            lastLocation = last
            await reverseGeocode(last)
            return last
        }

        return try await withCheckedThrowingContinuation { cont in
            self.continuation = cont
            manager.requestLocation()
            Task {
                try? await Task.sleep(nanoseconds: 12_000_000_000)
                if let c = self.continuation {
                    self.continuation = nil
                    c.resume(throwing: LocationError.timeout)
                }
            }
        }
    }

    private func reverseGeocode(_ location: CLLocation) async {
        let geocoder = CLGeocoder()
        do {
            let marks = try await geocoder.reverseGeocodeLocation(location)
            if let mark = marks.first {
                let parts = [mark.name, mark.locality, mark.administrativeArea].compactMap { $0 }
                placeName = parts.prefix(2).joined(separator: ", ")
            }
        } catch {
            placeName = nil
        }
    }
}

extension LocationService: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            self.authorization = manager.authorizationStatus
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        Task { @MainActor in
            self.lastLocation = location
            await self.reverseGeocode(location)
            if let cont = self.continuation {
                self.continuation = nil
                cont.resume(returning: location)
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            if let cont = self.continuation {
                self.continuation = nil
                cont.resume(throwing: error)
            }
        }
    }
}
