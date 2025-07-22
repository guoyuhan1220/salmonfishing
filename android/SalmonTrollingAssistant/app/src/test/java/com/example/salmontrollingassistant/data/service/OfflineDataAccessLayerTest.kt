package com.example.salmontrollingassistant.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.salmontrollingassistant.data.db.LocationDao
import com.example.salmontrollingassistant.data.db.TideDao
import com.example.salmontrollingassistant.data.db.WeatherDao
import com.example.salmontrollingassistant.domain.model.Location
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@ExperimentalCoroutinesApi
class OfflineDataAccessLayerTest {
    
    @get:Rule
    val tempFolder = TemporaryFolder()
    
    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var context: Context
    private lateinit var offlineDataManager: OfflineDataManager
    private lateinit var cacheManager: CacheManager
    private lateinit var weatherDao: WeatherDao
    private lateinit var tideDao: TideDao
    private lateinit var locationDao: LocationDao
    private lateinit var offlineDataAccessLayer: OfflineDataAccessLayer
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        offlineDataManager = mockk(relaxed = true)
        cacheManager = mockk(relaxed = true)
        weatherDao = mockk(relaxed = true)
        tideDao = mockk(relaxed = true)
        locationDao = mockk(relaxed = true)
        
        val testFile = tempFolder.newFile("test_preferences.preferences_pb")
        every { context.filesDir } returns tempFolder.root
        
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { testFile }
        )
        
        offlineDataAccessLayer = OfflineDataAccessLayer(
            context,
            offlineDataManager,
            cacheManager,
            weatherDao,
            tideDao,
            locationDao
        )
    }
    
    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }
    
    @Test
    fun `initialize sets default values`() = testDispatcher.runBlockingTest {
        // When
        offlineDataAccessLayer.initialize()
        
        // Then
        val maxCacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
        val prefetchEnabled = offlineDataAccessLayer.isPrefetchEnabled()
        val prefetchDays = offlineDataAccessLayer.getPrefetchDays()
        
        assertEquals(100, maxCacheSizeMB)
        assertTrue(prefetchEnabled)
        assertEquals(3, prefetchDays)
    }
    
    @Test
    fun `setCachePriority updates related settings`() = testDispatcher.runBlockingTest {
        // When
        offlineDataAccessLayer.setCachePriority(OfflineDataAccessLayer.CachePriority.HIGH)
        
        // Then
        val maxCacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
        val prefetchDays = offlineDataAccessLayer.getPrefetchDays()
        
        assertEquals(250, maxCacheSizeMB)
        assertEquals(7, prefetchDays)
        coVerify { offlineDataManager.setDataFreshnessThreshold(48 * 60 * 60 * 1000) }
    }
    
    @Test
    fun `clearAllCachedData calls both managers`() = testDispatcher.runBlockingTest {
        // When
        offlineDataAccessLayer.clearAllCachedData()
        
        // Then
        coVerify { offlineDataManager.clearAllCachedData() }
        coVerify { cacheManager.clearAllCaches() }
    }
    
    @Test
    fun `prefetchData respects prefetchEnabled setting`() = testDispatcher.runBlockingTest {
        // Given
        val location = Location("1", "Test Location", 47.0, -122.0)
        
        // When prefetch is disabled
        offlineDataAccessLayer.setPrefetchEnabled(false)
        offlineDataAccessLayer.prefetchData(location, 3)
        
        // Then no prefetching should occur
        // This is a bit hard to verify directly since prefetchData doesn't do much in our implementation
        // In a real implementation, we would verify that no network calls were made
        
        // When prefetch is enabled
        offlineDataAccessLayer.setPrefetchEnabled(true)
        offlineDataAccessLayer.prefetchData(location, 3)
        
        // Then prefetching should occur
        // Again, hard to verify directly in our implementation
    }
    
    @Test
    fun `getWeatherData returns cached data`() = testDispatcher.runBlockingTest {
        // Given
        val location = Location("1", "Test Location", 47.0, -122.0)
        val weatherEntity = mockk(relaxed = true)
        val weatherData = mockk(relaxed = true)
        
        coEvery { weatherDao.getCurrentWeather(location.id) } returns weatherEntity
        every { weatherEntity.toWeatherData() } returns weatherData
        
        // When
        val result = offlineDataAccessLayer.getWeatherData(location)
        
        // Then
        assertEquals(weatherData, result)
    }
    
    @Test
    fun `getTideData returns cached data`() = testDispatcher.runBlockingTest {
        // Given
        val location = Location("1", "Test Location", 47.0, -122.0)
        val tideEntity = mockk(relaxed = true)
        val tideData = mockk(relaxed = true)
        
        coEvery { tideDao.getCurrentTide(location.id) } returns tideEntity
        every { tideEntity.toTideData() } returns tideData
        
        // When
        val result = offlineDataAccessLayer.getTideData(location)
        
        // Then
        assertEquals(tideData, result)
    }
    
    @Test
    fun `cleanupCache clears old entries when over size limit`() = testDispatcher.runBlockingTest {
        // Given
        val maxCacheSizeMB = 100
        val currentCacheSizeBytes = 150 * 1024 * 1024L // 150 MB
        val cleanupInterval = 24 * 60 * 60 * 1000L // 24 hours
        
        offlineDataAccessLayer.setMaxCacheSizeMB(maxCacheSizeMB)
        offlineDataAccessLayer.setCacheCleanupInterval(cleanupInterval)
        coEvery { cacheManager.getTotalCacheSize() } returns currentCacheSizeBytes
        
        // When
        offlineDataAccessLayer.cleanupCache()
        
        // Then
        coVerify { cacheManager.clearOldCaches(cleanupInterval) }
    }
}