import Foundation

enum FocusCyclePolicy {
    /// After a completed focus, a positive count that lands on the long-break
    /// cadence selects `.longBreak`. Every other completed focus selects
    /// `.shortBreak`. Any break returns to `.focus`.
    static func nextPhase(
        after current: TimerPhase,
        completedFocusCount: Int,
        sessionsUntilLongBreak: Int
    ) -> TimerPhase {
        switch current {
        case .focus:
            if completedFocusCount > 0,
               completedFocusCount % sessionsUntilLongBreak == 0 {
                return .longBreak
            }
            return .shortBreak
        case .shortBreak, .longBreak:
            return .focus
        }
    }
}
