import Foundation
import XCTest

// This would be in a test target in a real project
// For now, we'll include it in the main target for demonstration purposes
class RecommendationServiceTests {
    
    func testRecommendationService() {
        let service = RuleBasedRecommendationService()
        
        // Create test data
        let weatherData = WeatherData(
            timestamp: Date(),
            temperature: 18.0,
            windSpeed: 10.0,
            windDirection: "NW",
            precipitation: 0.0,
            cloudCover: 20,
            visibility: 8.0,
            pressure: 1013.0,
            humidity: 65,
            uvIndex: 6,
            waterTemperature: 15.0
        )
        
        let tideData = TideData(
            timestamp: Date(),
            height: 2.5,
            type: .rising,
            nextHighTide: TideEvent(timestamp: Date().addingTimeInterval(3600), height: 3.2),
            nextLowTide: TideEvent(timestamp: Date().addingTimeInterval(21600), height: 0.5)
        )
        
        // Get recommendations
        let recommendations = service.getRecommendations(
            weatherData: weatherData,
            tideData: tideData,
            fishSpecies: .chinook,
            userEquipment: nil
        )
        
        // Verify recommendations
        assert(recommendations.count == 3, "Should have 3 recommendations (flasher, lure, leader)")
        
        // Check that each equipment type is represented
        let equipmentTypes = recommendations.map { $0.type }
        assert(equipmentTypes.contains(.flasher), "Should have flasher recommendation")
        assert(equipmentTypes.contains(.lure), "Should have lure recommendation")
        assert(equipmentTypes.contains(.leader), "Should have leader recommendation")
        
        // Check that each recommendation has items
        for recommendation in recommendations {
            assert(!recommendation.items.isEmpty, "Recommendation should have items")
            assert(!recommendation.reasonForRecommendation.isEmpty, "Recommendation should have explanation")
            assert(recommendation.confidenceScore >= 0.0 && recommendation.confidenceScore <= 1.0, "Confidence score should be between 0 and 1")
        }
        
        print("All recommendation service tests passed!")
    }
    
    func runTests() {
        testRecommendationService()
    }
}

// Uncomment to run tests manually
// let tests = RecommendationServiceTests()
// tests.runTests()