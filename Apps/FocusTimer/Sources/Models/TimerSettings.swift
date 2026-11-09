import Foundation
import SwiftUI

@Observable
final class TimerSettings {
    private enum Keys {
        static let focus = "settings.focusMinutes"
        static let shortBreak = "settings.shortBreakMinutes"
        static let longBreak = "settings.longBreakMinutes"
        static let sessionsUntilLong = "settings.sessionsUntilLong"
        static let autoStartBreaks = "settings.autoStartBreaks"
        static let autoStartFocus = "settings.autoStartFocus"
        static let soundEnabled = "settings.soundEnabled"
        static let hapticsEnabled = "settings.hapticsEnabled"
    }

    var focusMinutes: Int {
        didSet { AppGroup.defaults.set(focusMinutes, forKey: Keys.focus) }
    }
    var shortBreakMinutes: Int {
        didSet { AppGroup.defaults.set(shortBreakMinutes, forKey: Keys.shortBreak) }
    }
    var longBreakMinutes: Int {
        didSet { AppGroup.defaults.set(longBreakMinutes, forKey: Keys.longBreak) }
    }
    var sessionsUntilLongBreak: Int {
        didSet { AppGroup.defaults.set(sessionsUntilLongBreak, forKey: Keys.sessionsUntilLong) }
    }
    var autoStartBreaks: Bool {
        didSet { AppGroup.defaults.set(autoStartBreaks, forKey: Keys.autoStartBreaks) }
    }
    var autoStartFocus: Bool {
        didSet { AppGroup.defaults.set(autoStartFocus, forKey: Keys.autoStartFocus) }
    }
    var soundEnabled: Bool {
        didSet { AppGroup.defaults.set(soundEnabled, forKey: Keys.soundEnabled) }
    }
    var hapticsEnabled: Bool {
        didSet { AppGroup.defaults.set(hapticsEnabled, forKey: Keys.hapticsEnabled) }
    }

    init() {
        let d = AppGroup.defaults
        focusMinutes = d.object(forKey: Keys.focus) as? Int ?? 25
        shortBreakMinutes = d.object(forKey: Keys.shortBreak) as? Int ?? 5
        longBreakMinutes = d.object(forKey: Keys.longBreak) as? Int ?? 15
        sessionsUntilLongBreak = d.object(forKey: Keys.sessionsUntilLong) as? Int ?? 4
        autoStartBreaks = d.object(forKey: Keys.autoStartBreaks) as? Bool ?? true
        autoStartFocus = d.object(forKey: Keys.autoStartFocus) as? Bool ?? false
        soundEnabled = d.object(forKey: Keys.soundEnabled) as? Bool ?? true
        hapticsEnabled = d.object(forKey: Keys.hapticsEnabled) as? Bool ?? true
    }

    func seconds(for phase: TimerPhase) -> Int {
        switch phase {
        case .focus: return focusMinutes * 60
        case .shortBreak: return shortBreakMinutes * 60
        case .longBreak: return longBreakMinutes * 60
        }
    }

    func resetToDefaults() {
        focusMinutes = 25
        shortBreakMinutes = 5
        longBreakMinutes = 15
        sessionsUntilLongBreak = 4
        autoStartBreaks = true
        autoStartFocus = false
        soundEnabled = true
        hapticsEnabled = true
    }
}
