package com.example.salmontrollingassistant.data.service

import com.example.salmontrollingassistant.data.db.WeatherDao
import com.example.salmontrollingassistant.data.db.WeatherEntity
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.WeatherData
import com.example.salmontrollingassistant.domain.service.WeatherException
import com.example.salmontrollingassistant.domain.service.WeatherResult
import com.example.salmontrollingassistant.domain.service.WeatherService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

class CachedWeatherService(
    private val weatherService: WeatherService,
    private val weatherDao: WeatherDao
) : WeatherService {
    
    // Cache expiration times (in milliseconds)
    private val currentWeatherExpiration = 30 * 60 * 1000 // 30 minutes
    private val forecastExpiration = 3 * 60 * 60 * 1000 // 3 hours
    
    override suspend fun getCurrentWeather(location: Location): Flow<WeatherResult<WeatherData>> = flow {
        // Check if we have a valid cached current weather
        val cachedWeather = weatherDao.getCurrentWeather(location.id)
        val currentTime = System.currentTimeMillis()
        
        if (cachedWeather != null && (currentTime - cachedWeather.cacheTimestamp) < currentWeatherExpiration) {
            // Cache is valid, return cached data
            emit(WeatherResult.Success(cachedWeather.toWeatherData()))
        } else {
            // Cache is invalid or doesn't exist, fetch from service
            weatherService.getCurrentWeather(location).collect { result ->
                when (result) {
                    is WeatherResult.Success -> {
                        // Cache the new data
                        val weatherEntity = WeatherEntity.fromWeatherData(
                            result.data,
                            location.id,
                            isForecast = false
                        )
                        weatherDao.deleteCurrentWeather(location.id)
                        weatherDao.insertCurrentWeather(weatherEntity)
                        
                        // Return the fetched data
                        emit(result)
                    }
                    is WeatherResult.Error -> {
                        // If fetch failed but we have cached data (even if expired), return it
                        if (cachedWeather != null) {
                            emit(WeatherResult.Success(cachedWeather.toWeatherData()))
                        } else {
                            emit(result)
                        }
                    }
                }
            }
        }
    }
    
    override suspend fun getForecast(location: Location, days: Int): Flow<WeatherResult<List<WeatherData>>> = flow {
        // Check if we have a valid cached forecast
        val cachedForecast = weatherDao.getForecast(location.id)
        val currentTime = System.currentTimeMillis()
        
        if (cachedForecast.isNotEmpty() && 
            cachedForecast.size >= days && 
            (currentTime - cachedForecast.first().cacheTimestamp) < forecastExpiration) {
            // Cache is valid, return cached data
            val weatherDataList = cachedForecast.map { it.toWeatherData() }.take(days)
            emit(WeatherResult.Success(weatherDataList))
        } else {
            // Cache is invalid or doesn't exist, fetch from service
            weatherService.getForecast(location, days).collect { result ->
                when (result) {
                    is WeatherResult.Success -> {
                        // Cache the new data
                        val weatherEntities = result.data.map { weatherData ->
                            WeatherEntity.fromWeatherData(
                                weatherData,
                                location.id,
                                isForecast = true
                            )
                        }
                        weatherDao.deleteForecast(location.id)
                        weatherDao.insertForecast(weatherEntities)
                        
                        // Return the fetched data
                        emit(result)
                    }
                    is WeatherResult.Error -> {
                        // If fetch failed but we have cached data (even if expired), return it
                        if (cachedForecast.isNotEmpty()) {
                            val weatherDataList = cachedForecast.map { it.toWeatherData() }.take(days)
                            emit(WeatherResult.Success(weatherDataList))
                        } else {
                            emit(result)
                        }
                    }
                }
            }
        }
    }
    
    override suspend fun getWeatherForDateTime(location: Location, dateTime: Date): Flow<WeatherResult<WeatherData>> = flow {
        // Check if we have a valid cached forecast that contains the requested date/time
        val cachedForecast = weatherDao.getForecast(location.id)
        val currentTime = System.currentTimeMillis()
        
        if (cachedForecast.isNotEmpty() && (currentTime - cachedForecast.first().cacheTimestamp) < forecastExpiration) {
            // Find the forecast closest to the requested date/time
            val calendar = Calendar.getInstance()
            val requestedDay = calendar.apply {
                time = dateTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val matchingForecast = cachedForecast.find { forecast ->
                val forecastDay = calendar.apply {
                    timeInMillis = forecast.timestamp
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                forecastDay == requestedDay
            }
            
            if (matchingForecast != null) {
                emit(WeatherResult.Success(matchingForecast.toWeatherData()))
                return@flow
            }
        }
        
        // If no valid cache or no matching forecast, fetch from service
        weatherService.getWeatherForDateTime(location, dateTime).collect { result ->
            when (result) {
                is WeatherResult.Success -> {
                    // We don't cache this specifically as it's covered by the forecast cache
                    emit(result)
                }
                is WeatherResult.Error -> {
                    // If fetch failed but we have any cached forecast, find the closest one
                    if (cachedForecast.isNotEmpty()) {
                        val closest = cachedForecast.minByOrNull { 
                            abs(it.timestamp - dateTime.time) 
                        }
                        
                        if (closest != null) {
                            emit(WeatherResult.Success(closest.toWeatherData()))
                        } else {
                            emit(result)
                        }
                    } else {
                        emit(result)
                    }
                }
            }
        }
    }
    
    suspend fun clearCache() {
        weatherDao.clearAllWeatherData()
    }
}