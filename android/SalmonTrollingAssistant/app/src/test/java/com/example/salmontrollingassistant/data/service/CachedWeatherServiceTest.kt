package com.example.salmontrollingassistant.data.service

import com.example.salmontrollingassistant.data.db.WeatherDao
import com.example.salmontrollingassistant.data.db.WeatherEntity
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.WeatherData
import com.example.salmontrollingassistant.domain.service.WeatherException
import com.example.salmontrollingassistant.domain.service.WeatherResult
import com.example.salmontrollingassistant.domain.service.WeatherService
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class CachedWeatherServiceTest {

    // Mock dependencies
    private val mockWeatherService: WeatherService = mockk()
    private val mockWeatherDao: WeatherDao = mockk()
    
    // Test data
    private lateinit var testLocation: Location
    private lateinit var testWeatherData: WeatherData
    private lateinit var testWeatherEntity: WeatherEntity
    
    // System under test
    private lateinit var cachedWeatherService: CachedWeatherService
    
    @Before
    fun setup() {
        // Setup test data
        testLocation = Location(
            id = "location1",
            name = "Test Location",
            latitude = 47.6062,
            longitude = -122.3321
        )
        
        testWeatherData = WeatherData(
            id = "weather1",
            timestamp = Date(),
            temperature = 22.0,
            windSpeed = 5.0,
            windDirection = "N",
            precipitation = 0.0,
            cloudCover = 20,
            visibility = 10.0,
            pressure = 1013.0,
            humidity = 60,
            uvIndex = 5
        )
        
        testWeatherEntity = WeatherEntity(
            id = "weather1",
            locationId = "location1",
            timestamp = testWeatherData.timestamp.time,
            temperature = 22.0,
            windSpeed = 5.0,
            windDirection = "N",
            precipitation = 0.0,
            cloudCover = 20,
            visibility = 10.0,
            pressure = 1013.0,
            humidity = 60,
            uvIndex = 5,
            waterTemperature = null,
            isForecast = false,
            cacheTimestamp = System.currentTimeMillis()
        )
        
        // Create the service with mocked dependencies
        cachedWeatherService = CachedWeatherService(mockWeatherService, mockWeatherDao)
    }
    
    @Test
    fun `getCurrentWeather should return cached data when cache is valid`() = runBlocking {
        // Given
        coEvery { mockWeatherDao.getCurrentWeather(testLocation.id) } returns testWeatherEntity
        
        // When
        val results = cachedWeatherService.getCurrentWeather(testLocation).toList()
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0]).isInstanceOf(WeatherResult.Success::class.java)
        
        val weatherData = (results[0] as WeatherResult.Success).data
        assertThat(weatherData.id).isEqualTo(testWeatherEntity.id)
        assertThat(weatherData.temperature).isEqualTo(testWeatherEntity.temperature)
        
        // Verify that the remote service was not called
        coVerify(exactly = 0) { mockWeatherService.getCurrentWeather(any()) }
    }
    
    @Test
    fun `getCurrentWeather should fetch from service when cache is invalid`() = runBlocking {
        // Given
        val expiredEntity = testWeatherEntity.copy(
            cacheTimestamp = System.currentTimeMillis() - 60 * 60 * 1000 // 1 hour old (expired)
        )
        coEvery { mockWeatherDao.getCurrentWeather(testLocation.id) } returns expiredEntity
        coEvery { mockWeatherService.getCurrentWeather(testLocation) } returns flowOf(WeatherResult.Success(testWeatherData))
        coEvery { mockWeatherDao.deleteCurrentWeather(testLocation.id) } just Runs
        coEvery { mockWeatherDao.insertCurrentWeather(any()) } just Runs
        
        // When
        val results = cachedWeatherService.getCurrentWeather(testLocation).toList()
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0]).isInstanceOf(WeatherResult.Success::class.java)
        
        // Verify that the remote service was called
        coVerify(exactly = 1) { mockWeatherService.getCurrentWeather(testLocation) }
        
        // Verify that the cache was updated
        coVerify(exactly = 1) { mockWeatherDao.deleteCurrentWeather(testLocation.id) }
        coVerify(exactly = 1) { mockWeatherDao.insertCurrentWeather(any()) }
    }
    
    @Test
    fun `getCurrentWeather should return cached data when service fails`() = runBlocking {
        // Given
        val expiredEntity = testWeatherEntity.copy(
            cacheTimestamp = System.currentTimeMillis() - 60 * 60 * 1000 // 1 hour old (expired)
        )
        coEvery { mockWeatherDao.getCurrentWeather(testLocation.id) } returns expiredEntity
        coEvery { mockWeatherService.getCurrentWeather(testLocation) } returns flowOf(
            WeatherResult.Error(WeatherException("Network error"))
        )
        
        // When
        val results = cachedWeatherService.getCurrentWeather(testLocation).toList()
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0]).isInstanceOf(WeatherResult.Success::class.java)
        
        val weatherData = (results[0] as WeatherResult.Success).data
        assertThat(weatherData.id).isEqualTo(expiredEntity.id)
        
        // Verify that the remote service was called
        coVerify(exactly = 1) { mockWeatherService.getCurrentWeather(testLocation) }
        
        // Verify that the cache was not updated
        coVerify(exactly = 0) { mockWeatherDao.deleteCurrentWeather(any()) }
        coVerify(exactly = 0) { mockWeatherDao.insertCurrentWeather(any()) }
    }
    
    @Test
    fun `getForecast should return cached data when cache is valid`() = runBlocking {
        // Given
        val forecastEntities = listOf(
            testWeatherEntity.copy(id = "forecast1", isForecast = true),
            testWeatherEntity.copy(id = "forecast2", isForecast = true),
            testWeatherEntity.copy(id = "forecast3", isForecast = true)
        )
        coEvery { mockWeatherDao.getForecast(testLocation.id) } returns forecastEntities
        
        // When
        val results = cachedWeatherService.getForecast(testLocation, 3).toList()
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0]).isInstanceOf(WeatherResult.Success::class.java)
        
        val forecastData = (results[0] as WeatherResult.Success).data
        assertThat(forecastData).hasSize(3)
        
        // Verify that the remote service was not called
        coVerify(exactly = 0) { mockWeatherService.getForecast(any(), any()) }
    }
    
    @Test
    fun `getForecast should fetch from service when cache is invalid`() = runBlocking {
        // Given
        val expiredEntities = listOf(
            testWeatherEntity.copy(
                id = "forecast1", 
                isForecast = true,
                cacheTimestamp = System.currentTimeMillis() - 4 * 60 * 60 * 1000 // 4 hours old (expired)
            )
        )
        val forecastData = listOf(
            testWeatherData.copy(id = "new_forecast1"),
            testWeatherData.copy(id = "new_forecast2"),
            testWeatherData.copy(id = "new_forecast3")
        )
        
        coEvery { mockWeatherDao.getForecast(testLocation.id) } returns expiredEntities
        coEvery { mockWeatherService.getForecast(testLocation, 3) } returns flowOf(WeatherResult.Success(forecastData))
        coEvery { mockWeatherDao.deleteForecast(testLocation.id) } just Runs
        coEvery { mockWeatherDao.insertForecast(any()) } just Runs
        
        // When
        val results = cachedWeatherService.getForecast(testLocation, 3).toList()
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0]).isInstanceOf(WeatherResult.Success::class.java)
        
        val resultData = (results[0] as WeatherResult.Success).data
        assertThat(resultData).hasSize(3)
        
        // Verify that the remote service was called
        coVerify(exactly = 1) { mockWeatherService.getForecast(testLocation, 3) }
        
        // Verify that the cache was updated
        coVerify(exactly = 1) { mockWeatherDao.deleteForecast(testLocation.id) }
        coVerify(exactly = 1) { mockWeatherDao.insertForecast(any()) }
    }
    
    @Test
    fun `getWeatherForDateTime should find matching forecast in cache`() = runBlocking {
        // Given
        val targetDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }.time
        
        val tomorrowMidnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val forecastEntities = listOf(
            testWeatherEntity.copy(id = "forecast1", isForecast = true),
            testWeatherEntity.copy(
                id = "forecast2", 
                isForecast = true,
                timestamp = tomorrowMidnight.time
            ),
            testWeatherEntity.copy(id = "forecast3", isForecast = true)
        )
        
        coEvery { mockWeatherDao.getForecast(testLocation.id) } returns forecastEntities
        
        // When
        val results = cachedWeatherService.getWeatherForDateTime(testLocation, targetDate).toList()
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0]).isInstanceOf(WeatherResult.Success::class.java)
        
        val weatherData = (results[0] as WeatherResult.Success).data
        assertThat(weatherData.id).isEqualTo("forecast2")
        
        // Verify that the remote service was not called
        coVerify(exactly = 0) { mockWeatherService.getWeatherForDateTime(any(), any()) }
    }
    
    @Test
    fun `clearCache should delete all weather data`() = runBlocking {
        // Given
        coEvery { mockWeatherDao.clearAllWeatherData() } just Runs
        
        // When
        cachedWeatherService.clearCache()
        
        // Then
        coVerify(exactly = 1) { mockWeatherDao.clearAllWeatherData() }
    }
}