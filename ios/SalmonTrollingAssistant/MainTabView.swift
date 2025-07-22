import SwiftUI

struct MainTabView: View {
    var body: some View {
        TabView {
            Text("Weather & Tide")
                .tabItem {
                    Label("Weather", systemImage: "cloud.sun.fill")
                }
            
            Text("Recommendations")
                .tabItem {
                    Label("Recommendations", systemImage: "list.bullet")
                }
            
            Text("Settings")
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}