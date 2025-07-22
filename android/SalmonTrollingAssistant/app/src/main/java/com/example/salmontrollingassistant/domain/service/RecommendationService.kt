package com.example.salmontrollingassistant.domain.service

import android.content.Context
import com.example.salmontrollingassistant.data.db.EquipmentDatabase
import com.example.salmontrollingassistant.data.db.EquipmentEntity
import com.example.salmontrollingassistant.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.*

interface RecommendationService {
    fun getRecommendations(
        weatherData: WeatherData,
        tideData: TideData,
        fishSpecies: FishSpecies? = null,
        userEquipment: List<UserEquipment>? = null
    ): List<EquipmentRecommendation>
    
    fun getEquipmentDatabase(): List<EquipmentItem>
    
    fun getEquipmentById(id: String): EquipmentItem?
    
    fun getEquipmentByType(type: EquipmentType): List<EquipmentItem>
}

class RuleBasedRecommendationService(private val context: Context) : RecommendationService {
    // Equipment database
    private val equipmentDao = EquipmentDatabase.getDatabase(context).equipmentDao()
    private var cachedEquipment: List<EquipmentItem> = emptyList()
    
    init {
        // Load equipment database
        loadEquipmentDatabase()
    }
    
    private fun loadEquipmentDatabase() {
        // In a real implementation, this would be done asynchronously
        // For simplicity, we're using runBlocking here
        runBlocking {
            cachedEquipment = equipmentDao.getAllEquipment().first().map { it.toDomainModel() }
        }
    }
    
    override fun getRecommendations(
        weatherData: WeatherData,
        tideData: TideData,
        fishSpecies: FishSpecies?,
        userEquipment: List<UserEquipment>?
    ): List<EquipmentRecommendation> {
        val recommendations: MutableList<EquipmentRecommendation> = mutableListOf()
        
        // Determine environmental conditions
        val waterClarity = determineWaterClarity(weatherData)
        val lightCondition = determineLightCondition(weatherData)
        val weatherCondition = determineWeatherCondition(weatherData)
        
        // Get recommendations for each equipment type
        recommendations.add(getFlasherRecommendation(
            waterClarity = waterClarity,
            lightCondition = lightCondition,
            weatherCondition = weatherCondition,
            tideData = tideData
        ))
        
        recommendations.add(getLureRecommendation(
            waterClarity = waterClarity,
            lightCondition = lightCondition,
            weatherCondition = weatherCondition,
            tideData = tideData
        ))
        
        recommendations.add(getLeaderRecommendation(
            waterClarity = waterClarity,
            lightCondition = lightCondition,
            weatherCondition = weatherCondition,
            tideData = tideData
        ))
        
        // Apply filters if specified
        val filter = RecommendationFilterImpl(context)
        
        // Apply species filter if specified
        var filteredRecommendations = recommendations.toList()
        if (fishSpecies != null) {
            filteredRecommendations = filter.filterBySpecies(filteredRecommendations, fishSpecies)
        }
        
        // Apply water clarity filter
        filteredRecommendations = filter.filterByWaterClarity(filteredRecommendations, waterClarity)
        
        // Apply user preferences if available
        if (!userEquipment.isNullOrEmpty()) {
            filteredRecommendations = filter.prioritizeUserPreferences(filteredRecommendations, userEquipment)
        }
        
        return filteredRecommendations
    }
    
    override fun getEquipmentDatabase(): List<EquipmentItem> {
        return cachedEquipment
    }
    
    override fun getEquipmentById(id: String): EquipmentItem? {
        return cachedEquipment.find { it.id == id }
    }
    
    override fun getEquipmentByType(type: EquipmentType): List<EquipmentItem> {
        return cachedEquipment.filter { it.type == type }
    }
    
    // Helper methods for determining environmental conditions
    private fun determineWaterClarity(weatherData: WeatherData): WaterClarity {
        return WaterClarity.fromVisibility(weatherData.visibility)
    }
    
    private fun determineLightCondition(weatherData: WeatherData): LightCondition {
        return LightCondition.fromWeatherData(weatherData)
    }
    
    private fun determineWeatherCondition(weatherData: WeatherData): WeatherCondition {
        return WeatherCondition.fromWeatherData(weatherData)
    }
    
    // Recommendation methods for each equipment type
    private fun getFlasherRecommendation(
        waterClarity: WaterClarity,
        lightCondition: LightCondition,
        weatherCondition: WeatherCondition,
        tideData: TideData
    ): EquipmentRecommendation {
        // Filter flashers based on conditions
        var filteredFlashers = equipmentDatabase.filter { it.type == EquipmentType.FLASHER }
        
        // Filter by water clarity
        filteredFlashers = filteredFlashers.filter { flasher ->
            flasher.waterClarityConditions?.contains(waterClarity.name.lowercase()) ?: true
        }
        
        // Filter by light condition
        filteredFlashers = filteredFlashers.filter { flasher ->
            flasher.lightConditions?.contains(lightCondition.name.lowercase()) ?: true
        }
        
        // Filter by weather condition
        filteredFlashers = filteredFlashers.filter { flasher ->
            flasher.weatherConditions?.contains(weatherCondition.name.lowercase()) ?: true
        }
        
        // Filter by tide condition
        filteredFlashers = filteredFlashers.filter { flasher ->
            flasher.tideConditions?.contains(tideData.type) ?: true
        }
        
        // Note: Species filtering and user equipment prioritization are now handled by RecommendationFilter
        
        // If no flashers match the criteria, use all flashers
        if (filteredFlashers.isEmpty()) {
            filteredFlashers = equipmentDatabase.filter { it.type == EquipmentType.FLASHER }
        }
        
        // Generate explanation based on conditions
        val reason = generateFlasherExplanation(
            waterClarity = waterClarity,
            lightCondition = lightCondition,
            weatherCondition = weatherCondition,
            tideData = tideData
        )
        
        // Calculate confidence score based on how many filters were applied
        val confidenceScore = calculateConfidenceScore(
            filteredItems = filteredFlashers, 
            totalItems = equipmentDatabase.filter { it.type == EquipmentType.FLASHER }.size
        )
        
        return EquipmentRecommendation(
            type = EquipmentType.FLASHER,
            items = filteredFlashers,
            reasonForRecommendation = reason,
            confidenceScore = confidenceScore
        )
    }
    
    private fun getLureRecommendation(
        waterClarity: WaterClarity,
        lightCondition: LightCondition,
        weatherCondition: WeatherCondition,
        tideData: TideData
    ): EquipmentRecommendation {
        // Filter lures based on conditions
        var filteredLures = equipmentDatabase.filter { it.type == EquipmentType.LURE }
        
        // Filter by water clarity
        filteredLures = filteredLures.filter { lure ->
            lure.waterClarityConditions?.contains(waterClarity.name.lowercase()) ?: true
        }
        
        // Filter by light condition
        filteredLures = filteredLures.filter { lure ->
            lure.lightConditions?.contains(lightCondition.name.lowercase()) ?: true
        }
        
        // Filter by weather condition
        filteredLures = filteredLures.filter { lure ->
            lure.weatherConditions?.contains(weatherCondition.name.lowercase()) ?: true
        }
        
        // Filter by tide condition
        filteredLures = filteredLures.filter { lure ->
            lure.tideConditions?.contains(tideData.type) ?: true
        }
        
        // Note: Species filtering and user equipment prioritization are now handled by RecommendationFilter
        
        // If no lures match the criteria, use all lures
        if (filteredLures.isEmpty()) {
            filteredLures = equipmentDatabase.filter { it.type == EquipmentType.LURE }
        }
        
        // Generate explanation based on conditions
        val reason = generateLureExplanation(
            waterClarity = waterClarity,
            lightCondition = lightCondition,
            weatherCondition = weatherCondition,
            tideData = tideData
        )
        
        // Calculate confidence score based on how many filters were applied
        val confidenceScore = calculateConfidenceScore(
            filteredItems = filteredLures, 
            totalItems = equipmentDatabase.filter { it.type == EquipmentType.LURE }.size
        )
        
        return EquipmentRecommendation(
            type = EquipmentType.LURE,
            items = filteredLures,
            reasonForRecommendation = reason,
            confidenceScore = confidenceScore
        )
    }
    
    private fun getLeaderRecommendation(
        waterClarity: WaterClarity,
        lightCondition: LightCondition,
        weatherCondition: WeatherCondition,
        tideData: TideData
    ): EquipmentRecommendation {
        // Filter leaders based on conditions
        var filteredLeaders = equipmentDatabase.filter { it.type == EquipmentType.LEADER }
        
        // Filter by water clarity
        filteredLeaders = filteredLeaders.filter { leader ->
            leader.waterClarityConditions?.contains(waterClarity.name.lowercase()) ?: true
        }
        
        // Filter by light condition
        filteredLeaders = filteredLeaders.filter { leader ->
            leader.lightConditions?.contains(lightCondition.name.lowercase()) ?: true
        }
        
        // Filter by weather condition
        filteredLeaders = filteredLeaders.filter { leader ->
            leader.weatherConditions?.contains(weatherCondition.name.lowercase()) ?: true
        }
        
        // Filter by tide condition
        filteredLeaders = filteredLeaders.filter { leader ->
            leader.tideConditions?.contains(tideData.type) ?: true
        }
        
        // Note: Species filtering and user equipment prioritization are now handled by RecommendationFilter
        
        // If no leaders match the criteria, use all leaders
        if (filteredLeaders.isEmpty()) {
            filteredLeaders = equipmentDatabase.filter { it.type == EquipmentType.LEADER }
        }
        
        // Generate explanation based on conditions
        val reason = generateLeaderExplanation(
            waterClarity = waterClarity,
            lightCondition = lightCondition,
            weatherCondition = weatherCondition,
            tideData = tideData
        )
        
        // Calculate confidence score based on how many filters were applied
        val confidenceScore = calculateConfidenceScore(
            filteredItems = filteredLeaders, 
            totalItems = equipmentDatabase.filter { it.type == EquipmentType.LEADER }.size
        )
        
        return EquipmentRecommendation(
            type = EquipmentType.LEADER,
            items = filteredLeaders,
            reasonForRecommendation = reason,
            confidenceScore = confidenceScore
        )
    }
    
    // Helper methods for generating explanations
    private fun generateFlasherExplanation(
        waterClarity: WaterClarity,
        lightCondition: LightCondition,
        weatherCondition: WeatherCondition,
        tideData: TideData
    ): String {
        var explanation = "Based on the current conditions: "
        
        // Water clarity explanation
        when (waterClarity) {
            WaterClarity.CLEAR -> 
                explanation += "The water is clear, so less flashy and more natural colored flashers will work well. "
            WaterClarity.MEDIUM -> 
                explanation += "The water has medium clarity, so moderately bright flashers will be effective. "
            WaterClarity.MURKY -> 
                explanation += "The water is murky, so bright, high-visibility flashers are recommended. "
        }
        
        // Light condition explanation
        when (lightCondition) {
            LightCondition.BRIGHT -> 
                explanation += "It's bright outside, so UV enhanced flashers will be more visible. "
            LightCondition.OVERCAST -> 
                explanation += "It's overcast, so glow or chrome flashers will provide good contrast. "
            LightCondition.LOW_LIGHT -> 
                explanation += "Light conditions are low, so glow flashers will be most effective. "
        }
        
        // Tide explanation
        when (tideData.type) {
            TideType.HIGH, TideType.RISING -> 
                explanation += "During high/rising tide, larger flashers create more attraction. "
            TideType.LOW, TideType.FALLING -> 
                explanation += "During low/falling tide, smaller flashers with less drag work better. "
        }
        
        return explanation
    }
    
    private fun generateLureExplanation(
        waterClarity: WaterClarity,
        lightCondition: LightCondition,
        weatherCondition: WeatherCondition,
        tideData: TideData
    ): String {
        var explanation = "Based on the current conditions: "
        
        // Water clarity explanation
        when (waterClarity) {
            WaterClarity.CLEAR -> 
                explanation += "The water is clear, so smaller, more natural colored lures are recommended. "
            WaterClarity.MEDIUM -> 
                explanation += "The water has medium clarity, so medium-sized lures with some flash will be effective. "
            WaterClarity.MURKY -> 
                explanation += "The water is murky, so larger, brighter lures will help attract fish. "
        }
        
        // Light condition explanation
        when (lightCondition) {
            LightCondition.BRIGHT -> 
                explanation += "It's bright outside, so silver and blue colors will reflect more light. "
            LightCondition.OVERCAST -> 
                explanation += "It's overcast, so green and chartreuse colors will provide good visibility. "
            LightCondition.LOW_LIGHT -> 
                explanation += "Light conditions are low, so glow or UV enhanced lures will be most visible. "
        }
        
        // Weather condition explanation
        when (weatherCondition) {
            WeatherCondition.CALM -> 
                explanation += "In calm conditions, subtle action lures work well. "
            WeatherCondition.WINDY -> 
                explanation += "In windy conditions, lures with more action help attract attention. "
            WeatherCondition.RAINY -> 
                explanation += "During rain, darker lures create better silhouettes. "
        }
        
        return explanation
    }
    
    private fun generateLeaderExplanation(
        waterClarity: WaterClarity,
        lightCondition: LightCondition,
        weatherCondition: WeatherCondition,
        tideData: TideData
    ): String {
        var explanation = "Based on the current conditions: "
        
        // Water clarity explanation
        when (waterClarity) {
            WaterClarity.CLEAR -> 
                explanation += "The water is clear, so fluorocarbon leaders are less visible to fish. "
            WaterClarity.MEDIUM -> 
                explanation += "The water has medium clarity, so standard monofilament leaders will work well. "
            WaterClarity.MURKY -> 
                explanation += "The water is murky, so leader visibility is less important than strength. "
        }
        
        // Tide and weather explanation
        if (tideData.type == TideType.HIGH || tideData.type == TideType.RISING) {
            explanation += "During high/rising tide, longer leaders allow for more natural presentation. "
        } else {
            explanation += "During low/falling tide, shorter leaders help maintain control. "
        }
        
        if (weatherCondition == WeatherCondition.WINDY || weatherCondition == WeatherCondition.RAINY) {
            explanation += "In rough conditions, heavier leaders provide better durability. "
        }
        
        return explanation
    }
    
    // Calculate confidence score based on filtering results
    private fun calculateConfidenceScore(filteredItems: List<EquipmentItem>, totalItems: Int): Float {
        if (filteredItems.isEmpty() || totalItems == 0) {
            return 0.5f // Default medium confidence
        }
        
        // More specific recommendations (fewer items) have higher confidence
        val specificityScore = 1.0f - filteredItems.size.toFloat() / totalItems.toFloat()
        
        // Minimum confidence of 0.3, maximum of 0.95
        return minOf(0.95f, maxOf(0.3f, 0.5f + specificityScore * 0.5f))
    }
}