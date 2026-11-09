import SwiftUI
import SwiftData

struct RootTabView: View {
    @State private var settings = TimerSettings()
    @State private var timerVM = TimerViewModel()
    @State private var historyVM = HistoryViewModel()
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        TabView {
            TimerView(viewModel: timerVM)
                .tabItem { Label("Timer", systemImage: "timer") }

            HistoryView(viewModel: historyVM)
                .tabItem { Label("History", systemImage: "chart.bar.fill") }

            SettingsView(settings: settings, timerVM: timerVM)
                .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
        .tint(.accentColor)
        .onAppear {
            timerVM.settings = settings
            timerVM.attach(context: modelContext)
            historyVM.attach(context: modelContext)
            timerVM.reloadDurationsFromSettings()
        }
        .onChange(of: settings.focusMinutes) { _, _ in timerVM.reloadDurationsFromSettings() }
        .onChange(of: settings.shortBreakMinutes) { _, _ in timerVM.reloadDurationsFromSettings() }
        .onChange(of: settings.longBreakMinutes) { _, _ in timerVM.reloadDurationsFromSettings() }
    }
}
