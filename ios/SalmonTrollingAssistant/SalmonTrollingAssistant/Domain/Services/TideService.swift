import Foundation
import Combine

protocol TideService {
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error>
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error>
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error>
}

class WorldTidesService: TideService {
    private let apiKey = "YOUR_API_KEY" // Replace with your actual API key
    private let baseURL = "https://www.worldtides.info/api/v2"
    
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error> {
        // In a real app, this would make an API call to WorldTides
        // For now, we'll return mock data
        return Just(TideData.mockData())
            .setFailureType(to: Error.self)
            .eraseToAnyPublisher()
    }
    
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error> {
        // In a real app, this would make an API call to WorldTides
        // For now, we'll return mock data
        let calendar = Calendar.current
        let tideTypes: [TideData.TideType] = [.high, .falling, .low, .rising]
        
        let predictions = (0..<days).flatMap { dayOffset -> [TideData] in
            let baseDate = calendar.date(byAdding: .day, value: dayOffset, to: Date())!
            
            return (0..<4).map { tideIndex in
                let hoursToAdd = tideIndex * 6 // Tides roughly every 6 hours
                let timestamp = calendar.date(byAdding: .hour, value: hoursToAdd, to: baseDate)!
                let tideType = tideTypes[tideIndex % tideTypes.count]
                let height = tideType == .high || tideType == .rising ? 
                    Double.random(in: 2.5...4.5) : Double.random(in: 0.5...2.0)
                
                return TideData(
                    id: UUID().uuidString,
                    height: height,
                    type: tideType,
                    timestamp: timestamp
                )
            }
        }
        
        return Just(predictions)
            .setFailureType(to: Error.self)
            .eraseToAnyPublisher()
    }
    
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error> {
        // In a real app, this would make an API call to WorldTides
        // For now, we'll return mock data
        let tideTypes: [TideData.TideType] = [.high, .falling, .low, .rising]
        let randomType = tideTypes.randomElement()!
        let height = randomType == .high || randomType == .rising ? 
            Double.random(in: 2.5...4.5) : Double.random(in: 0.5...2.0)
        
        return Just(TideData(
            id: UUID().uuidString,
            height: height,
            type: randomType,
            timestamp: dateTime
        ))
        .setFailureType(to: Error.self)
        .eraseToAnyPublisher()
    }
}

class CachedTideService: TideService {
    private let remoteService: TideService
    private var cache: [String: TideData] = [:]
    private var predictionsCache: [String: [TideData]] = [:]
    
    init(remoteService: TideService) {
        self.remoteService = remoteService
    }
    
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error> {
        let cacheKey = "current_\(location.id)"
        
        // Check if we have cached data that's less than 30 minutes old
        if let cachedData = cache[cacheKey],
           Date().timeIntervalSince(cachedData.timestamp) < 30 * 60 {
            return Just(cachedData)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // Otherwise fetch from remote
        return remoteService.getCurrentTide(for: location)
            .handleEvents(receiveOutput: { [weak self] tideData in
                self?.cache[cacheKey] = tideData
            })
            .eraseToAnyPublisher()
    }
    
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error> {
        let cacheKey = "predictions_\(location.id)_\(days)"
        
        // Check if we have cached data that's less than 12 hours old
        if let cachedData = predictionsCache[cacheKey],
           let firstItem = cachedData.first,
           Date().timeIntervalSince(firstItem.timestamp) < 12 * 60 * 60 {
            return Just(cachedData)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // Otherwise fetch from remote
        return remoteService.getTidePredictions(for: location, days: days)
            .handleEvents(receiveOutput: { [weak self] predictions in
                self?.predictionsCache[cacheKey] = predictions
            })
            .eraseToAnyPublisher()
    }
    
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error> {
        let calendar = Calendar.current
        let dateString = "\(calendar.component(.year, from: dateTime))-\(calendar.component(.month, from: dateTime))-\(calendar.component(.day, from: dateTime))-\(calendar.component(.hour, from: dateTime))"
        let cacheKey = "tide_\(location.id)_\(dateString)"
        
        // Check if we have cached data
        if let cachedData = cache[cacheKey] {
            return Just(cachedData)
                .setFailureType(to: Error.self)
                .eraseToAnyPublisher()
        }
        
        // Otherwise fetch from remote
        return remoteService.getTideForDateTime(for: location, dateTime: dateTime)
            .handleEvents(receiveOutput: { [weak self] tideData in
                self?.cache[cacheKey] = tideData
            })
            .eraseToAnyPublisher()
    }
}