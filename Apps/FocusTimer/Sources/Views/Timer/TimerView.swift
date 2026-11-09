import SwiftUI

struct TimerView: View {
    @Bindable var viewModel: TimerViewModel

    var body: some View {
        NavigationStack {
            ZStack {
                backgroundGradient
                VStack(spacing: 32) {
                    phaseBadge
                    ring
                    controls
                    todaySummary
                }
                .padding()
            }
            .navigationTitle("Focus Timer")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var backgroundGradient: some View {
        LinearGradient(
            colors: [
                Color.accentColor.opacity(viewModel.phase == .focus ? 0.18 : 0.10),
                Color(.systemBackground)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .ignoresSafeArea()
        .animation(.easeInOut(duration: 0.4), value: viewModel.phase)
    }

    private var phaseBadge: some View {
        Label(viewModel.phase.displayName, systemImage: viewModel.phase.systemImage)
            .font(.headline)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(.ultraThinMaterial, in: Capsule())
            .accessibilityLabel("Current phase: \(viewModel.phase.displayName)")
    }

    private var ring: some View {
        ZStack {
            Circle()
                .stroke(Color.secondary.opacity(0.2), lineWidth: 16)
            Circle()
                .trim(from: 0, to: viewModel.progress)
                .stroke(
                    AngularGradient(
                        colors: [.accentColor, .accentColor.opacity(0.5), .accentColor],
                        center: .center
                    ),
                    style: StrokeStyle(lineWidth: 16, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .animation(.linear(duration: 0.9), value: viewModel.progress)

            VStack(spacing: 6) {
                Text(viewModel.timeDisplay)
                    .font(.system(size: 56, weight: .semibold, design: .rounded))
                    .monospacedDigit()
                    .contentTransition(.numericText())
                    .accessibilityLabel("Time remaining \(viewModel.timeDisplay)")
                Text(viewModel.isRunning ? "Running" : "Paused")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: 260, height: 260)
        .padding(.vertical, 12)
    }

    private var controls: some View {
        HStack(spacing: 28) {
            Button {
                viewModel.reset()
            } label: {
                Image(systemName: "arrow.counterclockwise")
                    .font(.title2)
                    .frame(width: 56, height: 56)
                    .background(Circle().fill(Color.secondary.opacity(0.15)))
            }
            .accessibilityLabel("Reset timer")

            Button {
                viewModel.toggle()
            } label: {
                Image(systemName: viewModel.isRunning ? "pause.fill" : "play.fill")
                    .font(.title)
                    .foregroundStyle(.white)
                    .frame(width: 80, height: 80)
                    .background(Circle().fill(Color.accentColor))
                    .shadow(color: Color.accentColor.opacity(0.4), radius: 12, y: 6)
            }
            .accessibilityLabel(viewModel.isRunning ? "Pause" : "Start")

            Button {
                viewModel.skip()
            } label: {
                Image(systemName: "forward.end.fill")
                    .font(.title2)
                    .frame(width: 56, height: 56)
                    .background(Circle().fill(Color.secondary.opacity(0.15)))
            }
            .accessibilityLabel("Skip to next phase")
        }
        .buttonStyle(.plain)
    }

    private var todaySummary: some View {
        HStack {
            Image(systemName: "checkmark.circle.fill")
                .foregroundStyle(.green)
            Text("\(viewModel.completedFocusCount) focus sessions today")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .accessibilityElement(children: .combine)
    }
}
