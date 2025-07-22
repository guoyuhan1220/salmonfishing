import Foundation
import Combine

class CachedWeatherService: WeatherService {
    private let weatherService: WeatherService
    private let cache: WeatherCache
    
    init(weatherService: WeatherService, cache: WeatherCache = UserDefaultsWeatherCache()) {
        self.weatherService = weatherService
        self.cache = cache
    }
    
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, WeatherError> {
        // Check if we have a valid cached current weather
        if let cachedWeather = cache.getCurrentWeather(for: location), !cache.isCurrentWeatherExpired(for: location) {
            return Just(cachedWeather)
                .setFailureType(to: WeatherError.self)
                .eraseToAnyPublisher()
        }
        
        // If no valid cache, fetch from service and cache the result
        return weatherService.getCurrentWeather(for: location)
            .handleEvents(receiveOutput: { [weak self] weatherData in
                self?.cache.saveCurrentWeather(weatherData, for: location)
            })
            .eraseToAnyPublisher()
    }
    
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], WeatherError> {
        // Check if we have a valid cached forecast
        if let cachedForecast = cache.getForecast(for: location, days: days), !cache.isForecastExpired(for: location) {
            return Just(cachedForecast)
                .setFailureType(to: WeatherError.self)
                .eraseToAnyPublisher()
        }
        
        // If no valid cache, fetch from service and cache the result
        return weatherService.getForecast(for: location, days: days)
            .handleEvents(receiveOutput: { [weak self] forecast in
                self?.cache.saveForecast(forecast, for: location)
            })
            .eraseToAnyPublisher()
    }
    
    func getWeather(for location: Location, at dateTime: Date) -> AnyPublisher<WeatherData, WeatherError> {
        // Check if we have a valid cached forecast that contains the requested date/time
        if let cachedForecast = cache.getForecast(for: location, days: 7), !cache.isForecastExpired(for: location) {
            // Find the forecast closest to the requested date/time
            let calendar = Calendar.current
            if let matchingForecast = cachedForecast.first(where: { calendar.isDate($0.timestamp, inSameDayAs: dateTime) }) {
                return Just(matchingForecast)
                    .setFailureType(to: WeatherError.self)
                    .eraseToAnyPublisher()
            }
        }
        
        // If no valid cache, fetch from service
        return weatherService.getWeather(for: location, at: dateTime)
            .eraseToAnyPublisher()
    }
}

// MARK: - Weather Cache Protocol

protocol WeatherCache {
    func getCurrentWeather(for location: Location) -> WeatherData?
    func getForecast(for location: Location, days: Int) -> [WeatherData]?
    func saveCurrentWeather(_ weather: WeatherData, for location: Location)
    func saveForecast(_ forecast: [WeatherData], for location: Location)
    func isCurrentWeatherExpired(for location: Location) -> Bool
    func isForecastExpired(for location: Location) -> Bool
    func clearCache()
}

// MARK: - UserDefaults Implementation

class UserDefaultsWeatherCache: WeatherCache {
    private let userDefaults = UserDefaults.standard
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()
    
    // Cache keys
    private let currentWeatherKey = "cached_current_weather"
    private let forecastKey = "cached_forecast"
    private let currentWeatherTimestampKey = "cached_current_weather_timestamp"
    private let forecastTimestampKey = "cached_forecast_timestamp"
    
    // Cache expiration times (in seconds)
    private let currentWeatherExpiration: TimeInterval = 30 * 60 // 30 minutes
    private let forecastExpiration: TimeInterval = 3 * 60 * 60 // 3 hours
    
    func getCurrentWeather(for location: Location) -> WeatherData? {
        let key = "\(currentWeatherKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else { return nil }
        
        do {
            return try decoder.decode(WeatherData.self, from: data)
        } catch {
            print("Error decoding cached weather: \(error)")
            return nil
        }
    }
    
    func getForecast(for location: Location, days: Int) -> [WeatherData]? {
        let key = "\(forecastKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else { return nil }
        
        do {
            let forecast = try decoder.decode([WeatherData].self, from: data)
            return Array(forecast.prefix(days))
        } catch {
            print("Error decoding cached forecast: \(error)")
            return nil
        }
    }
    
    func saveCurrentWeather(_ weather: WeatherData, for location: Location) {
        let key = "\(currentWeatherKey)_\(location.id)"
        let timestampKey = "\(currentWeatherTimestampKey)_\(location.id)"
        
        do {
            let data = try encoder.encode(weather)
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: timestampKey)
        } catch {
            print("Error encoding weather for cache: \(error)")
        }
    }
    
    func saveForecast(_ forecast: [WeatherData], for location: Location) {
        let key = "\(forecastKey)_\(location.id)"
        let timestampKey = "\(forecastTimestampKey)_\(location.id)"
        
        do {
            let data = try encoder.encode(forecast)
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: timestampKey)
        } catch {
            print("Error encoding forecast for cache: \(error)")
        }
    }
    
    func isCurrentWeatherExpired(for location: Location) -> Bool {
        let timestampKey = "\(currentWeatherTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let cacheDate = Date(timeIntervalSince1970: timestamp)
        return Date().timeIntervalSince(cacheDate) > currentWeatherExpiration
    }
    
    func isForecastExpired(for location: Location) -> Bool {
        let timestampKey = "\(forecastTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let cacheDate = Date(timeIntervalSince1970: timestamp)
        return Date().timeIntervalSince(cacheDate) > forecastExpiration
    }
    
    func clearCache() {
        // Find all keys related to weather caching
        let allKeys = userDefaults.dictionaryRepresentation().keys
        let weatherKeys = allKeys.filter { key in
            key.hasPrefix(currentWeatherKey) || 
            key.hasPrefix(forecastKey) || 
            key.hasPrefix(currentWeatherTimestampKey) || 
            key.hasPrefix(forecastTimestampKey)
        }
        
        // Remove all weather cache keys
        for key in weatherKeys {
            userDefaults.removeObject(forKey: key)
        }
    }
}