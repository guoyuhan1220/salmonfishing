package com.example.salmontrollingassistant.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.salmontrollingassistant.data.db.LocationDao
import com.example.salmontrollingassistant.data.db.TideDao
import com.example.salmontrollingassistant.data.db.WeatherDao
import com.example.salmontrollingassistant.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineDataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_data_settings")

/**
 * Manages offline data access, caching strategies, and data freshness tracking
 */
@Singleton
class OfflineDataManager @Inject constructor(
    private val context: Context,
    private val weatherDao: WeatherDao,
    private val tideDao: TideDao,
    private val locationDao: LocationDao
) {
    // Keys for offline settings
    private val OFFLINE_MODE_ENABLED = booleanPreferencesKey("offline_mode_enabled")
    private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    private val SYNC_STATUS = stringPreferencesKey("sync_status")
    private val DATA_FRESHNESS_THRESHOLD = longPreferencesKey("data_freshness_threshold")
    private val WIFI_ONLY_SYNC = booleanPreferencesKey("wifi_only_sync")
    private val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
    
    // Sync status values
    enum class SyncStatus {
        SYNCED, SYNCING, FAILED, PENDING
    }
    
    /**
     * Check if offline mode is currently enabled
     */
    fun isOfflineModeEnabled(): Flow<Boolean> {
        return context.offlineDataStore.data.map { preferences ->
            preferences[OFFLINE_MODE_ENABLED] ?: false
        }
    }
    
    /**
     * Enable or disable offline mode
     */
    suspend fun setOfflineMode(enabled: Boolean) {
        context.offlineDataStore.edit { preferences ->
            preferences[OFFLINE_MODE_ENABLED] = enabled
        }
    }
    
    /**
     * Get the timestamp of the last successful data synchronization
     */
    fun getLastSyncTimestamp(): Flow<Long> {
        return context.offlineDataStore.data.map { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] ?: 0L
        }
    }
    
    /**
     * Update the last sync timestamp to the current time
     */
    suspend fun updateLastSyncTimestamp() {
        context.offlineDataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }
    
    /**
     * Get the current sync status
     */
    fun getSyncStatus(): Flow<SyncStatus> {
        return context.offlineDataStore.data.map { preferences ->
            val statusString = preferences[SYNC_STATUS] ?: SyncStatus.SYNCED.name
            try {
                SyncStatus.valueOf(statusString)
            } catch (e: Exception) {
                SyncStatus.FAILED
            }
        }
    }
    
    /**
     * Update the sync status
     */
    suspend fun updateSyncStatus(status: SyncStatus) {
        context.offlineDataStore.edit { preferences ->
            preferences[SYNC_STATUS] = status.name
        }
    }
    
    /**
     * Get the data freshness threshold in milliseconds
     * Data older than this threshold will be considered stale
     */
    fun getDataFreshnessThreshold(): Flow<Long> {
        return context.offlineDataStore.data.map { preferences ->
            // Default to 24 hours
            preferences[DATA_FRESHNESS_THRESHOLD] ?: (24 * 60 * 60 * 1000L)
        }
    }
    
    /**
     * Set the data freshness threshold in milliseconds
     */
    suspend fun setDataFreshnessThreshold(thresholdMs: Long) {
        context.offlineDataStore.edit { preferences ->
            preferences[DATA_FRESHNESS_THRESHOLD] = thresholdMs
        }
    }
    
    /**
     * Check if WiFi-only sync is enabled
     */
    fun isWifiOnlySyncEnabled(): Flow<Boolean> {
        return context.offlineDataStore.data.map { preferences ->
            preferences[WIFI_ONLY_SYNC] ?: true
        }
    }
    
    /**
     * Enable or disable WiFi-only sync
     */
    suspend fun setWifiOnlySync(enabled: Boolean) {
        context.offlineDataStore.edit { preferences ->
            preferences[WIFI_ONLY_SYNC] = enabled
        }
    }
    
    /**
     * Check if auto-sync is enabled
     */
    fun isAutoSyncEnabled(): Flow<Boolean> {
        return context.offlineDataStore.data.map { preferences ->
            preferences[AUTO_SYNC_ENABLED] ?: true
        }
    }
    
    /**
     * Enable or disable auto-sync
     */
    suspend fun setAutoSync(enabled: Boolean) {
        context.offlineDataStore.edit { preferences ->
            preferences[AUTO_SYNC_ENABLED] = enabled
        }
    }
    
    /**
     * Check if data for a specific location is fresh (not stale)
     */
    suspend fun isDataFresh(locationId: String): Boolean {
        val threshold = getDataFreshnessThreshold().first()
        val currentTime = System.currentTimeMillis()
        
        // Check weather data freshness
        val weatherData = weatherDao.getCurrentWeather(locationId)
        val weatherFresh = weatherData != null && 
                          (currentTime - weatherData.cacheTimestamp) < threshold
        
        // Check tide data freshness
        val tideData = tideDao.getCurrentTide(locationId)
        val tideFresh = tideData != null && 
                       (currentTime - tideData.cacheTimestamp) < threshold
        
        return weatherFresh && tideFresh
    }
    
    /**
     * Get data freshness percentage for a location (100% = completely fresh, 0% = completely stale)
     */
    suspend fun getDataFreshnessPercentage(locationId: String): Int {
        val threshold = getDataFreshnessThreshold().first()
        val currentTime = System.currentTimeMillis()
        
        // Check weather data freshness
        val weatherData = weatherDao.getCurrentWeather(locationId)
        val weatherAge = if (weatherData != null) {
            currentTime - weatherData.cacheTimestamp
        } else {
            threshold
        }
        
        // Check tide data freshness
        val tideData = tideDao.getCurrentTide(locationId)
        val tideAge = if (tideData != null) {
            currentTime - tideData.cacheTimestamp
        } else {
            threshold
        }
        
        // Calculate average freshness
        val weatherFreshness = ((threshold - weatherAge).coerceAtLeast(0) * 100 / threshold).toInt()
        val tideFreshness = ((threshold - tideAge).coerceAtLeast(0) * 100 / threshold).toInt()
        
        return (weatherFreshness + tideFreshness) / 2
    }
    
    /**
     * Get the timestamp when data for a location will become stale
     */
    suspend fun getDataExpirationTime(locationId: String): Date? {
        val threshold = getDataFreshnessThreshold().first()
        
        // Get the most recent cache timestamp between weather and tide data
        val weatherData = weatherDao.getCurrentWeather(locationId)
        val tideData = tideDao.getCurrentTide(locationId)
        
        val weatherTimestamp = weatherData?.cacheTimestamp ?: 0L
        val tideTimestamp = tideData?.cacheTimestamp ?: 0L
        
        val mostRecentTimestamp = maxOf(weatherTimestamp, tideTimestamp)
        
        return if (mostRecentTimestamp > 0) {
            Date(mostRecentTimestamp + threshold)
        } else {
            null
        }
    }
    
    /**
     * Check if there is any cached data available for a location
     */
    suspend fun hasCachedData(locationId: String): Boolean {
        val weatherData = weatherDao.getCurrentWeather(locationId)
        val tideData = tideDao.getCurrentTide(locationId)
        
        return weatherData != null || tideData != null
    }
    
    /**
     * Clear all cached data
     */
    suspend fun clearAllCachedData() {
        weatherDao.clearAllWeatherData()
        tideDao.clearAllTideData()
    }
    
    /**
     * Clear cached data for a specific location
     */
    suspend fun clearCachedData(locationId: String) {
        weatherDao.deleteCurrentWeather(locationId)
        weatherDao.deleteForecast(locationId)
        tideDao.deleteCurrentTide(locationId)
        tideDao.deleteTideForecast(locationId)
    }
    
    /**
     * Get all locations that have cached data
     */
    suspend fun getLocationsWithCachedData(): List<Location> {
        val weatherLocations = weatherDao.getLocationsWithWeatherData()
        val tideLocations = tideDao.getLocationsWithTideData()
        
        val locationIds = (weatherLocations + tideLocations).distinct()
        return locationDao.getLocationsByIds(locationIds).map { it.toLocation() }
    }
}