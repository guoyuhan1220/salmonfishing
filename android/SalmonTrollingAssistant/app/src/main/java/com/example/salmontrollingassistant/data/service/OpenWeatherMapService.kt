package com.example.salmontrollingassistant.data.service

import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.WeatherData
import com.example.salmontrollingassistant.domain.service.WeatherException
import com.example.salmontrollingassistant.domain.service.WeatherResult
import com.example.salmontrollingassistant.domain.service.WeatherService
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class OpenWeatherMapService @Inject constructor(
    private val apiKey: String,
    private val dataUsageManager: DataUsageManager,
    private val networkBatchManager: NetworkBatchManager,
    private val context: android.content.Context
) : WeatherService {
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"
    private val api: OpenWeatherMapApi
    
    init {
        val moshi = Moshi.Builder().build()
        
        val cacheSize = 10 * 1024 * 1024L // 10 MB cache
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = okhttp3.Cache(cacheDir, cacheSize)
        
        // Create logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        // Create compression interceptor
        val compressionInterceptor = Interceptor { chain ->
            // Get the original request
            val originalRequest = chain.request()
            
            // Add gzip header to enable compression
            val compressedRequest = originalRequest.newBuilder()
                .header("Accept-Encoding", "gzip, deflate")
                .build()
            
            // Proceed with the request
            val response = chain.proceed(compressedRequest)
            
            // Record data usage
            val responseSize = response.body?.contentLength() ?: 0
            kotlinx.coroutines.runBlocking {
                dataUsageManager.recordDataUsage(responseSize)
            }
            
            response
        }
        
        // Create cache control interceptor
        val cacheControlInterceptor = Interceptor { chain ->
            var request = chain.request()
            
            // Check if we should use cached data based on network conditions
            val useCache = kotlinx.coroutines.runBlocking {
                !dataUsageManager.shouldPrefetch()
            }
            
            request = if (useCache) {
                // Use cached data if available, for up to 1 hour
                request.newBuilder()
                    .cacheControl(CacheControl.Builder()
                        .maxStale(1, TimeUnit.HOURS)
                        .build())
                    .build()
            } else {
                // Use network data, cache for 10 minutes
                request.newBuilder()
                    .cacheControl(CacheControl.Builder()
                        .maxAge(10, TimeUnit.MINUTES)
                        .build())
                    .build()
            }
            
            chain.proceed(request)
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .cache(cache)
            .addInterceptor(compressionInterceptor)
            .addInterceptor(cacheControlInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        
        api = retrofit.create(OpenWeatherMapApi::class.java)
    }
    
    override suspend fun getCurrentWeather(location: Location): Flow<WeatherResult<WeatherData>> = flow {
        try {
            // Check if we should use the network batch manager
            val shouldBatch = dataUsageManager.isOnMeteredConnection() && 
                             !dataUsageManager.isOnWifi()
            
            if (shouldBatch) {
                // Use network batch manager to optimize data usage
                val request = networkBatchManager.enqueueRequest("weather") {
                    api.getCurrentWeather(
                        lat = location.latitude,
                        lon = location.longitude,
                        units = "imperial",
                        appId = apiKey
                    )
                }
                
                // Wait for the request to complete
                var result: Result<CurrentWeatherResponse>? = null
                request.onComplete { 
                    result = it
                }
                
                // Wait for result with timeout
                var timeoutCounter = 0
                while (result == null && timeoutCounter < 30) {
                    delay(100)
                    timeoutCounter++
                }
                
                // Process result
                result?.fold(
                    onSuccess = { response ->
                        emit(WeatherResult.Success(mapCurrentResponseToWeatherData(response)))
                    },
                    onFailure = { error ->
                        emit(WeatherResult.Error(mapExceptionToWeatherException(error)))
                    }
                ) ?: emit(WeatherResult.Error(WeatherException.NetworkError(IOException("Request timeout"))))
            } else {
                // Direct API call when on WiFi or no data restrictions
                val response = api.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    units = "imperial",
                    appId = apiKey
                )
                
                emit(WeatherResult.Success(mapCurrentResponseToWeatherData(response)))
            }
        } catch (e: Exception) {
            emit(WeatherResult.Error(mapExceptionToWeatherException(e)))
        }
    }
    
    override suspend fun getForecast(location: Location, days: Int): Flow<WeatherResult<List<WeatherData>>> = flow {
        try {
            // Check if we should prefetch based on data usage settings
            val shouldPrefetch = dataUsageManager.shouldPrefetch()
            
            // If we shouldn't prefetch and days > 1, only get current day
            val requestedDays = if (shouldPrefetch) days else 1
            
            // Check if we should use the network batch manager
            val shouldBatch = dataUsageManager.isOnMeteredConnection() && 
                             !dataUsageManager.isOnWifi()
            
            if (shouldBatch) {
                // Use network batch manager to optimize data usage
                val request = networkBatchManager.enqueueRequest("forecast") {
                    api.getOneCallForecast(
                        lat = location.latitude,
                        lon = location.longitude,
                        exclude = "minutely,hourly",
                        units = "imperial",
                        appId = apiKey
                    )
                }
                
                // Wait for the request to complete
                var result: Result<OneCallResponse>? = null
                request.onComplete { 
                    result = it
                }
                
                // Wait for result with timeout
                var timeoutCounter = 0
                while (result == null && timeoutCounter < 30) {
                    delay(100)
                    timeoutCounter++
                }
                
                // Process result
                result?.fold(
                    onSuccess = { response ->
                        // Limit to requested number of days (max 7)
                        val limitedDays = minOf(requestedDays, 7)
                        val dailyForecasts = response.daily.take(limitedDays)
                        val weatherDataList = dailyForecasts.map { mapDailyForecastToWeatherData(it) }
                        
                        emit(WeatherResult.Success(weatherDataList))
                    },
                    onFailure = { error ->
                        emit(WeatherResult.Error(mapExceptionToWeatherException(error)))
                    }
                ) ?: emit(WeatherResult.Error(WeatherException.NetworkError(IOException("Request timeout"))))
            } else {
                // Direct API call when on WiFi or no data restrictions
                val response = api.getOneCallForecast(
                    lat = location.latitude,
                    lon = location.longitude,
                    exclude = "minutely,hourly",
                    units = "imperial",
                    appId = apiKey
                )
                
                // Limit to requested number of days (max 7)
                val limitedDays = minOf(requestedDays, 7)
                val dailyForecasts = response.daily.take(limitedDays)
                val weatherDataList = dailyForecasts.map { mapDailyForecastToWeatherData(it) }
                
                emit(WeatherResult.Success(weatherDataList))
            }
        } catch (e: Exception) {
            emit(WeatherResult.Error(mapExceptionToWeatherException(e)))
        }
    }
    
    override suspend fun getWeatherForDateTime(location: Location, dateTime: Date): Flow<WeatherResult<WeatherData>> = flow {
        try {
            val now = Date()
            
            if (dateTime.time <= now.time) {
                // For current or past dates, return current weather
                val currentWeather = api.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    units = "imperial",
                    appId = apiKey
                )
                emit(WeatherResult.Success(mapCurrentResponseToWeatherData(currentWeather)))
            } else {
                // For future dates, get forecast and find the closest match
                val forecast = api.getOneCallForecast(
                    lat = location.latitude,
                    lon = location.longitude,
                    exclude = "minutely,hourly",
                    units = "imperial",
                    appId = apiKey
                )
                
                val weatherDataList = forecast.daily.map { mapDailyForecastToWeatherData(it) }
                
                // Find the forecast closest to the requested date/time
                val closest = weatherDataList.minByOrNull { abs(it.timestamp.time - dateTime.time) }
                
                if (closest != null) {
                    emit(WeatherResult.Success(closest))
                } else {
                    // Fallback to current weather if no forecast is available
                    val currentWeather = api.getCurrentWeather(
                        lat = location.latitude,
                        lon = location.longitude,
                        units = "imperial",
                        appId = apiKey
                    )
                    emit(WeatherResult.Success(mapCurrentResponseToWeatherData(currentWeather)))
                }
            }
        } catch (e: Exception) {
            emit(WeatherResult.Error(mapExceptionToWeatherException(e)))
        }
    }
    
    // MARK: - Private Helper Methods
    
    private fun mapCurrentResponseToWeatherData(response: CurrentWeatherResponse): WeatherData {
        return WeatherData(
            id = UUID.randomUUID().toString(),
            timestamp = Date(response.dt * 1000L),
            temperature = response.main.temp,
            windSpeed = response.wind.speed,
            windDirection = mapWindDegreesToDirection(response.wind.deg),
            precipitation = response.rain?.lastHour ?: 0.0,
            cloudCover = response.clouds.all,
            visibility = response.visibility / 1000.0, // Convert from meters to kilometers
            pressure = response.main.pressure,
            humidity = response.main.humidity,
            uvIndex = 0, // UV Index not available in this endpoint
            waterTemperature = null // Water temperature not available
        )
    }
    
    private fun mapDailyForecastToWeatherData(daily: DailyForecast): WeatherData {
        return WeatherData(
            id = UUID.randomUUID().toString(),
            timestamp = Date(daily.dt * 1000L),
            temperature = daily.temp.day,
            windSpeed = daily.windSpeed,
            windDirection = mapWindDegreesToDirection(daily.windDeg),
            precipitation = daily.rain ?: 0.0,
            cloudCover = daily.clouds,
            visibility = 10.0, // Visibility not provided in daily forecast
            pressure = daily.pressure,
            humidity = daily.humidity,
            uvIndex = daily.uvi.toInt(),
            waterTemperature = null // Water temperature not available
        )
    }
    
    private fun mapWindDegreesToDirection(degrees: Int): String {
        val directions = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((degrees / 22.5) + 0.5).toInt() % 16
        return directions[index]
    }
    
    private fun mapExceptionToWeatherException(exception: Exception): WeatherException {
        return when (exception) {
            is IOException -> WeatherException.NetworkError(exception)
            is HttpException -> WeatherException.ServerError(exception.code())
            else -> WeatherException.Unknown()
        }
    }
}

// MARK: - Retrofit API Interface

interface OpenWeatherMapApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") appId: String
    ): CurrentWeatherResponse
    
    @GET("onecall")
    suspend fun getOneCallForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String,
        @Query("units") units: String,
        @Query("appid") appId: String
    ): OneCallResponse
}

// MARK: - API Response Models

@JsonClass(generateAdapter = true)
data class CurrentWeatherResponse(
    val dt: Long,
    val main: MainWeather,
    val wind: Wind,
    val clouds: Clouds,
    val rain: Rain?,
    val visibility: Int
)

@JsonClass(generateAdapter = true)
data class MainWeather(
    val temp: Double,
    val pressure: Double,
    val humidity: Int
)

@JsonClass(generateAdapter = true)
data class Wind(
    val speed: Double,
    val deg: Int
)

@JsonClass(generateAdapter = true)
data class Clouds(
    val all: Int
)

@JsonClass(generateAdapter = true)
data class Rain(
    @Json(name = "1h") val lastHour: Double
)

@JsonClass(generateAdapter = true)
data class OneCallResponse(
    val daily: List<DailyForecast>
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    val dt: Long,
    val temp: Temperature,
    val pressure: Double,
    val humidity: Int,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "wind_deg") val windDeg: Int,
    val clouds: Int,
    val rain: Double?,
    val uvi: Double
)

@JsonClass(generateAdapter = true)
data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)