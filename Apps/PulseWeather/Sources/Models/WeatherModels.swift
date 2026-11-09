import Foundation

struct GeoResult: Codable, Identifiable, Hashable {
    var id: Int
    var name: String
    var latitude: Double
    var longitude: Double
    var countryCode: String?
    var admin1: String?
    var timezone: String?

    var displayName: String {
        [name, admin1, countryCode].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: ", ")
    }

    enum CodingKeys: String, CodingKey {
        case id, name, latitude, longitude, timezone
        case countryCode = "country_code"
        case admin1
    }
}

struct GeoResponse: Codable {
    var results: [GeoResult]?
}

struct WeatherBundle: Codable, Sendable {
    var latitude: Double
    var longitude: Double
    var timezone: String
    var current: CurrentWeather
    var hourly: HourlyWeather
    var daily: DailyWeather
    var fetchedAt: Date
    var placeName: String

    enum CodingKeys: String, CodingKey {
        case latitude, longitude, timezone, current, hourly, daily, fetchedAt, placeName
    }
}

struct CurrentWeather: Codable, Sendable {
    var time: String
    var temperature: Double
    var weatherCode: Int
    var windSpeed: Double
    var relativeHumidity: Int
    var apparentTemperature: Double
    var isDay: Int

    enum CodingKeys: String, CodingKey {
        case time
        case temperature = "temperature_2m"
        case weatherCode = "weather_code"
        case windSpeed = "wind_speed_10m"
        case relativeHumidity = "relative_humidity_2m"
        case apparentTemperature = "apparent_temperature"
        case isDay = "is_day"
    }
}

struct HourlyWeather: Codable, Sendable {
    var time: [String]
    var temperature: [Double]
    var weatherCode: [Int]
    var precipitationProbability: [Int]?

    enum CodingKeys: String, CodingKey {
        case time
        case temperature = "temperature_2m"
        case weatherCode = "weather_code"
        case precipitationProbability = "precipitation_probability"
    }
}

struct DailyWeather: Codable, Sendable {
    var time: [String]
    var weatherCode: [Int]
    var temperatureMax: [Double]
    var temperatureMin: [Double]
    var sunrise: [String]?
    var sunset: [String]?

    enum CodingKeys: String, CodingKey {
        case time
        case weatherCode = "weather_code"
        case temperatureMax = "temperature_2m_max"
        case temperatureMin = "temperature_2m_min"
        case sunrise, sunset
    }
}

struct ForecastDay: Identifiable {
    var id: String { date }
    var date: String
    var weatherCode: Int
    var tempMax: Double
    var tempMin: Double
}

struct ForecastHour: Identifiable {
    var id: String { time }
    var time: String
    var temperature: Double
    var weatherCode: Int
    var precipChance: Int?
}

extension WeatherBundle {
    var dailyForecast: [ForecastDay] {
        zip(zip(zip(daily.time, daily.weatherCode), daily.temperatureMax), daily.temperatureMin).map {
            ForecastDay(
                date: $0.0.0.0,
                weatherCode: $0.0.0.1,
                tempMax: $0.0.1,
                tempMin: $0.1
            )
        }
    }

    var nextHours: [ForecastHour] {
        let pairs = zip(zip(hourly.time, hourly.temperature), hourly.weatherCode)
        var precip = hourly.precipitationProbability ?? []
        var result: [ForecastHour] = []
        for (index, item) in pairs.enumerated() {
            let chance = index < precip.count ? precip[index] : nil
            result.append(
                ForecastHour(
                    time: item.0.0,
                    temperature: item.0.1,
                    weatherCode: item.1,
                    precipChance: chance
                )
            )
        }
        // Prefer upcoming hours relative to current time string if possible
        return Array(result.prefix(24))
    }
}
