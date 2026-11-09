import Foundation
import ActivityKit

@MainActor
final class LiveActivityService {
    static let shared = LiveActivityService()
    private var activity: Activity<FocusActivityAttributes>?

    var isSupported: Bool {
        ActivityAuthorizationInfo().areActivitiesEnabled
    }

    func start(phase: TimerPhase, totalSeconds: Int, endDate: Date) {
        guard isSupported else { return }
        end()

        let attributes = FocusActivityAttributes(sessionLabel: phase.displayName)
        let state = FocusActivityAttributes.ContentState(
            phase: phase,
            endDate: endDate,
            totalSeconds: totalSeconds,
            isRunning: true
        )

        do {
            activity = try Activity.request(
                attributes: attributes,
                content: .init(state: state, staleDate: endDate),
                pushType: nil
            )
        } catch {
            // Live Activities unavailable (simulator without support, denied, etc.)
            activity = nil
        }
    }

    func update(phase: TimerPhase, totalSeconds: Int, endDate: Date, isRunning: Bool) {
        guard let activity else { return }
        let state = FocusActivityAttributes.ContentState(
            phase: phase,
            endDate: endDate,
            totalSeconds: totalSeconds,
            isRunning: isRunning
        )
        Task {
            await activity.update(.init(state: state, staleDate: endDate))
        }
    }

    func end() {
        guard let activity else { return }
        Task {
            await activity.end(nil, dismissalPolicy: .immediate)
        }
        self.activity = nil
    }
}
