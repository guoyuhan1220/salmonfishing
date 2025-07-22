import Foundation
import Combine

class CatchAnalyticsServiceImpl: CatchAnalyticsService {
    private let catchLoggingService: CatchLoggingService
    
    init(catchLoggingService: CatchLoggingService) {
        self.catchLoggingService = catchLoggingService
    }
    
    var catchCountBySpecies: AnyPublisher<[FishSpecies: Int], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                Dictionary(grouping: catches, by: { $0.species })
                    .mapValues { $0.count }
            }
            .eraseToAnyPublisher()
    }
    
    var catchCountByLocation: AnyPublisher<[String: Int], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                Dictionary(grouping: catches, by: { $0.locationId })
                    .mapValues { $0.count }
            }
            .eraseToAnyPublisher()
    }
    
    var catchCountByMonth: AnyPublisher<[Int: Int], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                let calendar = Calendar.current
                let groupedByMonth = Dictionary(grouping: catches) { catch in
                    calendar.component(.month, from: catch.timestamp) - 1 // 0-based month
                }
                return groupedByMonth.mapValues { $0.count }
            }
            .eraseToAnyPublisher()
    }
    
    var averageSizeBySpecies: AnyPublisher<[FishSpecies: Double], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                let catchesWithSize = catches.filter { $0.size != nil }
                let groupedBySpecies = Dictionary(grouping: catchesWithSize, by: { $0.species })
                
                return groupedBySpecies.mapValues { catches in
                    let sizes = catches.compactMap { $0.size }
                    return sizes.isEmpty ? 0 : sizes.reduce(0, +) / Double(sizes.count)
                }
            }
            .eraseToAnyPublisher()
    }
    
    var averageWeightBySpecies: AnyPublisher<[FishSpecies: Double], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                let catchesWithWeight = catches.filter { $0.weight != nil }
                let groupedBySpecies = Dictionary(grouping: catchesWithWeight, by: { $0.species })
                
                return groupedBySpecies.mapValues { catches in
                    let weights = catches.compactMap { $0.weight }
                    return weights.isEmpty ? 0 : weights.reduce(0, +) / Double(weights.count)
                }
            }
            .eraseToAnyPublisher()
    }
    
    var mostSuccessfulEquipment: AnyPublisher<[(String, Int)], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                // Flatten the list of equipment used in each catch
                let allEquipment = catches.flatMap { $0.equipmentUsed }
                
                // Count occurrences of each equipment
                var equipmentCounts: [String: Int] = [:]
                for equipment in allEquipment {
                    equipmentCounts[equipment, default: 0] += 1
                }
                
                // Sort by count in descending order
                return equipmentCounts.sorted { $0.value > $1.value }
            }
            .eraseToAnyPublisher()
    }
    
    var mostSuccessfulLocations: AnyPublisher<[(String, Int)], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                let locationCounts = Dictionary(grouping: catches, by: { $0.locationId })
                    .mapValues { $0.count }
                
                return locationCounts.sorted { $0.value > $1.value }
            }
            .eraseToAnyPublisher()
    }
    
    var catchTrendOverTime: AnyPublisher<[(Date, Int)], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                let calendar = Calendar.current
                
                // Group catches by day
                let catchesByDay = Dictionary(grouping: catches) { catch in
                    calendar.startOfDay(for: catch.timestamp)
                }
                
                // Convert to array of (date, count) pairs and sort by date
                return catchesByDay.map { (date, catches) in
                    (date, catches.count)
                }.sorted { $0.0 < $1.0 }
            }
            .eraseToAnyPublisher()
    }
    
    var personalizedRecommendations: AnyPublisher<[String], Never> {
        return catchLoggingService.catchHistory
            .map { catches in
                if catches.isEmpty {
                    return ["Start logging your catches to get personalized recommendations!"]
                }
                
                var recommendations: [String] = []
                
                // Most successful species
                let speciesCounts = Dictionary(grouping: catches, by: { $0.species })
                    .mapValues { $0.count }
                
                if let mostCaughtSpecies = speciesCounts.max(by: { $0.value < $1.value })?.key {
                    recommendations.append("You've had the most success catching \(mostCaughtSpecies.rawValue.capitalized).")
                }
                
                // Most successful location
                let locationCounts = Dictionary(grouping: catches, by: { $0.locationId })
                    .mapValues { $0.count }
                
                if let mostSuccessfulLocation = locationCounts.max(by: { $0.value < $1.value })?.key {
                    recommendations.append("Your most productive fishing spot is location \(mostSuccessfulLocation).")
                }
                
                // Most successful equipment
                let allEquipment = catches.flatMap { $0.equipmentUsed }
                var equipmentCounts: [String: Int] = [:]
                for equipment in allEquipment {
                    equipmentCounts[equipment, default: 0] += 1
                }
                
                let mostSuccessfulEquipment = equipmentCounts.sorted { $0.value > $1.value }
                    .prefix(3)
                    .map { $0.key }
                
                if !mostSuccessfulEquipment.isEmpty {
                    recommendations.append("Your most effective equipment includes: \(mostSuccessfulEquipment.joined(separator: ", ")).")
                }
                
                // Best time of year
                let calendar = Calendar.current
                let monthCounts = Dictionary(grouping: catches) { catch in
                    calendar.component(.month, from: catch.timestamp) - 1 // 0-based month
                }.mapValues { $0.count }
                
                if let bestMonth = monthCounts.max(by: { $0.value < $1.value })?.key {
                    let dateFormatter = DateFormatter()
                    dateFormatter.dateFormat = "MMMM"
                    let date = calendar.date(from: DateComponents(month: bestMonth + 1))!
                    let monthName = dateFormatter.string(from: date)
                    
                    recommendations.append("Your best fishing month appears to be \(monthName).")
                }
                
                return recommendations
            }
            .eraseToAnyPublisher()
    }
}