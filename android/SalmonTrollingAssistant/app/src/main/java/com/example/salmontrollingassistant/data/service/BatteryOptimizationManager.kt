package com.example.salmontrollingassistant.data.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.batteryOptimizationStore by preferencesDataStore(name = "battery_optimization_settings")

/**
 * Manages battery optimization strategies across the app
 */
@Singleton
class BatteryOptimizationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys for battery optimization settings
    private val BATTERY_OPTIMIZATION_MODE = stringPreferencesKey("battery_optimization_mode")
    private val LOCATION_UPDATE_INTERVAL = longPreferencesKey("location_update_interval")
    private val NETWORK_BATCH_WINDOW = longPreferencesKey("network_batch_window")
    private val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
    private val LAST_BATTERY_LEVEL = intPreferencesKey("last_battery_level")
    private val LAST_BATTERY_CHECK = longPreferencesKey("last_battery_check")

    // Battery optimization modes
    enum class OptimizationMode {
        PERFORMANCE, // Prioritize app performance over battery life
        BALANCED,    // Balance between performance and battery life
        BATTERY_SAVER // Maximize battery life, potentially sacrificing some features
    }

    /**
     * Get the current battery optimization mode
     */
    fun getOptimizationMode(): Flow<OptimizationMode> {
        return context.batteryOptimizationStore.data.map { preferences ->
            val modeString = preferences[BATTERY_OPTIMIZATION_MODE] ?: OptimizationMode.BALANCED.name
            try {
                OptimizationMode.valueOf(modeString)
            } catch (e: Exception) {
                OptimizationMode.BALANCED
            }
        }
    }

    /**
     * Set the battery optimization mode
     */
    suspend fun setOptimizationMode(mode: OptimizationMode) {
        context.batteryOptimizationStore.edit { preferences ->
            preferences[BATTERY_OPTIMIZATION_MODE] = mode.name
        }
    }

    /**
     * Get the location update interval based on current optimization mode and battery level
     * @return Update interval in milliseconds
     */
    suspend fun getLocationUpdateInterval(): Long {
        val mode = getOptimizationMode().map { it }.collect { it }
        val batteryLevel = getBatteryLevel()
        
        // Base intervals for different modes
        return when {
            batteryLevel <= 15 -> 60000L // 1 minute when battery is low, regardless of mode
            mode == OptimizationMode.PERFORMANCE -> 15000L // 15 seconds in performance mode
            mode == OptimizationMode.BALANCED -> 30000L // 30 seconds in balanced mode
            mode == OptimizationMode.BATTERY_SAVER -> 60000L // 1 minute in battery saver mode
            else -> 30000L // Default to 30 seconds
        }
    }

    /**
     * Get the network batch window - time to wait to batch network requests
     * @return Batch window in milliseconds
     */
    suspend fun getNetworkBatchWindow(): Long {
        val mode = getOptimizationMode().map { it }.collect { it }
        
        return when (mode) {
            OptimizationMode.PERFORMANCE -> 1000L // 1 second in performance mode
            OptimizationMode.BALANCED -> 5000L // 5 seconds in balanced mode
            OptimizationMode.BATTERY_SAVER -> 15000L // 15 seconds in battery saver mode
        }
    }

    /**
     * Get the current battery level
     * @return Battery level percentage (0-100)
     */
    fun getBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }

    /**
     * Check if the device is in power save mode
     */
    fun isInPowerSaveMode(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }

    /**
     * Check if the device is charging
     */
    fun isCharging(): Boolean {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * Automatically adjust optimization mode based on device state
     */
    suspend fun autoAdjustOptimizationMode() {
        when {
            isCharging() -> setOptimizationMode(OptimizationMode.PERFORMANCE)
            isInPowerSaveMode() -> setOptimizationMode(OptimizationMode.BATTERY_SAVER)
            getBatteryLevel() <= 20 -> setOptimizationMode(OptimizationMode.BATTERY_SAVER)
            getBatteryLevel() >= 50 -> setOptimizationMode(OptimizationMode.BALANCED)
        }
    }

    /**
     * Update the stored battery level and check time
     */
    suspend fun updateBatteryStats() {
        val currentLevel = getBatteryLevel()
        val currentTime = System.currentTimeMillis()
        
        context.batteryOptimizationStore.edit { preferences ->
            preferences[LAST_BATTERY_LEVEL] = currentLevel
            preferences[LAST_BATTERY_CHECK] = currentTime
        }
    }

    /**
     * Get the low battery threshold percentage
     */
    fun getLowBatteryThreshold(): Flow<Int> {
        return context.batteryOptimizationStore.data.map { preferences ->
            preferences[LOW_BATTERY_THRESHOLD] ?: 15
        }
    }

    /**
     * Set the low battery threshold percentage
     */
    suspend fun setLowBatteryThreshold(threshold: Int) {
        context.batteryOptimizationStore.edit { preferences ->
            preferences[LOW_BATTERY_THRESHOLD] = threshold.coerceIn(5, 50)
        }
    }
}