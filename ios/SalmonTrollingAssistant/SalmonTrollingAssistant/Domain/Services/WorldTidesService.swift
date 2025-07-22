import Foundation
import Combine

/// Error types for tide service operations
enum TideError: Error {
    case networkError(Error)
    case serverError(Int)
    case decodingError(Error)
    case invalidLocation
    case noDataAvailable
    case unknown
}

/**
 * Implementation of TideService using WorldTides API
 * https://www.worldtides.info/api
 */
class WorldTidesService: TideService {
    private let apiKey: String
    private let baseUrl = "https://www.worldtides.info/api/v2"
    private let session: URLSession
    private let dateFormatter: DateFormatter
    
    init(apiKey: String, session: URLSession = .shared) {
        self.apiKey = apiKey
        self.session = session
        
        self.dateFormatter = DateFormatter()
        self.dateFormatter.dateFormat = "yyyy-MM-dd"
        self.dateFormatter.timeZone = TimeZone(identifier: "UTC")
    }
    
    func getCurrentTide(for location: Location) -> AnyPublisher<TideData, Error> {
        let now = Date()
        let calendar = Calendar.current
        
        // Format dates for API request
        let start = dateFormatter.string(from: now)
        
        // Add 1 day to get 24 hours of data
        let tomorrow = calendar.date(byAdding: .day, value: 1, to: now)!
        let end = dateFormatter.string(from: tomorrow)
        
        return fetchTideData(
            latitude: location.latitude,
            longitude: location.longitude,
            start: start,
            end: end
        )
        .map { response -> TideData in
            guard let extremes = response.extremes, !extremes.isEmpty else {
                throw TideError.noDataAvailable
            }
            
            return self.calculateCurrentTideState(for: now, extremes: extremes)
        }
        .eraseToAnyPublisher()
    }
    
    func getTidePredictions(for location: Location, days: Int) -> AnyPublisher<[TideData], Error> {
        let now = Date()
        let calendar = Calendar.current
        
        // Format dates for API request
        let start = dateFormatter.string(from: now)
        
        // Limit to 7 days maximum
        let limitedDays = min(days, 7)
        
        // Add the requested number of days
        let endDate = calendar.date(byAdding: .day, value: limitedDays, to: now)!
        let end = dateFormatter.string(from: endDate)
        
        return fetchTideData(
            latitude: location.latitude,
            longitude: location.longitude,
            start: start,
            end: end
        )
        .map { response -> [TideData] in
            guard let extremes = response.extremes, !extremes.isEmpty else {
                throw TideError.noDataAvailable
            }
            
            return self.groupExtremesByDay(extremes: extremes)
        }
        .eraseToAnyPublisher()
    }
    
    func getTideForDateTime(for location: Location, dateTime: Date) -> AnyPublisher<TideData, Error> {
        let calendar = Calendar.current
        
        // Get data for the day before and after the requested date
        let startDate = calendar.date(byAdding: .day, value: -1, to: dateTime)!
        let start = dateFormatter.string(from: startDate)
        
        let endDate = calendar.date(byAdding: .day, value: 1, to: dateTime)!
        let end = dateFormatter.string(from: endDate)
        
        return fetchTideData(
            latitude: location.latitude,
            longitude: location.longitude,
            start: start,
            end: end
        )
        .map { response -> TideData in
            guard let extremes = response.extremes, !extremes.isEmpty else {
                throw TideError.noDataAvailable
            }
            
            return self.calculateCurrentTideState(for: dateTime, extremes: extremes)
        }
        .eraseToAnyPublisher()
    }
    
    func observeTideData(for location: Location) -> AnyPublisher<TideData, Error> {
        // Create a timer that emits every 15 minutes
        return Timer.publish(every: 15 * 60, on: .main, in: .common)
            .autoconnect()
            .flatMap { _ in
                self.getCurrentTide(for: location)
            }
            .eraseToAnyPublisher()
    }
    
    // MARK: - Private Helper Methods
    
    private func fetchTideData(latitude: Double, longitude: Double, start: String, end: String) -> AnyPublisher<TideResponse, Error> {
        let endpoint = "\(baseUrl)?lat=\(latitude)&lon=\(longitude)&start=\(start)&end=\(end)&datum=MLLW&key=\(apiKey)"
        
        guard let url = URL(string: endpoint) else {
            return Fail(error: TideError.invalidLocation).eraseToAnyPublisher()
        }
        
        return session.dataTaskPublisher(for: url)
            .mapError { error -> TideError in
                return .networkError(error)
            }
            .flatMap { data, response -> AnyPublisher<TideResponse, TideError> in
                guard let httpResponse = response as? HTTPURLResponse else {
                    return Fail(error: TideError.unknown).eraseToAnyPublisher()
                }
                
                guard 200..<300 ~= httpResponse.statusCode else {
                    return Fail(error: self.mapErrorCodeToTideError(httpResponse.statusCode)).eraseToAnyPublisher()
                }
                
                return Just(data)
                    .decode(type: TideResponse.self, decoder: JSONDecoder())
                    .mapError { error -> TideError in
                        return .decodingError(error)
                    }
                    .flatMap { response -> AnyPublisher<TideResponse, TideError> in
                        if response.status != 200 {
                            return Fail(error: self.mapErrorCodeToTideError(response.status)).eraseToAnyPublisher()
                        }
                        return Just(response).setFailureType(to: TideError.self).eraseToAnyPublisher()
                    }
                    .eraseToAnyPublisher()
            }
            .eraseToAnyPublisher()
    }
    
    private func calculateCurrentTideState(for dateTime: Date, extremes: [TideExtreme]) -> TideData {
        // Sort extremes by time
        let sortedExtremes = extremes.sorted { $0.dt < $1.dt }
        
        // Find the extremes before and after the current time
        var previousExtreme: TideExtreme?
        var nextExtreme: TideExtreme?
        
        for extreme in sortedExtremes {
            let extremeDate = Date(timeIntervalSince1970: TimeInterval(extreme.dt))
            if extremeDate < dateTime {
                previousExtreme = extreme
            } else {
                nextExtreme = extreme
                break
            }
        }
        
        // If we don't have a previous extreme, use the first one
        if previousExtreme == nil && !sortedExtremes.isEmpty {
            previousExtreme = sortedExtremes.first
        }
        
        // If we don't have a next extreme, use the last one
        if nextExtreme == nil && !sortedExtremes.isEmpty {
            nextExtreme = sortedExtremes.last
        }
        
        // Determine tide type and height
        let tideType: TideType
        let height: Double
        
        if let previousExtreme = previousExtreme, let nextExtreme = nextExtreme {
            let previousDate = Date(timeIntervalSince1970: TimeInterval(previousExtreme.dt))
            let nextDate = Date(timeIntervalSince1970: TimeInterval(nextExtreme.dt))
            
            // Calculate where we are between the two extremes
            let totalDuration = nextDate.timeIntervalSince(previousDate)
            let elapsedDuration = dateTime.timeIntervalSince(previousDate)
            let progressRatio = elapsedDuration / totalDuration
            
            // Interpolate height
            height = previousExtreme.height + (nextExtreme.height - previousExtreme.height) * progressRatio
            
            // Determine tide type
            if previousExtreme.type == "High" && nextExtreme.type == "Low" {
                tideType = .falling
            } else if previousExtreme.type == "Low" && nextExtreme.type == "High" {
                tideType = .rising
            } else if previousExtreme.type == "High" {
                tideType = .high
            } else {
                tideType = .low
            }
        } else if let previousExtreme = previousExtreme {
            height = previousExtreme.height
            tideType = previousExtreme.type == "High" ? .high : .low
        } else if let nextExtreme = nextExtreme {
            height = nextExtreme.height
            tideType = nextExtreme.type == "High" ? .high : .low
        } else {
            // Fallback if no extremes are available
            height = 0.0
            tideType = .low
        }
        
        // Find next high and low tides
        let nextHighTide = sortedExtremes.first { 
            $0.type == "High" && Date(timeIntervalSince1970: TimeInterval($0.dt)) > dateTime 
        }
        
        let nextLowTide = sortedExtremes.first { 
            $0.type == "Low" && Date(timeIntervalSince1970: TimeInterval($0.dt)) > dateTime 
        }
        
        return TideData(
            timestamp: dateTime,
            height: height,
            type: tideType,
            nextHighTide: nextHighTide.map { 
                TideEvent(
                    timestamp: Date(timeIntervalSince1970: TimeInterval($0.dt)),
                    height: $0.height
                )
            },
            nextLowTide: nextLowTide.map { 
                TideEvent(
                    timestamp: Date(timeIntervalSince1970: TimeInterval($0.dt)),
                    height: $0.height
                )
            }
        )
    }
    
    private func groupExtremesByDay(extremes: [TideExtreme]) -> [TideData] {
        let calendar = Calendar.current
        var tideDataList = [TideData]()
        
        // Group extremes by day
        let extremesByDay = Dictionary(grouping: extremes) { extreme -> Int in
            let date = Date(timeIntervalSince1970: TimeInterval(extreme.dt))
            let day = calendar.component(.day, from: date)
            let month = calendar.component(.month, from: date)
            let year = calendar.component(.year, from: date)
            return day + month * 100 + year * 10000 // Unique key for each day
        }
        
        // Create a tide data for each day at noon
        for (_, dayExtremes) in extremesByDay {
            if !dayExtremes.isEmpty {
                // Use the first extreme's date to set the day
                let firstExtremeDate = Date(timeIntervalSince1970: TimeInterval(dayExtremes.first!.dt))
                
                // Set time to noon
                var components = calendar.dateComponents([.year, .month, .day], from: firstExtremeDate)
                components.hour = 12
                components.minute = 0
                components.second = 0
                
                if let noonDate = calendar.date(from: components) {
                    // Calculate tide state at noon
                    let tideData = calculateCurrentTideState(for: noonDate, extremes: dayExtremes)
                    tideDataList.append(tideData)
                }
            }
        }
        
        return tideDataList.sorted { $0.timestamp < $1.timestamp }
    }
    
    private func mapErrorCodeToTideError(_ code: Int) -> TideError {
        switch code {
        case 400:
            return .invalidLocation
        case 401, 403:
            return .serverError(code)
        case 404:
            return .noDataAvailable
        default:
            return .serverError(code)
        }
    }
}

// MARK: - API Response Models

struct TideResponse: Decodable {
    let status: Int
    let callCount: Int
    let copyright: String
    let requestLat: Double
    let requestLon: Double
    let responseLat: Double
    let responseLon: Double
    let atlas: String
    let station: String?
    let extremes: [TideExtreme]?
}

struct TideExtreme: Decodable {
    let dt: Int
    let date: String
    let height: Double
    let type: String // "High" or "Low"
}