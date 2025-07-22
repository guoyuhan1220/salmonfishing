import SwiftUI

struct SmartRecsView: View {
    let userGear: [FishingGear]
    let selectedLocation: SimpleLocation
    let weatherConditions: String
    let windSpeed: Int
    let tideType: String
    
    var body: some View {
        SmartRecommendationsView(
            userGear: userGear,
            selectedLocation: selectedLocation,
            weatherConditions: weatherConditions,
            windSpeed: windSpeed,
            tideType: tideType
        )
    }
}

struct SmartRecsView_Previews: PreviewProvider {
    static var previews: some View {
        SmartRecsView(
            userGear: FishingGear.mockGear(),
            selectedLocation: SimpleLocation(id: "1", name: "Puget Sound", latitude: 47.6062, longitude: -122.3321),
            weatherConditions: "Partly Cloudy",
            windSpeed: 8,
            tideType: "Rising"
        )
    }
}