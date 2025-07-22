package com.example.salmontrollingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.salmontrollingassistant.data.service.LocationPermissionManager
import com.example.salmontrollingassistant.domain.service.LocationPermissionStatus
import com.example.salmontrollingassistant.presentation.navigation.AppNavHost
import com.example.salmontrollingassistant.presentation.screens.LocationPermissionScreen
import com.example.salmontrollingassistant.presentation.theme.SalmonTrollingAssistantTheme
import com.example.salmontrollingassistant.presentation.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var locationPermissionManager: LocationPermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register the permission manager with this activity
        locationPermissionManager.registerWithActivity(this)
        
        setContent {
            SalmonTrollingAssistantTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val permissionStatus by locationPermissionManager.permissionStatus.collectAsState(initial = LocationPermissionStatus.NOT_DETERMINED)
                    var skipPermissionCheck by remember { mutableStateOf(false) }
                    
                    if ((permissionStatus == LocationPermissionStatus.GRANTED || skipPermissionCheck)) {
                        val navController = rememberNavController()
                        AppNavHost(navController = navController)
                    } else {
                        LocationPermissionScreen(
                            onPermissionGranted = { /* Permission was granted, handled by state */ },
                            onContinueWithoutPermission = { skipPermissionCheck = true }
                        )
                    }
                }
            }
        }
    }
}