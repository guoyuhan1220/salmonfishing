import Foundation
import Combine

class CachedTideService: TideService {
    private let tideService: TideService
    private let cache: TideCache
    
    init(tideService: TideService, cache: TideCache = UserDefaultsTideCache()) {
        self.tideService = tideService
        self.cache = cache
    }
    
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error> {
        // Check if we have a valid cached current tide
        if let cachedTide = cache.getCurrentTide(for: location), !cache.isCurrentTideExpired(for: location) {
            return Just(cachedTide)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // If no valid cache, fetch from service and cache the result
        return tideService.getCurrentTide(for: location)
            .handleEvents(receiveOutput: { [weak self] tideData in
                self?.cache.saveCurrentTide(tideData, for: location)
            })
            .catch { error -> AnyPublisher<TideData, Error> in
                // If fetch failed but we have cached data (even if expired), return it
                if let cachedTide = self.cache.getCurrentTide(for: location) {
                    return Just(cachedTide)
                        .setFailureType(to: Error.self)
                        .eraseToAnyPublisher()
                }
                return Fail(error: error).eraseToAnyPublisher()
            }
            .eraseToAnyPublisher()
    }
    
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error> {
        // Check if we have a valid cached forecast
        if let cachedForecast = cache.getTideForecast(for: location, days: days), !cache.isTideForecastExpired(for: location) {
            return Just(cachedForecast)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // If no valid cache, fetch from service and cache the result
        return tideService.getTidePredictions(for: location, days: days)
            .handleEvents(receiveOutput: { [weak self] forecast in
                self?.cache.saveTideForecast(forecast, for: location)
            })
            .catch { error -> AnyPublisher<[TideData], Error> in
                // If fetch failed but we have cached data (even if expired), return it
                if let cachedForecast = self.cache.getTideForecast(for: location, days: days) {
                    return Just(cachedForecast)
                        .setFailureType(to: Error.self)
                        .eraseToAnyPublisher()
                }
                return Fail(error: error).eraseToAnyPublisher()
            }
            .eraseToAnyPublisher()
    }
    
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error> {
        // Check if we have a valid cached forecast that contains the requested date/time
        if let cachedForecast = cache.getTideForecast(for: location, days: 7), !cache.isTideForecastExpired(for: location) {
            // Find the forecast closest to the requested date/time
            let calendar = Calendar.current
            if let matchingForecast = cachedForecast.first(where: { calendar.isDate($0.timestamp, inSameDayAs: dateTime) }) {
                return Just(matchingForecast)
                    .setFailureType(to: Error.self)
                    .eraseToAnyPublisher()
            }
        }
        
        // If no valid cache, fetch from service
        return tideService.getTideForDateTime(for: location, dateTime: dateTime)
            .catch { error -> AnyPublisher<TideData, Error> in
                // If fetch failed but we have any cached forecast, find the closest one
                if let cachedForecast = self.cache.getTideForecast(for: location, days: 7), !cachedForecast.isEmpty {
                    let closest = cachedForecast.min(by: { 
                        abs($0.timestamp.timeIntervalSince(dateTime)) < abs($1.timestamp.timeIntervalSince(dateTime)) 
                    })
                    
                    if let closest = closest {
                        return Just(closest)
                            .setFailureType(to: Error.self)
                            .eraseToAnyPublisher()
                    }
                }
                return Fail(error: error).eraseToAnyPublisher()
            }
            .eraseToAnyPublisher()
    }
    
    func observeTideData(for location: Location) -> AnyPublisher<TideData, Error> {
        // Create a timer that emits every 15 minutes
        return Timer.publish(every: 15 * 60, on: .main, in: .common)
            .autoconnect()
            .flatMap { [weak self] _ -> AnyPublisher<TideData, Error> in
                guard let self = self else {
                    return Fail(error: TideError.unknown).eraseToAnyPublisher()
                }
                return self.getCurrentTide(for: location)
            }
            .eraseToAnyPublisher()
    }
}

// MARK: - Tide Cache Protocol

protocol TideCache {
    func getCurrentTide(for location: Location) -> TideData?
    func getTideForecast(for location: Location, days: Int) -> [TideData]?
    func saveCurrentTide(_ tide: TideData, for location: Location)
    func saveTideForecast(_ forecast: [TideData], for location: Location)
    func isCurrentTideExpired(for location: Location) -> Bool
    func isTideForecastExpired(for location: Location) -> Bool
    func clearCache()
}

// MARK: - UserDefaults Implementation

class UserDefaultsTideCache: TideCache {
    private let userDefaults = UserDefaults.standard
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()
    
    // Cache keys
    private let currentTideKey = "cached_current_tide"
    private let tideForecastKey = "cached_tide_forecast"
    private let currentTideTimestampKey = "cached_current_tide_timestamp"
    private let tideForecastTimestampKey = "cached_tide_forecast_timestamp"
    
    // Cache expiration times (in seconds)
    private let currentTideExpiration: TimeInterval = 30 * 60 // 30 minutes
    private let tideForecastExpiration: TimeInterval = 6 * 60 * 60 // 6 hours (tides change less frequently than weather)
    
    func getCurrentTide(for location: Location) -> TideData? {
        let key = "\(currentTideKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else { return nil }
        
        do {
            return try decoder.decode(TideData.self, from: data)
        } catch {
            print("Error decoding cached tide: \(error)")
            return nil
        }
    }
    
    func getTideForecast(for location: Location, days: Int) -> [TideData]? {
        let key = "\(tideForecastKey)_\(location.id)"
        guard let data = userDefaults.data(forKey: key) else { return nil }
        
        do {
            let forecast = try decoder.decode([TideData].self, from: data)
            return Array(forecast.prefix(days))
        } catch {
            print("Error decoding cached tide forecast: \(error)")
            return nil
        }
    }
    
    func saveCurrentTide(_ tide: TideData, for location: Location) {
        let key = "\(currentTideKey)_\(location.id)"
        let timestampKey = "\(currentTideTimestampKey)_\(location.id)"
        
        do {
            let data = try encoder.encode(tide)
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: timestampKey)
        } catch {
            print("Error encoding tide for cache: \(error)")
        }
    }
    
    func saveTideForecast(_ forecast: [TideData], for location: Location) {
        let key = "\(tideForecastKey)_\(location.id)"
        let timestampKey = "\(tideForecastTimestampKey)_\(location.id)"
        
        do {
            let data = try encoder.encode(forecast)
            userDefaults.set(data, forKey: key)
            userDefaults.set(Date().timeIntervalSince1970, forKey: timestampKey)
        } catch {
            print("Error encoding tide forecast for cache: \(error)")
        }
    }
    
    func isCurrentTideExpired(for location: Location) -> Bool {
        let timestampKey = "\(currentTideTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let cacheDate = Date(timeIntervalSince1970: timestamp)
        return Date().timeIntervalSince(cacheDate) > currentTideExpiration
    }
    
    func isTideForecastExpired(for location: Location) -> Bool {
        let timestampKey = "\(tideForecastTimestampKey)_\(location.id)"
        guard let timestamp = userDefaults.object(forKey: timestampKey) as? TimeInterval else {
            return true
        }
        
        let cacheDate = Date(timeIntervalSince1970: timestamp)
        return Date().timeIntervalSince(cacheDate) > tideForecastExpiration
    }
    
    func clearCache() {
        // Find all keys related to tide caching
        let allKeys = userDefaults.dictionaryRepresentation().keys
        let tideKeys = allKeys.filter { key in
            key.hasPrefix(currentTideKey) || 
            key.hasPrefix(tideForecastKey) || 
            key.hasPrefix(currentTideTimestampKey) || 
            key.hasPrefix(tideForecastTimestampKey)
        }
        
        // Remove all tide cache keys
        for key in tideKeys {
            userDefaults.removeObject(forKey: key)
        }
    }
}