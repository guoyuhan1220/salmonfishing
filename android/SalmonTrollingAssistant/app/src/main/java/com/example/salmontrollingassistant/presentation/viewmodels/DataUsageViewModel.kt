package com.example.salmontrollingassistant.presentation.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.data.service.DataUsageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataUsageViewModel @Inject constructor(
    private val dataUsageManager: DataUsageManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Data usage limit settings
    val dataUsageLimitEnabled: StateFlow<Boolean> = dataUsageManager.isDataUsageLimitEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val dataUsageLimit: StateFlow<Int> = dataUsageManager.getDataUsageLimit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)
    
    // WiFi-only prefetch setting
    val wifiOnlyPrefetch: StateFlow<Boolean> = dataUsageManager.isWifiOnlyPrefetchEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    // Compression settings
    val compressionEnabled: StateFlow<Boolean> = dataUsageManager.isCompressionEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val compressionQuality: StateFlow<Int> = dataUsageManager.getCompressionQuality()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)
    
    // Current data usage
    val currentDataUsage: StateFlow<Long> = dataUsageManager.getCurrentDataUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    // Current network type
    val networkType: StateFlow<String> = dataUsageManager.getCurrentDataUsage() // Using this flow as a trigger
        .map { getCurrentNetworkType() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "UNKNOWN")
    
    /**
     * Set data usage limit enabled/disabled
     */
    fun setDataUsageLimitEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataUsageManager.setDataUsageLimitEnabled(enabled)
        }
    }
    
    /**
     * Set data usage limit in MB
     */
    fun setDataUsageLimit(limitMb: Int) {
        viewModelScope.launch {
            dataUsageManager.setDataUsageLimit(limitMb)
        }
    }
    
    /**
     * Set WiFi-only prefetch enabled/disabled
     */
    fun setWifiOnlyPrefetch(enabled: Boolean) {
        viewModelScope.launch {
            dataUsageManager.setWifiOnlyPrefetch(enabled)
        }
    }
    
    /**
     * Set compression enabled/disabled
     */
    fun setCompressionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataUsageManager.setCompressionEnabled(enabled)
        }
    }
    
    /**
     * Set compression quality (0-100)
     */
    fun setCompressionQuality(quality: Int) {
        viewModelScope.launch {
            dataUsageManager.setCompressionQuality(quality)
        }
    }
    
    /**
     * Reset data usage counter
     */
    fun resetDataUsage() {
        viewModelScope.launch {
            dataUsageManager.resetDataUsage()
        }
    }
    
    /**
     * Get the current network type as a string
     */
    private fun getCurrentNetworkType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "NONE"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "NONE"
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "OTHER"
        }
    }
}