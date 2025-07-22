package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.model.CatchData
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.service.CatchLoggingService
import com.example.salmontrollingassistant.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CatchLoggingViewModel @Inject constructor(
    private val catchLoggingService: CatchLoggingService,
    private val locationService: LocationService
) : ViewModel() {
    
    private val _catchHistory = MutableStateFlow<List<CatchData>>(emptyList())
    val catchHistory: StateFlow<List<CatchData>> = _catchHistory.asStateFlow()
    
    private val _selectedCatch = MutableStateFlow<CatchData?>(null)
    val selectedCatch: StateFlow<CatchData?> = _selectedCatch.asStateFlow()
    
    private val _catchLoggingState = MutableStateFlow<CatchLoggingState>(CatchLoggingState.Initial)
    val catchLoggingState: StateFlow<CatchLoggingState> = _catchLoggingState.asStateFlow()
    
    init {
        loadCatchHistory()
    }
    
    private fun loadCatchHistory() {
        viewModelScope.launch {
            catchLoggingService.getCatchHistory().collectLatest { catches ->
                _catchHistory.value = catches.sortedByDescending { it.timestamp }
            }
        }
    }
    
    fun logCatch(
        locationId: String,
        species: FishSpecies,
        size: Double?,
        weight: Double?,
        equipmentUsed: List<String>,
        weatherConditionsId: String?,
        tideConditionsId: String?,
        notes: String?,
        photoUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _catchLoggingState.value = CatchLoggingState.Loading
            
            val catchData = CatchData(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                locationId = locationId,
                species = species,
                size = size,
                weight = weight,
                equipmentUsed = equipmentUsed,
                weatherConditionsId = weatherConditionsId,
                tideConditionsId = tideConditionsId,
                notes = notes,
                photoUrls = photoUrls
            )
            
            val result = catchLoggingService.logCatch(catchData)
            
            _catchLoggingState.value = if (result.isSuccess) {
                CatchLoggingState.Success
            } else {
                CatchLoggingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    fun updateCatch(catchData: CatchData) {
        viewModelScope.launch {
            _catchLoggingState.value = CatchLoggingState.Loading
            
            val result = catchLoggingService.updateCatch(catchData)
            
            _catchLoggingState.value = if (result.isSuccess) {
                CatchLoggingState.Success
            } else {
                CatchLoggingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    fun deleteCatch(catchId: String) {
        viewModelScope.launch {
            _catchLoggingState.value = CatchLoggingState.Loading
            
            val result = catchLoggingService.deleteCatch(catchId)
            
            _catchLoggingState.value = if (result.isSuccess) {
                CatchLoggingState.Success
            } else {
                CatchLoggingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    fun addPhotoCatch(catchId: String, photoUri: String) {
        viewModelScope.launch {
            _catchLoggingState.value = CatchLoggingState.Loading
            
            val result = catchLoggingService.addPhotoCatch(catchId, photoUri)
            
            _catchLoggingState.value = if (result.isSuccess) {
                CatchLoggingState.Success
            } else {
                CatchLoggingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    fun removePhotoCatch(catchId: String, photoUri: String) {
        viewModelScope.launch {
            _catchLoggingState.value = CatchLoggingState.Loading
            
            val result = catchLoggingService.removePhotoCatch(catchId, photoUri)
            
            _catchLoggingState.value = if (result.isSuccess) {
                CatchLoggingState.Success
            } else {
                CatchLoggingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    fun selectCatch(catchId: String) {
        viewModelScope.launch {
            val result = catchLoggingService.getCatchById(catchId)
            if (result.isSuccess) {
                _selectedCatch.value = result.getOrNull()
            }
        }
    }
    
    fun clearSelectedCatch() {
        _selectedCatch.value = null
    }
    
    fun resetCatchLoggingState() {
        _catchLoggingState.value = CatchLoggingState.Initial
    }
    
    sealed class CatchLoggingState {
        object Initial : CatchLoggingState()
        object Loading : CatchLoggingState()
        object Success : CatchLoggingState()
        data class Error(val message: String) : CatchLoggingState()
    }
}