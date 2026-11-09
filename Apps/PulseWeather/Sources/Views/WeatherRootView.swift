import SwiftUI

struct WeatherRootView: View {
    @State private var viewModel = WeatherViewModel()

    var body: some View {
        NavigationStack {
            Group {
                switch viewModel.state {
                case .idle, .loading where viewModel.bundle == nil:
                    ProgressView("Loading weather…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                case .failed(let message) where viewModel.bundle == nil:
                    ContentUnavailableView(
                        "Couldn't load weather",
                        systemImage: "wifi.slash",
                        description: Text(message)
                    )
                    .overlay(alignment: .bottom) {
                        Button("Try again") {
                            Task { await viewModel.refreshUsingLocation() }
                        }
                        .buttonStyle(.borderedProminent)
                        .padding()
                    }
                default:
                    WeatherDashboardView(viewModel: viewModel)
                }
            }
            .navigationTitle("Pulse Weather")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Toggle(isOn: $viewModel.useCelsius) {
                        Text(viewModel.useCelsius ? "°C" : "°F")
                    }
                    .toggleStyle(.button)
                    .accessibilityLabel("Temperature unit")
                }
            }
            .searchable(text: $viewModel.searchQuery, prompt: "Search city")
            .onChange(of: viewModel.searchQuery) { _, _ in
                Task { await viewModel.search() }
            }
            .overlay(alignment: .top) {
                if !viewModel.searchResults.isEmpty {
                    searchDropdown
                }
            }
        }
        .task { await viewModel.bootstrap() }
    }

    private var searchDropdown: some View {
        VStack(spacing: 0) {
            ForEach(viewModel.searchResults) { result in
                Button {
                    Task { await viewModel.select(result) }
                } label: {
                    HStack {
                        Image(systemName: "mappin.circle")
                        Text(result.displayName)
                        Spacer()
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 10)
                }
                Divider()
            }
        }
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal)
        .shadow(radius: 8)
    }
}
