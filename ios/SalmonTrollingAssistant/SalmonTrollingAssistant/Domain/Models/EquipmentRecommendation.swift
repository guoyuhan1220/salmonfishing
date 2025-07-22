import Foundation

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