package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.model.ExperienceLevel
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.NotificationSettings
import com.example.salmontrollingassistant.domain.model.UserEquipment
import com.example.salmontrollingassistant.domain.model.UserPreferences
import com.example.salmontrollingassistant.domain.service.UserPreferencesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    private val preferencesService: UserPreferencesService
) : ViewModel() {
    
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()
    
    private val _userEquipment = MutableStateFlow<List<UserEquipment>>(emptyList())
    val userEquipment: StateFlow<List<UserEquipment>> = _userEquipment.asStateFlow()
    
    private val _preferredSpecies = MutableStateFlow<List<FishSpecies>>(emptyList())
    val preferredSpecies: StateFlow<List<FishSpecies>> = _preferredSpecies.asStateFlow()
    
    private val _preferredEquipment = MutableStateFlow<List<String>>(emptyList())
    val preferredEquipment: StateFlow<List<String>> = _preferredEquipment.asStateFlow()
    
    private val _experienceLevel = MutableStateFlow<ExperienceLevel>(ExperienceLevel.BEGINNER)
    val experienceLevel: StateFlow<ExperienceLevel> = _experienceLevel.asStateFlow()
    
    private val _notificationSettings = MutableStateFlow<NotificationSettings>(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()
    
    private val _displaySettings = MutableStateFlow<DisplaySettings>(DisplaySettings())
    val displaySettings: StateFlow<DisplaySettings> = _displaySettings.asStateFlow()
    
    private val _dataSettings = MutableStateFlow<DataSettings>(DataSettings())
    val dataSettings: StateFlow<DataSettings> = _dataSettings.asStateFlow()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    init {
        loadUserPreferences()
        loadUserEquipment()
    }
    
    private fun loadUserPreferences() {
        viewModelScope.launch {
            preferencesService.getUserPreferences().collectLatest { preferences ->
                _userPreferences.value = preferences
                _preferredSpecies.value = preferences.preferredSpecies
                _preferredEquipment.value = preferences.preferredEquipment
                _experienceLevel.value = preferences.experienceLevel
                _notificationSettings.value = preferences.notificationSettings
                _displaySettings.value = preferences.displaySettings
                _dataSettings.value = preferences.dataSettings
            }
        }
    }
    
    private fun loadUserEquipment() {
        viewModelScope.launch {
            preferencesService.getUserEquipment().collectLatest { equipment ->
                _userEquipment.value = equipment
            }
        }
    }
    
    fun updatePreferredSpecies(species: List<FishSpecies>) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updatePreferredSpecies(species)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update preferred species")
            }
        }
    }
    
    fun updatePreferredEquipment(equipmentIds: List<String>) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updatePreferredEquipment(equipmentIds)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update preferred equipment")
            }
        }
    }
    
    fun updateExperienceLevel(experienceLevel: ExperienceLevel) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updateExperienceLevel(experienceLevel.name)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update experience level")
            }
        }
    }
    
    fun updateNotificationSettings(
        enableWeatherAlerts: Boolean,
        enableTideAlerts: Boolean,
        enableOptimalConditionAlerts: Boolean
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updateNotificationSettings(
                enableWeatherAlerts,
                enableTideAlerts,
                enableOptimalConditionAlerts
            )
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update notification settings")
            }
        }
    }
    
    fun addUserEquipment(equipment: UserEquipment) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.addUserEquipment(equipment)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to add equipment")
            }
        }
    }
    
    fun removeUserEquipment(equipmentId: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.removeUserEquipment(equipmentId)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to remove equipment")
            }
        }
    }
    
    fun updateUserEquipment(equipment: UserEquipment) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updateUserEquipment(equipment)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update equipment")
            }
        }
    }
    
    fun updateDisplaySettings(
        useDarkMode: Boolean,
        useHighContrastMode: Boolean,
        useMetricSystem: Boolean,
        fontSize: FontSize
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updateDisplaySettings(
                useDarkMode,
                useHighContrastMode,
                useMetricSystem,
                fontSize.name
            )
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update display settings")
            }
        }
    }
    
    fun updateDataSettings(
        dataRefreshInterval: Int,
        wifiOnlyDownloads: Boolean,
        imageQuality: ImageQuality,
        prefetchData: Boolean,
        locationUpdateFrequency: Int
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updateDataSettings(
                dataRefreshInterval,
                wifiOnlyDownloads,
                imageQuality.name,
                prefetchData,
                locationUpdateFrequency
            )
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update data settings")
            }
        }
    }
    
    fun exportPreferences(filePath: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.exportPreferences(filePath)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to export preferences")
            }
        }
    }
    
    fun importPreferences(filePath: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.importPreferences(filePath)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to import preferences")
            }
        }
    }
    
    fun clearAllPreferences() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.clearAllPreferences()
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to clear preferences")
            }
        }
    }
    
    fun updateUserPreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val success = preferencesService.updateUserPreferences(preferences)
            _updateState.value = if (success) {
                UpdateState.Success
            } else {
                UpdateState.Error("Failed to update preferences")
            }
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = UpdateState.Initial
    }
    
    sealed class UpdateState {
        object Initial : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}