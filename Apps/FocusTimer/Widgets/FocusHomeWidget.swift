import WidgetKit
import SwiftUI

struct FocusTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> FocusEntry {
        FocusEntry(date: .now, snapshot: WidgetSnapshot())
    }

    func getSnapshot(in context: Context, completion: @escaping (FocusEntry) -> Void) {
        completion(FocusEntry(date: .now, snapshot: WidgetSnapshot.load()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<FocusEntry>) -> Void) {
        let snap = WidgetSnapshot.load()
        let entry = FocusEntry(date: .now, snapshot: snap)
        let next = Date().addingTimeInterval(60)
        completion(Timeline(entries: [entry], policy: .after(next)))
    }
}

struct FocusEntry: TimelineEntry {
    let date: Date
    let snapshot: WidgetSnapshot
}

struct FocusHomeWidget: Widget {
    let kind = "FocusHomeWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: FocusTimelineProvider()) { entry in
            FocusHomeWidgetView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Focus Timer")
        .description("See the current phase and today's completed focus sessions.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct FocusHomeWidgetView: View {
    let entry: FocusEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(entry.snapshot.phase.displayName, systemImage: entry.snapshot.phase.systemImage)
                .font(.caption.weight(.semibold))
                .foregroundStyle(.secondary)
            Text(timeString(entry.snapshot.remainingSeconds))
                .font(.system(size: 34, weight: .bold, design: .rounded))
                .monospacedDigit()
            HStack {
                Image(systemName: entry.snapshot.isRunning ? "play.fill" : "pause.fill")
                Text("\(entry.snapshot.completedFocusToday) today")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .padding()
    }

    private func timeString(_ seconds: Int) -> String {
        String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }
}
