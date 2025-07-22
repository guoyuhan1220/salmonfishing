package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.salmontrollingassistant.presentation.viewmodels.DataUsageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUsageSettingsScreen(
    navController: NavController,
    viewModel: DataUsageViewModel = hiltViewModel()
) {
    val dataUsageLimitEnabled by viewModel.dataUsageLimitEnabled.collectAsState()
    val dataUsageLimit by viewModel.dataUsageLimit.collectAsState()
    val wifiOnlyPrefetch by viewModel.wifiOnlyPrefetch.collectAsState()
    val compressionEnabled by viewModel.compressionEnabled.collectAsState()
    val compressionQuality by viewModel.compressionQuality.collectAsState()
    val currentDataUsage by viewModel.currentDataUsage.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Usage Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Current data usage card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Data Usage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "$currentDataUsage MB used",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (dataUsageLimitEnabled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                LinearProgressIndicator(
                                    progress = (currentDataUsage.toFloat() / dataUsageLimit).coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Limit: $dataUsageLimit MB",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (networkType) {
                                "WIFI" -> Icons.Default.Wifi
                                else -> Icons.Default.NetworkCell
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Current network: $networkType",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    androidx.compose.material3.Button(
                        onClick = { viewModel.resetDataUsage() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset Counter")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data usage limit settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data Usage Limits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable data usage limit",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = dataUsageLimitEnabled,
                            onCheckedChange = { viewModel.setDataUsageLimitEnabled(it) }
                        )
                    }
                    
                    if (dataUsageLimitEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Data usage limit: $dataUsageLimit MB",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var sliderPosition by remember { mutableStateOf(dataUsageLimit.toFloat()) }
                        
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            onValueChangeFinished = {
                                viewModel.setDataUsageLimit(sliderPosition.toInt())
                            },
                            valueRange = 10f..1000f,
                            steps = 99
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Prefetching settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Prefetching Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "WiFi-only prefetching",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = wifiOnlyPrefetch,
                            onCheckedChange = { viewModel.setWifiOnlyPrefetch(it) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "When enabled, the app will only download forecast data in advance when connected to WiFi.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Compression settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Compression Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable data compression",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = compressionEnabled,
                            onCheckedChange = { viewModel.setCompressionEnabled(it) }
                        )
                    }
                    
                    if (compressionEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Image compression quality: $compressionQuality%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var sliderPosition by remember { mutableStateOf(compressionQuality.toFloat()) }
                        
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            onValueChangeFinished = {
                                viewModel.setCompressionQuality(sliderPosition.toInt())
                            },
                            valueRange = 10f..100f,
                            steps = 9
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Lower quality saves more data but reduces image clarity.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}