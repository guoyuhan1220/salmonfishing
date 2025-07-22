package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.salmontrollingassistant.data.service.OfflineDataAccessLayer
import com.example.salmontrollingassistant.data.service.OfflineDataManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    offlineDataAccessLayer: OfflineDataAccessLayer = hiltViewModel(),
    offlineDataManager: OfflineDataManager = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    // State for offline data settings
    var cacheSizeMB by remember { mutableStateOf(100) }
    var cachePriority by remember { mutableStateOf(OfflineDataAccessLayer.CachePriority.MEDIUM) }
    var prefetchEnabled by remember { mutableStateOf(true) }
    var prefetchDays by remember { mutableStateOf(3) }
    var totalCacheSizeMB by remember { mutableStateOf(0f) }
    var offlineModeEnabled by remember { mutableStateOf(false) }
    
    // Load initial values
    LaunchedEffect(Unit) {
        cacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
        cachePriority = offlineDataAccessLayer.getCachePriority()
        prefetchEnabled = offlineDataAccessLayer.isPrefetchEnabled()
        prefetchDays = offlineDataAccessLayer.getPrefetchDays()
        totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
        offlineModeEnabled = offlineDataManager.isOfflineModeEnabled().first()
    }
    
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to clear all your data? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            offlineDataAccessLayer.clearAllCachedData()
                            totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
                        }
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("Are you sure you want to clear all cached data? You'll need to reconnect to download fresh data.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            offlineDataAccessLayer.clearAllCachedData()
                            totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
                        }
                        showClearCacheDialog = false
                    }
                ) {
                    Text("Clear Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { Text("Your data will be exported to a JSON file in your Downloads folder.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Export data action
                        showExportDialog = false
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text = { Text("This will replace your current data with the imported data. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Import data action
                        showImportDialog = false
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
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
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Export your data to a file or import from a previous backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export Data")
                        }
                        
                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Import Data")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Storage Usage",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Weather Data")
                        Text("${(totalCacheSizeMB * 0.4).toInt()} MB")
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tide Data")
                        Text("${(totalCacheSizeMB * 0.3).toInt()} MB")
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Location Data")
                        Text("${(totalCacheSizeMB * 0.1).toInt()} MB")
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Other Data")
                        Text("${(totalCacheSizeMB * 0.2).toInt()} MB")
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${totalCacheSizeMB.toInt()} MB",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showClearCacheDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Cache"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Cache")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Offline Data Settings",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Offline Mode")
                        Switch(
                            checked = offlineModeEnabled,
                            onCheckedChange = { enabled ->
                                offlineModeEnabled = enabled
                                coroutineScope.launch {
                                    offlineDataManager.setOfflineMode(enabled)
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "When offline mode is enabled, the app will not attempt to connect to the internet and will use cached data only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Cache Priority")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilterChip(
                            selected = cachePriority == OfflineDataAccessLayer.CachePriority.LOW,
                            onClick = {
                                cachePriority = OfflineDataAccessLayer.CachePriority.LOW
                                coroutineScope.launch {
                                    offlineDataAccessLayer.setCachePriority(OfflineDataAccessLayer.CachePriority.LOW)
                                    cacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
                                    prefetchDays = offlineDataAccessLayer.getPrefetchDays()
                                }
                            },
                            label = { Text("Low") }
                        )
                        
                        FilterChip(
                            selected = cachePriority == OfflineDataAccessLayer.CachePriority.MEDIUM,
                            onClick = {
                                cachePriority = OfflineDataAccessLayer.CachePriority.MEDIUM
                                coroutineScope.launch {
                                    offlineDataAccessLayer.setCachePriority(OfflineDataAccessLayer.CachePriority.MEDIUM)
                                    cacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
                                    prefetchDays = offlineDataAccessLayer.getPrefetchDays()
                                }
                            },
                            label = { Text("Medium") }
                        )
                        
                        FilterChip(
                            selected = cachePriority == OfflineDataAccessLayer.CachePriority.HIGH,
                            onClick = {
                                cachePriority = OfflineDataAccessLayer.CachePriority.HIGH
                                coroutineScope.launch {
                                    offlineDataAccessLayer.setCachePriority(OfflineDataAccessLayer.CachePriority.HIGH)
                                    cacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
                                    prefetchDays = offlineDataAccessLayer.getPrefetchDays()
                                }
                            },
                            label = { Text("High") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (cachePriority) {
                            OfflineDataAccessLayer.CachePriority.LOW -> "Minimal caching to save storage space. Limited offline functionality."
                            OfflineDataAccessLayer.CachePriority.MEDIUM -> "Balanced caching for moderate offline functionality."
                            OfflineDataAccessLayer.CachePriority.HIGH -> "Maximum caching for best offline experience."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prefetch Data")
                        Switch(
                            checked = prefetchEnabled,
                            onCheckedChange = { enabled ->
                                prefetchEnabled = enabled
                                coroutineScope.launch {
                                    offlineDataAccessLayer.setPrefetchEnabled(enabled)
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Automatically download data for saved locations to ensure offline availability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (prefetchEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Prefetch Days: $prefetchDays")
                        
                        Slider(
                            value = prefetchDays.toFloat(),
                            onValueChange = { value ->
                                prefetchDays = value.toInt()
                            },
                            onValueChangeFinished = {
                                coroutineScope.launch {
                                    offlineDataAccessLayer.setPrefetchDays(prefetchDays)
                                }
                            },
                            valueRange = 1f..7f,
                            steps = 5
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Maximum Cache Size: $cacheSizeMB MB")
                    
                    Slider(
                        value = cacheSizeMB.toFloat(),
                        onValueChange = { value ->
                            cacheSizeMB = value.toInt()
                        },
                        onValueChangeFinished = {
                            coroutineScope.launch {
                                offlineDataAccessLayer.setMaxCacheSizeMB(cacheSizeMB)
                            }
                        },
                        valueRange = 10f..500f
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                offlineDataAccessLayer.cleanupCache()
                                totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clean Up Cache"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clean Up Cache")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Clear Data",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Remove all your data from this device. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Data")
                    }
                }
            }
        }
    }
}