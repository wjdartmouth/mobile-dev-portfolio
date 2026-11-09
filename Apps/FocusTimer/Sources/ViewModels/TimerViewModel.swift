import Foundation
import SwiftData
import SwiftUI

@MainActor
@Observable
final class TimerViewModel {
    private(set) var phase: TimerPhase = .focus
    private(set) var remainingSeconds: Int = 25 * 60
    private(set) var totalSeconds: Int = 25 * 60
    private(set) var isRunning = false
    private(set) var completedFocusCount = 0
    private(set) var endDate: Date?

    var settings: TimerSettings
    private var tickTask: Task<Void, Never>?
    private var modelContext: ModelContext?
    private var elapsedInCurrent = 0

    var progress: Double {
        guard totalSeconds > 0 else { return 0 }
        return 1 - (Double(remainingSeconds) / Double(totalSeconds))
    }

    var timeDisplay: String {
        let m = remainingSeconds / 60
        let s = remainingSeconds % 60
        return String(format: "%02d:%02d", m, s)
    }

    init(settings: TimerSettings = TimerSettings()) {
        self.settings = settings
        applyPhase(.focus, autoStart: false)
    }

    func attach(context: ModelContext) {
        self.modelContext = context
        refreshTodayCount()
        publishWidget()
    }

    func toggle() {
        if isRunning { pause() } else { start() }
    }

    func start() {
        guard !isRunning else { return }
        isRunning = true
        let end = Date().addingTimeInterval(TimeInterval(remainingSeconds))
        endDate = end

        NotificationService.shared.scheduleSessionEnd(phase: phase, after: remainingSeconds)
        LiveActivityService.shared.start(phase: phase, totalSeconds: totalSeconds, endDate: end)
        if settings.hapticsEnabled { HapticsService.impact(.light) }

        tickTask?.cancel()
        tickTask = Task { [weak self] in
            while let self, !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                await self.tick()
            }
        }
        publishWidget()
    }

    func pause() {
        isRunning = false
        tickTask?.cancel()
        tickTask = nil
        NotificationService.shared.cancelPending()
        if let endDate {
            LiveActivityService.shared.update(
                phase: phase,
                totalSeconds: totalSeconds,
                endDate: endDate,
                isRunning: false
            )
        }
        publishWidget()
    }

    func reset() {
        pause()
        LiveActivityService.shared.end()
        applyPhase(phase, autoStart: false)
    }

    func skip() {
        pause()
        persistSession(completed: false)
        advancePhase()
    }

    private func tick() async {
        guard isRunning else { return }
        remainingSeconds = max(0, remainingSeconds - 1)
        elapsedInCurrent += 1
        publishWidget()

        if remainingSeconds == 0 {
            await completeCurrent()
        }
    }

    private func completeCurrent() async {
        pause()
        persistSession(completed: true)
        if settings.hapticsEnabled { HapticsService.notify(.success) }
        LiveActivityService.shared.end()

        if phase == .focus {
            completedFocusCount += 1
        }
        advancePhase()
    }

    private func advancePhase() {
        let next: TimerPhase
        switch phase {
        case .focus:
            if completedFocusCount > 0,
               completedFocusCount % settings.sessionsUntilLongBreak == 0 {
                next = .longBreak
            } else {
                next = .shortBreak
            }
        case .shortBreak, .longBreak:
            next = .focus
        }

        let auto: Bool
        switch next {
        case .focus: auto = settings.autoStartFocus
        case .shortBreak, .longBreak: auto = settings.autoStartBreaks
        }
        applyPhase(next, autoStart: auto)
    }

    private func applyPhase(_ newPhase: TimerPhase, autoStart: Bool) {
        phase = newPhase
        totalSeconds = settings.seconds(for: newPhase)
        remainingSeconds = totalSeconds
        elapsedInCurrent = 0
        endDate = nil
        publishWidget()
        if autoStart { start() }
    }

    func reloadDurationsFromSettings() {
        guard !isRunning else { return }
        applyPhase(phase, autoStart: false)
    }

    private func persistSession(completed: Bool) {
        guard let modelContext else { return }
        let session = FocusSession(
            phase: phase,
            plannedSeconds: totalSeconds,
            actualSeconds: elapsedInCurrent,
            wasCompleted: completed,
            note: ""
        )
        modelContext.insert(session)
        try? modelContext.save()
        refreshTodayCount()
    }

    private func refreshTodayCount() {
        guard let modelContext else { return }
        let start = Calendar.current.startOfDay(for: .now)
        let focusRaw = TimerPhase.focus.rawValue
        var descriptor = FetchDescriptor<FocusSession>(
            predicate: #Predicate {
                $0.completedAt >= start && $0.phaseRaw == focusRaw && $0.wasCompleted == true
            }
        )
        descriptor.fetchLimit = 500
        completedFocusCount = (try? modelContext.fetch(descriptor).count) ?? completedFocusCount
    }

    private func publishWidget() {
        WidgetSnapshot(
            phase: phase,
            remainingSeconds: remainingSeconds,
            isRunning: isRunning,
            completedFocusToday: completedFocusCount,
            updatedAt: .now
        ).save()
    }
}
