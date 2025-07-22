package com.example.salmontrollingassistant.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.domain.service.LocationPermissionStatus
import com.example.salmontrollingassistant.presentation.viewmodels.LocationPermissionViewModel

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit,
    onContinueWithoutPermission: () -> Unit,
    viewModel: LocationPermissionViewModel = hiltViewModel()
) {
    val permissionStatus by viewModel.permissionStatus.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colors.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Location Access Required",
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Salmon Trolling Assistant needs your location to provide accurate weather and tide information for your fishing spot.",
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        when (permissionStatus) {
            LocationPermissionStatus.PERMANENTLY_DENIED -> {
                Text(
                    text = "You have permanently denied location access. Please enable it in Settings to use all features.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            }
            LocationPermissionStatus.DENIED -> {
                Text(
                    text = "Location permission is needed for the best experience.",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.requestLocationPermission() }
                ) {
                    Text("Allow Location Access")
                }
            }
            else -> {
                Button(
                    onClick = { viewModel.requestLocationPermission() }
                ) {
                    Text("Allow Location Access")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = {
                viewModel.continueWithoutPermission()
                onContinueWithoutPermission()
            }
        ) {
            Text("Continue Without Location")
        }
    }
    
    // Observe permission status changes
    if (permissionStatus == LocationPermissionStatus.GRANTED) {
        onPermissionGranted()
    }
}