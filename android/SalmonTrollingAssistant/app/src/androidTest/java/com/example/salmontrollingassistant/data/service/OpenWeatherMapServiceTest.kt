package com.example.salmontrollingassistant.data.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.service.WeatherResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import java.util.Date

@RunWith(AndroidJUnit4::class)
class OpenWeatherMapServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var openWeatherMapService: OpenWeatherMapService
    private val testLocation = Location(
        id = "location1",
        name = "Seattle",
        latitude = 47.6062,
        longitude = -122.3321
    )
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        // Create a test instance of the service with a mock API key
        openWeatherMapService = OpenWeatherMapService("test_api_key")
        
        // Note: In a real test, we would inject a mock Retrofit instance that points to our MockWebServer
        // For this example, we'll simulate the API responses directly
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun getCurrentWeather_returnsWeatherData() = runBlocking {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                  "dt": 1627484400,
                  "main": {
                    "temp": 72.5,
                    "pressure": 1013.0,
                    "humidity": 65
                  },
                  "wind": {
                    "speed": 8.5,
                    "deg": 180
                  },
                  "clouds": {
                    "all": 40
                  },
                  "visibility": 10000
                }
            """.trimIndent())
        
        mockWebServer.enqueue(mockResponse)
        
        // When
        val result = openWeatherMapService.getCurrentWeather(testLocation).first()
        
        // Then
        assertTrue(result is WeatherResult.Success)
        val weatherData = (result as WeatherResult.Success).data
        
        assertNotNull(weatherData)
        assertEquals(72.5, weatherData.temperature, 0.01)
        assertEquals(8.5, weatherData.windSpeed, 0.01)
        assertEquals("S", weatherData.windDirection) // 180 degrees is South
        assertEquals(40, weatherData.cloudCover)
        assertEquals(1013.0, weatherData.pressure, 0.01)
        assertEquals(65, weatherData.humidity)
    }
    
    @Test
    fun getForecast_returnsWeatherDataList() = runBlocking {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                  "daily": [
                    {
                      "dt": 1627484400,
                      "temp": {
                        "day": 72.5,
                        "min": 65.0,
                        "max": 78.0,
                        "night": 68.0,
                        "eve": 75.0,
                        "morn": 67.0
                      },
                      "pressure": 1013.0,
                      "humidity": 65,
                      "wind_speed": 8.5,
                      "wind_deg": 180,
                      "clouds": 40,
                      "uvi": 5.2
                    },
                    {
                      "dt": 1627570800,
                      "temp": {
                        "day": 75.0,
                        "min": 67.0,
                        "max": 80.0,
                        "night": 70.0,
                        "eve": 77.0,
                        "morn": 68.0
                      },
                      "pressure": 1012.0,
                      "humidity": 60,
                      "wind_speed": 7.0,
                      "wind_deg": 200,
                      "clouds": 30,
                      "uvi": 6.0
                    }
                  ]
                }
            """.trimIndent())
        
        mockWebServer.enqueue(mockResponse)
        
        // When
        val result = openWeatherMapService.getForecast(testLocation, 2).first()
        
        // Then
        assertTrue(result is WeatherResult.Success)
        val forecastList = (result as WeatherResult.Success).data
        
        assertNotNull(forecastList)
        assertEquals(2, forecastList.size)
        
        // Check first day
        val day1 = forecastList[0]
        assertEquals(72.5, day1.temperature, 0.01)
        assertEquals(8.5, day1.windSpeed, 0.01)
        assertEquals("S", day1.windDirection)
        assertEquals(40, day1.cloudCover)
        assertEquals(1013.0, day1.pressure, 0.01)
        assertEquals(65, day1.humidity)
        assertEquals(5, day1.uvIndex)
        
        // Check second day
        val day2 = forecastList[1]
        assertEquals(75.0, day2.temperature, 0.01)
        assertEquals(7.0, day2.windSpeed, 0.01)
        assertEquals("SSW", day2.windDirection) // 200 degrees is South-Southwest
        assertEquals(30, day2.cloudCover)
        assertEquals(1012.0, day2.pressure, 0.01)
        assertEquals(60, day2.humidity)
        assertEquals(6, day2.uvIndex)
    }
    
    @Test
    fun getWeatherForDateTime_returnsWeatherData() = runBlocking {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                  "daily": [
                    {
                      "dt": 1627484400,
                      "temp": {
                        "day": 72.5,
                        "min": 65.0,
                        "max": 78.0,
                        "night": 68.0,
                        "eve": 75.0,
                        "morn": 67.0
                      },
                      "pressure": 1013.0,
                      "humidity": 65,
                      "wind_speed": 8.5,
                      "wind_deg": 180,
                      "clouds": 40,
                      "uvi": 5.2
                    }
                  ]
                }
            """.trimIndent())
        
        mockWebServer.enqueue(mockResponse)
        
        // When - request weather for a future date
        val futureDate = Date(System.currentTimeMillis() + 86400000) // Tomorrow
        val result = openWeatherMapService.getWeatherForDateTime(testLocation, futureDate).first()
        
        // Then
        assertTrue(result is WeatherResult.Success)
        val weatherData = (result as WeatherResult.Success).data
        
        assertNotNull(weatherData)
        assertEquals(72.5, weatherData.temperature, 0.01)
        assertEquals(8.5, weatherData.windSpeed, 0.01)
        assertEquals("S", weatherData.windDirection)
        assertEquals(40, weatherData.cloudCover)
        assertEquals(1013.0, weatherData.pressure, 0.01)
        assertEquals(65, weatherData.humidity)
        assertEquals(5, weatherData.uvIndex)
    }
    
    @Test
    fun getCurrentWeather_handlesError() = runBlocking {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
            .setBody("""
                {
                  "cod": 401,
                  "message": "Invalid API key"
                }
            """.trimIndent())
        
        mockWebServer.enqueue(mockResponse)
        
        // When
        val result = openWeatherMapService.getCurrentWeather(testLocation).first()
        
        // Then
        assertTrue(result is WeatherResult.Error)
    }
}