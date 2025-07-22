import Foundation
import SwiftUI
import Combine
import CoreLocation

class AppDependencies: ObservableObject {
    let locationService: LocationService
    let weatherService: WeatherService
    let tideService: TideService
    let recommendationService: RecommendationService
    let userPreferencesService: UserPreferencesService
    
    init(
        locationService: LocationService,
        weatherService: WeatherService,
        tideService: TideService,
        recommendationService: RecommendationService,
        userPreferencesService: UserPreferencesService
    ) {
        self.locationService = locationService
        self.weatherService = weatherService
        self.tideService = tideService
        self.recommendationService = recommendationService
        self.userPreferencesService = userPreferencesService
    }
    
    static var preview: AppDependencies {
        AppDependencies(
            locationService: MockLocationService(),
            weatherService: MockWeatherService(),
            tideService: MockTideService(),
            recommendationService: MockRecommendationService(),
            userPreferencesService: MockUserPreferencesService()
        )
    }
    
    static var live: AppDependencies {
        let locationService = LocationServiceImpl()
        let weatherService = CachedWeatherService(
            remoteService: OpenWeatherMapService()
        )
        let tideService = CachedTideService(
            remoteService: WorldTidesService()
        )
        let recommendationService = RecommendationServiceImpl()
        let userPreferencesService = UserPreferencesServiceImpl()
        
        return AppDependencies(
            locationService: locationService,
            weatherService: weatherService,
            tideService: tideService,
            recommendationService: recommendationService,
            userPreferencesService: userPreferencesService
        )
    }
}

// Mock implementations for preview
class MockLocationService: LocationService {
    func requestLocationPermission() {}
    
    func getCurrentLocation() -> AnyPublisher<CLLocation?, Error> {
        Just(nil).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func searchLocations(query: String) -> AnyPublisher<[Location], Error> {
        Just([]).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func getSavedLocations() -> AnyPublisher<[Location], Never> {
        Just([]).eraseToAnyPublisher()
    }
    
    func saveLocation(_ location: Location) -> AnyPublisher<Bool, Error> {
        Just(true).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func deleteLocation(withId id: String) -> AnyPublisher<Bool, Error> {
        Just(true).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    var currentLocation: AnyPublisher<CLLocation?, Never> {
        Just(nil).eraseToAnyPublisher()
    }
    
    var permissionStatus: AnyPublisher<LocationPermissionStatus, Never> {
        Just(.authorized).eraseToAnyPublisher()
    }
}

class MockWeatherService: WeatherService {
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, Error> {
        Just(WeatherData.mockData()).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], Error> {
        Just([WeatherData.mockData()]).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func getWeather(for location: Location, at date: Date) -> AnyPublisher<WeatherData, Error> {
        Just(WeatherData.mockData()).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
}

class MockTideService: TideService {
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error> {
        Just(TideData.mockData()).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error> {
        Just([TideData.mockData()]).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
    
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error> {
        Just(TideData.mockData()).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
}

class MockRecommendationService: RecommendationService {
    func getRecommendations(
        weatherData: WeatherData,
        tideData: TideData,
        fishSpecies: FishSpecies?,
        userPreferences: UserPreferences?
    ) -> [EquipmentRecommendation] {
        return EquipmentRecommendation.mockRecommendations()
    }
}

class MockUserPreferencesService: UserPreferencesService {
    func getUserPreferences() -> AnyPublisher<UserPreferences?, Never> {
        Just(nil).eraseToAnyPublisher()
    }
    
    func updateUserPreferences(_ preferences: UserPreferences) -> AnyPublisher<Bool, Error> {
        Just(true).setFailureType(to: Error.self).eraseToAnyPublisher()
    }
}