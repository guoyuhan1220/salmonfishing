package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.service.LocationPermissionStatus
import com.example.salmontrollingassistant.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPermissionViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {
    
    private val _permissionStatus = MutableStateFlow(LocationPermissionStatus.NOT_DETERMINED)
    val permissionStatus: StateFlow<LocationPermissionStatus> = _permissionStatus.asStateFlow()
    
    private val _continueWithoutPermission = MutableStateFlow(false)
    val continueWithoutPermission: StateFlow<Boolean> = _continueWithoutPermission.asStateFlow()
    
    init {
        viewModelScope.launch {
            locationService.permissionStatus.collect { status ->
                _permissionStatus.value = status
            }
        }
    }
    
    fun requestLocationPermission() {
        viewModelScope.launch {
            locationService.requestLocationPermission()
        }
    }
    
    fun continueWithoutPermission() {
        _continueWithoutPermission.value = true
    }
}