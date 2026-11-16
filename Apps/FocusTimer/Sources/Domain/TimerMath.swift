import Foundation

enum TimerMath {
    static func seconds(
        for phase: TimerPhase,
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int
    ) -> Int {
        switch phase {
        case .focus: return focusMinutes * 60
        case .shortBreak: return shortBreakMinutes * 60
        case .longBreak: return longBreakMinutes * 60
        }
    }

    /// History-style duration. `0` → `0:00`, `65` → `1:05`, `3660` → `1h 01m`.
    static func duration(seconds: Int) -> String {
        let minutes = seconds / 60
        let secs = seconds % 60
        if minutes >= 60 {
            let hours = minutes / 60
            let remainingMinutes = minutes % 60
            return String(format: "%dh %02dm", hours, remainingMinutes)
        }
        return String(format: "%d:%02d", minutes, secs)
    }
}
