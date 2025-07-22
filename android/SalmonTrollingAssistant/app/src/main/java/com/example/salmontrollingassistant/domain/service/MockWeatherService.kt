package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.WeatherData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class MockWeatherService : WeatherService {
    private val mockWeatherData: WeatherData
    private val mockForecastData: List<WeatherData>
    
    init {
        // Create mock data for testing
        val now = Date()
        
        mockWeatherData = WeatherData(
            id = UUID.randomUUID().toString(),
            timestamp = now,
            temperature = 68.5,
            windSpeed = 8.3,
            windDirection = "NW",
            precipitation = 0.0,
            cloudCover = 25,
            visibility = 10.0,
            pressure = 1012.5,
            humidity = 65,
            uvIndex = 6,
            waterTemperature = 58.2
        )
        
        // Create mock forecast data for 7 days
        val forecastData = mutableListOf<WeatherData>()
        val calendar = Calendar.getInstance()
        val windDirections = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        
        for (i in 0 until 7) {
            calendar.time = now
            calendar.add(Calendar.DAY_OF_YEAR, i)
            val forecastDate = calendar.time
            
            forecastData.add(
                WeatherData(
                    id = UUID.randomUUID().toString(),
                    timestamp = forecastDate,
                    temperature = 65.0 + Random.nextDouble(-5.0, 10.0),
                    windSpeed = 5.0 + Random.nextDouble(0.0, 10.0),
                    windDirection = windDirections.random(),
                    precipitation = Random.nextDouble(0.0, 0.5),
                    cloudCover = Random.nextInt(0, 100),
                    visibility = Random.nextDouble(5.0, 15.0),
                    pressure = 1010.0 + Random.nextDouble(-5.0, 5.0),
                    humidity = Random.nextInt(50, 90),
                    uvIndex = Random.nextInt(1, 10),
                    waterTemperature = 55.0 + Random.nextDouble(-2.0, 5.0)
                )
            )
        }
        mockForecastData = forecastData
    }
    
    override suspend fun getCurrentWeather(location: Location): Flow<WeatherResult<WeatherData>> = flow {
        // Simulate network delay
        delay(500)
        emit(WeatherResult.Success(mockWeatherData))
    }
    
    override suspend fun getForecast(location: Location, days: Int): Flow<WeatherResult<List<WeatherData>>> = flow {
        // Simulate network delay
        delay(500)
        val daysToReturn = minOf(days, 7) // Limit to 7 days
        emit(WeatherResult.Success(mockForecastData.take(daysToReturn)))
    }
    
    override suspend fun getWeatherForDateTime(location: Location, dateTime: Date): Flow<WeatherResult<WeatherData>> = flow {
        // Simulate network delay
        delay(500)
        
        // Find the closest forecast to the requested date/time
        val calendar = Calendar.getInstance()
        val requestedDay = calendar.apply {
            time = dateTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val matchingForecast = mockForecastData.find { forecast ->
            val forecastDay = calendar.apply {
                time = forecast.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            forecastDay == requestedDay
        }
        
        if (matchingForecast != null) {
            emit(WeatherResult.Success(matchingForecast))
        } else {
            // If no matching forecast, return current weather
            emit(WeatherResult.Success(mockWeatherData))
        }
    }
}