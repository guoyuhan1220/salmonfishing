import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Home", systemImage: "house")
                }
            
            RecommendationsView()
                .tabItem {
                    Label("Recommendations", systemImage: "list.bullet")
                }
            
            WeatherTideView()
                .tabItem {
                    Label("Weather & Tide", systemImage: "cloud.sun")
                }
            
            LocationsView()
                .tabItem {
                    Label("Locations", systemImage: "mappin.and.ellipse")
                }
            
            ProfileView()
                .tabItem {
                    Label("Profile", systemImage: "person")
                }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}