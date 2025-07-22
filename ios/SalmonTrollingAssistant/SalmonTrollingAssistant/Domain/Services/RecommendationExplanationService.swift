import Foundation

// Protocol for recommendation explanation service
protocol RecommendationExplanationService {
    func generateDetailedExplanation(recommendation: EquipmentRecommendation, weatherData: WeatherData, tideData: TideData) -> String
    func generateConfidenceDescription(confidenceScore: Float) -> String
    func getConfidenceColor(confidenceScore: Float) -> String // Returns color name that can be used in UI
}

class RecommendationExplanationServiceImpl: RecommendationExplanationService {
    
    func generateDetailedExplanation(recommendation: EquipmentRecommendation, weatherData: WeatherData, tideData: TideData) -> String {
        var explanation = recommendation.reasonForRecommendation
        
        // Add more detailed explanation based on equipment type
        switch recommendation.type {
        case .flasher:
            explanation += generateFlasherDetailedExplanation(weatherData: weatherData, tideData: tideData)
        case .lure:
            explanation += generateLureDetailedExplanation(weatherData: weatherData, tideData: tideData)
        case .leader:
            explanation += generateLeaderDetailedExplanation(weatherData: weatherData, tideData: tideData)
        }
        
        // Add specific equipment details if there are recommended items
        if !recommendation.items.isEmpty {
            explanation += "\n\nRecommended \(recommendation.type.rawValue.lowercased())s include:"
            
            for item in recommendation.items.prefix(3) { // Limit to top 3 recommendations
                explanation += "\n• \(item.name): \(item.description)"
                
                // Add key specifications based on equipment type
                switch recommendation.type {
                case .flasher:
                    if let size = item.size, let color = item.color {
                        explanation += " (\(size), \(color))"
                    }
                case .lure:
                    if let size = item.size, let color = item.color {
                        explanation += " (\(size), \(color))"
                    }
                case .leader:
                    if let length = item.length, let material = item.material, let weight = item.weight {
                        explanation += " (\(length), \(material), \(weight))"
                    }
                }
            }
            
            // Indicate if there are more recommendations
            if recommendation.items.count > 3 {
                explanation += "\n• ...and \(recommendation.items.count - 3) more options"
            }
        }
        
        return explanation
    }
    
    func generateConfidenceDescription(confidenceScore: Float) -> String {
        if confidenceScore >= 0.8 {
            return "High confidence recommendation based on current conditions"
        } else if confidenceScore >= 0.6 {
            return "Medium-high confidence recommendation"
        } else if confidenceScore >= 0.4 {
            return "Medium confidence recommendation"
        } else if confidenceScore >= 0.2 {
            return "Low-medium confidence recommendation"
        } else {
            return "Low confidence recommendation - consider trying different options"
        }
    }
    
    func getConfidenceColor(confidenceScore: Float) -> String {
        if confidenceScore >= 0.8 {
            return "green" // High confidence
        } else if confidenceScore >= 0.6 {
            return "lightGreen" // Medium-high confidence
        } else if confidenceScore >= 0.4 {
            return "yellow" // Medium confidence
        } else if confidenceScore >= 0.2 {
            return "orange" // Low-medium confidence
        } else {
            return "red" // Low confidence
        }
    }
    
    // Helper methods for generating detailed explanations
    private func generateFlasherDetailedExplanation(weatherData: WeatherData, tideData: TideData) -> String {
        var explanation = "\n\nFlashers create visual attraction through reflection and movement in the water. "
        
        // Water clarity explanation
        let waterClarity = WaterClarity.fromVisibility(weatherData.visibility)
        switch waterClarity {
        case .clear:
            explanation += "In clear water conditions (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), fish can see further, so more subtle flashers with natural colors work well. "
        case .medium:
            explanation += "In medium water clarity (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), moderately bright flashers provide good visibility without being too aggressive. "
        case .murky:
            explanation += "In murky water conditions (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), bright, high-contrast flashers help fish locate your lure from further away. "
        }
        
        // Light conditions explanation
        let lightCondition = LightCondition.fromWeatherData(weatherData)
        switch lightCondition {
        case .bright:
            explanation += "With bright light conditions (cloud cover: \(weatherData.cloudCover)%), UV enhanced flashers will reflect more light and create more flash. "
        case .overcast:
            explanation += "Under overcast conditions (cloud cover: \(weatherData.cloudCover)%), glow or chrome flashers provide better visibility. "
        case .lowLight:
            explanation += "In low light conditions, glow flashers will be most visible to fish. "
        }
        
        // Tide explanation
        switch tideData.type {
        case .high, .rising:
            explanation += "During \(tideData.type.rawValue.lowercased()) tide (height: \(String(format: "%.1f", tideData.height)) meters), larger flashers create more attraction in deeper water. "
        case .low, .falling:
            explanation += "During \(tideData.type.rawValue.lowercased()) tide (height: \(String(format: "%.1f", tideData.height)) meters), smaller flashers with less drag work better in shallower water. "
        }
        
        return explanation
    }
    
    private func generateLureDetailedExplanation(weatherData: WeatherData, tideData: TideData) -> String {
        var explanation = "\n\nLures mimic prey fish and attract salmon through their appearance and action. "
        
        // Water clarity explanation
        let waterClarity = WaterClarity.fromVisibility(weatherData.visibility)
        switch waterClarity {
        case .clear:
            explanation += "In clear water conditions (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), smaller, more natural colored lures that closely resemble actual prey fish are most effective. "
        case .medium:
            explanation += "In medium water clarity (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), medium-sized lures with some flash will attract fish without spooking them. "
        case .murky:
            explanation += "In murky water conditions (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), larger, brighter lures create more vibration and visibility to help salmon find them. "
        }
        
        // Light conditions explanation
        let lightCondition = LightCondition.fromWeatherData(weatherData)
        switch lightCondition {
        case .bright:
            explanation += "With bright light conditions (cloud cover: \(weatherData.cloudCover)%), silver and blue colors reflect more light and create attractive flashes. "
        case .overcast:
            explanation += "Under overcast conditions (cloud cover: \(weatherData.cloudCover)%), green and chartreuse colors provide good visibility and contrast. "
        case .lowLight:
            explanation += "In low light conditions, glow or UV enhanced lures will be most visible to fish. "
        }
        
        // Weather condition explanation
        let weatherCondition = WeatherCondition.fromWeatherData(weatherData)
        switch weatherCondition {
        case .calm:
            explanation += "In calm conditions (wind speed: \(String(format: "%.1f", weatherData.windSpeed)) mph), subtle action lures work well as fish can detect minor movements. "
        case .windy:
            explanation += "In windy conditions (wind speed: \(String(format: "%.1f", weatherData.windSpeed)) mph), lures with more action help attract attention in choppy water. "
        case .rainy:
            explanation += "During rainy conditions (precipitation: \(String(format: "%.1f", weatherData.precipitation)) mm), darker lures create better silhouettes against the surface. "
        }
        
        return explanation
    }
    
    private func generateLeaderDetailedExplanation(weatherData: WeatherData, tideData: TideData) -> String {
        var explanation = "\n\nLeaders connect your flasher to your lure and affect how your lure moves in the water. "
        
        // Water clarity explanation
        let waterClarity = WaterClarity.fromVisibility(weatherData.visibility)
        switch waterClarity {
        case .clear:
            explanation += "In clear water conditions (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), fluorocarbon leaders are nearly invisible to fish, making them less likely to spook. "
        case .medium:
            explanation += "In medium water clarity (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), standard monofilament leaders provide a good balance of invisibility and strength. "
        case .murky:
            explanation += "In murky water conditions (visibility: \(String(format: "%.1f", weatherData.visibility)) meters), leader visibility is less important than strength and durability. "
        }
        
        // Tide and weather explanation
        if tideData.type == .high || tideData.type == .rising {
            explanation += "During \(tideData.type.rawValue.lowercased()) tide (height: \(String(format: "%.1f", tideData.height)) meters), longer leaders (36-42 inches) allow for more natural presentation and movement of your lure. "
        } else {
            explanation += "During \(tideData.type.rawValue.lowercased()) tide (height: \(String(format: "%.1f", tideData.height)) meters), shorter leaders (24-30 inches) help maintain control in shallower water. "
        }
        
        let weatherCondition = WeatherCondition.fromWeatherData(weatherData)
        if weatherCondition == .windy || weatherCondition == .rainy {
            explanation += "In rough conditions (wind speed: \(String(format: "%.1f", weatherData.windSpeed)) mph, precipitation: \(String(format: "%.1f", weatherData.precipitation)) mm), heavier leaders (40-60 lb) provide better durability and control. "
        } else {
            explanation += "In calm conditions (wind speed: \(String(format: "%.1f", weatherData.windSpeed)) mph), lighter leaders (20-30 lb) allow for more natural lure action. "
        }
        
        return explanation
    }
}