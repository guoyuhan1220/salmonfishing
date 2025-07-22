package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.domain.model.CatchData
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.presentation.viewmodels.CatchLoggingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchLoggingScreen(
    onNavigateBack: () -> Unit,
    viewModel: CatchLoggingViewModel = hiltViewModel()
) {
    val catchHistory by viewModel.catchHistory.collectAsState()
    val selectedCatch by viewModel.selectedCatch.collectAsState()
    val catchLoggingState by viewModel.catchLoggingState.collectAsState()
    
    var showAddCatchDialog by remember { mutableStateOf(false) }
    var showEditCatchDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(catchLoggingState) {
        when (catchLoggingState) {
            is CatchLoggingViewModel.CatchLoggingState.Success -> {
                snackbarHostState.showSnackbar("Operation completed successfully")
                viewModel.resetCatchLoggingState()
            }
            is CatchLoggingViewModel.CatchLoggingState.Error -> {
                snackbarHostState.showSnackbar((catchLoggingState as CatchLoggingViewModel.CatchLoggingState.Error).message)
                viewModel.resetCatchLoggingState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catch History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCatchDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Catch")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (catchHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No catches logged yet.\nTap the + button to add your first catch!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(catchHistory) { catchData ->
                        CatchItem(
                            catchData = catchData,
                            onEditClick = {
                                viewModel.selectCatch(catchData.id)
                                showEditCatchDialog = true
                            },
                            onDeleteClick = {
                                viewModel.deleteCatch(catchData.id)
                            }
                        )
                    }
                }
            }
            
            if (showAddCatchDialog) {
                AddEditCatchDialog(
                    catchData = null,
                    onDismiss = { showAddCatchDialog = false },
                    onSave = { locationId, species, size, weight, equipment, weatherId, tideId, notes ->
                        viewModel.logCatch(
                            locationId = locationId,
                            species = species,
                            size = size,
                            weight = weight,
                            equipmentUsed = equipment,
                            weatherConditionsId = weatherId,
                            tideConditionsId = tideId,
                            notes = notes
                        )
                        showAddCatchDialog = false
                    }
                )
            }
            
            if (showEditCatchDialog && selectedCatch != null) {
                AddEditCatchDialog(
                    catchData = selectedCatch,
                    onDismiss = {
                        showEditCatchDialog = false
                        viewModel.clearSelectedCatch()
                    },
                    onSave = { locationId, species, size, weight, equipment, weatherId, tideId, notes ->
                        selectedCatch?.let { catch ->
                            val updatedCatch = catch.copy(
                                locationId = locationId,
                                species = species,
                                size = size,
                                weight = weight,
                                equipmentUsed = equipment,
                                weatherConditionsId = weatherId,
                                tideConditionsId = tideId,
                                notes = notes
                            )
                            viewModel.updateCatch(updatedCatch)
                        }
                        showEditCatchDialog = false
                        viewModel.clearSelectedCatch()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchItem(
    catchData: CatchData,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(catchData.timestamp))
    
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
                Text(
                    text = catchData.species.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (catchData.size != null) {
                        Text(
                            text = "Size: ${catchData.size} inches",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (catchData.weight != null) {
                        Text(
                            text = "Weight: ${catchData.weight} lbs",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (catchData.notes != null && catchData.notes.isNotEmpty()) {
                        Text(
                            text = "Notes: ${catchData.notes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCatchDialog(
    catchData: CatchData?,
    onDismiss: () -> Unit,
    onSave: (locationId: String, species: FishSpecies, size: Double?, weight: Double?, equipment: List<String>, weatherId: String?, tideId: String?, notes: String?) -> Unit
) {
    val isEditing = catchData != null
    
    var locationId by remember { mutableStateOf(catchData?.locationId ?: "") }
    var selectedSpecies by remember { mutableStateOf(catchData?.species ?: FishSpecies.CHINOOK) }
    var sizeText by remember { mutableStateOf(catchData?.size?.toString() ?: "") }
    var weightText by remember { mutableStateOf(catchData?.weight?.toString() ?: "") }
    var equipment by remember { mutableStateOf(catchData?.equipmentUsed ?: emptyList()) }
    var weatherId by remember { mutableStateOf(catchData?.weatherConditionsId ?: "") }
    var tideId by remember { mutableStateOf(catchData?.tideConditionsId ?: "") }
    var notes by remember { mutableStateOf(catchData?.notes ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Catch" else "Log New Catch") },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = locationId,
                        onValueChange = { locationId = it },
                        label = { Text("Location ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    Text(
                        text = "Species",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        FishSpecies.values().forEach { species ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSpecies == species,
                                    onClick = { selectedSpecies = species }
                                )
                                Text(
                                    text = species.name,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = sizeText,
                        onValueChange = { sizeText = it },
                        label = { Text("Size (inches)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (lbs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(100.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        locationId,
                        selectedSpecies,
                        sizeText.toDoubleOrNull(),
                        weightText.toDoubleOrNull(),
                        equipment,
                        if (weatherId.isNotEmpty()) weatherId else null,
                        if (tideId.isNotEmpty()) tideId else null,
                        if (notes.isNotEmpty()) notes else null
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}