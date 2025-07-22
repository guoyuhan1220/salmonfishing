import Foundation

enum FishSpecies: String, Codable, CaseIterable {
    case chinook = "Chinook"
    case coho = "Coho"
    case sockeye = "Sockeye"
    case pink = "Pink"
    case chum = "Chum"
}

struct EquipmentRecommendation: Identifiable {
    let id: String
    let type: EquipmentType
    let items: [EquipmentItem]
    let reasonForRecommendation: String
    let confidenceScore: Float
    
    enum EquipmentType: String, Codable {
        case flasher = "Flasher"
        case lure = "Lure"
        case leader = "Leader"
    }
    
    static func mockRecommendations() -> [EquipmentRecommendation] {
        return [
            EquipmentRecommendation(
                id: UUID().uuidString,
                type: .flasher,
                items: [
                    EquipmentItem(
                        id: "f1",
                        name: "Hot Spot Flasher",
                        description: "11\" UV Green Glow",
                        imageUrl: nil,
                        specifications: ["Size": "11\"", "Color": "UV Green"]
                    ),
                    EquipmentItem(
                        id: "f2",
                        name: "Silver Horde Kingfisher",
                        description: "Chrome with Blue Stripe",
                        imageUrl: nil,
                        specifications: ["Size": "11\"", "Color": "Chrome/Blue"]
                    )
                ],
                reasonForRecommendation: "Bright flashers work well in current water clarity and light conditions",
                confidenceScore: 0.85
            ),
            EquipmentRecommendation(
                id: UUID().uuidString,
                type: .lure,
                items: [
                    EquipmentItem(
                        id: "l1",
                        name: "Coho Killer",
                        description: "Green Pirate",
                        imageUrl: nil,
                        specifications: ["Size": "3.5\"", "Color": "Green/Black"]
                    ),
                    EquipmentItem(
                        id: "l2",
                        name: "Silver Horde Kingfisher Lite",
                        description: "Army Truck",
                        imageUrl: nil,
                        specifications: ["Size": "3.75\"", "Color": "Green/Yellow"]
                    )
                ],
                reasonForRecommendation: "Green patterns match current baitfish and stand out in today's water conditions",
                confidenceScore: 0.78
            ),
            EquipmentRecommendation(
                id: UUID().uuidString,
                type: .leader,
                items: [
                    EquipmentItem(
                        id: "ld1",
                        name: "Fluorocarbon Leader",
                        description: "40lb test, 42-48\" length",
                        imageUrl: nil,
                        specifications: ["Material": "Fluorocarbon", "Test": "40lb", "Length": "42-48\""]
                    )
                ],
                reasonForRecommendation: "Longer leader recommended due to clear water conditions and cautious fish",
                confidenceScore: 0.92
            )
        ]
    }
}

struct EquipmentItem: Identifiable {
    let id: String
    let name: String
    let description: String
    let imageUrl: String?
    let specifications: [String: String]
}

protocol RecommendationService {
    func getRecommendations(
        weatherData: WeatherData,
        tideData: TideData,
        fishSpecies: FishSpecies?,
        userPreferences: UserPreferences?
    ) -> [EquipmentRecommendation]
}

class RecommendationServiceImpl: RecommendationService {
    func getRecommendations(
        weatherData: WeatherData,
        tideData: TideData,
        fishSpecies: FishSpecies?,
        userPreferences: UserPreferences?
    ) -> [EquipmentRecommendation] {
        // In a real app, this would implement a complex algorithm
        // For now, we'll return mock data
        return EquipmentRecommendation.mockRecommendations()
    }
}