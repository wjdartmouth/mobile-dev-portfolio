import SwiftUI

struct SettingsView: View {
    @Bindable var settings: TimerSettings
    var timerVM: TimerViewModel

    var body: some View {
        NavigationStack {
            Form {
                Section("Durations") {
                    Stepper("Focus: \(settings.focusMinutes) min", value: $settings.focusMinutes, in: 1...90)
                    Stepper("Short break: \(settings.shortBreakMinutes) min", value: $settings.shortBreakMinutes, in: 1...30)
                    Stepper("Long break: \(settings.longBreakMinutes) min", value: $settings.longBreakMinutes, in: 5...60)
                    Stepper("Long break every \(settings.sessionsUntilLongBreak) sessions", value: $settings.sessionsUntilLongBreak, in: 2...8)
                }

                Section("Automation") {
                    Toggle("Auto-start breaks", isOn: $settings.autoStartBreaks)
                    Toggle("Auto-start focus", isOn: $settings.autoStartFocus)
                }

                Section("Feedback") {
                    Toggle("Sounds (system notification)", isOn: $settings.soundEnabled)
                    Toggle("Haptics", isOn: $settings.hapticsEnabled)
                }

                Section {
                    Button("Reset to defaults", role: .destructive) {
                        settings.resetToDefaults()
                        timerVM.reloadDurationsFromSettings()
                    }
                }

                Section("About") {
                    LabeledContent("Version", value: "1.0.0")
                    LabeledContent("Deployment", value: "iOS 17+")
                    Text("Widgets and Live Activities share state via App Group.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Settings")
        }
    }
}
