import Foundation
import UIKit
import Combine

/**
 * Manages battery optimization strategies across the app
 */
class BatteryOptimizationManager {
    // Singleton instance
    static let shared = BatteryOptimizationManager()
    
    // Battery optimization modes
    enum OptimizationMode: String {
        case performance // Prioritize app performance over battery life
        case balanced    // Balance between performance and battery life
        case batterySaver // Maximize battery life, potentially sacrificing some features
    }
    
    // UserDefaults keys
    private let optimizationModeKey = "battery_optimization_mode"
    private let locationUpdateIntervalKey = "location_update_interval"
    private let networkBatchWindowKey = "network_batch_window"
    private let lowBatteryThresholdKey = "low_battery_threshold"
    private let lastBatteryLevelKey = "last_battery_level"
    private let lastBatteryCheckKey = "last_battery_check"
    
    // Publishers
    private let optimizationModeSubject = CurrentValueSubject<OptimizationMode, Never>(.balanced)
    private let batteryLevelSubject = CurrentValueSubject<Int, Never>(100)
    private let isChargingSubject = CurrentValueSubject<Bool, Never>(false)
    
    // Public publishers
    var optimizationMode: AnyPublisher<OptimizationMode, Never> {
        return optimizationModeSubject.eraseToAnyPublisher()
    }
    
    var batteryLevel: AnyPublisher<Int, Never> {
        return batteryLevelSubject.eraseToAnyPublisher()
    }
    
    var isCharging: AnyPublisher<Bool, Never> {
        return isChargingSubject.eraseToAnyPublisher()
    }
    
    private init() {
        // Load saved mode from UserDefaults
        if let savedModeString = UserDefaults.standard.string(forKey: optimizationModeKey),
           let savedMode = OptimizationMode(rawValue: savedModeString) {
            optimizationModeSubject.send(savedMode)
        }
        
        // Start monitoring battery
        setupBatteryMonitoring()
        
        // Initial battery check
        updateBatteryStats()
    }
    
    // MARK: - Public Methods
    
    /**
     * Set the battery optimization mode
     */
    func setOptimizationMode(_ mode: OptimizationMode) {
        optimizationModeSubject.send(mode)
        UserDefaults.standard.set(mode.rawValue, forKey: optimizationModeKey)
    }
    
    /**
     * Get the location update interval based on current optimization mode and battery level
     * @return Update interval in seconds
     */
    func getLocationUpdateInterval() -> TimeInterval {
        let mode = optimizationModeSubject.value
        let level = batteryLevelSubject.value
        
        // Base intervals for different modes
        switch (mode, level) {
        case (_, 0...15):
            return 60.0 // 1 minute when battery is low, regardless of mode
        case (.performance, _):
            return 15.0 // 15 seconds in performance mode
        case (.balanced, _):
            return 30.0 // 30 seconds in balanced mode
        case (.batterySaver, _):
            return 60.0 // 1 minute in battery saver mode
        }
    }
    
    /**
     * Get the network batch window - time to wait to batch network requests
     * @return Batch window in seconds
     */
    func getNetworkBatchWindow() -> TimeInterval {
        let mode = optimizationModeSubject.value
        
        switch mode {
        case .performance:
            return 1.0 // 1 second in performance mode
        case .balanced:
            return 5.0 // 5 seconds in balanced mode
        case .batterySaver:
            return 15.0 // 15 seconds in battery saver mode
        }
    }
    
    /**
     * Get the current low battery threshold
     */
    func getLowBatteryThreshold() -> Int {
        return UserDefaults.standard.integer(forKey: lowBatteryThresholdKey)
    }
    
    /**
     * Set the low battery threshold
     */
    func setLowBatteryThreshold(_ threshold: Int) {
        let validThreshold = min(max(threshold, 5), 50)
        UserDefaults.standard.set(validThreshold, forKey: lowBatteryThresholdKey)
    }
    
    /**
     * Automatically adjust optimization mode based on device state
     */
    func autoAdjustOptimizationMode() {
        let level = batteryLevelSubject.value
        let charging = isChargingSubject.value
        let lowPowerMode = ProcessInfo.processInfo.isLowPowerModeEnabled
        
        switch (charging, lowPowerMode, level) {
        case (true, _, _):
            setOptimizationMode(.performance)
        case (_, true, _):
            setOptimizationMode(.batterySaver)
        case (_, _, 0...20):
            setOptimizationMode(.batterySaver)
        case (_, _, 21...49):
            setOptimizationMode(.balanced)
        case (_, _, 50...100):
            setOptimizationMode(.balanced)
        default:
            setOptimizationMode(.balanced)
        }
    }
    
    // MARK: - Private Methods
    
    private func setupBatteryMonitoring() {
        // Enable battery monitoring
        UIDevice.current.isBatteryMonitoringEnabled = true
        
        // Observe battery level changes
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(batteryLevelDidChange),
            name: UIDevice.batteryLevelDidChangeNotification,
            object: nil
        )
        
        // Observe battery state changes (charging/discharging)
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(batteryStateDidChange),
            name: UIDevice.batteryStateDidChangeNotification,
            object: nil
        )
        
        // Observe low power mode changes
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(lowPowerModeDidChange),
            name: .NSProcessInfoPowerStateDidChange,
            object: nil
        )
    }
    
    @objc private func batteryLevelDidChange() {
        updateBatteryStats()
        autoAdjustOptimizationMode()
    }
    
    @objc private func batteryStateDidChange() {
        let isCurrentlyCharging = UIDevice.current.batteryState == .charging || 
                                 UIDevice.current.batteryState == .full
        isChargingSubject.send(isCurrentlyCharging)
        autoAdjustOptimizationMode()
    }
    
    @objc private func lowPowerModeDidChange() {
        autoAdjustOptimizationMode()
    }
    
    private func updateBatteryStats() {
        let currentLevel = Int(UIDevice.current.batteryLevel * 100)
        batteryLevelSubject.send(currentLevel)
        
        UserDefaults.standard.set(currentLevel, forKey: lastBatteryLevelKey)
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: lastBatteryCheckKey)
    }
}