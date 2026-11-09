import Foundation

public struct WidgetSnapshot: Codable, Sendable {
    public var phase: TimerPhase
    public var remainingSeconds: Int
    public var isRunning: Bool
    public var completedFocusToday: Int
    public var updatedAt: Date

    public init(
        phase: TimerPhase = .focus,
        remainingSeconds: Int = 25 * 60,
        isRunning: Bool = false,
        completedFocusToday: Int = 0,
        updatedAt: Date = .now
    ) {
        self.phase = phase
        self.remainingSeconds = remainingSeconds
        self.isRunning = isRunning
        self.completedFocusToday = completedFocusToday
        self.updatedAt = updatedAt
    }

    private static let key = "widget.snapshot"

    public static func load() -> WidgetSnapshot {
        guard let data = AppGroup.defaults.data(forKey: key),
              let value = try? JSONDecoder().decode(WidgetSnapshot.self, from: data) else {
            return WidgetSnapshot()
        }
        return value
    }

    public func save() {
        if let data = try? JSONEncoder().encode(self) {
            AppGroup.defaults.set(data, forKey: Self.key)
        }
    }
}
