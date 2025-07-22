import Foundation
import Combine

/**
 * Service for analyzing catch history data and providing insights
 */
protocol CatchAnalyticsService {
    /**
     * Get catch count by species
     */
    var catchCountBySpecies: AnyPublisher<[FishSpecies: Int], Never> { get }
    
    /**
     * Get catch count by location
     */
    var catchCountByLocation: AnyPublisher<[String: Int], Never> { get }
    
    /**
     * Get catch count by month
     */
    var catchCountByMonth: AnyPublisher<[Int: Int], Never> { get }
    
    /**
     * Get average fish size by species
     */
    var averageSizeBySpecies: AnyPublisher<[FishSpecies: Double], Never> { get }
    
    /**
     * Get average fish weight by species
     */
    var averageWeightBySpecies: AnyPublisher<[FishSpecies: Double], Never> { get }
    
    /**
     * Get most successful equipment
     */
    var mostSuccessfulEquipment: AnyPublisher<[(String, Int)], Never> { get }
    
    /**
     * Get most successful locations
     */
    var mostSuccessfulLocations: AnyPublisher<[(String, Int)], Never> { get }
    
    /**
     * Get catch trend over time
     */
    var catchTrendOverTime: AnyPublisher<[(Date, Int)], Never> { get }
    
    /**
     * Get personalized recommendations based on catch history
     */
    var personalizedRecommendations: AnyPublisher<[String], Never> { get }
}