import Foundation
import Combine

/**
 * Manages offline data access, caching strategies, and data freshness tracking
 */
class OfflineDataManager: ObservableObject {
    // Singleton instance
    static let shared = OfflineDataManager()
    
    // UserDefaults for storing offline settings
    private let userDefaults = UserDefaults.standard
    
    // Keys for offline settings
    private let offlineModeEnabledKey = "offline_mode_enabled"
    private let lastSyncTimestampKey = "last_sync_timestamp"
    private let syncStatusKey = "sync_status"
    private let dataFreshnessThresholdKey = "data_freshness_threshold"
    private let wifiOnlySyncKey = "wifi_only_sync"
    private let autoSyncEnabledKey = "auto_sync_enabled"
    
    // Publishers
    @Published var offlineModeEnabled = false
    private let syncStatusSubject = CurrentValueSubject<SyncStatus, Never>(.synced)
    private let lastSyncTimestampSubject = CurrentValueSubject<Date?, Never>(nil)
    
    // Sync status values
    enum SyncStatus: String {
        case synced = "SYNCED"
        case syncing = "SYNCING"
        case failed = "FAILED"
        case pending = "PENDING"
    }
    
    private init() {
        // Load initial values
        loadSettings()
    }
    
    // MARK: - Public API
    
    /// Publisher for sync status
    var syncStatus: AnyPublisher<SyncStatus, Never> {
        return syncStatusSubject.eraseToAnyPublisher()
    }
    
    /// Publisher for last sync timestamp
    var lastSyncTimestamp: AnyPublisher<Date?, Never> {
        return lastSyncTimestampSubject.eraseToAnyPublisher()
    }
    
    /// Check if offline mode is currently enabled
    func isOfflineModeEnabled() -> Bool {
        return offlineModeEnabled
    }
    
    /// Enable or disable offline mode
    func setOfflineMode(enabled: Bool) {
        userDefaults.set(enabled, forKey: offlineModeEnabledKey)
        offlineModeEnabled = enabled
    }
    
    /// Get the timestamp of the last successful data synchronization
    func getLastSyncTimestamp() -> Date? {
        return lastSyncTimestampSubject.value
    }
    
    /// Update the last sync timestamp to the current time
    func updateLastSyncTimestamp() {
        let now = Date()
        userDefaults.set(now.timeIntervalSince1970, forKey: lastSyncTimestampKey)
        lastSyncTimestampSubject.send(now)
    }
    
    /// Get the current sync status
    func getSyncStatus() -> SyncStatus {
        return syncStatusSubject.value
    }
    
    /// Update the sync status
    func updateSyncStatus(_ status: SyncStatus) {
        userDefaults.set(status.rawValue, forKey: syncStatusKey)
        syncStatusSubject.send(status)
    }
    
    /// Get the data freshness threshold in seconds
    /// Data older than this threshold will be considered stale
    func getDataFreshnessThreshold() -> TimeInterval {
        return userDefaults.double(forKey: dataFreshnessThresholdKey).isZero ? 
            (24 * 60 * 60) : userDefaults.double(forKey: dataFreshnessThresholdKey)
    }
    
    /// Set the data freshness threshold in seconds
    func setDataFreshnessThreshold(_ thresholdSeconds: TimeInterval) {
        userDefaults.set(thresholdSeconds, forKey: dataFreshnessThresholdKey)
    }
    
    /// Check if WiFi-only sync is enabled
    func isWifiOnlySyncEnabled() -> Bool {
        return userDefaults.bool(forKey: wifiOnlySyncKey)
    }
    
    /// Enable or disable WiFi-only sync
    func setWifiOnlySync(enabled: Bool) {
        userDefaults.set(enabled, forKey: wifiOnlySyncKey)
    }
    
    /// Check if auto-sync is enabled
    func isAutoSyncEnabled() -> Bool {
        return userDefaults.bool(forKey: autoSyncEnabledKey)
    }
    
    /// Enable or disable auto-sync
    func setAutoSync(enabled: Bool) {
        userDefaults.set(enabled, forKey: autoSyncEnabledKey)
    }
    
    /// Check if data for a specific location is fresh (not stale)
    func isDataFresh(for location: Location) -> Bool {
        // Simplified implementation for demo
        return true
    }
    
    /// Get data freshness percentage for a location (100% = completely fresh, 0% = completely stale)
    func getDataFreshnessPercentage(for location: Location) -> Int {
        // Simplified implementation for demo
        return 85
    }
    
    /// Get the timestamp when data for a location will become stale
    func getDataExpirationTime(for location: Location) -> Date? {
        // Simplified implementation for demo
        return Date().addingTimeInterval(3600) // 1 hour from now
    }
    
    /// Check if there is any cached data available for a location
    func hasCachedData(for location: Location) -> Bool {
        // Simplified implementation for demo
        return true
    }
    
    /// Clear all cached data
    func clearAllCachedData() {
        // Simplified implementation for demo
        print("Clearing all cached data")
    }
    
    /// Clear cached data for a specific location
    func clearCachedData(for location: Location) {
        // Simplified implementation for demo
        print("Clearing cached data for location: \(location.name)")
    }
    
    // MARK: - Private Methods
    
    private func loadSettings() {
        // Load offline mode setting
        offlineModeEnabled = userDefaults.bool(forKey: offlineModeEnabledKey)
        
        // Load sync status
        if let statusString = userDefaults.string(forKey: syncStatusKey),
           let status = SyncStatus(rawValue: statusString) {
            syncStatusSubject.send(status)
        } else {
            syncStatusSubject.send(.synced)
        }
        
        // Load last sync timestamp
        if let timestamp = userDefaults.object(forKey: lastSyncTimestampKey) as? TimeInterval {
            lastSyncTimestampSubject.send(Date(timeIntervalSince1970: timestamp))
        } else {
            lastSyncTimestampSubject.send(nil)
        }
        
        // Set defaults if not already set
        if userDefaults.object(forKey: dataFreshnessThresholdKey) == nil {
            userDefaults.set(24 * 60 * 60, forKey: dataFreshnessThresholdKey) // 24 hours
        }
        
        if userDefaults.object(forKey: wifiOnlySyncKey) == nil {
            userDefaults.set(true, forKey: wifiOnlySyncKey)
        }
        
        if userDefaults.object(forKey: autoSyncEnabledKey) == nil {
            userDefaults.set(true, forKey: autoSyncEnabledKey)
        }
    }
}