import SwiftUI
import Charts

struct WeatherDashboardView: View {
    @Bindable var viewModel: WeatherViewModel

    var body: some View {
        ScrollView {
            if let bundle = viewModel.bundle {
                VStack(alignment: .leading, spacing: 20) {
                    header(bundle)
                    metrics(bundle)
                    hourly(bundle)
                    daily(bundle)
                    if viewModel.isOfflineCache {
                        Label("Showing cached forecast — pull to refresh when online.", systemImage: "externaldrive")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                    Text("Updated \(bundle.fetchedAt.formatted(date: .abbreviated, time: .shortened)) · Open-Meteo")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
                .padding()
            }
        }
        .refreshable { await viewModel.refresh() }
        .background(background.ignoresSafeArea())
    }

    private func header(_ bundle: WeatherBundle) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(bundle.placeName)
                .font(.title2.weight(.semibold))
            HStack(alignment: .top, spacing: 16) {
                Image(systemName: WeatherCode.symbol(for: bundle.current.weatherCode, isDay: bundle.current.isDay == 1))
                    .symbolRenderingMode(.hierarchical)
                    .font(.system(size: 64))
                    .foregroundStyle(WeatherCode.tint(for: bundle.current.weatherCode))
                    .accessibilityHidden(true)
                VStack(alignment: .leading) {
                    Text(viewModel.displayTemp(bundle.current.temperature))
                        .font(.system(size: 52, weight: .bold, design: .rounded))
                    Text(WeatherCode.description(for: bundle.current.weatherCode))
                        .font(.title3)
                        .foregroundStyle(.secondary)
                    Text("Feels like \(viewModel.displayTemp(bundle.current.apparentTemperature))")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            .accessibilityElement(children: .combine)
        }
    }

    private func metrics(_ bundle: WeatherBundle) -> some View {
        HStack(spacing: 12) {
            MetricTile(icon: "humidity", title: "Humidity", value: "\(bundle.current.relativeHumidity)%")
            MetricTile(icon: "wind", title: "Wind", value: String(format: "%.0f km/h", bundle.current.windSpeed))
        }
    }

    private func hourly(_ bundle: WeatherBundle) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Next hours").font(.headline)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(bundle.nextHours.prefix(12)) { hour in
                        VStack(spacing: 6) {
                            Text(shortHour(hour.time))
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                            Image(systemName: WeatherCode.symbol(for: hour.weatherCode))
                                .foregroundStyle(WeatherCode.tint(for: hour.weatherCode))
                            Text(viewModel.displayTemp(hour.temperature))
                                .font(.caption.weight(.semibold))
                            if let p = hour.precipChance {
                                Text("\(p)%")
                                    .font(.caption2)
                                    .foregroundStyle(.blue)
                            }
                        }
                        .frame(width: 62)
                        .padding(.vertical, 10)
                        .background(RoundedRectangle(cornerRadius: 14).fill(.ultraThinMaterial))
                    }
                }
            }
            Chart(bundle.nextHours.prefix(12)) { hour in
                LineMark(
                    x: .value("Hour", shortHour(hour.time)),
                    y: .value("Temp", hour.temperature)
                )
                .interpolationMethod(.catmullRom)
                .foregroundStyle(Color.accentColor)
                AreaMark(
                    x: .value("Hour", shortHour(hour.time)),
                    y: .value("Temp", hour.temperature)
                )
                .foregroundStyle(Color.accentColor.opacity(0.15))
            }
            .frame(height: 140)
            .accessibilityLabel("Hourly temperature chart")
        }
    }

    private func daily(_ bundle: WeatherBundle) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("7-day forecast").font(.headline)
            ForEach(bundle.dailyForecast) { day in
                HStack {
                    Text(shortDay(day.date))
                        .frame(width: 72, alignment: .leading)
                    Image(systemName: WeatherCode.symbol(for: day.weatherCode))
                        .foregroundStyle(WeatherCode.tint(for: day.weatherCode))
                        .frame(width: 28)
                    Spacer()
                    Text(viewModel.displayTemp(day.tempMin))
                        .foregroundStyle(.secondary)
                        .frame(width: 52, alignment: .trailing)
                    Text(viewModel.displayTemp(day.tempMax))
                        .fontWeight(.semibold)
                        .frame(width: 52, alignment: .trailing)
                }
                .padding(.vertical, 4)
                .accessibilityElement(children: .combine)
            }
        }
        .padding()
        .background(RoundedRectangle(cornerRadius: 16).fill(.ultraThinMaterial))
    }

    private var background: some View {
        LinearGradient(
            colors: [Color.accentColor.opacity(0.25), Color(.systemBackground)],
            startPoint: .topLeading,
            endPoint: .bottom
        )
    }

    private func shortHour(_ iso: String) -> String {
        // "2024-01-01T15:00" → "15:00"
        if let t = iso.split(separator: "T").last { return String(t.prefix(5)) }
        return iso
    }

    private func shortDay(_ iso: String) -> String {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        if let date = f.date(from: iso) {
            let out = DateFormatter()
            out.setLocalizedDateFormatFromTemplate("EEE d")
            return out.string(from: date)
        }
        return iso
    }
}

private struct MetricTile: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Label(title, systemImage: icon)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.title3.weight(.semibold))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(RoundedRectangle(cornerRadius: 14).fill(.ultraThinMaterial))
        .accessibilityElement(children: .combine)
    }
}
