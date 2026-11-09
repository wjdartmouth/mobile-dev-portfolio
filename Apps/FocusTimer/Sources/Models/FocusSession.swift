import Foundation
import SwiftData

@Model
final class FocusSession {
    var id: UUID
    var phaseRaw: String
    var plannedSeconds: Int
    var actualSeconds: Int
    var completedAt: Date
    var wasCompleted: Bool
    var note: String

    var phase: TimerPhase {
        get { TimerPhase(rawValue: phaseRaw) ?? .focus }
        set { phaseRaw = newValue.rawValue }
    }

    init(
        phase: TimerPhase,
        plannedSeconds: Int,
        actualSeconds: Int,
        completedAt: Date = .now,
        wasCompleted: Bool,
        note: String = ""
    ) {
        self.id = UUID()
        self.phaseRaw = phase.rawValue
        self.plannedSeconds = plannedSeconds
        self.actualSeconds = actualSeconds
        self.completedAt = completedAt
        self.wasCompleted = wasCompleted
        self.note = note
    }
}
