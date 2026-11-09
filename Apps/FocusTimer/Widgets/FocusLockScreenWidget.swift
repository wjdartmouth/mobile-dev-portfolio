import WidgetKit
import SwiftUI

struct FocusLockScreenWidget: Widget {
    let kind = "FocusLockScreenWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: FocusTimelineProvider()) { entry in
            FocusLockView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Focus remaining")
        .description("Countdown on the Lock Screen.")
        .supportedFamilies([.accessoryCircular, .accessoryRectangular, .accessoryInline])
    }
}

struct FocusLockView: View {
    @Environment(\.widgetFamily) private var family
    let entry: FocusEntry

    var body: some View {
        switch family {
        case .accessoryCircular:
            Gauge(value: progress) {
                Text(shortTime)
            }
            .gaugeStyle(.accessoryCircularCapacity)
        case .accessoryRectangular:
            VStack(alignment: .leading) {
                Text(entry.snapshot.phase.displayName)
                    .font(.headline)
                Text(shortTime)
                    .font(.title2.monospacedDigit())
            }
        default:
            Text("\(entry.snapshot.phase.displayName) \(shortTime)")
        }
    }

    private var shortTime: String {
        let s = entry.snapshot.remainingSeconds
        return String(format: "%d:%02d", s / 60, s % 60)
    }

    private var progress: Double {
        // Approximate from remaining vs a default 25m when unknown
        min(1, max(0, 1 - Double(entry.snapshot.remainingSeconds) / (25 * 60)))
    }
}
