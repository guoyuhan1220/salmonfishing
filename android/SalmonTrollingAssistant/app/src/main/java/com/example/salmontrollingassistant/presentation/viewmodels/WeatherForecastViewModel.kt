package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.model.WeatherData
import com.example.salmontrollingassistant.domain.service.LocationService
import com.example.salmontrollingassistant.domain.service.TideService
import com.example.salmontrollingassistant.domain.service.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WeatherForecastViewModel @Inject constructor(
    private val weatherService: WeatherService,
    private val tideService: TideService,
    private val locationService: LocationService
) : ViewModel() {
    
    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather
    
    private val _currentTide = MutableStateFlow<TideData?>(null)
    val currentTide: StateFlow<TideData?> = _currentTide
    
    private val _forecastWeather = MutableStateFlow<List<WeatherData>>(emptyList())
    val forecastWeather: StateFlow<List<WeatherData>> = _forecastWeather
    
    private val _forecastTides = MutableStateFlow<List<TideData>>(emptyList())
    val forecastTides: StateFlow<List<TideData>> = _forecastTides
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var currentLocation: Location? = null
    
    init {
        getCurrentLocation()
    }
    
    private fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                locationService.getCurrentLocation().fold(
                    onSuccess = { location ->
                        currentLocation = location
                        loadCurrentWeather()
                        loadCurrentTide()
                    },
                    onFailure = { e ->
                        _error.value = "Could not get location: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Error getting location: ${e.message}"
            }
        }
    }
    
    fun loadCurrentWeather() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentLocation?.let { location ->
                    weatherService.getCurrentWeather(location).fold(
                        onSuccess = { weather ->
                            _currentWeather.value = weather
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = "Could not load weather: ${e.message}"
                        }
                    )
                } ?: run {
                    _error.value = "No location available"
                }
            } catch (e: Exception) {
                _error.value = "Error loading weather: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCurrentTide() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentLocation?.let { location ->
                    tideService.getCurrentTide(location).fold(
                        onSuccess = { tide ->
                            _currentTide.value = tide
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = "Could not load tide data: ${e.message}"
                        }
                    )
                } ?: run {
                    _error.value = "No location available"
                }
            } catch (e: Exception) {
                _error.value = "Error loading tide data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadForecast(days: Int = 7) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentLocation?.let { location ->
                    weatherService.getForecast(location, days).fold(
                        onSuccess = { forecast ->
                            _forecastWeather.value = forecast
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = "Could not load forecast: ${e.message}"
                        }
                    )
                    
                    tideService.getTidePredictions(location, days).fold(
                        onSuccess = { tides ->
                            _forecastTides.value = tides
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = "Could not load tide predictions: ${e.message}"
                        }
                    )
                } ?: run {
                    _error.value = "No location available"
                }
            } catch (e: Exception) {
                _error.value = "Error loading forecast: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getWeatherForDateTime(dateTime: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentLocation?.let { location ->
                    weatherService.getWeatherForDateTime(location, dateTime.time).fold(
                        onSuccess = { weather ->
                            _currentWeather.value = weather
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = "Could not load weather for selected time: ${e.message}"
                        }
                    )
                    
                    tideService.getTideForDateTime(location, dateTime.time).fold(
                        onSuccess = { tide ->
                            _currentTide.value = tide
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = "Could not load tide for selected time: ${e.message}"
                        }
                    )
                } ?: run {
                    _error.value = "No location available"
                }
            } catch (e: Exception) {
                _error.value = "Error loading data for selected time: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateLocation(location: Location) {
        currentLocation = location
        loadCurrentWeather()
        loadCurrentTide()
        loadForecast()
    }
}