import ActivityKit
import Foundation

public struct FocusActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable, Sendable {
        public var phase: TimerPhase
        public var endDate: Date
        public var totalSeconds: Int
        public var isRunning: Bool

        public init(phase: TimerPhase, endDate: Date, totalSeconds: Int, isRunning: Bool) {
            self.phase = phase
            self.endDate = endDate
            self.totalSeconds = totalSeconds
            self.isRunning = isRunning
        }
    }

    public var sessionLabel: String

    public init(sessionLabel: String) {
        self.sessionLabel = sessionLabel
    }
}
