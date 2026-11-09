import Foundation
import UserNotifications

@MainActor
final class NotificationService {
    static let shared = NotificationService()
    private let center = UNUserNotificationCenter.current()

    func requestAuthorization() async -> Bool {
        do {
            return try await center.requestAuthorization(options: [.alert, .sound, .badge])
        } catch {
            return false
        }
    }

    func scheduleSessionEnd(phase: TimerPhase, after seconds: Int) {
        cancelPending()
        guard seconds > 0 else { return }

        let content = UNMutableNotificationContent()
        content.title = "\(phase.displayName) complete"
        switch phase {
        case .focus:
            content.body = "Nice work. Time for a break."
        case .shortBreak, .longBreak:
            content.body = "Break over — ready to focus again?"
        }
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: TimeInterval(seconds), repeats: false)
        let request = UNNotificationRequest(
            identifier: "focus.session.end",
            content: content,
            trigger: trigger
        )
        center.add(request)
    }

    func cancelPending() {
        center.removePendingNotificationRequests(withIdentifiers: ["focus.session.end"])
    }
}
