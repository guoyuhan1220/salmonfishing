package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for tide data services
 */
interface TideService {
    /**
     * Get the current tide information for a specific location
     * @param location The location to get tide data for
     * @return Result containing TideData or an exception
     */
    suspend fun getCurrentTide(location: Location): Result<TideData>
    
    /**
     * Get tide predictions for a specific location for a number of days
     * @param location The location to get tide predictions for
     * @param days Number of days to get predictions for (max 7)
     * @return Result containing a list of TideData or an exception
     */
    suspend fun getTidePredictions(location: Location, days: Int): Result<List<TideData>>
    
    /**
     * Get tide information for a specific date and time
     * @param location The location to get tide data for
     * @param dateTime The specific date and time to get tide data for
     * @return Result containing TideData or an exception
     */
    suspend fun getTideForDateTime(location: Location, dateTime: Date): Result<TideData>
    
    /**
     * Get a flow of tide data for a specific location
     * This can be used to observe tide changes over time
     * @param location The location to observe tide data for
     * @return Flow of TideData
     */
    fun observeTideData(location: Location): Flow<Result<TideData>>
}