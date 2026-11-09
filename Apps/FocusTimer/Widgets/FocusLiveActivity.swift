import ActivityKit
import WidgetKit
import SwiftUI

struct FocusLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: FocusActivityAttributes.self) { context in
            HStack {
                Label(context.state.phase.displayName, systemImage: context.state.phase.systemImage)
                Spacer()
                if context.state.isRunning {
                    Text(context.state.endDate, style: .timer)
                        .monospacedDigit()
                        .font(.title2.weight(.semibold))
                } else {
                    Text("Paused")
                        .foregroundStyle(.secondary)
                }
            }
            .padding()
            .activityBackgroundTint(Color.accentColor.opacity(0.2))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Label(context.state.phase.displayName, systemImage: context.state.phase.systemImage)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(context.state.endDate, style: .timer)
                        .monospacedDigit()
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text(context.attributes.sessionLabel)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            } compactLeading: {
                Image(systemName: context.state.phase.systemImage)
            } compactTrailing: {
                Text(context.state.endDate, style: .timer)
                    .monospacedDigit()
                    .frame(minWidth: 48)
            } minimal: {
                Image(systemName: "timer")
            }
        }
    }
}
