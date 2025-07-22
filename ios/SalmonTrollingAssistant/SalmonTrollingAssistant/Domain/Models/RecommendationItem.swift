import SwiftUI

struct RecommendationItem: Identifiable {
    let id = UUID()
    let name: String
    let description: String
    let matchScore: Int
    let isInUserGear: Bool
    let gearType: FishingGear.GearType
    let icon: String
    let reasons: [String]
}

struct RecommendationCategory: Identifiable {
    let id = UUID()
    let name: String
    let items: [RecommendationItem]
}

enum RecommendationMode {
    case optimal
    case fromYourGear
}