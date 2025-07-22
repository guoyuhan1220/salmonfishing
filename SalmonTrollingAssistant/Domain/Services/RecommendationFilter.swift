import Foundation

// Protocol for recommendation filtering
protocol RecommendationFilter {
    func filterBySpecies(recommendations: [EquipmentRecommendation], species: FishSpecies) -> [EquipmentRecommendation]
    func filterByWaterClarity(recommendations: [EquipmentRecommendation], clarity: WaterClarity) -> [EquipmentRecommendation]
    func prioritizeUserPreferences(recommendations: [EquipmentRecommendation], userEquipment: [UserEquipment]) -> [EquipmentRecommendation]
}

class RecommendationFilterImpl: RecommendationFilter {
    private let equipmentDatabase: EquipmentDatabase
    
    init(equipmentDatabase: EquipmentDatabase? = nil) {
        self.equipmentDatabase = equipmentDatabase ?? EquipmentDatabaseFactory.getDatabase()
    }
    
    func filterBySpecies(recommendations: [EquipmentRecommendation], species: FishSpecies) -> [EquipmentRecommendation] {
        return recommendations.map { recommendation in
            // Filter items in each recommendation by species
            let filteredItems = recommendation.items.filter { item in
                guard let targetSpecies = item.targetSpecies else { return true }
                return targetSpecies.contains(species)
            }
            
            // If no items match, keep original items
            let items = filteredItems.isEmpty ? recommendation.items : filteredItems
            
            // Create new recommendation with filtered items
            return EquipmentRecommendation(
                id: recommendation.id,
                type: recommendation.type,
                items: items,
                reasonForRecommendation: recommendation.reasonForRecommendation + " Filtered for \(species.rawValue) salmon.",
                confidenceScore: calculateConfidenceScore(filteredItems: items, originalItems: recommendation.items, recommendation: recommendation)
            )
        }
    }
    
    func filterByWaterClarity(recommendations: [EquipmentRecommendation], clarity: WaterClarity) -> [EquipmentRecommendation] {
        return recommendations.map { recommendation in
            // Filter items in each recommendation by water clarity
            let filteredItems = recommendation.items.filter { item in
                guard let waterClarityConditions = item.waterClarityConditions else { return true }
                return waterClarityConditions.contains(clarity.rawValue.lowercased())
            }
            
            // If no items match, keep original items
            let items = filteredItems.isEmpty ? recommendation.items : filteredItems
            
            // Create new recommendation with filtered items
            return EquipmentRecommendation(
                id: recommendation.id,
                type: recommendation.type,
                items: items,
                reasonForRecommendation: recommendation.reasonForRecommendation + " Optimized for \(clarity.rawValue.lowercased()) water clarity.",
                confidenceScore: calculateConfidenceScore(filteredItems: items, originalItems: recommendation.items, recommendation: recommendation)
            )
        }
    }
    
    func prioritizeUserPreferences(recommendations: [EquipmentRecommendation], userEquipment: [UserEquipment]) -> [EquipmentRecommendation] {
        return recommendations.map { recommendation in
            // Find user equipment of the same type
            let userEquipmentIds = userEquipment.map { $0.equipmentId }
            
            // Filter items that are in user's equipment
            let userItems = recommendation.items.filter { item in
                userEquipmentIds.contains(item.id)
            }
            
            // If user has equipment of this type, prioritize it
            let items = userItems.isEmpty ? recommendation.items : (userItems + recommendation.items.filter { !userItems.contains($0) })
            
            // Create new recommendation with prioritized items
            return EquipmentRecommendation(
                id: recommendation.id,
                type: recommendation.type,
                items: items,
                reasonForRecommendation: userItems.isEmpty ? recommendation.reasonForRecommendation : recommendation.reasonForRecommendation + " Prioritized based on your equipment preferences.",
                confidenceScore: userItems.isEmpty ? recommendation.confidenceScore : min(0.95, recommendation.confidenceScore + 0.1)
            )
        }
    }
    
    // Helper method to calculate confidence score after filtering
    private func calculateConfidenceScore(filteredItems: [EquipmentItem], originalItems: [EquipmentItem], recommendation: EquipmentRecommendation) -> Float {
        if filteredItems.count == originalItems.count {
            // No filtering occurred
            return recommendation.confidenceScore
        }
        
        if filteredItems.isEmpty {
            // All items were filtered out, which means low confidence
            return max(0.3, recommendation.confidenceScore - 0.2)
        }
        
        // More specific recommendations (fewer items) have higher confidence
        let specificityIncrease = Float(originalItems.count - filteredItems.count) / Float(originalItems.count) * 0.2
        
        // Increase confidence due to more specific filtering, but cap at 0.95
        return min(0.95, recommendation.confidenceScore + specificityIncrease)
    }
}