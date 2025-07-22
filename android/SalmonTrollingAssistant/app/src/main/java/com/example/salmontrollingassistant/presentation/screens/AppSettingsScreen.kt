package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var useDarkMode by remember { mutableStateOf(false) }
    var useHighContrastMode by remember { mutableStateOf(false) }
    var useMetricSystem by remember { mutableStateOf(false) }
    var dataRefreshInterval by remember { mutableStateOf(30) } // minutes
    
    // Load preferences from DataStore
    LaunchedEffect(Unit) {
        // In a real app, this would load from DataStore
        // For now, we're just using default values
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
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
                text = "Display Settings",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode")
                Switch(
                    checked = useDarkMode,
                    onCheckedChange = { 
                        useDarkMode = it
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    }
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("High Contrast Mode")
                    Text(
                        "Better visibility in bright sunlight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = useHighContrastMode,
                    onCheckedChange = { 
                        useHighContrastMode = it
                        if (it) {
                            // When high contrast is enabled, we should disable dark mode
                            useDarkMode = false
                        }
                        scope.launch {
                            // In a real implementation, this would use the ThemeViewModel
                            // themeViewModel.setHighContrastEnabled(it)
                        }
                    }
                )
            }
            
            // Auto brightness detection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Auto Brightness Detection")
                    Text(
                        "Automatically enable high contrast in bright light",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = remember { mutableStateOf(false) }.value,
                    onCheckedChange = { 
                        scope.launch {
                            // In a real implementation, this would use the ThemeViewModel
                            // themeViewModel.setAutoBrightnessDetectionEnabled(it)
                        }
                    }
                )
            }
            
            // Preview of high contrast mode if enabled
            if (useHighContrastMode) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "High Contrast Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Yellow
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "This text is easier to read in bright sunlight.",
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Important information",
                                color = Color.Yellow
                            )
                            
                            Text(
                                "Details",
                                color = Color.Yellow.copy(alpha = 0.8f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Yellow
                            )
                        ) {
                            Text(
                                "Action Button",
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use Metric System")
                Switch(
                    checked = useMetricSystem,
                    onCheckedChange = { 
                        useMetricSystem = it
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = "Mobile Optimization",
                style = MaterialTheme.typography.titleMedium
            )
            
            // One-handed mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("One-Handed Mode")
                    Text(
                        "Optimizes layout for one-handed use",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = remember { mutableStateOf(false) }.value,
                    onCheckedChange = { 
                        scope.launch {
                            // In a real implementation, this would save to DataStore
                        }
                    }
                )
            }
            
            // Large touch targets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Large Touch Targets")
                    Text(
                        "Easier to tap with gloves or wet hands",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = remember { mutableStateOf(true) }.value,
                    onCheckedChange = { 
                        scope.launch {
                            // In a real implementation, this would save to DataStore
                        }
                    }
                )
            }
            
            // Orientation lock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Lock Orientation")
                    Text(
                        "Prevents screen rotation while on the water",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = remember { mutableStateOf(false) }.value,
                    onCheckedChange = { 
                        scope.launch {
                            // In a real implementation, this would save to DataStore
                        }
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = "Data Settings",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Data Refresh Interval",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = dataRefreshInterval == 15,
                    onClick = { 
                        dataRefreshInterval = 15
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("15 min") }
                )
                
                FilterChip(
                    selected = dataRefreshInterval == 30,
                    onClick = { 
                        dataRefreshInterval = 30
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("30 min") }
                )
                
                FilterChip(
                    selected = dataRefreshInterval == 60,
                    onClick = { 
                        dataRefreshInterval = 60
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("1 hour") }
                )
                
                FilterChip(
                    selected = dataRefreshInterval == 120,
                    onClick = { 
                        dataRefreshInterval = 120
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("2 hours") }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = "Battery & Data Usage",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { /* Navigate to battery optimization */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Battery Optimization",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Navigate to Battery Optimization"
                    )
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { /* Navigate to data usage settings */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Data Usage Settings",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Navigate to Data Usage Settings"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryOptimizationScreen(
    onNavigateBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    var locationUpdateFrequency by remember { mutableStateOf(5) } // minutes
    var backgroundRefresh by remember { mutableStateOf(true) }
    var lowPowerMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Optimization") },
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
                text = "Location Services",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Location Update Frequency",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = locationUpdateFrequency == 1,
                    onClick = { 
                        locationUpdateFrequency = 1
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("1 min") }
                )
                
                FilterChip(
                    selected = locationUpdateFrequency == 5,
                    onClick = { 
                        locationUpdateFrequency = 5
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("5 min") }
                )
                
                FilterChip(
                    selected = locationUpdateFrequency == 15,
                    onClick = { 
                        locationUpdateFrequency = 15
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("15 min") }
                )
                
                FilterChip(
                    selected = locationUpdateFrequency == 30,
                    onClick = { 
                        locationUpdateFrequency = 30
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    },
                    label = { Text("30 min") }
                )
            }
            
            Text(
                text = "Less frequent updates save battery but may reduce accuracy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Background Activity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Background Refresh")
                Switch(
                    checked = backgroundRefresh,
                    onCheckedChange = { 
                        backgroundRefresh = it
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    }
                )
            }
            
            Text(
                text = "Disable to save battery, but you'll need to manually refresh data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Power Saving",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low Power Mode")
                Switch(
                    checked = lowPowerMode,
                    onCheckedChange = { 
                        lowPowerMode = it
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    }
                )
            }
            
            Text(
                text = "Reduces app functionality to maximize battery life",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUsageScreen(
    onNavigateBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    var wifiOnlyDownloads by remember { mutableStateOf(false) }
    var imageQuality by remember { mutableStateOf(1) } // 0: Low, 1: Medium, 2: High
    var prefetchData by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Usage") },
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
                text = "Network Usage",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Download Updates on WiFi Only")
                Switch(
                    checked = wifiOnlyDownloads,
                    onCheckedChange = { 
                        wifiOnlyDownloads = it
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    }
                )
            }
            
            Text(
                text = "Prevents data usage on cellular networks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Image Quality",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterChip(
                        selected = imageQuality == 0,
                        onClick = { 
                            imageQuality = 0
                            scope.launch {
                                // Save to DataStore in a real app
                            }
                        },
                        label = { Text("Low (Save Data)") }
                    )
                    
                    FilterChip(
                        selected = imageQuality == 1,
                        onClick = { 
                            imageQuality = 1
                            scope.launch {
                                // Save to DataStore in a real app
                            }
                        },
                        label = { Text("Medium") }
                    )
                    
                    FilterChip(
                        selected = imageQuality == 2,
                        onClick = { 
                            imageQuality = 2
                            scope.launch {
                                // Save to DataStore in a real app
                            }
                        },
                        label = { Text("High") }
                    )
                }
            }
            
            Text(
                text = "Lower quality uses less data but images may appear pixelated",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Data Prefetching",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Prefetch Data When on WiFi")
                Switch(
                    checked = prefetchData,
                    onCheckedChange = { 
                        prefetchData = it
                        scope.launch {
                            // Save to DataStore in a real app
                        }
                    }
                )
            }
            
            Text(
                text = "Downloads data in advance for offline use",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Usage Statistics",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Monthly Usage")
                Text(
                    text = "~45 MB",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = { /* Reset usage statistics action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Reset Usage Statistics")
            }
        }
    }
}