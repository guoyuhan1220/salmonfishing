import SwiftUI
import Combine

class AppState: ObservableObject {
    @Published var userGear: [FishingGear] = []
    @Published var selectedTab: Int = 0
    
    static let shared = AppState()
    
    func switchToTab(_ index: Int) {
        print("AppState: Switching to tab \(index)")
        self.selectedTab = index
    }
}