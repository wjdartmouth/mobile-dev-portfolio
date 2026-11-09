import Foundation
import CoreLocation

@MainActor
final class LocationProvider: NSObject, ObservableObject {
    @Published private(set) var status: CLAuthorizationStatus
    private let manager = CLLocationManager()
    private var continuation: CheckedContinuation<CLLocationCoordinate2D, Error>?

    override init() {
        status = manager.authorizationStatus
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    func request() {
        manager.requestWhenInUseAuthorization()
    }

    func coordinate() async throws -> CLLocationCoordinate2D {
        status = manager.authorizationStatus
        if status == .notDetermined {
            request()
            try await Task.sleep(nanoseconds: 500_000_000)
            return try await coordinate()
        }
        if status == .denied || status == .restricted {
            throw LocationFailure.denied
        }
        if let loc = manager.location {
            return loc.coordinate
        }
        return try await withCheckedThrowingContinuation { cont in
            continuation = cont
            manager.requestLocation()
        }
    }

    enum LocationFailure: LocalizedError {
        case denied
        var errorDescription: String? { "Location permission denied." }
    }
}

extension LocationProvider: CLLocationManagerDelegate {
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in self.status = manager.authorizationStatus }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let coord = locations.last?.coordinate else { return }
        Task { @MainActor in
            continuation?.resume(returning: coord)
            continuation = nil
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            continuation?.resume(throwing: error)
            continuation = nil
        }
    }
}
