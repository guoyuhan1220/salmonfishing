import Foundation

// Location model
struct Location: Identifiable, Equatable, Hashable {
    let id: String
    let name: String
    let latitude: Double
    let longitude: Double
    
    static func == (lhs: Location, rhs: Location) -> Bool {
        return lhs.id == rhs.id
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

// Weather data model
struct WeatherData: Identifiable, Codable {
    let id: String
    let temperature: Double
    let condition: String
    let windSpeed: Double
    let windDirection: String
    let humidity: Int
    let timestamp: Date
    
    static func mockData() -> WeatherData {
        return WeatherData(
            id: UUID().uuidString,
            temperature: 68.5,
            condition: "Partly Cloudy",
            windSpeed: 8.3,
            windDirection: "NW",
            humidity: 65,
            timestamp: Date()
        )
    }
}

// Tide data model
struct TideData: Identifiable, Codable {
    let id: String
    let height: Double
    let type: TideType
    let timestamp: Date
    
    enum TideType: String, Codable {
        case high = "HIGH"
        case low = "LOW"
        case rising = "RISING"
        case falling = "FALLING"
    }
    
    static func mockData() -> TideData {
        return TideData(
            id: UUID().uuidString,
            height: 3.2,
            type: .rising,
            timestamp: Date()
        )
    }
}

// Equipment recommendation model
struct EquipmentRecommendation: Identifiable {
    let id: String
    let name: String
    let description: String
    let confidence: Int // 0-100
    let category: Category
    
    enum Category: String, CaseIterable {
        case lure = "Lure"
        case rod = "Rod"
        case reel = "Reel"
        case line = "Line"
        case depth = "Depth"
        case speed = "Speed"
    }
    
    static func mockRecommendations() -> [EquipmentRecommendation] {
        return [
            EquipmentRecommendation(
                id: UUID().uuidString,
                name: "Coho Killer Spoon",
                description: "Silver/Blue pattern, 3.5 inch",
                confidence: 85,
                category: .lure
            ),
            EquipmentRecommendation(
                id: UUID().uuidString,
                name: "Medium-Heavy Action Rod",
                description: "8-17 lb test, 7-9 foot",
                confidence: 75,
                category: .rod
            ),
            EquipmentRecommendation(
                id: UUID().uuidString,
                name: "15-20 lb Monofilament Line",
                description: "Clear blue color recommended",
                confidence: 80,
                category: .line
            ),
            EquipmentRecommendation(
                id: UUID().uuidString,
                name: "30-45 feet",
                description: "Based on current water temperature and clarity",
                confidence: 90,
                category: .depth
            ),
            EquipmentRecommendation(
                id: UUID().uuidString,
                name: "1.8-2.2 mph",
                description: "Optimal trolling speed for current conditions",
                confidence: 85,
                category: .speed
            )
        ]
    }
}