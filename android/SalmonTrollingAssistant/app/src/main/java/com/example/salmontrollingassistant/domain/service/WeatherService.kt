package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow
import java.util.Date

sealed class WeatherResult<out T> {
    data class Success<T>(val data: T) : WeatherResult<T>()
    data class Error(val exception: WeatherException) : WeatherResult<Nothing>()
}

sealed class WeatherException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(cause: Throwable) : WeatherException("Network error", cause)
    class InvalidResponse : WeatherException("Invalid response from server")
    class DecodingError(cause: Throwable) : WeatherException("Error decoding response", cause)
    class InvalidLocation : WeatherException("Invalid location provided")
    class ServerError(val code: Int) : WeatherException("Server error with code: $code")
    class Unknown : WeatherException("Unknown error occurred")
}

interface WeatherService {
    /**
     * Get current weather for a specific location
     * @param location The location to get weather for
     * @return Flow emitting WeatherResult containing WeatherData or an error
     */
    suspend fun getCurrentWeather(location: Location): Flow<WeatherResult<WeatherData>>
    
    /**
     * Get weather forecast for a specific location
     * @param location The location to get forecast for
     * @param days Number of days to forecast (max 7)
     * @return Flow emitting WeatherResult containing a list of WeatherData or an error
     */
    suspend fun getForecast(location: Location, days: Int): Flow<WeatherResult<List<WeatherData>>>
    
    /**
     * Get weather for a specific date and time
     * @param location The location to get weather for
     * @param dateTime The specific date and time
     * @return Flow emitting WeatherResult containing WeatherData or an error
     */
    suspend fun getWeatherForDateTime(location: Location, dateTime: Date): Flow<WeatherResult<WeatherData>>
}