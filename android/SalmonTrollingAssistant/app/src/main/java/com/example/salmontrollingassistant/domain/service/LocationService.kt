package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.Location
import kotlinx.coroutines.flow.Flow

enum class LocationPermissionStatus {
    NOT_DETERMINED,
    DENIED,
    GRANTED,
    PERMANENTLY_DENIED;
    
    val isGranted: Boolean
        get() = this == GRANTED
}

interface LocationService {
    val currentLocation: Flow<android.location.Location?>
    val permissionStatus: Flow<LocationPermissionStatus>
    
    suspend fun requestLocationPermission()
    suspend fun getCurrentLocation(): Result<android.location.Location?>
    suspend fun searchLocations(query: String): Result<List<Location>>
    fun getSavedLocations(): Flow<List<Location>>
    suspend fun saveLocation(location: Location): Result<Boolean>
    suspend fun deleteLocation(locationId: String): Result<Boolean>
}