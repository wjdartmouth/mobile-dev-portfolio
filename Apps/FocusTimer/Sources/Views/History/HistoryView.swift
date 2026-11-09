import SwiftUI
import Charts

struct HistoryView: View {
    @Bindable var viewModel: HistoryViewModel

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.sessions.isEmpty {
                    emptyState
                } else {
                    List {
                        statsHeader
                        chartSection
                        sessionSection
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("History")
            .toolbar {
                if !viewModel.sessions.isEmpty {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Clear", role: .destructive) { viewModel.clearAll() }
                    }
                }
            }
            .onAppear { viewModel.reload() }
        }
    }

    private var emptyState: some View {
        ContentUnavailableView(
            "No sessions yet",
            systemImage: "chart.bar",
            description: Text("Complete a focus session to see history and charts here.")
        )
    }

    private var statsHeader: some View {
        Section {
            HStack {
                StatCard(title: "Focus min", value: "\(viewModel.totalFocusMinutes)", icon: "clock.fill")
                StatCard(title: "Streak", value: "\(viewModel.streakDays)d", icon: "flame.fill")
            }
            .listRowInsets(EdgeInsets(top: 12, leading: 0, bottom: 12, trailing: 0))
            .listRowBackground(Color.clear)
        }
    }

    private var chartSection: some View {
        Section("Last 14 days") {
            let recent = Array(viewModel.dayStats.suffix(14))
            if recent.isEmpty {
                Text("Not enough data yet.")
                    .foregroundStyle(.secondary)
            } else {
                Chart(recent) { day in
                    BarMark(
                        x: .value("Day", day.date, unit: .day),
                        y: .value("Minutes", day.focusMinutes)
                    )
                    .foregroundStyle(Color.accentColor.gradient)
                    .cornerRadius(4)
                }
                .frame(height: 180)
                .accessibilityLabel("Focus minutes chart for the last two weeks")
            }
        }
    }

    private var sessionSection: some View {
        Section("Sessions") {
            ForEach(viewModel.sessions, id: \.id) { session in
                HStack {
                    Image(systemName: session.phase.systemImage)
                        .foregroundStyle(session.wasCompleted ? Color.accentColor : Color.secondary)
                        .frame(width: 28)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(session.phase.displayName)
                            .font(.body.weight(.medium))
                        Text("\(Formatters.shortDate.string(from: session.completedAt)) · \(Formatters.time.string(from: session.completedAt))")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                    Text(Formatters.duration(seconds: session.actualSeconds))
                        .font(.subheadline.monospacedDigit())
                        .foregroundStyle(.secondary)
                }
                .accessibilityElement(children: .combine)
                .swipeActions {
                    Button(role: .destructive) {
                        viewModel.delete(session)
                    } label: {
                        Label("Delete", systemImage: "trash")
                    }
                }
            }
        }
    }
}

private struct StatCard: View {
    let title: String
    let value: String
    let icon: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(title, systemImage: icon)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.title.bold())
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground)))
    }
}
