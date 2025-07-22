package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.CatchData
import kotlinx.coroutines.flow.Flow

interface CatchLoggingService {
    /**
     * Log a new catch
     */
    suspend fun logCatch(catchData: CatchData): Result<Boolean>
    
    /**
     * Get all catch history
     */
    fun getCatchHistory(): Flow<List<CatchData>>
    
    /**
     * Get catch history for a specific location
     */
    fun getCatchHistoryByLocation(locationId: String): Flow<List<CatchData>>
    
    /**
     * Get catch history for a specific species
     */
    fun getCatchHistoryBySpecies(species: String): Flow<List<CatchData>>
    
    /**
     * Get a specific catch by ID
     */
    suspend fun getCatchById(catchId: String): Result<CatchData>
    
    /**
     * Update an existing catch
     */
    suspend fun updateCatch(catchData: CatchData): Result<Boolean>
    
    /**
     * Delete a catch
     */
    suspend fun deleteCatch(catchId: String): Result<Boolean>
    
    /**
     * Add a photo to a catch
     */
    suspend fun addPhotoCatch(catchId: String, photoUri: String): Result<Boolean>
    
    /**
     * Remove a photo from a catch
     */
    suspend fun removePhotoCatch(catchId: String, photoUri: String): Result<Boolean>
}