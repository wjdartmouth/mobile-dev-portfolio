import Foundation

actor WeatherCache {
    static let shared = WeatherCache()

    private let fileURL: URL
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init(fileManager: FileManager = .default) {
        let dir = fileManager.urls(for: .cachesDirectory, in: .userDomainMask).first!
        fileURL = dir.appendingPathComponent("pulse_weather_cache.json")
        encoder.dateEncodingStrategy = .iso8601
        decoder.dateDecodingStrategy = .iso8601
    }

    func save(_ bundle: WeatherBundle) throws {
        let data = try encoder.encode(bundle)
        try data.write(to: fileURL, options: [.atomic])
    }

    func load() -> WeatherBundle? {
        guard let data = try? Data(contentsOf: fileURL) else { return nil }
        return try? decoder.decode(WeatherBundle.self, from: data)
    }

    func clear() {
        try? FileManager.default.removeItem(at: fileURL)
    }
}
