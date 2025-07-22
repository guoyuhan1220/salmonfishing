package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.UserEquipment
import com.example.salmontrollingassistant.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for managing user preferences
 */
interface UserPreferencesService {
    /**
     * Get the user's preferences
     */
    fun getUserPreferences(): Flow<UserPreferences>
    
    /**
     * Update the user's preferences
     * @param preferences The updated preferences
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateUserPreferences(preferences: UserPreferences): Boolean
    
    /**
     * Get the user's equipment
     */
    fun getUserEquipment(): Flow<List<UserEquipment>>
    
    /**
     * Add equipment to the user's inventory
     * @param equipment The equipment to add
     * @return True if the addition was successful, false otherwise
     */
    suspend fun addUserEquipment(equipment: UserEquipment): Boolean
    
    /**
     * Remove equipment from the user's inventory
     * @param equipmentId The ID of the equipment to remove
     * @return True if the removal was successful, false otherwise
     */
    suspend fun removeUserEquipment(equipmentId: String): Boolean
    
    /**
     * Update equipment in the user's inventory
     * @param equipment The updated equipment
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateUserEquipment(equipment: UserEquipment): Boolean
    
    /**
     * Update the user's preferred fish species
     * @param species The list of preferred species
     * @return True if the update was successful, false otherwise
     */
    suspend fun updatePreferredSpecies(species: List<FishSpecies>): Boolean
    
    /**
     * Update the user's preferred equipment
     * @param equipmentIds The list of preferred equipment IDs
     * @return True if the update was successful, false otherwise
     */
    suspend fun updatePreferredEquipment(equipmentIds: List<String>): Boolean
    
    /**
     * Update the user's experience level
     * @param experienceLevel The experience level as a string
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateExperienceLevel(experienceLevel: String): Boolean
    
    /**
     * Update the user's notification settings
     * @param enableWeatherAlerts Whether to enable weather alerts
     * @param enableTideAlerts Whether to enable tide alerts
     * @param enableOptimalConditionAlerts Whether to enable optimal condition alerts
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateNotificationSettings(
        enableWeatherAlerts: Boolean,
        enableTideAlerts: Boolean,
        enableOptimalConditionAlerts: Boolean
    ): Boolean
    
    /**
     * Update the user's display settings
     * @param useDarkMode Whether to use dark mode
     * @param useHighContrastMode Whether to use high contrast mode
     * @param useMetricSystem Whether to use the metric system
     * @param fontSize The font size
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateDisplaySettings(
        useDarkMode: Boolean,
        useHighContrastMode: Boolean,
        useMetricSystem: Boolean,
        fontSize: String
    ): Boolean
    
    /**
     * Update the user's data settings
     * @param dataRefreshInterval The data refresh interval in minutes
     * @param wifiOnlyDownloads Whether to only download on WiFi
     * @param imageQuality The image quality
     * @param prefetchData Whether to prefetch data
     * @param locationUpdateFrequency The location update frequency in minutes
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateDataSettings(
        dataRefreshInterval: Int,
        wifiOnlyDownloads: Boolean,
        imageQuality: String,
        prefetchData: Boolean,
        locationUpdateFrequency: Int
    ): Boolean
    
    /**
     * Export user preferences to a file
     * @param filePath The path to export to
     * @return True if the export was successful, false otherwise
     */
    suspend fun exportPreferences(filePath: String): Boolean
    
    /**
     * Import user preferences from a file
     * @param filePath The path to import from
     * @return True if the import was successful, false otherwise
     */
    suspend fun importPreferences(filePath: String): Boolean
    
    /**
     * Clear all user preferences
     * @return True if the clear was successful, false otherwise
     */
    suspend fun clearAllPreferences(): Boolean
}