import XCTest

final class OpenMeteoDecodingTests: XCTestCase {
    private let fetchedAt = Date(timeIntervalSince1970: 1_763_270_400)

    func testForecastFixtureDecodesCurrentHourlyAndDaily() throws {
        let bundle = try OpenMeteoDecoding.forecast(
            from: Fixtures.data("forecast"),
            placeName: "Tokyo",
            fetchedAt: fetchedAt
        )

        XCTAssertEqual(bundle.placeName, "Tokyo")
        XCTAssertEqual(bundle.timezone, "Asia/Tokyo")
        XCTAssertEqual(bundle.latitude, 35.6895, accuracy: 0.0001)
        XCTAssertEqual(bundle.current.temperature, 18.2, accuracy: 0.0001)
        XCTAssertEqual(bundle.current.weatherCode, 61)
        XCTAssertEqual(bundle.current.relativeHumidity, 72)
        XCTAssertEqual(bundle.fetchedAt, fetchedAt)

        XCTAssertEqual(bundle.dailyForecast.map(\.weatherCode), [61, 95])
        XCTAssertEqual(bundle.dailyForecast[0].tempMax, 20.1, accuracy: 0.0001)
        XCTAssertEqual(bundle.dailyForecast[1].tempMax, 17.4, accuracy: 0.0001)
        XCTAssertEqual(bundle.dailyForecast.map(\.date), ["2026-11-16", "2026-11-17"])

        XCTAssertEqual(bundle.nextHours.count, 3)
        XCTAssertEqual(bundle.nextHours.map(\.precipChance), [80, 10, nil])
        XCTAssertEqual(bundle.nextHours[2].weatherCode, 95)
    }

    func testMissingPrecipitationAndSunTimesStillDecode() throws {
        let bundle = try OpenMeteoDecoding.forecast(
            from: Fixtures.data("forecast_missing_optionals"),
            placeName: "Shibuya",
            fetchedAt: fetchedAt
        )

        XCTAssertNil(bundle.hourly.precipitationProbability)
        XCTAssertNil(bundle.daily.sunrise)
        XCTAssertNil(bundle.daily.sunset)
        XCTAssertEqual(bundle.nextHours.map(\.precipChance), [nil])
        XCTAssertEqual(bundle.dailyForecast.count, 1)
        XCTAssertEqual(bundle.current.weatherCode, 0)
    }

    func testGeocodingFixtureDecodesDisplayName() throws {
        let cities = try OpenMeteoDecoding.cities(from: Fixtures.data("geocoding"))
        XCTAssertEqual(cities.count, 1)
        XCTAssertEqual(cities[0].name, "Tokyo")
        XCTAssertEqual(cities[0].countryCode, "JP")
        XCTAssertEqual(cities[0].displayName, "Tokyo, Tokyo, JP")
    }

    func testMissingOrNullGeocodingResultsAreEmpty() throws {
        XCTAssertEqual(try OpenMeteoDecoding.cities(from: Fixtures.data("geocoding_missing_results")), [])
        XCTAssertEqual(try OpenMeteoDecoding.cities(from: Fixtures.data("geocoding_null_results")), [])
    }

    func testNextHoursKeepsTheFirstDayOnly() {
        let hours = (0..<25).map { index in
            String(format: "2026-11-16T%02d:00", index)
        }
        let bundle = WeatherBundle(
            latitude: 1,
            longitude: 2,
            timezone: "UTC",
            current: CurrentWeather(
                time: hours[0],
                temperature: 10,
                weatherCode: 0,
                windSpeed: 1,
                relativeHumidity: 50,
                apparentTemperature: 9,
                isDay: 1
            ),
            hourly: HourlyWeather(
                time: hours,
                temperature: Array(repeating: 10, count: 25),
                weatherCode: Array(repeating: 1, count: 25),
                precipitationProbability: [40]
            ),
            daily: DailyWeather(
                time: ["2026-11-16"],
                weatherCode: [1],
                temperatureMax: [12],
                temperatureMin: [8],
                sunrise: nil,
                sunset: nil
            ),
            fetchedAt: fetchedAt,
            placeName: "UTC"
        )

        XCTAssertEqual(bundle.nextHours.count, 24)
        XCTAssertEqual(bundle.nextHours[0].precipChance, 40)
        XCTAssertNil(bundle.nextHours[1].precipChance)
    }
}

final class WeatherCodeTests: XCTestCase {
    func testKnownAndUnknownCodes() {
        XCTAssertEqual(WeatherCode.description(for: 0), "Clear")
        XCTAssertEqual(WeatherCode.description(for: 61), "Rain")
        XCTAssertEqual(WeatherCode.description(for: 95), "Thunderstorm")
        XCTAssertEqual(WeatherCode.description(for: 999), "Unknown")
    }
}
