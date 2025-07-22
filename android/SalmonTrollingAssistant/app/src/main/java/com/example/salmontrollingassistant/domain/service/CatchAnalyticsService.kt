package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.CatchData
import com.example.salmontrollingassistant.domain.model.FishSpecies
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Service for analyzing catch history data and providing insights
 */
interface CatchAnalyticsService {
    /**
     * Get catch count by species
     */
    fun getCatchCountBySpecies(): Flow<Map<FishSpecies, Int>>
    
    /**
     * Get catch count by location
     */
    fun getCatchCountByLocation(): Flow<Map<String, Int>>
    
    /**
     * Get catch count by month
     */
    fun getCatchCountByMonth(): Flow<Map<Int, Int>>
    
    /**
     * Get average fish size by species
     */
    fun getAverageSizeBySpecies(): Flow<Map<FishSpecies, Double>>
    
    /**
     * Get average fish weight by species
     */
    fun getAverageWeightBySpecies(): Flow<Map<FishSpecies, Double>>
    
    /**
     * Get most successful equipment
     */
    fun getMostSuccessfulEquipment(): Flow<List<Pair<String, Int>>>
    
    /**
     * Get most successful locations
     */
    fun getMostSuccessfulLocations(): Flow<List<Pair<String, Int>>>
    
    /**
     * Get catch trend over time
     */
    fun getCatchTrendOverTime(): Flow<List<Pair<Date, Int>>>
    
    /**
     * Get personalized recommendations based on catch history
     */
    fun getPersonalizedRecommendations(): Flow<List<String>>
}