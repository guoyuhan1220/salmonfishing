import Foundation
import Combine

/// Mock implementation of TideService for testing and development
class MockTideService: TideService {
    
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error> {
        // Simulate network delay
        return Just(generateTideData(for: Date()))
            .setFailureType(to: Error.self)
            .delay(for: .seconds(0.5), scheduler: DispatchQueue.global())
            .eraseToAnyPublisher()
    }
    
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error> {
        // Simulate network delay
        return Future<[TideData], Error> { promise in
            DispatchQueue.global().asyncAfter(deadline: .now() + 0.8) {
                let limitedDays = min(days, 7) // Enforce 7-day limit
                let calendar = Calendar.current
                var predictions = [TideData]()
                
                var currentDate = Date()
                
                // Generate 4 tide events per day (roughly every 6 hours)
                for _ in 0..<limitedDays {
                    for _ in 0..<4 {
                        currentDate = calendar.date(byAdding: .hour, value: 6, to: currentDate)!
                        predictions.append(self.generateTideData(for: currentDate))
                    }
                }
                
                promise(.success(predictions))
            }
        }
        .eraseToAnyPublisher()
    }
    
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error> {
        // Simulate network delay
        return Just(generateTideData(for: dateTime))
            .setFailureType(to: Error.self)
            .delay(for: .seconds(0.5), scheduler: DispatchQueue.global())
            .eraseToAnyPublisher()
    }
    
    func observeTideData(for location: Location) -> AnyPublisher<TideData, Error> {
        // Create a timer that emits every 15 minutes
        return Timer.publish(every: 15 * 60, on: .main, in: .common)
            .autoconnect()
            .map { _ in self.generateTideData(for: Date()) }
            .setFailureType(to: Error.self)
            .eraseToAnyPublisher()
    }
    
    /// Generate realistic mock tide data for a given date
    private func generateTideData(for date: Date) -> TideData {
        let calendar = Calendar.current
        let hour = calendar.component(.hour, from: date)
        
        // Determine tide type based on hour of day (simplified model)
        let tideType: TideType
        switch hour {
        case 0...5:
            tideType = .falling
        case 6...11:
            tideType = .low
        case 12...17:
            tideType = .rising
        default:
            tideType = .high
        }
        
        // Generate tide height based on type
        let height: Double
        switch tideType {
        case .high:
            height = Double.random(in: 2.0...3.5)
        case .low:
            height = Double.random(in: 0.1...0.8)
        case .rising, .falling:
            height = Double.random(in: 0.8...2.0)
        }
        
        // Generate next high and low tide events
        var nextHighTideDate = date
        var nextLowTideDate = date
        
        switch tideType {
        case .high:
            nextHighTideDate = calendar.date(byAdding: .hour, value: 12, to: date)!
            nextLowTideDate = calendar.date(byAdding: .hour, value: 6, to: date)!
        case .low:
            nextHighTideDate = calendar.date(byAdding: .hour, value: 6, to: date)!
            nextLowTideDate = calendar.date(byAdding: .hour, value: 12, to: date)!
        case .rising:
            nextHighTideDate = calendar.date(byAdding: .hour, value: 3, to: date)!
            nextLowTideDate = calendar.date(byAdding: .hour, value: 9, to: date)!
        case .falling:
            nextHighTideDate = calendar.date(byAdding: .hour, value: 9, to: date)!
            nextLowTideDate = calendar.date(byAdding: .hour, value: 3, to: date)!
        }
        
        let nextHighTide = TideEvent(
            timestamp: nextHighTideDate,
            height: Double.random(in: 2.0...3.5)
        )
        
        let nextLowTide = TideEvent(
            timestamp: nextLowTideDate,
            height: Double.random(in: 0.1...0.8)
        )
        
        return TideData(
            timestamp: date,
            height: height,
            type: tideType,
            nextHighTide: nextHighTide,
            nextLowTide: nextLowTide
        )
    }
}