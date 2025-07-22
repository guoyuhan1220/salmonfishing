package com.example.salmontrollingassistant.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages batching of network requests to optimize battery and data usage
 */
@Singleton
class NetworkBatchManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryOptimizationManager: BatteryOptimizationManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    
    // Queue of pending requests by endpoint
    private val requestQueues = ConcurrentHashMap<String, ConcurrentLinkedQueue<NetworkRequest<*>>>()
    
    // Scheduled batch processing
    private val scheduledBatches = ConcurrentHashMap<String, Boolean>()
    
    /**
     * Add a request to the batch queue
     * @param endpoint The API endpoint identifier
     * @param request The network request to execute
     * @param priority Priority of the request (higher number = higher priority)
     */
    fun <T> enqueueRequest(endpoint: String, request: suspend () -> T, priority: Int = 0): NetworkRequest<T> {
        val networkRequest = NetworkRequest(request, priority)
        
        // Get or create queue for this endpoint
        val queue = requestQueues.getOrPut(endpoint) { ConcurrentLinkedQueue() }
        queue.add(networkRequest as NetworkRequest<*>)
        
        // Schedule batch processing if not already scheduled
        if (scheduledBatches.putIfAbsent(endpoint, true) == null) {
            scheduleBatchProcessing(endpoint)
        }
        
        return networkRequest
    }
    
    /**
     * Schedule batch processing for an endpoint
     */
    private fun scheduleBatchProcessing(endpoint: String) {
        scope.launch {
            val batchWindow = batteryOptimizationManager.getNetworkBatchWindow()
            
            withContext(Dispatchers.Main) {
                handler.postDelayed({
                    processBatch(endpoint)
                }, batchWindow)
            }
        }
    }
    
    /**
     * Process all requests in the batch for an endpoint
     */
    private fun processBatch(endpoint: String) {
        scope.launch {
            val queue = requestQueues[endpoint] ?: return@launch
            
            // Mark batch as no longer scheduled
            scheduledBatches.remove(endpoint)
            
            // Sort requests by priority (higher priority first)
            val sortedRequests = queue.sortedByDescending { it.priority }
            
            // Clear the queue
            queue.clear()
            
            // Process requests based on network conditions
            val networkType = getNetworkType()
            
            // If on metered connection and in battery saver mode, only process high priority requests
            val shouldProcessAllRequests = networkType != NetworkType.METERED || 
                                         batteryOptimizationManager.getOptimizationMode().map { it }.collect { it } != 
                                         BatteryOptimizationManager.OptimizationMode.BATTERY_SAVER
            
            val requestsToProcess = if (shouldProcessAllRequests) {
                sortedRequests
            } else {
                // Only process high priority requests (priority > 0)
                sortedRequests.filter { it.priority > 0 }
            }
            
            // Execute requests
            for (request in requestsToProcess) {
                try {
                    val result = request.execute()
                    request.complete(result)
                } catch (e: Exception) {
                    request.completeExceptionally(e)
                }
            }
            
            // If there are still requests in the queue (new ones added during processing),
            // schedule another batch
            if (requestQueues[endpoint]?.isNotEmpty() == true && 
                scheduledBatches.putIfAbsent(endpoint, true) == null) {
                scheduleBatchProcessing(endpoint)
            }
        }
    }
    
    /**
     * Get the current network type
     */
    private fun getNetworkType(): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                    NetworkType.UNMETERED
                } else {
                    NetworkType.METERED
                }
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }
    }
    
    /**
     * Network types for connection management
     */
    enum class NetworkType {
        WIFI,
        ETHERNET,
        METERED,
        UNMETERED,
        OTHER,
        NONE
    }
    
    /**
     * Represents a network request with its execution function and completion handlers
     */
    class NetworkRequest<T>(
        private val requestFn: suspend () -> T,
        val priority: Int
    ) {
        private val callbacks = mutableListOf<(Result<T>) -> Unit>()
        private var result: Result<T>? = null
        
        /**
         * Execute the network request
         */
        suspend fun execute(): Any? {
            return requestFn()
        }
        
        /**
         * Complete the request with a successful result
         */
        @Suppress("UNCHECKED_CAST")
        fun complete(value: Any?) {
            val typedResult = value as T
            result = Result.success(typedResult)
            notifyCallbacks()
        }
        
        /**
         * Complete the request with an exception
         */
        fun completeExceptionally(exception: Throwable) {
            result = Result.failure(exception)
            notifyCallbacks()
        }
        
        /**
         * Add a callback to be notified when the request completes
         */
        fun onComplete(callback: (Result<T>) -> Unit) {
            synchronized(callbacks) {
                // If result is already available, notify immediately
                result?.let {
                    callback(it)
                    return
                }
                
                callbacks.add(callback)
            }
        }
        
        /**
         * Notify all callbacks of the result
         */
        private fun notifyCallbacks() {
            synchronized(callbacks) {
                val currentResult = result ?: return
                callbacks.forEach { it(currentResult) }
                callbacks.clear()
            }
        }
    }
}