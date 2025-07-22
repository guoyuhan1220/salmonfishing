package com.example.salmontrollingassistant.data.service

import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.model.TideEvent
import com.example.salmontrollingassistant.domain.model.TideType
import com.example.salmontrollingassistant.domain.service.TideException
import com.example.salmontrollingassistant.domain.service.TideService
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Implementation of TideService using WorldTides API
 * https://www.worldtides.info/api
 */
class WorldTidesService(private val apiKey: String) : TideService {
    private val baseUrl = "https://www.worldtides.info/api/v2/"
    private val api: WorldTidesApi
    
    init {
        val moshi = Moshi.Builder().build()
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        
        api = retrofit.create(WorldTidesApi::class.java)
    }
    
    override suspend fun getCurrentTide(location: Location): Result<TideData> {
        return try {
            val now = Date()
            val calendar = Calendar.getInstance()
            
            // Format dates for API request
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val start = dateFormat.format(now)
            
            // Add 1 day to get 24 hours of data
            calendar.time = now
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val end = dateFormat.format(calendar.time)
            
            val response = api.getTides(
                lat = location.latitude,
                lon = location.longitude,
                start = start,
                end = end,
                datum = "MLLW", // Mean Lower Low Water
                key = apiKey
            )
            
            if (response.status != 200) {
                return Result.failure(mapErrorCodeToException(response.status))
            }
            
            if (response.extremes.isNullOrEmpty()) {
                return Result.failure(TideException.NoDataAvailable())
            }
            
            // Find the current tide state based on the extremes
            val currentTide = calculateCurrentTideState(now, response.extremes)
            Result.success(currentTide)
        } catch (e: Exception) {
            Result.failure(mapExceptionToTideException(e))
        }
    }
    
    override suspend fun getTidePredictions(location: Location, days: Int): Result<List<TideData>> {
        return try {
            val now = Date()
            val calendar = Calendar.getInstance()
            
            // Format dates for API request
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val start = dateFormat.format(now)
            
            // Limit to 7 days maximum
            val limitedDays = minOf(days, 7)
            
            // Add the requested number of days
            calendar.time = now
            calendar.add(Calendar.DAY_OF_MONTH, limitedDays)
            val end = dateFormat.format(calendar.time)
            
            val response = api.getTides(
                lat = location.latitude,
                lon = location.longitude,
                start = start,
                end = end,
                datum = "MLLW", // Mean Lower Low Water
                key = apiKey
            )
            
            if (response.status != 200) {
                return Result.failure(mapErrorCodeToException(response.status))
            }
            
            if (response.extremes.isNullOrEmpty()) {
                return Result.failure(TideException.NoDataAvailable())
            }
            
            // Group extremes by day and create tide data for each day
            val tideDataList = groupExtremesByDay(response.extremes)
            Result.success(tideDataList)
        } catch (e: Exception) {
            Result.failure(mapExceptionToTideException(e))
        }
    }
    
    override suspend fun getTideForDateTime(location: Location, dateTime: Date): Result<TideData> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = dateTime
            
            // Format dates for API request
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            // Get data for the day before and after the requested date
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val start = dateFormat.format(calendar.time)
            
            calendar.add(Calendar.DAY_OF_MONTH, 2)
            val end = dateFormat.format(calendar.time)
            
            val response = api.getTides(
                lat = location.latitude,
                lon = location.longitude,
                start = start,
                end = end,
                datum = "MLLW", // Mean Lower Low Water
                key = apiKey
            )
            
            if (response.status != 200) {
                return Result.failure(mapErrorCodeToException(response.status))
            }
            
            if (response.extremes.isNullOrEmpty()) {
                return Result.failure(TideException.NoDataAvailable())
            }
            
            // Calculate tide state for the specific date/time
            val tideData = calculateCurrentTideState(dateTime, response.extremes)
            Result.success(tideData)
        } catch (e: Exception) {
            Result.failure(mapExceptionToTideException(e))
        }
    }
    
    override fun observeTideData(location: Location): Flow<Result<TideData>> = flow {
        while (true) {
            emit(getCurrentTide(location))
            kotlinx.coroutines.delay(TimeUnit.MINUTES.toMillis(15)) // Update every 15 minutes
        }
    }
    
    // MARK: - Private Helper Methods
    
    private fun calculateCurrentTideState(dateTime: Date, extremes: List<TideExtreme>): TideData {
        // Sort extremes by time
        val sortedExtremes = extremes.sortedBy { it.dt }
        
        // Find the extremes before and after the current time
        var previousExtreme: TideExtreme? = null
        var nextExtreme: TideExtreme? = null
        
        for (extreme in sortedExtremes) {
            val extremeDate = Date(extreme.dt * 1000L)
            if (extremeDate.before(dateTime)) {
                previousExtreme = extreme
            } else {
                nextExtreme = extreme
                break
            }
        }
        
        // If we don't have a previous extreme, use the first one
        if (previousExtreme == null && sortedExtremes.isNotEmpty()) {
            previousExtreme = sortedExtremes.first()
        }
        
        // If we don't have a next extreme, use the last one
        if (nextExtreme == null && sortedExtremes.isNotEmpty()) {
            nextExtreme = sortedExtremes.last()
        }
        
        // Determine tide type and height
        val tideType: TideType
        val height: Double
        
        if (previousExtreme != null && nextExtreme != null) {
            val previousDate = Date(previousExtreme.dt * 1000L)
            val nextDate = Date(nextExtreme.dt * 1000L)
            
            // Calculate where we are between the two extremes
            val totalDuration = nextDate.time - previousDate.time
            val elapsedDuration = dateTime.time - previousDate.time
            val progressRatio = elapsedDuration.toDouble() / totalDuration
            
            // Interpolate height
            height = previousExtreme.height + (nextExtreme.height - previousExtreme.height) * progressRatio
            
            // Determine tide type
            tideType = if (previousExtreme.type == "High" && nextExtreme.type == "Low") {
                TideType.FALLING
            } else if (previousExtreme.type == "Low" && nextExtreme.type == "High") {
                TideType.RISING
            } else if (previousExtreme.type == "High") {
                TideType.HIGH
            } else {
                TideType.LOW
            }
        } else if (previousExtreme != null) {
            height = previousExtreme.height
            tideType = if (previousExtreme.type == "High") TideType.HIGH else TideType.LOW
        } else if (nextExtreme != null) {
            height = nextExtreme.height
            tideType = if (nextExtreme.type == "High") TideType.HIGH else TideType.LOW
        } else {
            // Fallback if no extremes are available
            height = 0.0
            tideType = TideType.LOW
        }
        
        // Find next high and low tides
        val nextHighTide = sortedExtremes.firstOrNull { 
            it.type == "High" && Date(it.dt * 1000L).after(dateTime) 
        }
        
        val nextLowTide = sortedExtremes.firstOrNull { 
            it.type == "Low" && Date(it.dt * 1000L).after(dateTime) 
        }
        
        return TideData(
            id = UUID.randomUUID().toString(),
            timestamp = dateTime,
            height = height,
            type = tideType,
            nextHighTide = nextHighTide?.let { 
                TideEvent(
                    timestamp = Date(it.dt * 1000L),
                    height = it.height
                )
            },
            nextLowTide = nextLowTide?.let { 
                TideEvent(
                    timestamp = Date(it.dt * 1000L),
                    height = it.height
                )
            }
        )
    }
    
    private fun groupExtremesByDay(extremes: List<TideExtreme>): List<TideData> {
        val calendar = Calendar.getInstance()
        val tideDataList = mutableListOf<TideData>()
        
        // Group extremes by day
        val extremesByDay = extremes.groupBy { extreme ->
            calendar.timeInMillis = extreme.dt * 1000L
            calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000 // Unique key for each day
        }
        
        // Create a tide data for each day at noon
        for ((_, dayExtremes) in extremesByDay) {
            if (dayExtremes.isNotEmpty()) {
                // Use the first extreme's date to set the day
                calendar.timeInMillis = dayExtremes.first().dt * 1000L
                
                // Set time to noon
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                val noonDate = calendar.time
                
                // Calculate tide state at noon
                val tideData = calculateCurrentTideState(noonDate, dayExtremes)
                tideDataList.add(tideData)
            }
        }
        
        return tideDataList.sortedBy { it.timestamp }
    }
    
    private fun mapExceptionToTideException(exception: Exception): TideException {
        return when (exception) {
            is IOException -> TideException.NetworkError(exception)
            is HttpException -> TideException.ServerError(exception.code())
            is TideException -> exception
            else -> TideException.Unknown()
        }
    }
    
    private fun mapErrorCodeToException(code: Int): TideException {
        return when (code) {
            400 -> TideException.InvalidLocation()
            401, 403 -> TideException.ServerError(code)
            404 -> TideException.NoDataAvailable()
            else -> TideException.ServerError(code)
        }
    }
}

// MARK: - Retrofit API Interface

interface WorldTidesApi {
    @GET(".")
    suspend fun getTides(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("datum") datum: String,
        @Query("key") key: String
    ): TideResponse
}

// MARK: - API Response Models

@JsonClass(generateAdapter = true)
data class TideResponse(
    val status: Int,
    val callCount: Int,
    val copyright: String,
    val requestLat: Double,
    val requestLon: Double,
    val responseLat: Double,
    val responseLon: Double,
    val atlas: String,
    val station: String?,
    val extremes: List<TideExtreme>?
)

@JsonClass(generateAdapter = true)
data class TideExtreme(
    val dt: Long,
    val date: String,
    val height: Double,
    val type: String // "High" or "Low"
)