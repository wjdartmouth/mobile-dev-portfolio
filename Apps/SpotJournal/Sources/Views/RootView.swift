import SwiftUI
import SwiftData

struct RootView: View {
    @StateObject private var locationService = LocationService()
    @State private var listVM = JournalListViewModel()
    @Environment(\.modelContext) private var modelContext
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            JournalListView(viewModel: listVM, locationService: locationService)
                .tabItem { Label("Journal", systemImage: "book.pages") }
                .tag(0)

            JournalMapView(viewModel: listVM)
                .tabItem { Label("Map", systemImage: "map") }
                .tag(1)
        }
        .onAppear {
            listVM.attach(context: modelContext)
            locationService.requestPermission()
        }
    }
}
