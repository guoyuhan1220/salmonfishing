package com.example.salmontrollingassistant.data.service

import com.example.salmontrollingassistant.data.db.TideDao
import com.example.salmontrollingassistant.data.db.TideEntity
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.service.TideException
import com.example.salmontrollingassistant.domain.service.TideService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

/**
 * Implementation of TideService that caches tide data locally
 */
class CachedTideService(
    private val tideService: TideService,
    private val tideDao: TideDao
) : TideService {
    
    // Cache expiration times (in milliseconds)
    private val currentTideExpiration = 30 * 60 * 1000 // 30 minutes
    private val tideForecastExpiration = 6 * 60 * 60 * 1000 // 6 hours (tides change less frequently than weather)
    
    override suspend fun getCurrentTide(location: Location): Result<TideData> {
        // Check if we have a valid cached current tide
        val cachedTide = tideDao.getCurrentTide(location.id)
        val currentTime = System.currentTimeMillis()
        
        if (cachedTide != null && (currentTime - cachedTide.cacheTimestamp) < currentTideExpiration) {
            // Cache is valid, return cached data
            return Result.success(cachedTide.toTideData())
        } else {
            // Cache is invalid or doesn't exist, fetch from service
            return try {
                val result = tideService.getCurrentTide(location)
                
                if (result.isSuccess) {
                    // Cache the new data
                    val tideData = result.getOrNull()!!
                    val tideEntity = TideEntity.fromTideData(
                        tideData,
                        location.id,
                        isForecast = false
                    )
                    tideDao.deleteCurrentTide(location.id)
                    tideDao.insertCurrentTide(tideEntity)
                    
                    // Return the fetched data
                    result
                } else {
                    // If fetch failed but we have cached data (even if expired), return it
                    if (cachedTide != null) {
                        Result.success(cachedTide.toTideData())
                    } else {
                        result
                    }
                }
            } catch (e: Exception) {
                // If an unexpected error occurred but we have cached data, return it
                if (cachedTide != null) {
                    Result.success(cachedTide.toTideData())
                } else {
                    Result.failure(TideException.Unknown())
                }
            }
        }
    }
    
    override suspend fun getTidePredictions(location: Location, days: Int): Result<List<TideData>> {
        // Check if we have a valid cached forecast
        val cachedForecast = tideDao.getTideForecast(location.id)
        val currentTime = System.currentTimeMillis()
        
        if (cachedForecast.isNotEmpty() && 
            cachedForecast.size >= days && 
            (currentTime - cachedForecast.first().cacheTimestamp) < tideForecastExpiration) {
            // Cache is valid, return cached data
            val tideDataList = cachedForecast.map { it.toTideData() }.take(days)
            return Result.success(tideDataList)
        } else {
            // Cache is invalid or doesn't exist, fetch from service
            return try {
                val result = tideService.getTidePredictions(location, days)
                
                if (result.isSuccess) {
                    // Cache the new data
                    val tideDataList = result.getOrNull()!!
                    val tideEntities = tideDataList.map { tideData ->
                        TideEntity.fromTideData(
                            tideData,
                            location.id,
                            isForecast = true
                        )
                    }
                    tideDao.deleteTideForecast(location.id)
                    tideDao.insertTideForecast(tideEntities)
                    
                    // Return the fetched data
                    result
                } else {
                    // If fetch failed but we have cached data (even if expired), return it
                    if (cachedForecast.isNotEmpty()) {
                        val tideDataList = cachedForecast.map { it.toTideData() }.take(days)
                        Result.success(tideDataList)
                    } else {
                        result
                    }
                }
            } catch (e: Exception) {
                // If an unexpected error occurred but we have cached data, return it
                if (cachedForecast.isNotEmpty()) {
                    val tideDataList = cachedForecast.map { it.toTideData() }.take(days)
                    Result.success(tideDataList)
                } else {
                    Result.failure(TideException.Unknown())
                }
            }
        }
    }
    
    override suspend fun getTideForDateTime(location: Location, dateTime: Date): Result<TideData> {
        // Check if we have a valid cached forecast that contains the requested date/time
        val cachedForecast = tideDao.getTideForecast(location.id)
        val currentTime = System.currentTimeMillis()
        
        if (cachedForecast.isNotEmpty() && (currentTime - cachedForecast.first().cacheTimestamp) < tideForecastExpiration) {
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
                return Result.success(matchingForecast.toTideData())
            }
        }
        
        // If no valid cache or no matching forecast, fetch from service
        return try {
            val result = tideService.getTideForDateTime(location, dateTime)
            
            // We don't cache this specifically as it's covered by the forecast cache
            if (result.isSuccess) {
                result
            } else {
                // If fetch failed but we have any cached forecast, find the closest one
                if (cachedForecast.isNotEmpty()) {
                    val closest = cachedForecast.minByOrNull { 
                        abs(it.timestamp - dateTime.time) 
                    }
                    
                    if (closest != null) {
                        Result.success(closest.toTideData())
                    } else {
                        result
                    }
                } else {
                    result
                }
            }
        } catch (e: Exception) {
            // If an unexpected error occurred but we have cached forecast, find the closest one
            if (cachedForecast.isNotEmpty()) {
                val closest = cachedForecast.minByOrNull { 
                    abs(it.timestamp - dateTime.time) 
                }
                
                if (closest != null) {
                    Result.success(closest.toTideData())
                } else {
                    Result.failure(TideException.Unknown())
                }
            } else {
                Result.failure(TideException.Unknown())
            }
        }
    }
    
    override fun observeTideData(location: Location): Flow<Result<TideData>> = flow {
        while (true) {
            emit(getCurrentTide(location))
            kotlinx.coroutines.delay(15 * 60 * 1000) // Update every 15 minutes
        }
    }
    
    /**
     * Clear all cached tide data
     */
    suspend fun clearCache() {
        tideDao.clearAllTideData()
    }
}