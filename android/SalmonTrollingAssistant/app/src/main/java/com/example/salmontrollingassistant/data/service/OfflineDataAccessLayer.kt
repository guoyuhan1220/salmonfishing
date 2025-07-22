package com.example.salmontrollingassistant.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.salmontrollingassistant.data.db.LocationDao
import com.example.salmontrollingassistant.data.db.TideDao
import com.example.salmontrollingAssistant.data.db.WeatherDao
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineAccessDataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_access_settings")

/**
 * Central layer for managing offline data access across the application.
 * This class coordinates between different data sources and implements caching strategies.
 */
@Singleton
class OfflineDataAccessLayer @Inject constructor(
    private val context: Context,
    private val offlineDataManager: OfflineDataManager,
    private val cacheManager: CacheManager,
    private val weatherDao: WeatherDao,
    private val tideDao: TideDao,
    private val locationDao: LocationDao
) {
    // Keys for offline access settings
    private val MAX_CACHE_SIZE_MB = intPreferencesKey("max_cache_size_mb")
    private val CACHE_CLEANUP_INTERVAL = longPreferencesKey("cache_cleanup_interval")
    private val PREFETCH_ENABLED = intPreferencesKey("prefetch_enabled")
    private val PREFETCH_DAYS = intPreferencesKey("prefetch_days")
    private val CACHE_PRIORITY = intPreferencesKey("cache_priority")
    
    // Cache priority values
    enum class CachePriority(val value: Int) {
        LOW(0),      // Minimal caching, save storage space
        MEDIUM(1),   // Balance between storage and offline availability
        HIGH(2)      // Maximum caching for best offline experience
    }

    /**
     * Initialize the offline data access layer with default settings if not already set
     */
    suspend fun initialize() {
        context.offlineAccessDataStore.edit { preferences ->
            if (!preferences.contains(MAX_CACHE_SIZE_MB)) {
                preferences[MAX_CACHE_SIZE_MB] = 100 // 100 MB default
            }
            if (!preferences.contains(CACHE_CLEANUP_INTERVAL)) {
                preferences[CACHE_CLEANUP_INTERVAL] = 24 * 60 * 60 * 1000 // 24 hours default
            }
            if (!preferences.contains(PREFETCH_ENABLED)) {
                preferences[PREFETCH_ENABLED] = 1 // Enabled by default
            }
            if (!preferences.contains(PREFETCH_DAYS)) {
                preferences[PREFETCH_DAYS] = 3 // 3 days default
            }
            if (!preferences.contains(CACHE_PRIORITY)) {
                preferences[CACHE_PRIORITY] = CachePriority.MEDIUM.value
            }
        }
    }

    /**
     * Get weather data for a location, prioritizing offline access
     * @param location The location to get weather for
     * @return Weather data result
     */
    suspend fun getWeatherData(location: Location): WeatherData? {
        val cachedWeather = weatherDao.getCurrentWeather(location.id)
        return cachedWeather?.toWeatherData()
    }

    /**
     * Get tide data for a location, prioritizing offline access
     * @param location The location to get tide for
     * @return Tide data result
     */
    suspend fun getTideData(location: Location): TideData? {
        val cachedTide = tideDao.getCurrentTide(location.id)
        return cachedTide?.toTideData()
    }

    /**
     * Get weather forecast for a location, prioritizing offline access
     * @param location The location to get forecast for
     * @param days Number of days to get forecast for
     * @return List of weather data for forecast
     */
    suspend fun getWeatherForecast(location: Location, days: Int): List<WeatherData> {
        val cachedForecast = weatherDao.getForecast(location.id)
        return cachedForecast.map { it.toWeatherData() }.take(days)
    }

    /**
     * Get tide predictions for a location, prioritizing offline access
     * @param location The location to get predictions for
     * @param days Number of days to get predictions for
     * @return List of tide data for predictions
     */
    suspend fun getTidePredictions(location: Location, days: Int): List<TideData> {
        val cachedPredictions = tideDao.getTideForecast(location.id)
        return cachedPredictions.map { it.toTideData() }.take(days)
    }

    /**
     * Prefetch data for a location to ensure offline availability
     * @param location The location to prefetch data for
     * @param days Number of days to prefetch
     */
    suspend fun prefetchData(location: Location, days: Int) {
        val prefetchEnabled = isPrefetchEnabled()
        if (!prefetchEnabled) return

        // This would trigger the cached services to fetch and store data
        // The actual implementation would depend on how the services are structured
        // For now, we'll just mark that prefetching was attempted
        context.offlineAccessDataStore.edit { preferences ->
            preferences[longPreferencesKey("last_prefetch_${location.id}")] = System.currentTimeMillis()
        }
    }

    /**
     * Check if data prefetching is enabled
     */
    suspend fun isPrefetchEnabled(): Boolean {
        return context.offlineAccessDataStore.data.map { preferences ->
            preferences[PREFETCH_ENABLED] ?: 1
        }.first() == 1
    }

    /**
     * Enable or disable data prefetching
     */
    suspend fun setPrefetchEnabled(enabled: Boolean) {
        context.offlineAccessDataStore.edit { preferences ->
            preferences[PREFETCH_ENABLED] = if (enabled) 1 else 0
        }
    }

    /**
     * Get the number of days to prefetch data for
     */
    suspend fun getPrefetchDays(): Int {
        return context.offlineAccessDataStore.data.map { preferences ->
            preferences[PREFETCH_DAYS] ?: 3
        }.first()
    }

    /**
     * Set the number of days to prefetch data for
     */
    suspend fun setPrefetchDays(days: Int) {
        context.offlineAccessDataStore.edit { preferences ->
            preferences[PREFETCH_DAYS] = days.coerceIn(1, 7) // Limit to 1-7 days
        }
    }

    /**
     * Get the current cache priority setting
     */
    suspend fun getCachePriority(): CachePriority {
        val value = context.offlineAccessDataStore.data.map { preferences ->
            preferences[CACHE_PRIORITY] ?: CachePriority.MEDIUM.value
        }.first()
        
        return when (value) {
            CachePriority.LOW.value -> CachePriority.LOW
            CachePriority.HIGH.value -> CachePriority.HIGH
            else -> CachePriority.MEDIUM
        }
    }

    /**
     * Set the cache priority
     */
    suspend fun setCachePriority(priority: CachePriority) {
        context.offlineAccessDataStore.edit { preferences ->
            preferences[CACHE_PRIORITY] = priority.value
        }
        
        // Adjust cache settings based on priority
        when (priority) {
            CachePriority.LOW -> {
                setMaxCacheSizeMB(50)
                offlineDataManager.setDataFreshnessThreshold(12 * 60 * 60 * 1000) // 12 hours
                setPrefetchDays(1)
            }
            CachePriority.MEDIUM -> {
                setMaxCacheSizeMB(100)
                offlineDataManager.setDataFreshnessThreshold(24 * 60 * 60 * 1000) // 24 hours
                setPrefetchDays(3)
            }
            CachePriority.HIGH -> {
                setMaxCacheSizeMB(250)
                offlineDataManager.setDataFreshnessThreshold(48 * 60 * 60 * 1000) // 48 hours
                setPrefetchDays(7)
            }
        }
    }

    /**
     * Get the maximum cache size in MB
     */
    suspend fun getMaxCacheSizeMB(): Int {
        return context.offlineAccessDataStore.data.map { preferences ->
            preferences[MAX_CACHE_SIZE_MB] ?: 100
        }.first()
    }

    /**
     * Set the maximum cache size in MB
     */
    suspend fun setMaxCacheSizeMB(sizeMB: Int) {
        context.offlineAccessDataStore.edit { preferences ->
            preferences[MAX_CACHE_SIZE_MB] = sizeMB.coerceAtLeast(10) // Minimum 10MB
        }
    }

    /**
     * Get the cache cleanup interval in milliseconds
     */
    suspend fun getCacheCleanupInterval(): Long {
        return context.offlineAccessDataStore.data.map { preferences ->
            preferences[CACHE_CLEANUP_INTERVAL] ?: (24 * 60 * 60 * 1000L)
        }.first()
    }

    /**
     * Set the cache cleanup interval in milliseconds
     */
    suspend fun setCacheCleanupInterval(intervalMs: Long) {
        context.offlineAccessDataStore.edit { preferences ->
            preferences[CACHE_CLEANUP_INTERVAL] = intervalMs
        }
    }

    /**
     * Clean up old cache entries based on current settings
     */
    suspend fun cleanupCache() {
        val maxCacheSizeMB = getMaxCacheSizeMB()
        val maxCacheSizeBytes = maxCacheSizeMB * 1024 * 1024L
        
        // Check current cache size
        val currentCacheSize = cacheManager.getTotalCacheSize()
        
        if (currentCacheSize > maxCacheSizeBytes) {
            // Cache is too large, clean up old entries
            val cleanupInterval = getCacheCleanupInterval()
            cacheManager.clearOldCaches(cleanupInterval)
        }
    }

    /**
     * Get the total size of all caches in MB
     */
    suspend fun getTotalCacheSizeMB(): Float {
        val totalBytes = cacheManager.getTotalCacheSize()
        return totalBytes / (1024f * 1024f)
    }

    /**
     * Clear all cached data
     */
    suspend fun clearAllCachedData() {
        offlineDataManager.clearAllCachedData()
        cacheManager.clearAllCaches()
    }

    /**
     * Get all locations that have cached data available
     */
    suspend fun getLocationsWithCachedData(): List<Location> {
        return offlineDataManager.getLocationsWithCachedData()
    }

    /**
     * Check if there is any cached data available for a location
     */
    suspend fun hasCachedData(locationId: String): Boolean {
        return offlineDataManager.hasCachedData(locationId)
    }

    /**
     * Get data freshness percentage for a location
     */
    suspend fun getDataFreshnessPercentage(locationId: String): Int {
        return offlineDataManager.getDataFreshnessPercentage(locationId)
    }

    /**
     * Get the timestamp when data for a location will become stale
     */
    suspend fun getDataExpirationTime(locationId: String): Date? {
        return offlineDataManager.getDataExpirationTime(locationId)
    }
}