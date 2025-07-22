import Foundation
import Combine

/**
 * Central layer for managing offline data access across the application.
 * This class coordinates between different data sources and implements caching strategies.
 */
class OfflineDataAccessLayer: ObservableObject {
    // Singleton instance
    static let shared = OfflineDataAccessLayer()
    
    // UserDefaults for storing offline access settings
    private let userDefaults = UserDefaults.standard
    
    // Keys for offline access settings
    private let maxCacheSizeMBKey = "max_cache_size_mb"
    private let cacheCleanupIntervalKey = "cache_cleanup_interval"
    private let prefetchEnabledKey = "prefetch_enabled"
    private let prefetchDaysKey = "prefetch_days"
    private let cachePriorityKey = "cache_priority"
    
    // Cache priority values
    enum CachePriority: Int {
        case low = 0      // Minimal caching, save storage space
        case medium = 1   // Balance between storage and offline availability
        case high = 2     // Maximum caching for best offline experience
    }
    
    // Dependencies
    private let offlineDataManager = OfflineDataManager.shared
    
    // Published properties
    @Published var cachePriorityValue: CachePriority = .medium
    @Published var prefetchEnabledValue: Bool = true
    @Published var prefetchDaysValue: Int = 3
    @Published var maxCacheSizeMBValue: Int = 100
    @Published var totalCacheSizeMBValue: Float = 25.0
    
    private init() {
        // Load initial values
        loadSettings()
    }
    
    // MARK: - Public API
    
    /// Initialize the offline data access layer with default settings if not already set
    func initialize() {
        if userDefaults.object(forKey: maxCacheSizeMBKey) == nil {
            userDefaults.set(100, forKey: maxCacheSizeMBKey) // 100 MB default
        }
        
        if userDefaults.object(forKey: cacheCleanupIntervalKey) == nil {
            userDefaults.set(24 * 60 * 60, forKey: cacheCleanupIntervalKey) // 24 hours default
        }
        
        if userDefaults.object(forKey: prefetchEnabledKey) == nil {
            userDefaults.set(true, forKey: prefetchEnabledKey) // Enabled by default
        }
        
        if userDefaults.object(forKey: prefetchDaysKey) == nil {
            userDefaults.set(3, forKey: prefetchDaysKey) // 3 days default
        }
        
        if userDefaults.object(forKey: cachePriorityKey) == nil {
            userDefaults.set(CachePriority.medium.rawValue, forKey: cachePriorityKey)
        }
        
        loadSettings()
    }
    
    /// Prefetch data for a location to ensure offline availability
    func prefetchData(for location: Location, days: Int) {
        if !isPrefetchEnabled() {
            return
        }
        
        // This would trigger the cached services to fetch and store data
        // For now, we'll just mark that prefetching was attempted
        userDefaults.set(Date().timeIntervalSince1970, forKey: "last_prefetch_\(location.id)")
    }
    
    /// Check if data prefetching is enabled
    func isPrefetchEnabled() -> Bool {
        return prefetchEnabledValue
    }
    
    /// Enable or disable data prefetching
    func setPrefetchEnabled(_ enabled: Bool) {
        userDefaults.set(enabled, forKey: prefetchEnabledKey)
        prefetchEnabledValue = enabled
    }
    
    /// Get the number of days to prefetch data for
    func getPrefetchDays() -> Int {
        return prefetchDaysValue
    }
    
    /// Set the number of days to prefetch data for
    func setPrefetchDays(_ days: Int) {
        let clampedDays = min(max(days, 1), 7) // Limit to 1-7 days
        userDefaults.set(clampedDays, forKey: prefetchDaysKey)
        prefetchDaysValue = clampedDays
    }
    
    /// Get the current cache priority setting
    func getCachePriority() -> CachePriority {
        return cachePriorityValue
    }
    
    /// Set the cache priority
    func setCachePriority(_ priority: CachePriority) {
        userDefaults.set(priority.rawValue, forKey: cachePriorityKey)
        cachePriorityValue = priority
        
        // Adjust cache settings based on priority
        switch priority {
        case .low:
            setMaxCacheSizeMB(50)
            offlineDataManager.setDataFreshnessThreshold(12 * 60 * 60) // 12 hours
            setPrefetchDays(1)
        case .medium:
            setMaxCacheSizeMB(100)
            offlineDataManager.setDataFreshnessThreshold(24 * 60 * 60) // 24 hours
            setPrefetchDays(3)
        case .high:
            setMaxCacheSizeMB(250)
            offlineDataManager.setDataFreshnessThreshold(48 * 60 * 60) // 48 hours
            setPrefetchDays(7)
        }
    }
    
    /// Get the maximum cache size in MB
    func getMaxCacheSizeMB() -> Int {
        return maxCacheSizeMBValue
    }
    
    /// Set the maximum cache size in MB
    func setMaxCacheSizeMB(_ sizeMB: Int) {
        let minSize = 10 // Minimum 10MB
        let finalSize = max(sizeMB, minSize)
        userDefaults.set(finalSize, forKey: maxCacheSizeMBKey)
        maxCacheSizeMBValue = finalSize
    }
    
    /// Get the cache cleanup interval in seconds
    func getCacheCleanupInterval() -> TimeInterval {
        return userDefaults.double(forKey: cacheCleanupIntervalKey)
    }
    
    /// Set the cache cleanup interval in seconds
    func setCacheCleanupInterval(_ intervalSeconds: TimeInterval) {
        userDefaults.set(intervalSeconds, forKey: cacheCleanupIntervalKey)
    }
    
    /// Clean up old cache entries based on current settings
    func cleanupCache() {
        // Simulate cache cleanup
        totalCacheSizeMBValue *= 0.8
    }
    
    /// Get the total size of all caches in MB
    func getTotalCacheSizeMB() -> Float {
        return totalCacheSizeMBValue
    }
    
    /// Clear all cached data
    func clearAllCachedData() {
        offlineDataManager.clearAllCachedData()
        totalCacheSizeMBValue = 0
    }
    
    /// Check if there is any cached data available for a location
    func hasCachedData(for location: Location) -> Bool {
        return offlineDataManager.hasCachedData(for: location)
    }
    
    /// Get data freshness percentage for a location
    func getDataFreshnessPercentage(for location: Location) -> Int {
        return offlineDataManager.getDataFreshnessPercentage(for: location)
    }
    
    /// Get the timestamp when data for a location will become stale
    func getDataExpirationTime(for location: Location) -> Date? {
        return offlineDataManager.getDataExpirationTime(for: location)
    }
    
    // MARK: - Private Methods
    
    private func loadSettings() {
        // Load cache priority
        let priorityValue = userDefaults.integer(forKey: cachePriorityKey)
        if let priority = CachePriority(rawValue: priorityValue) {
            cachePriorityValue = priority
        } else {
            cachePriorityValue = .medium
        }
        
        // Load prefetch enabled
        prefetchEnabledValue = userDefaults.bool(forKey: prefetchEnabledKey)
        
        // Load prefetch days
        let days = userDefaults.integer(forKey: prefetchDaysKey)
        prefetchDaysValue = days == 0 ? 3 : days
        
        // Load max cache size
        let size = userDefaults.integer(forKey: maxCacheSizeMBKey)
        maxCacheSizeMBValue = size == 0 ? 100 : size
        
        // Set initial cache size
        totalCacheSizeMBValue = 25.0
    }
}