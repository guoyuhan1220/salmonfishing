import Foundation
import Combine

protocol WeatherService {
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, Error>
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], Error>
    func getWeather(for location: Location, at date: Date) -> AnyPublisher<WeatherData, Error>
}

class OpenWeatherMapService: WeatherService {
    private let apiKey = "YOUR_API_KEY" // Replace with your actual API key
    private let baseURL = "https://api.openweathermap.org/data/2.5"
    
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, Error> {
        // In a real app, this would make an API call to OpenWeatherMap
        // For now, we'll return mock data
        return Just(WeatherData.mockData())
            .setFailureType(to: Error.self)
            .eraseToAnyPublisher()
    }
    
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], Error> {
        // In a real app, this would make an API call to OpenWeatherMap
        // For now, we'll return mock data
        let calendar = Calendar.current
        let forecast = (0..<days).map { dayOffset -> WeatherData in
            let date = calendar.date(byAdding: .day, value: dayOffset, to: Date())!
            return WeatherData(
                id: UUID().uuidString,
                temperature: 65.0 + Double.random(in: -5...10),
                condition: ["Sunny", "Partly Cloudy", "Cloudy", "Light Rain", "Overcast"].randomElement()!,
                windSpeed: 5.0 + Double.random(in: 0...10),
                windDirection: ["N", "NE", "E", "SE", "S", "SW", "W", "NW"].randomElement()!,
                humidity: Int.random(in: 50...90),
                timestamp: date
            )
        }
        
        return Just(forecast)
            .setFailureType(to: Error.self)
            .eraseToAnyPublisher()
    }
    
    func getWeather(for location: Location, at date: Date) -> AnyPublisher<WeatherData, Error> {
        // In a real app, this would make an API call to OpenWeatherMap
        // For now, we'll return mock data
        return Just(WeatherData(
            id: UUID().uuidString,
            temperature: 65.0 + Double.random(in: -5...10),
            condition: ["Sunny", "Partly Cloudy", "Cloudy", "Light Rain", "Overcast"].randomElement()!,
            windSpeed: 5.0 + Double.random(in: 0...10),
            windDirection: ["N", "NE", "E", "SE", "S", "SW", "W", "NW"].randomElement()!,
            humidity: Int.random(in: 50...90),
            timestamp: date
        ))
        .setFailureType(to: Error.self)
        .eraseToAnyPublisher()
    }
}

class CachedWeatherService: WeatherService {
    private let remoteService: WeatherService
    private var cache: [String: WeatherData] = [:]
    private var forecastCache: [String: [WeatherData]] = [:]
    
    init(remoteService: WeatherService) {
        self.remoteService = remoteService
    }
    
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, Error> {
        let cacheKey = "current_\(location.id)"
        
        // Check if we have cached data that's less than 30 minutes old
        if let cachedData = cache[cacheKey],
           Date().timeIntervalSince(cachedData.timestamp) < 30 * 60 {
            return Just(cachedData)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // Otherwise fetch from remote
        return remoteService.getCurrentWeather(for: location)
            .handleEvents(receiveOutput: { [weak self] weatherData in
                self?.cache[cacheKey] = weatherData
            })
            .eraseToAnyPublisher()
    }
    
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], Error> {
        let cacheKey = "forecast_\(location.id)_\(days)"
        
        // Check if we have cached data that's less than 3 hours old
        if let cachedData = forecastCache[cacheKey],
           let firstItem = cachedData.first,
           Date().timeIntervalSince(firstItem.timestamp) < 3 * 60 * 60 {
            return Just(cachedData)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // Otherwise fetch from remote
        return remoteService.getForecast(for: location, days: days)
            .handleEvents(receiveOutput: { [weak self] forecast in
                self?.forecastCache[cacheKey] = forecast
            })
            .eraseToAnyPublisher()
    }
    
    func getWeather(for location: Location, at date: Date) -> AnyPublisher<WeatherData, Error> {
        let calendar = Calendar.current
        let dateString = "\(calendar.component(.year, from: date))-\(calendar.component(.month, from: date))-\(calendar.component(.day, from: date))"
        let cacheKey = "weather_\(location.id)_\(dateString)"
        
        // Check if we have cached data
        if let cachedData = cache[cacheKey] {
            return Just(cachedData)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // Otherwise fetch from remote
        return remoteService.getWeather(for: location, at: date)
            .handleEvents(receiveOutput: { [weak self] weatherData in
                self?.cache[cacheKey] = weatherData
            })
            .eraseToAnyPublisher()
    }
}