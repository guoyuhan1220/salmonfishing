import SwiftUI

// This file serves as a bridge to import the views from separate files
// into the ContentView.swift file

// Re-export the views
public typealias MyGearViewType = MyGearView
public typealias SmartRecommendationsViewType = SmartRecommendationsView

// Supporting models for SmartRecommendationsView
enum RecommendationMode {
    case optimal
    case fromYourGear
}

struct RecommendationCategory: Identifiable {
    let id = UUID()
    let name: String
    let items: [RecommendationItem]
}

struct RecommendationItem: Identifiable {
    let id = UUID()
    let name: String
    let description: String
    let matchScore: Int
    let isInUserGear: Bool
    let gearType: GearType
    let icon: String
    let reasons: [String]
}