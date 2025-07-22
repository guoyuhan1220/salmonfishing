package com.example.salmontrollingassistant.data.service

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataUsageStore by preferencesDataStore(name = "data_usage_settings")

/**
 * Manages data usage optimization strategies
 */
@Singleton
class DataUsageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys for data usage settings
    private val DATA_USAGE_LIMIT_ENABLED = booleanPreferencesKey("data_usage_limit_enabled")
    private val DATA_USAGE_LIMIT_MB = intPreferencesKey("data_usage_limit_mb")
    private val WIFI_ONLY_PREFETCH = booleanPreferencesKey("wifi_only_prefetch")
    private val COMPRESSION_ENABLED = booleanPreferencesKey("compression_enabled")
    private val COMPRESSION_QUALITY = intPreferencesKey("compression_quality")
    private val LAST_RESET_DATE = longPreferencesKey("last_reset_date")
    private val CURRENT_USAGE_MB = longPreferencesKey("current_usage_mb")
    
    // Connectivity manager for network status
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Check if data usage limit is enabled
     */
    fun isDataUsageLimitEnabled(): Flow<Boolean> {
        return context.dataUsageStore.data.map { preferences ->
            preferences[DATA_USAGE_LIMIT_ENABLED] ?: false
        }
    }
    
    /**
     * Set data usage limit enabled/disabled
     */
    suspend fun setDataUsageLimitEnabled(enabled: Boolean) {
        context.dataUsageStore.edit { preferences ->
            preferences[DATA_USAGE_LIMIT_ENABLED] = enabled
        }
    }
    
    /**
     * Get the data usage limit in MB
     */
    fun getDataUsageLimit(): Flow<Int> {
        return context.dataUsageStore.data.map { preferences ->
            preferences[DATA_USAGE_LIMIT_MB] ?: 100 // Default to 100MB
        }
    }
    
    /**
     * Set the data usage limit in MB
     */
    suspend fun setDataUsageLimit(limitMb: Int) {
        context.dataUsageStore.edit { preferences ->
            preferences[DATA_USAGE_LIMIT_MB] = limitMb.coerceAtLeast(1)
        }
    }
    
    /**
     * Check if WiFi-only prefetching is enabled
     */
    fun isWifiOnlyPrefetchEnabled(): Flow<Boolean> {
        return context.dataUsageStore.data.map { preferences ->
            preferences[WIFI_ONLY_PREFETCH] ?: true // Default to true
        }
    }
    
    /**
     * Set WiFi-only prefetching enabled/disabled
     */
    suspend fun setWifiOnlyPrefetch(enabled: Boolean) {
        context.dataUsageStore.edit { preferences ->
            preferences[WIFI_ONLY_PREFETCH] = enabled
        }
    }
    
    /**
     * Check if compression is enabled
     */
    fun isCompressionEnabled(): Flow<Boolean> {
        return context.dataUsageStore.data.map { preferences ->
            preferences[COMPRESSION_ENABLED] ?: true // Default to true
        }
    }
    
    /**
     * Set compression enabled/disabled
     */
    suspend fun setCompressionEnabled(enabled: Boolean) {
        context.dataUsageStore.edit { preferences ->
            preferences[COMPRESSION_ENABLED] = enabled
        }
    }
    
    /**
     * Get the compression quality (0-100)
     */
    fun getCompressionQuality(): Flow<Int> {
        return context.dataUsageStore.data.map { preferences ->
            preferences[COMPRESSION_QUALITY] ?: 75 // Default to 75%
        }
    }
    
    /**
     * Set the compression quality (0-100)
     */
    suspend fun setCompressionQuality(quality: Int) {
        context.dataUsageStore.edit { preferences ->
            preferences[COMPRESSION_QUALITY] = quality.coerceIn(0, 100)
        }
    }
    
    /**
     * Get the current data usage in MB
     */
    fun getCurrentDataUsage(): Flow<Long> {
        return context.dataUsageStore.data.map { preferences ->
            preferences[CURRENT_USAGE_MB] ?: 0L
        }
    }
    
    /**
     * Record data usage
     * @param bytes The number of bytes used
     */
    suspend fun recordDataUsage(bytes: Long) {
        context.dataUsageStore.edit { preferences ->
            val currentUsage = preferences[CURRENT_USAGE_MB] ?: 0L
            val bytesInMb = bytes / (1024 * 1024)
            preferences[CURRENT_USAGE_MB] = currentUsage + bytesInMb
        }
    }
    
    /**
     * Reset data usage counter
     */
    suspend fun resetDataUsage() {
        context.dataUsageStore.edit { preferences ->
            preferences[CURRENT_USAGE_MB] = 0L
            preferences[LAST_RESET_DATE] = System.currentTimeMillis()
        }
    }
    
    /**
     * Check if the app should prefetch data based on current network conditions
     */
    suspend fun shouldPrefetch(): Boolean {
        val wifiOnlyPrefetch = isWifiOnlyPrefetchEnabled().map { it }.collect { it }
        
        // If WiFi-only prefetch is enabled, check if we're on WiFi
        if (wifiOnlyPrefetch) {
            return isOnWifi()
        }
        
        // Otherwise, check if we're under the data limit
        val limitEnabled = isDataUsageLimitEnabled().map { it }.collect { it }
        if (limitEnabled) {
            val currentUsage = getCurrentDataUsage().map { it }.collect { it }
            val limit = getDataUsageLimit().map { it }.collect { it }
            
            return currentUsage < limit
        }
        
        // If no restrictions, allow prefetching
        return true
    }
    
    /**
     * Check if the device is currently on a WiFi network
     */
    fun isOnWifi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Check if the device is on a metered connection
     */
    fun isOnMeteredConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
        
        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
    
    /**
     * Get the estimated data usage for the current month (requires permission)
     * Note: This requires the READ_NETWORK_USAGE_HISTORY permission
     */
    fun getMonthlyDataUsage(): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
                
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                
                // Start of month
                calendar.set(year, month, 1, 0, 0, 0)
                val startTime = calendar.timeInMillis
                
                // End of month (now)
                calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                val endTime = System.currentTimeMillis().coerceAtMost(calendar.timeInMillis)
                
                // Get mobile data usage
                val subscriberId = "" // This requires additional permissions
                val mobileStats = networkStatsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_MOBILE,
                    subscriberId,
                    startTime,
                    endTime
                )
                
                return mobileStats.rxBytes + mobileStats.txBytes
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Fallback to stored usage
        return 0L
    }
}