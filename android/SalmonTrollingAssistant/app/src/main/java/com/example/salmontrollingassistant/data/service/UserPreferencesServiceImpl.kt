package com.example.salmontrollingassistant.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.salmontrollingassistant.domain.model.DataSettings
import com.example.salmontrollingassistant.domain.model.DisplaySettings
import com.example.salmontrollingassistant.domain.model.ExperienceLevel
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.FontSize
import com.example.salmontrollingassistant.domain.model.ImageQuality
import com.example.salmontrollingassistant.domain.model.NotificationSettings
import com.example.salmontrollingassistant.domain.model.UserEquipment
import com.example.salmontrollingassistant.domain.model.UserPreferences
import com.example.salmontrollingassistant.domain.service.AuthenticationService
import com.example.salmontrollingassistant.domain.service.UserPreferencesService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesServiceImpl @Inject constructor(
    private val context: Context,
    private val authService: AuthenticationService,
    private val moshi: Moshi
) : UserPreferencesService {
    
    private val preferredSpeciesAdapter = moshi.adapter<List<FishSpecies>>(
        Types.newParameterizedType(List::class.java, FishSpecies::class.java)
    )
    
    private val preferredEquipmentAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )
    
    private val userEquipmentAdapter = moshi.adapter<List<UserEquipment>>(
        Types.newParameterizedType(List::class.java, UserEquipment::class.java)
    )
    
    override fun getUserPreferences(): Flow<UserPreferences> {
        return combine(
            getPreferredSpecies(),
            getPreferredEquipment(),
            getExperienceLevel(),
            getNotificationSettings(),
            getDisplaySettings(),
            getDataSettings()
        ) { preferredSpecies, preferredEquipment, experienceLevel, notificationSettings, displaySettings, dataSettings ->
            UserPreferences(
                preferredSpecies = preferredSpecies,
                preferredEquipment = preferredEquipment,
                experienceLevel = experienceLevel,
                notificationSettings = notificationSettings,
                displaySettings = displaySettings,
                dataSettings = dataSettings
            )
        }
    }
    
    override suspend fun updateUserPreferences(preferences: UserPreferences): Boolean {
        return try {
            updatePreferredSpecies(preferences.preferredSpecies)
            updatePreferredEquipment(preferences.preferredEquipment)
            updateExperienceLevel(preferences.experienceLevel.name)
            updateNotificationSettings(
                preferences.notificationSettings.enableWeatherAlerts,
                preferences.notificationSettings.enableTideAlerts,
                preferences.notificationSettings.enableOptimalConditionAlerts
            )
            updateDisplaySettings(
                preferences.displaySettings.useDarkMode,
                preferences.displaySettings.useHighContrastMode,
                preferences.displaySettings.useMetricSystem,
                preferences.displaySettings.fontSize.name
            )
            updateDataSettings(
                preferences.dataSettings.dataRefreshInterval,
                preferences.dataSettings.wifiOnlyDownloads,
                preferences.dataSettings.imageQuality.name,
                preferences.dataSettings.prefetchData,
                preferences.dataSettings.locationUpdateFrequency
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getUserEquipment(): Flow<List<UserEquipment>> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map emptyList<UserEquipment>()
            val key = stringPreferencesKey("${userId}_equipment")
            val json = preferences[key] ?: return@map emptyList<UserEquipment>()
            userEquipmentAdapter.fromJson(json) ?: emptyList()
        }
    }
    
    override suspend fun addUserEquipment(equipment: UserEquipment): Boolean {
        val userId = getCurrentUserId() ?: return false
        val key = stringPreferencesKey("${userId}_equipment")
        
        return try {
            context.dataStore.edit { preferences ->
                val currentEquipment = preferences[key]?.let {
                    userEquipmentAdapter.fromJson(it) ?: emptyList()
                } ?: emptyList()
                
                val updatedEquipment = currentEquipment + equipment
                preferences[key] = userEquipmentAdapter.toJson(updatedEquipment)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun removeUserEquipment(equipmentId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val key = stringPreferencesKey("${userId}_equipment")
        
        return try {
            context.dataStore.edit { preferences ->
                val currentEquipment = preferences[key]?.let {
                    userEquipmentAdapter.fromJson(it) ?: emptyList()
                } ?: emptyList()
                
                val updatedEquipment = currentEquipment.filter { it.equipmentId != equipmentId }
                preferences[key] = userEquipmentAdapter.toJson(updatedEquipment)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updateUserEquipment(equipment: UserEquipment): Boolean {
        val userId = getCurrentUserId() ?: return false
        val key = stringPreferencesKey("${userId}_equipment")
        
        return try {
            context.dataStore.edit { preferences ->
                val currentEquipment = preferences[key]?.let {
                    userEquipmentAdapter.fromJson(it) ?: emptyList()
                } ?: emptyList()
                
                val updatedEquipment = currentEquipment.map {
                    if (it.id == equipment.id) equipment else it
                }
                preferences[key] = userEquipmentAdapter.toJson(updatedEquipment)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updatePreferredSpecies(species: List<FishSpecies>): Boolean {
        val userId = getCurrentUserId() ?: return false
        val key = stringPreferencesKey("${userId}_preferred_species")
        
        return try {
            context.dataStore.edit { preferences ->
                preferences[key] = preferredSpeciesAdapter.toJson(species)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updatePreferredEquipment(equipmentIds: List<String>): Boolean {
        val userId = getCurrentUserId() ?: return false
        val key = stringPreferencesKey("${userId}_preferred_equipment")
        
        return try {
            context.dataStore.edit { preferences ->
                preferences[key] = preferredEquipmentAdapter.toJson(equipmentIds)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updateExperienceLevel(experienceLevel: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val key = stringPreferencesKey("${userId}_experience_level")
        
        return try {
            context.dataStore.edit { preferences ->
                preferences[key] = experienceLevel
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updateNotificationSettings(
        enableWeatherAlerts: Boolean,
        enableTideAlerts: Boolean,
        enableOptimalConditionAlerts: Boolean
    ): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            context.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey("${userId}_enable_weather_alerts")] = enableWeatherAlerts
                preferences[booleanPreferencesKey("${userId}_enable_tide_alerts")] = enableTideAlerts
                preferences[booleanPreferencesKey("${userId}_enable_optimal_condition_alerts")] = enableOptimalConditionAlerts
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getPreferredSpecies(): Flow<List<FishSpecies>> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map emptyList<FishSpecies>()
            val key = stringPreferencesKey("${userId}_preferred_species")
            val json = preferences[key] ?: return@map emptyList<FishSpecies>()
            preferredSpeciesAdapter.fromJson(json) ?: emptyList()
        }
    }
    
    private fun getPreferredEquipment(): Flow<List<String>> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map emptyList<String>()
            val key = stringPreferencesKey("${userId}_preferred_equipment")
            val json = preferences[key] ?: return@map emptyList<String>()
            preferredEquipmentAdapter.fromJson(json) ?: emptyList()
        }
    }
    
    private fun getExperienceLevel(): Flow<ExperienceLevel> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map ExperienceLevel.BEGINNER
            val key = stringPreferencesKey("${userId}_experience_level")
            val value = preferences[key] ?: return@map ExperienceLevel.BEGINNER
            try {
                ExperienceLevel.valueOf(value)
            } catch (e: Exception) {
                ExperienceLevel.BEGINNER
            }
        }
    }
    
    private fun getNotificationSettings(): Flow<NotificationSettings> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map NotificationSettings()
            
            NotificationSettings(
                enableWeatherAlerts = preferences[booleanPreferencesKey("${userId}_enable_weather_alerts")] ?: false,
                enableTideAlerts = preferences[booleanPreferencesKey("${userId}_enable_tide_alerts")] ?: false,
                enableOptimalConditionAlerts = preferences[booleanPreferencesKey("${userId}_enable_optimal_condition_alerts")] ?: false
            )
        }
    }
    
    private fun getDisplaySettings(): Flow<DisplaySettings> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map DisplaySettings()
            
            val useDarkMode = preferences[booleanPreferencesKey("${userId}_use_dark_mode")] ?: false
            val useHighContrastMode = preferences[booleanPreferencesKey("${userId}_use_high_contrast_mode")] ?: false
            val useMetricSystem = preferences[booleanPreferencesKey("${userId}_use_metric_system")] ?: false
            val fontSizeStr = preferences[stringPreferencesKey("${userId}_font_size")] ?: FontSize.MEDIUM.name
            
            val fontSize = try {
                FontSize.valueOf(fontSizeStr)
            } catch (e: Exception) {
                FontSize.MEDIUM
            }
            
            DisplaySettings(
                useDarkMode = useDarkMode,
                useHighContrastMode = useHighContrastMode,
                useMetricSystem = useMetricSystem,
                fontSize = fontSize
            )
        }
    }
    
    private fun getDataSettings(): Flow<DataSettings> {
        return context.dataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map DataSettings()
            
            val dataRefreshInterval = preferences[intPreferencesKey("${userId}_data_refresh_interval")] ?: 30
            val wifiOnlyDownloads = preferences[booleanPreferencesKey("${userId}_wifi_only_downloads")] ?: true
            val imageQualityStr = preferences[stringPreferencesKey("${userId}_image_quality")] ?: ImageQuality.MEDIUM.name
            val prefetchData = preferences[booleanPreferencesKey("${userId}_prefetch_data")] ?: true
            val locationUpdateFrequency = preferences[intPreferencesKey("${userId}_location_update_frequency")] ?: 5
            
            val imageQuality = try {
                ImageQuality.valueOf(imageQualityStr)
            } catch (e: Exception) {
                ImageQuality.MEDIUM
            }
            
            DataSettings(
                dataRefreshInterval = dataRefreshInterval,
                wifiOnlyDownloads = wifiOnlyDownloads,
                imageQuality = imageQuality,
                prefetchData = prefetchData,
                locationUpdateFrequency = locationUpdateFrequency
            )
        }
    }
    
    override suspend fun updateDisplaySettings(
        useDarkMode: Boolean,
        useHighContrastMode: Boolean,
        useMetricSystem: Boolean,
        fontSize: String
    ): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            context.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey("${userId}_use_dark_mode")] = useDarkMode
                preferences[booleanPreferencesKey("${userId}_use_high_contrast_mode")] = useHighContrastMode
                preferences[booleanPreferencesKey("${userId}_use_metric_system")] = useMetricSystem
                preferences[stringPreferencesKey("${userId}_font_size")] = fontSize
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updateDataSettings(
        dataRefreshInterval: Int,
        wifiOnlyDownloads: Boolean,
        imageQuality: String,
        prefetchData: Boolean,
        locationUpdateFrequency: Int
    ): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            context.dataStore.edit { preferences ->
                preferences[intPreferencesKey("${userId}_data_refresh_interval")] = dataRefreshInterval
                preferences[booleanPreferencesKey("${userId}_wifi_only_downloads")] = wifiOnlyDownloads
                preferences[stringPreferencesKey("${userId}_image_quality")] = imageQuality
                preferences[booleanPreferencesKey("${userId}_prefetch_data")] = prefetchData
                preferences[intPreferencesKey("${userId}_location_update_frequency")] = locationUpdateFrequency
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun exportPreferences(filePath: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            val preferences = getUserPreferences().map { it }.collect { it }
            val equipment = getUserEquipment().map { it }.collect { it }
            
            val exportData = mapOf(
                "preferences" to preferences,
                "equipment" to equipment
            )
            
            val json = moshi.adapter<Map<String, Any>>().toJson(exportData)
            
            val file = File(filePath)
            FileOutputStream(file).use { output ->
                output.write(json.toByteArray())
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun importPreferences(filePath: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            val file = File(filePath)
            val json = FileInputStream(file).bufferedReader().use { it.readText() }
            
            val importData = moshi.adapter<Map<String, Any>>().fromJson(json)
                ?: return false
            
            val preferences = importData["preferences"] as? UserPreferences ?: return false
            val equipment = importData["equipment"] as? List<UserEquipment> ?: return false
            
            // Clear existing preferences
            clearAllPreferences()
            
            // Import new preferences
            updateUserPreferences(preferences)
            
            // Import equipment
            equipment.forEach { addUserEquipment(it) }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun clearAllPreferences(): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun getCurrentUserId(): String? {
        return authService.getCurrentUser().map { it?.id }.collect { it }
    }
}