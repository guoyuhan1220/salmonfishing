import Foundation
import Combine

class CatchAnalyticsViewModel: ObservableObject {
    private let catchAnalyticsService: CatchAnalyticsService
    private var cancellables = Set<AnyCancellable>()
    
    @Published var catchCountBySpecies: [FishSpecies: Int] = [:]
    @Published var catchCountByLocation: [String: Int] = [:]
    @Published var catchCountByMonth: [Int: Int] = [:]
    @Published var averageSizeBySpecies: [FishSpecies: Double] = [:]
    @Published var averageWeightBySpecies: [FishSpecies: Double] = [:]
    @Published var mostSuccessfulEquipment: [(String, Int)] = []
    @Published var mostSuccessfulLocations: [(String, Int)] = []
    @Published var catchTrendOverTime: [(Date, Int)] = []
    @Published var personalizedRecommendations: [String] = []
    
    init(catchAnalyticsService: CatchAnalyticsService) {
        self.catchAnalyticsService = catchAnalyticsService
        
        // Subscribe to analytics data
        catchAnalyticsService.catchCountBySpecies
            .assign(to: \.catchCountBySpecies, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.catchCountByLocation
            .assign(to: \.catchCountByLocation, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.catchCountByMonth
            .assign(to: \.catchCountByMonth, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.averageSizeBySpecies
            .assign(to: \.averageSizeBySpecies, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.averageWeightBySpecies
            .assign(to: \.averageWeightBySpecies, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.mostSuccessfulEquipment
            .assign(to: \.mostSuccessfulEquipment, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.mostSuccessfulLocations
            .assign(to: \.mostSuccessfulLocations, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.catchTrendOverTime
            .assign(to: \.catchTrendOverTime, on: self)
            .store(in: &cancellables)
        
        catchAnalyticsService.personalizedRecommendations
            .assign(to: \.personalizedRecommendations, on: self)
            .store(in: &cancellables)
    }
    
    // Helper methods for formatting data
    
    func getMonthName(for month: Int) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "MMMM"
        let calendar = Calendar.current
        if let date = calendar.date(from: DateComponents(month: month + 1)) {
            return dateFormatter.string(from: date)
        }
        return "Unknown"
    }
    
    func formatDate(_ date: Date) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .none
        return dateFormatter.string(from: date)
    }
    
    func formatDouble(_ value: Double) -> String {
        return String(format: "%.1f", value)
    }
}