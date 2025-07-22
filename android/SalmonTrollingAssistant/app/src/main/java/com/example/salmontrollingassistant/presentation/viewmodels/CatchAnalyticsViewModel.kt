package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.service.CatchAnalyticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CatchAnalyticsViewModel @Inject constructor(
    private val catchAnalyticsService: CatchAnalyticsService
) : ViewModel() {
    
    private val _catchCountBySpecies = MutableStateFlow<Map<FishSpecies, Int>>(emptyMap())
    val catchCountBySpecies: StateFlow<Map<FishSpecies, Int>> = _catchCountBySpecies.asStateFlow()
    
    private val _catchCountByLocation = MutableStateFlow<Map<String, Int>>(emptyMap())
    val catchCountByLocation: StateFlow<Map<String, Int>> = _catchCountByLocation.asStateFlow()
    
    private val _catchCountByMonth = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val catchCountByMonth: StateFlow<Map<Int, Int>> = _catchCountByMonth.asStateFlow()
    
    private val _averageSizeBySpecies = MutableStateFlow<Map<FishSpecies, Double>>(emptyMap())
    val averageSizeBySpecies: StateFlow<Map<FishSpecies, Double>> = _averageSizeBySpecies.asStateFlow()
    
    private val _averageWeightBySpecies = MutableStateFlow<Map<FishSpecies, Double>>(emptyMap())
    val averageWeightBySpecies: StateFlow<Map<FishSpecies, Double>> = _averageWeightBySpecies.asStateFlow()
    
    private val _mostSuccessfulEquipment = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val mostSuccessfulEquipment: StateFlow<List<Pair<String, Int>>> = _mostSuccessfulEquipment.asStateFlow()
    
    private val _mostSuccessfulLocations = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val mostSuccessfulLocations: StateFlow<List<Pair<String, Int>>> = _mostSuccessfulLocations.asStateFlow()
    
    private val _catchTrendOverTime = MutableStateFlow<List<Pair<Date, Int>>>(emptyList())
    val catchTrendOverTime: StateFlow<List<Pair<Date, Int>>> = _catchTrendOverTime.asStateFlow()
    
    private val _personalizedRecommendations = MutableStateFlow<List<String>>(emptyList())
    val personalizedRecommendations: StateFlow<List<String>> = _personalizedRecommendations.asStateFlow()
    
    init {
        loadAnalytics()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            catchAnalyticsService.getCatchCountBySpecies().collectLatest {
                _catchCountBySpecies.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getCatchCountByLocation().collectLatest {
                _catchCountByLocation.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getCatchCountByMonth().collectLatest {
                _catchCountByMonth.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getAverageSizeBySpecies().collectLatest {
                _averageSizeBySpecies.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getAverageWeightBySpecies().collectLatest {
                _averageWeightBySpecies.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getMostSuccessfulEquipment().collectLatest {
                _mostSuccessfulEquipment.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getMostSuccessfulLocations().collectLatest {
                _mostSuccessfulLocations.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getCatchTrendOverTime().collectLatest {
                _catchTrendOverTime.value = it
            }
        }
        
        viewModelScope.launch {
            catchAnalyticsService.getPersonalizedRecommendations().collectLatest {
                _personalizedRecommendations.value = it
            }
        }
    }
    
    fun refreshAnalytics() {
        loadAnalytics()
    }
}