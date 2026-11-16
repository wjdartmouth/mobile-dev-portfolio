import Foundation

enum OpenMeteoDecoding {
    private struct ForecastPayload: Codable {
        var latitude: Double
        var longitude: Double
        var timezone: String
        var current: CurrentWeather
        var hourly: HourlyWeather
        var daily: DailyWeather
    }

    static func cities(from data: Data) throws -> [GeoResult] {
        let decoded = try JSONDecoder().decode(GeoResponse.self, from: data)
        return decoded.results ?? []
    }

    static func forecast(from data: Data, placeName: String, fetchedAt: Date) throws -> WeatherBundle {
        let payload = try JSONDecoder().decode(ForecastPayload.self, from: data)
        return WeatherBundle(
            latitude: payload.latitude,
            longitude: payload.longitude,
            timezone: payload.timezone,
            current: payload.current,
            hourly: payload.hourly,
            daily: payload.daily,
            fetchedAt: fetchedAt,
            placeName: placeName
        )
    }
}
