package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.domain.model.EquipmentType
import com.example.salmontrollingassistant.domain.model.ExperienceLevel
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.UserEquipment
import com.example.salmontrollingassistant.presentation.viewmodels.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesScreen(
    viewModel: UserPreferencesViewModel = hiltViewModel(),
    onNavigateToAppSettings: () -> Unit = {},
    onNavigateToDataManagement: () -> Unit = {}
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val userEquipment by viewModel.userEquipment.collectAsState()
    val preferredSpecies by viewModel.preferredSpecies.collectAsState()
    val preferredEquipment by viewModel.preferredEquipment.collectAsState()
    val experienceLevel by viewModel.experienceLevel.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    
    var enableWeatherAlerts by remember { mutableStateOf(false) }
    var enableTideAlerts by remember { mutableStateOf(false) }
    var enableOptimalConditionAlerts by remember { mutableStateOf(false) }
    var selectedExperienceLevel by remember { mutableStateOf(ExperienceLevel.BEGINNER) }
    var selectedSpecies by remember { mutableStateOf<Set<FishSpecies>>(emptySet()) }
    var selectedPreferredEquipment by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var equipmentToEdit by remember { mutableStateOf<UserEquipment?>(null) }
    
    // Initialize state from preferences
    LaunchedEffect(userPreferences) {
        userPreferences?.let { prefs ->
            enableWeatherAlerts = prefs.notificationSettings.enableWeatherAlerts
            enableTideAlerts = prefs.notificationSettings.enableTideAlerts
            enableOptimalConditionAlerts = prefs.notificationSettings.enableOptimalConditionAlerts
            selectedExperienceLevel = prefs.experienceLevel
            selectedSpecies = prefs.preferredSpecies.toSet()
            selectedPreferredEquipment = prefs.preferredEquipment.toSet()
        }
    }
    
    // Show snackbar for update state
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserPreferencesViewModel.UpdateState.Success -> {
                snackbarHostState.showSnackbar("Preferences updated successfully")
                viewModel.resetUpdateState()
            }
            is UserPreferencesViewModel.UpdateState.Error -> {
                snackbarHostState.showSnackbar((updateState as UserPreferencesViewModel.UpdateState.Error).message)
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }
    
    if (showEquipmentDialog) {
        EquipmentEditorDialog(
            equipment = equipmentToEdit,
            onDismiss = { showEquipmentDialog = false },
            onSave = { equipment ->
                if (equipmentToEdit != null) {
                    viewModel.updateUserEquipment(equipment)
                } else {
                    viewModel.addUserEquipment(equipment)
                }
                showEquipmentDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Preferences") },
                actions = {
                    IconButton(onClick = {
                        // Save all preferences at once
                        userPreferences?.let { prefs ->
                            viewModel.updateUserPreferences(prefs)
                        }
                    }) {
                        Text("Save All")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Experience Level",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    ExperienceLevel.values().forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = level == selectedExperienceLevel,
                                    onClick = {
                                        selectedExperienceLevel = level
                                        viewModel.updateExperienceLevel(level)
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = level == selectedExperienceLevel,
                                onClick = null // null because we're handling the click on the row
                            )
                            Text(
                                text = level.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    text = "Preferred Fish Species",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FishSpecies.values().forEach { species ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedSpecies.contains(species),
                                onCheckedChange = { checked ->
                                    selectedSpecies = if (checked) {
                                        selectedSpecies + species
                                    } else {
                                        selectedSpecies - species
                                    }
                                    viewModel.updatePreferredSpecies(selectedSpecies.toList())
                                }
                            )
                            Text(
                                text = species.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    text = "Equipment Inventory",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (userEquipment.isEmpty()) {
                    Text(
                        text = "No equipment added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        userEquipment.forEach { equipment ->
                            EquipmentItem(
                                equipment = equipment,
                                onEdit = {
                                    equipmentToEdit = equipment
                                    showEquipmentDialog = true
                                },
                                onDelete = {
                                    viewModel.removeUserEquipment(equipment.equipmentId)
                                }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = {
                        equipmentToEdit = null
                        showEquipmentDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Equipment"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Equipment")
                }
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    text = "Preferred Equipment",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (userEquipment.isEmpty()) {
                    Text(
                        text = "Add equipment to your inventory first",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        userEquipment.forEach { equipment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedPreferredEquipment.contains(equipment.equipmentId),
                                    onCheckedChange = { checked ->
                                        selectedPreferredEquipment = if (checked) {
                                            selectedPreferredEquipment + equipment.equipmentId
                                        } else {
                                            selectedPreferredEquipment - equipment.equipmentId
                                        }
                                        viewModel.updatePreferredEquipment(selectedPreferredEquipment.toList())
                                    }
                                )
                                Text(
                                    text = equipment.equipmentId,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = enableWeatherAlerts,
                            onCheckedChange = { checked ->
                                enableWeatherAlerts = checked
                                viewModel.updateNotificationSettings(
                                    enableWeatherAlerts,
                                    enableTideAlerts,
                                    enableOptimalConditionAlerts
                                )
                            }
                        )
                        Text(
                            text = "Weather Alerts",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = enableTideAlerts,
                            onCheckedChange = { checked ->
                                enableTideAlerts = checked
                                viewModel.updateNotificationSettings(
                                    enableWeatherAlerts,
                                    enableTideAlerts,
                                    enableOptimalConditionAlerts
                                )
                            }
                        )
                        Text(
                            text = "Tide Alerts",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = enableOptimalConditionAlerts,
                            onCheckedChange = { checked ->
                                enableOptimalConditionAlerts = checked
                                viewModel.updateNotificationSettings(
                                    enableWeatherAlerts,
                                    enableTideAlerts,
                                    enableOptimalConditionAlerts
                                )
                            }
                        )
                        Text(
                            text = "Optimal Fishing Condition Alerts",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            
            item {
                Divider()
            }
            
            item {
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToAppSettings() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App Settings",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Navigate to App Settings"
                        )
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { navController.navigate("display_settings") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Display Settings",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Navigate to Display Settings"
                        )
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { navController.navigate("data_settings") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Data Settings",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Navigate to Data Settings"
                        )
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToDataManagement() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Data Management",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Navigate to Data Management"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentItem(
    equipment: UserEquipment,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (equipment.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color.Yellow,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    
                    Text(
                        text = if (equipment.name.isNotEmpty()) equipment.name else equipment.equipmentId,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Equipment"
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Equipment",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Text(
                text = equipment.equipmentType.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                equipment.color?.let { color ->
                    if (color.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = "Color",
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = color,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                equipment.size?.let { size ->
                    if (size.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LineWeight,
                                contentDescription = "Size",
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = size,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                equipment.brand?.let { brand ->
                    if (brand.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = "Brand",
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = brand,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            
            if (!equipment.notes.isNullOrEmpty()) {
                Text(
                    text = equipment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentEditorDialog(
    equipment: UserEquipment?,
    onDismiss: () -> Unit,
    onSave: (UserEquipment) -> Unit
) {
    var equipmentId by remember { mutableStateOf(equipment?.equipmentId ?: "") }
    var name by remember { mutableStateOf(equipment?.name ?: "") }
    var selectedType by remember { mutableStateOf(equipment?.equipmentType ?: EquipmentType.FLASHER) }
    var color by remember { mutableStateOf(equipment?.color ?: "") }
    var size by remember { mutableStateOf(equipment?.size ?: "") }
    var brand by remember { mutableStateOf(equipment?.brand ?: "") }
    var isFavorite by remember { mutableStateOf(equipment?.isFavorite ?: false) }
    var notes by remember { mutableStateOf(equipment?.notes ?: "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (equipment == null) "Add Equipment" else "Edit Equipment",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = equipmentId,
                    onValueChange = { equipmentId = it },
                    label = { Text("Equipment ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Type")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EquipmentType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { 
                                Text(
                                    type.name.lowercase().replaceFirstChar { it.uppercase() }
                                ) 
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Specifications",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Favorite")
                    Switch(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val newEquipment = UserEquipment(
                                id = equipment?.id ?: java.util.UUID.randomUUID().toString(),
                                equipmentId = equipmentId,
                                equipmentType = selectedType,
                                name = name,
                                color = color.ifEmpty { null },
                                size = size.ifEmpty { null },
                                brand = brand.ifEmpty { null },
                                isFavorite = isFavorite,
                                notes = notes.ifEmpty { null },
                                dateAdded = equipment?.dateAdded ?: System.currentTimeMillis()
                            )
                            onSave(newEquipment)
                        },
                        enabled = equipmentId.isNotEmpty() && name.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var useDarkMode by remember { mutableStateOf(false) }
    var useMetricSystem by remember { mutableStateOf(false) }
    var dataRefreshInterval by remember { mutableStateOf(30) } // minutes
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
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
                    onCheckedChange = { useDarkMode = it }
                )
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
                    onCheckedChange = { useMetricSystem = it }
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
                    onClick = { dataRefreshInterval = 15 },
                    label = { Text("15 min") }
                )
                
                FilterChip(
                    selected = dataRefreshInterval == 30,
                    onClick = { dataRefreshInterval = 30 },
                    label = { Text("30 min") }
                )
                
                FilterChip(
                    selected = dataRefreshInterval == 60,
                    onClick = { dataRefreshInterval = 60 },
                    label = { Text("1 hour") }
                )
                
                FilterChip(
                    selected = dataRefreshInterval == 120,
                    onClick = { dataRefreshInterval = 120 },
                    label = { Text("2 hours") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to clear all your data? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Clear data action
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
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
        ) {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium
            )
            
            Button(
                onClick = { /* Export data action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Export Data")
            }
            
            Button(
                onClick = { /* Import data action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Import Data")
            }
            
            Button(
                onClick = { showClearDataDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Clear All Data")
            }
        }
    }
}