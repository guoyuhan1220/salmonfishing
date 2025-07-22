import Foundation

/**
 * A cache implementation for weather data using UserDefaults
 */
class UserDefaultsWeatherCache {
    let userDefaults = UserDefaults.standard
    
    // Keys
    let currentWeatherKey = "current_weather"
    let forecastKey = "weather_forecast"
    let currentWeatherTimestampKey = "current_weather_timestamp"
    let forecastTimestampKey = "forecast_timestamp"
    
    // Cache expiration times (in seconds)
    let currentWeatherExpiration: TimeInterval = 30 * 60 // 30 minutes
    let forecastExpiration: TimeInterval = 3 * 60 * 60 // 3 hours
    
    /**
     * Get current weather for a location from cache
     */
    func getCurrentWeather(for location: Location) -> WeatherData? {
        let key = "\(currentWeatherKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else {
            return nil
        }
        
        do {
            let weatherData = try JSONDecoder().decode(WeatherData.self, from: data)
            return weatherData
        } catch {
            print("Error decoding cached weather data: \(error)")
            return nil
        }
    }
    
    /**
     * Cache current weather for a location
     */
    func cacheCurrentWeather(_ weatherData: WeatherData, for location: Location) {
        do {
            let data = try JSONEncoder().encode(weatherData)
            let key = "\(currentWeatherKey)_\(location.id)"
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: "\(currentWeatherTimestampKey)_\(location.id)")
        } catch {
            print("Error encoding weather data for cache: \(error)")
        }
    }
    
    /**
     * Check if current weather cache is expired
     */
    func isCurrentWeatherExpired(for location: Location) -> Bool {
        let timestampKey = "\(currentWeatherTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let now = Date().timeIntervalSince1970
        return (now - timestamp) > currentWeatherExpiration
    }
    
    /**
     * Get weather forecast for a location from cache
     */
    func getForecast(for location: Location, days: Int = 7) -> [WeatherData] {
        let key = "\(forecastKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else {
            return []
        }
        
        do {
            let forecast = try JSONDecoder().decode([WeatherData].self, from: data)
            return Array(forecast.prefix(days))
        } catch {
            print("Error decoding cached forecast data: \(error)")
            return []
        }
    }
    
    /**
     * Cache weather forecast for a location
     */
    func cacheForecast(_ forecast: [WeatherData], for location: Location) {
        do {
            let data = try JSONEncoder().encode(forecast)
            let key = "\(forecastKey)_\(location.id)"
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: "\(forecastTimestampKey)_\(location.id)")
        } catch {
            print("Error encoding forecast data for cache: \(error)")
        }
    }
    
    /**
     * Check if forecast cache is expired
     */
    func isForecastExpired(for location: Location) -> Bool {
        let timestampKey = "\(forecastTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let now = Date().timeIntervalSince1970
        return (now - timestamp) > forecastExpiration
    }
    
    /**
     * Clear all cached weather data
     */
    func clearCache() {
        let allKeys = userDefaults.dictionaryRepresentation().keys
        
        for key in allKeys {
            if key.hasPrefix(currentWeatherKey) || 
               key.hasPrefix(forecastKey) || 
               key.hasPrefix(currentWeatherTimestampKey) || 
               key.hasPrefix(forecastTimestampKey) {
                userDefaults.removeObject(forKey: key)
            }
        }
    }
}