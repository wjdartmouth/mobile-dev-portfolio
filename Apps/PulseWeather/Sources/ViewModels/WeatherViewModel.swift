import Foundation
import SwiftUI

enum WeatherLoadState: Equatable {
    case idle
    case loading
    case loaded
    case failed(String)
}

@MainActor
@Observable
final class WeatherViewModel {
    private(set) var bundle: WeatherBundle?
    private(set) var state: WeatherLoadState = .idle
    private(set) var isOfflineCache = false
    private(set) var searchResults: [GeoResult] = []
    var searchQuery = ""
    var useCelsius = true

    private let api: any WeatherServing
    private let cache: WeatherCache
    private let location: LocationProvider

    init(
        api: any WeatherServing = OpenMeteoClient(),
        cache: WeatherCache = .shared,
        location: LocationProvider = LocationProvider()
    ) {
        self.api = api
        self.cache = cache
        self.location = location
    }

    func bootstrap() async {
        if let cached = await cache.load() {
            bundle = cached
            isOfflineCache = true
            state = .loaded
        }
        location.request()
        await refreshUsingLocation()
    }

    func refreshUsingLocation() async {
        state = .loading
        do {
            let coord = try await location.coordinate()
            try await loadForecast(
                latitude: coord.latitude,
                longitude: coord.longitude,
                placeName: "Current Location"
            )
        } catch {
            if bundle != nil {
                state = .loaded
                isOfflineCache = true
            } else {
                state = .failed(error.localizedDescription)
            }
        }
    }

    func select(_ place: GeoResult) async {
        searchQuery = ""
        searchResults = []
        state = .loading
        do {
            try await loadForecast(
                latitude: place.latitude,
                longitude: place.longitude,
                placeName: place.displayName
            )
        } catch {
            state = .failed(error.localizedDescription)
        }
    }

    func search() async {
        let q = searchQuery
        guard !q.trimmingCharacters(in: .whitespaces).isEmpty else {
            searchResults = []
            return
        }
        do {
            searchResults = try await api.searchCities(query: q)
        } catch {
            searchResults = []
        }
    }

    func refresh() async {
        guard let bundle else {
            await refreshUsingLocation()
            return
        }
        state = .loading
        do {
            try await loadForecast(
                latitude: bundle.latitude,
                longitude: bundle.longitude,
                placeName: bundle.placeName
            )
        } catch {
            if self.bundle != nil {
                state = .loaded
                isOfflineCache = true
            } else {
                state = .failed(error.localizedDescription)
            }
        }
    }

    private func loadForecast(latitude: Double, longitude: Double, placeName: String) async throws {
        let fresh = try await api.forecast(latitude: latitude, longitude: longitude, placeName: placeName)
        bundle = fresh
        isOfflineCache = false
        state = .loaded
        try? await cache.save(fresh)
    }

    func displayTemp(_ value: Double) -> String {
        let v = useCelsius ? value : (value * 9 / 5 + 32)
        return String(format: "%.0f°%@", v, useCelsius ? "C" : "F")
    }
}
