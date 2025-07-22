package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.salmontrollingassistant.domain.model.ImageQuality
import com.example.salmontrollingassistant.presentation.viewmodels.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    navController: NavController,
    viewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val dataSettings by viewModel.dataSettings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    
    var dataRefreshInterval by remember { mutableIntStateOf(30) }
    var wifiOnlyDownloads by remember { mutableStateOf(true) }
    var imageQuality by remember { mutableStateOf(ImageQuality.MEDIUM) }
    var prefetchData by remember { mutableStateOf(true) }
    var locationUpdateFrequency by remember { mutableIntStateOf(5) }
    
    // Sliders
    var dataRefreshSliderValue by remember { mutableFloatStateOf(30f) }
    var locationUpdateSliderValue by remember { mutableFloatStateOf(5f) }
    
    // Initialize state from preferences
    LaunchedEffect(dataSettings) {
        dataRefreshInterval = dataSettings.dataRefreshInterval
        dataRefreshSliderValue = dataRefreshInterval.toFloat()
        wifiOnlyDownloads = dataSettings.wifiOnlyDownloads
        imageQuality = dataSettings.imageQuality
        prefetchData = dataSettings.prefetchData
        locationUpdateFrequency = dataSettings.locationUpdateFrequency
        locationUpdateSliderValue = locationUpdateFrequency.toFloat()
    }
    
    // Show snackbar for update state
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserPreferencesViewModel.UpdateState.Success -> {
                snackbarHostState.showSnackbar("Data settings updated successfully")
                viewModel.resetUpdateState()
            }
            is UserPreferencesViewModel.UpdateState.Error -> {
                snackbarHostState.showSnackbar((updateState as UserPreferencesViewModel.UpdateState.Error).message)
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Network Usage",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WiFi-Only Downloads",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = wifiOnlyDownloads,
                            onCheckedChange = {
                                wifiOnlyDownloads = it
                                viewModel.updateDataSettings(
                                    dataRefreshInterval,
                                    wifiOnlyDownloads,
                                    imageQuality,
                                    prefetchData,
                                    locationUpdateFrequency
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "When enabled, data will only be downloaded when connected to WiFi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prefetch Data",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = prefetchData,
                            onCheckedChange = {
                                prefetchData = it
                                viewModel.updateDataSettings(
                                    dataRefreshInterval,
                                    wifiOnlyDownloads,
                                    imageQuality,
                                    prefetchData,
                                    locationUpdateFrequency
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "When enabled, data will be downloaded in advance for offline use",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Image Quality",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = imageQuality == ImageQuality.LOW,
                            onClick = {
                                imageQuality = ImageQuality.LOW
                                viewModel.updateDataSettings(
                                    dataRefreshInterval,
                                    wifiOnlyDownloads,
                                    imageQuality,
                                    prefetchData,
                                    locationUpdateFrequency
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("Low")
                        }
                        
                        SegmentedButton(
                            selected = imageQuality == ImageQuality.MEDIUM,
                            onClick = {
                                imageQuality = ImageQuality.MEDIUM
                                viewModel.updateDataSettings(
                                    dataRefreshInterval,
                                    wifiOnlyDownloads,
                                    imageQuality,
                                    prefetchData,
                                    locationUpdateFrequency
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("Medium")
                        }
                        
                        SegmentedButton(
                            selected = imageQuality == ImageQuality.HIGH,
                            onClick = {
                                imageQuality = ImageQuality.HIGH
                                viewModel.updateDataSettings(
                                    dataRefreshInterval,
                                    wifiOnlyDownloads,
                                    imageQuality,
                                    prefetchData,
                                    locationUpdateFrequency
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("High")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Lower quality uses less data but images may appear pixelated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Update Frequency",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Data Refresh Interval: $dataRefreshInterval minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = dataRefreshSliderValue,
                        onValueChange = { dataRefreshSliderValue = it },
                        onValueChangeFinished = {
                            dataRefreshInterval = dataRefreshSliderValue.toInt()
                            viewModel.updateDataSettings(
                                dataRefreshInterval,
                                wifiOnlyDownloads,
                                imageQuality,
                                prefetchData,
                                locationUpdateFrequency
                            )
                        },
                        valueRange = 5f..120f,
                        steps = 23 // (120-5)/5 = 23 steps
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Location Update Frequency: $locationUpdateFrequency minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = locationUpdateSliderValue,
                        onValueChange = { locationUpdateSliderValue = it },
                        onValueChangeFinished = {
                            locationUpdateFrequency = locationUpdateSliderValue.toInt()
                            viewModel.updateDataSettings(
                                dataRefreshInterval,
                                wifiOnlyDownloads,
                                imageQuality,
                                prefetchData,
                                locationUpdateFrequency
                            )
                        },
                        valueRange = 1f..30f,
                        steps = 29 // (30-1)/1 = 29 steps
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Less frequent updates save battery and data but may reduce accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Storage Management",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.clearAllPreferences()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Cached Data")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "This will clear all cached data and preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}