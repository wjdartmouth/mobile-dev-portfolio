import Foundation

protocol WeatherServing: Sendable {
    func searchCities(query: String) async throws -> [GeoResult]
    func forecast(latitude: Double, longitude: Double, placeName: String) async throws -> WeatherBundle
}

enum WeatherAPIError: LocalizedError {
    case badURL
    case badResponse
    case decoding
    case emptyQuery

    var errorDescription: String? {
        switch self {
        case .badURL: return "Could not build the request URL."
        case .badResponse: return "The weather service returned an unexpected response."
        case .decoding: return "Could not decode weather data."
        case .emptyQuery: return "Enter a city name to search."
        }
    }
}

struct OpenMeteoClient: WeatherServing {
    private let session: URLSession
    private let decoder: JSONDecoder

    init(session: URLSession = .shared) {
        self.session = session
        self.decoder = JSONDecoder()
    }

    func searchCities(query: String) async throws -> [GeoResult] {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { throw WeatherAPIError.emptyQuery }

        var components = URLComponents(string: "https://geocoding-api.open-meteo.com/v1/search")!
        components.queryItems = [
            URLQueryItem(name: "name", value: trimmed),
            URLQueryItem(name: "count", value: "8"),
            URLQueryItem(name: "language", value: "en"),
            URLQueryItem(name: "format", value: "json")
        ]
        guard let url = components.url else { throw WeatherAPIError.badURL }

        let (data, response) = try await session.data(from: url)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw WeatherAPIError.badResponse
        }
        do {
            let decoded = try decoder.decode(GeoResponse.self, from: data)
            return decoded.results ?? []
        } catch {
            throw WeatherAPIError.decoding
        }
    }

    func forecast(latitude: Double, longitude: Double, placeName: String) async throws -> WeatherBundle {
        var components = URLComponents(string: "https://api.open-meteo.com/v1/forecast")!
        components.queryItems = [
            URLQueryItem(name: "latitude", value: String(latitude)),
            URLQueryItem(name: "longitude", value: String(longitude)),
            URLQueryItem(name: "current", value: "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m"),
            URLQueryItem(name: "hourly", value: "temperature_2m,precipitation_probability,weather_code"),
            URLQueryItem(name: "daily", value: "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset"),
            URLQueryItem(name: "timezone", value: "auto"),
            URLQueryItem(name: "forecast_days", value: "7")
        ]
        guard let url = components.url else { throw WeatherAPIError.badURL }

        let (data, response) = try await session.data(from: url)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw WeatherAPIError.badResponse
        }

        struct Payload: Codable {
            var latitude: Double
            var longitude: Double
            var timezone: String
            var current: CurrentWeather
            var hourly: HourlyWeather
            var daily: DailyWeather
        }

        do {
            let payload = try decoder.decode(Payload.self, from: data)
            return WeatherBundle(
                latitude: payload.latitude,
                longitude: payload.longitude,
                timezone: payload.timezone,
                current: payload.current,
                hourly: payload.hourly,
                daily: payload.daily,
                fetchedAt: .now,
                placeName: placeName
            )
        } catch {
            throw WeatherAPIError.decoding
        }
    }
}
