package com.example.salmontrollingassistant.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.salmontrollingassistant.data.db.AppDatabase
import com.example.salmontrollingassistant.data.db.LocationDao
import com.example.salmontrollingassistant.data.db.LocationEntity
import com.example.salmontrollingassistant.domain.model.Location as DomainLocation
import com.example.salmontrollingassistant.domain.service.LocationPermissionStatus
import com.example.salmontrollingassistant.domain.service.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private val Context.dataStore by preferencesDataStore(name = "saved_locations")
private val SAVED_LOCATIONS_KEY = stringPreferencesKey("saved_locations")

@Singleton
class LocationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationPermissionManager: LocationPermissionManager
) : LocationService {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val _currentLocation = MutableStateFlow<Location?>(null)
    
    // Shared preferences for location caching
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "location_cache", Context.MODE_PRIVATE
    )
    
    // Battery optimization settings
    private var isMoving = false
    private var lastSignificantMovement = Date()
    private val significantMovementThresholdMs = 5 * 60 * 1000 // 5 minutes
    
    // Location accuracy modes
    private var currentAccuracyMode = AccuracyMode.BALANCED
    
    // Gson for JSON serialization
    private val gson = Gson()
    
    // Keys for cached location
    private val CACHED_LOCATION_KEY = "cached_location"
    private val CACHED_LOCATION_TIME_KEY = "cached_location_time"
    
    enum class AccuracyMode {
        HIGH_ACCURACY,
        BALANCED,
        LOW_POWER
    }
    
    // Create location requests for different accuracy modes
    private val highAccuracyRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(5))
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(2))
        .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(10))
        .build()
    
    private val balancedRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, TimeUnit.SECONDS.toMillis(15))
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(10))
        .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(30))
        .build()
    
    private val lowPowerRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, TimeUnit.MINUTES.toMillis(1))
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
        .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(2))
        .build()
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleNewLocation(location)
            }
        }
    }
    
    init {
        // Load cached location on startup
        loadCachedLocation()?.let {
            _currentLocation.value = it
        }
        
        if (locationPermissionManager.hasLocationPermission()) {
            startLocationUpdates()
        }
    }
    
    override val currentLocation: Flow<Location?> = _currentLocation
    
    override val permissionStatus: Flow<LocationPermissionStatus> = locationPermissionManager.permissionStatus
    
    override suspend fun requestLocationPermission() {
        locationPermissionManager.requestLocationPermission()
        if (locationPermissionManager.hasLocationPermission()) {
            startLocationUpdates()
        }
    }
    
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<Location?> = runCatching {
        if (!locationPermissionManager.hasLocationPermission()) {
            return@runCatching null
        }
        
        // Check if we have a recent location
        val cachedLocation = _currentLocation.value
        if (cachedLocation != null) {
            // If the location is recent (less than 1 minute old), use it
            if (SystemClock.elapsedRealtimeNanos() - cachedLocation.elapsedRealtimeNanos < TimeUnit.MINUTES.toNanos(1)) {
                return@runCatching cachedLocation
            }
        }
        
        // Check if we have a stored location that's still valid
        loadCachedLocation()?.let { storedLocation ->
            // Use the stored location while requesting a fresh one in the background
            requestFreshLocation()
            return@runCatching storedLocation
        }
        
        // Request a fresh location
        return@runCatching suspendCancellableCoroutine { continuation ->
            // Temporarily switch to high accuracy
            val previousMode = currentAccuracyMode
            setAccuracyMode(AccuracyMode.HIGH_ACCURACY)
            
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        handleNewLocation(location)
                        continuation.resume(location)
                    } else {
                        // If last location is null, request a single update
                        val singleLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                            .setMaxUpdates(1)
                            .build()
                        
                        val singleLocationCallback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                result.lastLocation?.let { newLocation ->
                                    handleNewLocation(newLocation)
                                    continuation.resume(newLocation)
                                } ?: continuation.resume(null)
                                fusedLocationClient.removeLocationUpdates(this)
                                
                                // Reset accuracy mode
                                setAccuracyMode(previousMode)
                            }
                        }
                        
                        fusedLocationClient.requestLocationUpdates(
                            singleLocationRequest,
                            singleLocationCallback,
                            Looper.getMainLooper()
                        )
                        
                        continuation.invokeOnCancellation {
                            fusedLocationClient.removeLocationUpdates(singleLocationCallback)
                            setAccuracyMode(previousMode)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(null)
                    setAccuracyMode(previousMode)
                }
        }
    }
    
    override suspend fun searchLocations(query: String): Result<List<DomainLocation>> = runCatching {
        val geocoder = Geocoder(context)
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(query, 10) { addresses ->
                    continuation.resume(addresses ?: emptyList())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, 10) ?: emptyList()
        }
        
        return@runCatching addresses.map { address ->
            addressToDomainLocation(address)
        }
    }
    
    private val locationDao: LocationDao
    
    init {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "salmon_trolling_assistant_db"
        ).build()
        locationDao = db.locationDao()
    }
    
    override fun getSavedLocations(): Flow<List<DomainLocation>> = 
        locationDao.getAllLocations().map { entities ->
            entities.map { it.toDomainModel() }
        }
    
    override suspend fun saveLocation(location: DomainLocation): Result<Boolean> = runCatching {
        val locationToSave = location.copy(isSaved = true)
        locationDao.insertLocation(LocationEntity.fromDomainModel(locationToSave))
        true
    }
    
    override suspend fun deleteLocation(locationId: String): Result<Boolean> = runCatching {
        locationDao.deleteLocationById(locationId)
        true
    }
    
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!locationPermissionManager.hasLocationPermission()) {
            return
        }
        
        // Start with balanced accuracy
        setAccuracyMode(AccuracyMode.BALANCED)
    }
    
    @SuppressLint("MissingPermission")
    private fun setAccuracyMode(mode: AccuracyMode) {
        if (!locationPermissionManager.hasLocationPermission()) {
            return
        }
        
        // Remove any existing callbacks
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        currentAccuracyMode = mode
        
        // Select the appropriate request based on mode
        val request = when (mode) {
            AccuracyMode.HIGH_ACCURACY -> highAccuracyRequest
            AccuracyMode.BALANCED -> balancedRequest
            AccuracyMode.LOW_POWER -> lowPowerRequest
        }
        
        // Request location updates with the new settings
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
        
        // If in low power mode, schedule to stop updates after a while
        if (mode == AccuracyMode.LOW_POWER) {
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (currentAccuracyMode == AccuracyMode.LOW_POWER) {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }, TimeUnit.MINUTES.toMillis(5))
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun requestFreshLocation() {
        if (!locationPermissionManager.hasLocationPermission()) {
            return
        }
        
        // Save current mode
        val previousMode = currentAccuracyMode
        
        // Switch to high accuracy temporarily
        setAccuracyMode(AccuracyMode.HIGH_ACCURACY)
        
        // Reset after a short time
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            setAccuracyMode(previousMode)
        }, 10000) // 10 seconds
    }
    
    private fun handleNewLocation(location: Location) {
        val previousLocation = _currentLocation.value
        
        // Update the current location
        _currentLocation.value = location
        
        // Cache the location
        cacheLocation(location)
        
        // Check if this is a significant movement
        if (previousLocation != null) {
            val distance = location.distanceTo(previousLocation)
            val timeDiff = location.time - previousLocation.time
            
            // If moved more than 100 meters in less than 5 minutes, consider as moving
            if (distance > 100 && timeDiff < significantMovementThresholdMs) {
                isMoving = true
                lastSignificantMovement = Date()
                
                // If moving, increase accuracy if not already high
                if (currentAccuracyMode != AccuracyMode.HIGH_ACCURACY) {
                    setAccuracyMode(AccuracyMode.HIGH_ACCURACY)
                }
            } else {
                // Check if we haven't moved significantly for a while
                if (System.currentTimeMillis() - lastSignificantMovement.time > significantMovementThresholdMs) {
                    isMoving = false
                    
                    // If not moving, decrease accuracy to save battery
                    if (currentAccuracyMode == AccuracyMode.HIGH_ACCURACY) {
                        setAccuracyMode(AccuracyMode.BALANCED)
                    }
                }
            }
        } else {
            // First location update
            lastSignificantMovement = Date()
        }
    }
    
    private fun addressToDomainLocation(address: Address): DomainLocation {
        return DomainLocation(
            id = UUID.randomUUID().toString(),
            name = address.getAddressLine(0) ?: "Unknown Location",
            latitude = address.latitude,
            longitude = address.longitude,
            isSaved = false,
            notes = null
        )
    }
    
    // Location caching methods
    private fun cacheLocation(location: Location) {
        try {
            // Create a serializable location object
            val locationMap = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "accuracy" to location.accuracy,
                "altitude" to location.altitude,
                "speed" to location.speed,
                "bearing" to location.bearing,
                "time" to location.time
            )
            
            val locationJson = gson.toJson(locationMap)
            
            // Save to shared preferences
            sharedPreferences.edit()
                .putString(CACHED_LOCATION_KEY, locationJson)
                .putLong(CACHED_LOCATION_TIME_KEY, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
    
    private fun loadCachedLocation(): Location? {
        try {
            val locationJson = sharedPreferences.getString(CACHED_LOCATION_KEY, null) ?: return null
            val timestamp = sharedPreferences.getLong(CACHED_LOCATION_TIME_KEY, 0)
            
            // Check if cache is too old (more than 1 hour)
            if (System.currentTimeMillis() - timestamp > TimeUnit.HOURS.toMillis(1)) {
                return null
            }
            
            // Parse the location
            val locationMap = gson.fromJson<Map<String, Any>>(
                locationJson,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            // Create a new Location object
            val location = Location("cached")
            location.latitude = (locationMap["latitude"] as Number).toDouble()
            location.longitude = (locationMap["longitude"] as Number).toDouble()
            location.accuracy = (locationMap["accuracy"] as Number).toFloat()
            location.altitude = (locationMap["altitude"] as Number).toDouble()
            location.speed = (locationMap["speed"] as Number).toFloat()
            location.bearing = (locationMap["bearing"] as Number).toFloat()
            location.time = (locationMap["time"] as Number).toLong()
            
            return location
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
            return null
        }
    }
    
    private fun parseLocationsFromJson(json: String): List<DomainLocation> {
        return try {
            gson.fromJson(json, object : TypeToken<List<DomainLocation>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun locationsToJson(locations: List<DomainLocation>): String {
        return try {
            gson.toJson(locations)
        } catch (e: Exception) {
            "[]"
        }
    }
}