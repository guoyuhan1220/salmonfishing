import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0
    
    // Mock data
    private let location = Location(id: "1", name: "Puget Sound", latitude: 47.6062, longitude: -122.3321, isSaved: true)
    private let weather = WeatherData.mockData()
    private let tide = TideData.mockData()
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Weather & Tide Tab
            WeatherTideView(location: location, weather: weather, tide: tide)
                .tabItem {
                    Label("Weather & Tide", systemImage: "cloud.sun.fill")
                }
                .tag(0)
            
            // Map Tab
            Text("Map View")
                .font(.largeTitle)
                .tabItem {
                    Label("Map", systemImage: "map.fill")
                }
                .tag(1)
            
            // Recommendations Tab
            Text("Recommendations")
                .font(.largeTitle)
                .tabItem {
                    Label("Recommendations", systemImage: "list.bullet")
                }
                .tag(2)
            
            // Settings Tab
            Text("Settings")
                .font(.largeTitle)
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
                .tag(3)
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}