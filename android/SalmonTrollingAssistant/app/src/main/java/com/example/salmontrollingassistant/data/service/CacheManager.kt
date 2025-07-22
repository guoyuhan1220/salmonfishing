package com.example.salmontrollingassistant.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.room.Room
import com.example.salmontrollingassistant.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages caching strategies for different types of data
 * to optimize app loading and performance
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Memory cache for images
    private val memoryCache: LruCache<String, Bitmap>
    
    // Database instance for structured data caching
    private val database: AppDatabase
    
    // Cache directories
    private val imageCacheDir: File
    private val dataCacheDir: File
    
    init {
        // Initialize memory cache with 1/8th of available memory
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // Size in kilobytes
                return bitmap.byteCount / 1024
            }
        }
        
        // Initialize database
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "salmon_trolling_assistant_db"
        ).build()
        
        // Initialize cache directories
        imageCacheDir = File(context.cacheDir, "images")
        if (!imageCacheDir.exists()) {
            imageCacheDir.mkdirs()
        }
        
        dataCacheDir = File(context.cacheDir, "data")
        if (!dataCacheDir.exists()) {
            dataCacheDir.mkdirs()
        }
    }
    
    /**
     * Cache an image in memory and disk
     * @param key Unique identifier for the image
     * @param bitmap The bitmap to cache
     * @param quality Compression quality (0-100)
     */
    suspend fun cacheImage(key: String, bitmap: Bitmap, quality: Int = 85) {
        // Add to memory cache
        memoryCache.put(key, bitmap)
        
        // Save to disk cache
        withContext(Dispatchers.IO) {
            try {
                val file = File(imageCacheDir, hashKey(key))
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Get an image from cache
     * @param key Unique identifier for the image
     * @return The cached bitmap or null if not found
     */
    suspend fun getImage(key: String): Bitmap? {
        // Check memory cache first
        val memoryBitmap = memoryCache.get(key)
        if (memoryBitmap != null) {
            return memoryBitmap
        }
        
        // Check disk cache
        return withContext(Dispatchers.IO) {
            try {
                val file = File(imageCacheDir, hashKey(key))
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    // Add to memory cache
                    if (bitmap != null) {
                        memoryCache.put(key, bitmap)
                    }
                    bitmap
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Cache data to disk
     * @param key Unique identifier for the data
     * @param data The data to cache
     */
    suspend fun cacheData(key: String, data: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(dataCacheDir, hashKey(key))
                FileOutputStream(file).use { out ->
                    out.write(data)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Get data from disk cache
     * @param key Unique identifier for the data
     * @return The cached data or null if not found
     */
    suspend fun getData(key: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(dataCacheDir, hashKey(key))
                if (file.exists()) {
                    file.readBytes()
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Clear all caches
     */
    suspend fun clearAllCaches() {
        // Clear memory cache
        memoryCache.evictAll()
        
        // Clear disk caches
        withContext(Dispatchers.IO) {
            imageCacheDir.listFiles()?.forEach { it.delete() }
            dataCacheDir.listFiles()?.forEach { it.delete() }
        }
    }
    
    /**
     * Clear old cache entries
     * @param maxAgeMs Maximum age in milliseconds
     */
    suspend fun clearOldCaches(maxAgeMs: Long) {
        val cutoffTime = System.currentTimeMillis() - maxAgeMs
        
        withContext(Dispatchers.IO) {
            // Clear old image cache files
            imageCacheDir.listFiles()?.forEach {
                if (it.lastModified() < cutoffTime) {
                    it.delete()
                }
            }
            
            // Clear old data cache files
            dataCacheDir.listFiles()?.forEach {
                if (it.lastModified() < cutoffTime) {
                    it.delete()
                }
            }
        }
    }
    
    /**
     * Get the total size of all caches in bytes
     */
    suspend fun getTotalCacheSize(): Long {
        return withContext(Dispatchers.IO) {
            var size = 0L
            
            // Add image cache size
            imageCacheDir.listFiles()?.forEach {
                size += it.length()
            }
            
            // Add data cache size
            dataCacheDir.listFiles()?.forEach {
                size += it.length()
            }
            
            size
        }
    }
    
    /**
     * Hash a key to create a filename
     */
    private fun hashKey(key: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(key.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}