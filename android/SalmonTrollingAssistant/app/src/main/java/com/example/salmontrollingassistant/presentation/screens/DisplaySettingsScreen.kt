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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.salmontrollingassistant.domain.model.FontSize
import com.example.salmontrollingassistant.presentation.viewmodels.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    navController: NavController,
    viewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val displaySettings by viewModel.displaySettings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    
    var useDarkMode by remember { mutableStateOf(false) }
    var useHighContrastMode by remember { mutableStateOf(false) }
    var useMetricSystem by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(FontSize.MEDIUM) }
    
    // Initialize state from preferences
    LaunchedEffect(displaySettings) {
        useDarkMode = displaySettings.useDarkMode
        useHighContrastMode = displaySettings.useHighContrastMode
        useMetricSystem = displaySettings.useMetricSystem
        fontSize = displaySettings.fontSize
    }
    
    // Show snackbar for update state
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserPreferencesViewModel.UpdateState.Success -> {
                snackbarHostState.showSnackbar("Display settings updated successfully")
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
                title = { Text("Display Settings") },
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
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Mode",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = useDarkMode,
                            onCheckedChange = {
                                useDarkMode = it
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "High Contrast Mode",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = useHighContrastMode,
                            onCheckedChange = {
                                useHighContrastMode = it
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            }
                        )
                    }
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
                        text = "Text Size",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = fontSize == FontSize.SMALL,
                            onClick = {
                                fontSize = FontSize.SMALL
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                        ) {
                            Text("Small")
                        }
                        
                        SegmentedButton(
                            selected = fontSize == FontSize.MEDIUM,
                            onClick = {
                                fontSize = FontSize.MEDIUM
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                        ) {
                            Text("Medium")
                        }
                        
                        SegmentedButton(
                            selected = fontSize == FontSize.LARGE,
                            onClick = {
                                fontSize = FontSize.LARGE
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                        ) {
                            Text("Large")
                        }
                        
                        SegmentedButton(
                            selected = fontSize == FontSize.EXTRA_LARGE,
                            onClick = {
                                fontSize = FontSize.EXTRA_LARGE
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                        ) {
                            Text("XL")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Choose a text size that is comfortable for you to read",
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
                        text = "Units",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use Metric System",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = useMetricSystem,
                            onCheckedChange = {
                                useMetricSystem = it
                                viewModel.updateDisplaySettings(
                                    useDarkMode,
                                    useHighContrastMode,
                                    useMetricSystem,
                                    fontSize
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (useMetricSystem) {
                            "Using metric units (°C, km, m/s)"
                        } else {
                            "Using imperial units (°F, mi, mph)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}