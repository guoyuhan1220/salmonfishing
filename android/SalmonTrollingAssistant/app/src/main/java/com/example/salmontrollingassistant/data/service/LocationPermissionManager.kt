package com.example.salmontrollingassistant.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.salmontrollingassistant.domain.service.LocationPermissionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _permissionStatus = MutableStateFlow(getInitialPermissionStatus())
    val permissionStatus: Flow<LocationPermissionStatus> = _permissionStatus
    
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var permissionCallback: ((Boolean) -> Unit)? = null
    
    fun registerWithActivity(activity: FragmentActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            _permissionStatus.value = if (isGranted) {
                LocationPermissionStatus.GRANTED
            } else {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    LocationPermissionStatus.DENIED
                } else {
                    LocationPermissionStatus.PERMANENTLY_DENIED
                }
            }
            permissionCallback?.invoke(isGranted)
            permissionCallback = null
        }
    }
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun requestLocationPermission(): Boolean {
        if (hasLocationPermission()) {
            _permissionStatus.value = LocationPermissionStatus.GRANTED
            return true
        }
        
        val launcher = permissionLauncher ?: return false
        
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            permissionCallback = { isGranted ->
                continuation.resume(isGranted) { }
            }
            
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            
            continuation.invokeOnCancellation {
                permissionCallback = null
            }
        }
    }
    
    private fun getInitialPermissionStatus(): LocationPermissionStatus {
        return if (hasLocationPermission()) {
            LocationPermissionStatus.GRANTED
        } else {
            LocationPermissionStatus.NOT_DETERMINED
        }
    }
}