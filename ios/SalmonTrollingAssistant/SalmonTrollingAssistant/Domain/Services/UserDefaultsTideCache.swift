import Foundation

/**
 * A cache implementation for tide data using UserDefaults
 */
class UserDefaultsTideCache {
    let userDefaults = UserDefaults.standard
    
    // Keys
    let currentTideKey = "current_tide"
    let tideForecastKey = "tide_forecast"
    let currentTideTimestampKey = "current_tide_timestamp"
    let tideForecastTimestampKey = "tide_forecast_timestamp"
    
    // Cache expiration times (in seconds)
    let currentTideExpiration: TimeInterval = 30 * 60 // 30 minutes
    let tideForecastExpiration: TimeInterval = 6 * 60 * 60 // 6 hours (tides change less frequently than weather)
    
    /**
     * Get current tide for a location from cache
     */
    func getCurrentTide(for location: Location) -> TideData? {
        let key = "\(currentTideKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else {
            return nil
        }
        
        do {
            let tideData = try JSONDecoder().decode(TideData.self, from: data)
            return tideData
        } catch {
            print("Error decoding cached tide data: \(error)")
            return nil
        }
    }
    
    /**
     * Cache current tide for a location
     */
    func cacheCurrentTide(_ tideData: TideData, for location: Location) {
        do {
            let data = try JSONEncoder().encode(tideData)
            let key = "\(currentTideKey)_\(location.id)"
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: "\(currentTideTimestampKey)_\(location.id)")
        } catch {
            print("Error encoding tide data for cache: \(error)")
        }
    }
    
    /**
     * Check if current tide cache is expired
     */
    func isCurrentTideExpired(for location: Location) -> Bool {
        let timestampKey = "\(currentTideTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let now = Date().timeIntervalSince1970
        return (now - timestamp) > currentTideExpiration
    }
    
    /**
     * Get tide forecast for a location from cache
     */
    func getTideForecast(for location: Location, days: Int = 7) -> [TideData] {
        let key = "\(tideForecastKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else {
            return []
        }
        
        do {
            let forecast = try JSONDecoder().decode([TideData].self, from: data)
            return Array(forecast.prefix(days))
        } catch {
            print("Error decoding cached tide forecast data: \(error)")
            return []
        }
    }
    
    /**
     * Cache tide forecast for a location
     */
    func cacheTideForecast(_ forecast: [TideData], for location: Location) {
        do {
            let data = try JSONEncoder().encode(forecast)
            let key = "\(tideForecastKey)_\(location.id)"
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: "\(tideForecastTimestampKey)_\(location.id)")
        } catch {
            print("Error encoding tide forecast data for cache: \(error)")
        }
    }
    
    /**
     * Check if tide forecast cache is expired
     */
    func isTideForecastExpired(for location: Location) -> Bool {
        let timestampKey = "\(tideForecastTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let now = Date().timeIntervalSince1970
        return (now - timestamp) > tideForecastExpiration
    }
    
    /**
     * Clear all cached tide data
     */
    func clearCache() {
        let allKeys = userDefaults.dictionaryRepresentation().keys
        
        for key in allKeys {
            if key.hasPrefix(currentTideKey) || 
               key.hasPrefix(tideForecastKey) || 
               key.hasPrefix(currentTideTimestampKey) || 
               key.hasPrefix(tideForecastTimestampKey) {
                userDefaults.removeObject(forKey: key)
            }
        }
    }
}