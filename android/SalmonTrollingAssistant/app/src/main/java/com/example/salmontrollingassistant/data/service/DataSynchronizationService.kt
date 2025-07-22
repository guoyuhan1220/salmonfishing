package com.example.salmontrollingassistant.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for synchronizing data between local storage and remote APIs
 * with optimized background processing
 */
@Singleton
class DataSynchronizationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val offlineDataManager: OfflineDataManager,
    private val batteryOptimizationManager: BatteryOptimizationManager,
    private val networkBatchManager: NetworkBatchManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val workManager = WorkManager.getInstance(context)
    
    // Sync status
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    // Network status
    private val _networkStatus = MutableStateFlow(NetworkStatus.UNKNOWN)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    // Sync work name
    private val SYNC_WORK_NAME = "data_sync_work"
    
    init {
        // Monitor network connectivity
        setupNetworkCallback()
        
        // Schedule periodic sync based on user preferences
        setupPeriodicSync()
    }
    
    /**
     * Sync statuses
     */
    enum class SyncStatus {
        IDLE,
        SYNCING,
        SUCCESS,
        ERROR,
        WAITING_FOR_NETWORK
    }
    
    /**
     * Network statuses
     */
    enum class NetworkStatus {
        CONNECTED_WIFI,
        CONNECTED_CELLULAR,
        CONNECTED_OTHER,
        DISCONNECTED,
        UNKNOWN
    }
    
    /**
     * Trigger an immediate data synchronization
     * @param forceSync If true, sync will be performed even on metered connections
     */
    fun syncNow(forceSync: Boolean = false) {
        scope.launch {
            // Check if we should sync based on network and battery conditions
            if (!shouldSync(forceSync)) {
                _syncStatus.value = SyncStatus.WAITING_FOR_NETWORK
                return@launch
            }
            
            _syncStatus.value = SyncStatus.SYNCING
            
            try {
                // Perform sync operations
                performSync()
                
                // Update last sync timestamp
                offlineDataManager.updateLastSyncTimestamp()
                offlineDataManager.updateSyncStatus(OfflineDataManager.SyncStatus.SYNCED)
                
                _syncStatus.value = SyncStatus.SUCCESS
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR
                offlineDataManager.updateSyncStatus(OfflineDataManager.SyncStatus.FAILED)
            }
        }
    }
    
    /**
     * Set up periodic background synchronization
     */
    private fun setupPeriodicSync() {
        scope.launch {
            // Get user preferences for sync
            val wifiOnly = offlineDataManager.isWifiOnlySyncEnabled().first()
            val autoSync = offlineDataManager.isAutoSyncEnabled().first()
            
            if (!autoSync) {
                // Cancel any existing work if auto-sync is disabled
                workManager.cancelUniqueWork(SYNC_WORK_NAME)
                return@launch
            }
            
            // Set network constraints
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true) // Don't sync when battery is low
                .build()
            
            // Create periodic work request
            // Interval depends on battery optimization mode
            val syncInterval = when (batteryOptimizationManager.getOptimizationMode().map { it }.collect { it }) {
                BatteryOptimizationManager.OptimizationMode.PERFORMANCE -> 15L // 15 minutes
                BatteryOptimizationManager.OptimizationMode.BALANCED -> 30L // 30 minutes
                BatteryOptimizationManager.OptimizationMode.BATTERY_SAVER -> 60L // 60 minutes
            }
            
            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                syncInterval, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // Flex period
            )
                .setConstraints(constraints)
                .build()
            
            // Enqueue the work
            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                syncWorkRequest
            )
        }
    }
    
    /**
     * Set up network callback to monitor connectivity changes
     */
    private fun setupNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateNetworkStatus()
                
                // If we were waiting for network, try to sync now
                if (_syncStatus.value == SyncStatus.WAITING_FOR_NETWORK) {
                    syncNow(false)
                }
            }
            
            override fun onLost(network: Network) {
                updateNetworkStatus()
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetworkStatus()
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    /**
     * Update the current network status
     */
    private fun updateNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        _networkStatus.value = when {
            capabilities == null -> NetworkStatus.DISCONNECTED
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.CONNECTED_WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CONNECTED_CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.CONNECTED_OTHER
            else -> NetworkStatus.UNKNOWN
        }
    }
    
    /**
     * Check if we should perform sync based on network and battery conditions
     */
    private suspend fun shouldSync(forceSync: Boolean): Boolean {
        // If force sync, always proceed
        if (forceSync) return true
        
        // Check network conditions
        val wifiOnly = offlineDataManager.isWifiOnlySyncEnabled().first()
        if (wifiOnly && _networkStatus.value != NetworkStatus.CONNECTED_WIFI) {
            return false
        }
        
        // Check if we're disconnected
        if (_networkStatus.value == NetworkStatus.DISCONNECTED) {
            return false
        }
        
        // Check battery conditions
        val batteryLevel = batteryOptimizationManager.getBatteryLevel()
        val lowBatteryThreshold = batteryOptimizationManager.getLowBatteryThreshold().first()
        
        // Don't sync if battery is below threshold and not charging
        if (batteryLevel <= lowBatteryThreshold && !batteryOptimizationManager.isCharging()) {
            return false
        }
        
        return true
    }
    
    /**
     * Perform the actual synchronization operations
     */
    private suspend fun performSync() {
        // This would contain the actual sync logic
        // For now, we'll just update the sync status
        offlineDataManager.updateSyncStatus(OfflineDataManager.SyncStatus.SYNCING)
        
        // Simulate some work
        kotlinx.coroutines.delay(1000)
        
        // Update sync status
        offlineDataManager.updateSyncStatus(OfflineDataManager.SyncStatus.SYNCED)
    }
    
    /**
     * Worker class for background synchronization
     */
    class SyncWorker(
        appContext: Context,
        workerParams: WorkerParameters
    ) : Worker(appContext, workerParams) {
        
        override fun doWork(): Result {
            // Get the sync service
            val syncService = DataSynchronizationService(
                applicationContext,
                applicationContext.getSystemService(OfflineDataManager::class.java),
                applicationContext.getSystemService(BatteryOptimizationManager::class.java),
                applicationContext.getSystemService(NetworkBatchManager::class.java)
            )
            
            // Trigger sync
            syncService.syncNow(false)
            
            return Result.success()
        }
    }
}