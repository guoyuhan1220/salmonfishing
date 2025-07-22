package com.example.salmontrollingassistant.presentation.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.model.Location as DomainLocation
import com.example.salmontrollingassistant.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {
    
    val savedLocations: Flow<List<DomainLocation>> = locationService.getSavedLocations()
    val currentLocation: Flow<Location?> = locationService.currentLocation
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<DomainLocation>>(emptyList())
    val searchResults: StateFlow<List<DomainLocation>> = _searchResults.asStateFlow()
    
    @OptIn(FlowPreview::class)
    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500) // Wait for 500ms of inactivity before searching
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .flatMapLatest { query ->
                    _isSearching.value = true
                    try {
                        val results = locationService.searchLocations(query).getOrNull() ?: emptyList()
                        flowOf(results)
                    } catch (e: Exception) {
                        flowOf(emptyList())
                    } finally {
                        _isSearching.value = false
                    }
                }
                .collect { results ->
                    _searchResults.value = results
                }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
        }
    }
    
    fun getCurrentLocation() {
        viewModelScope.launch {
            locationService.getCurrentLocation()
        }
    }
    
    fun saveLocation(location: DomainLocation) {
        viewModelScope.launch {
            locationService.saveLocation(location)
        }
    }
    
    fun deleteLocation(locationId: String) {
        viewModelScope.launch {
            locationService.deleteLocation(locationId)
        }
    }
}