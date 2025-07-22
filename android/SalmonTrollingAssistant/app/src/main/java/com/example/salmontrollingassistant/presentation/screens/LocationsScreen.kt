package com.example.salmontrollingassistant.presentation.screens

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.domain.model.Location as DomainLocation
import com.example.salmontrollingassistant.presentation.viewmodels.LocationsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationsScreen(
    viewModel: LocationsViewModel = hiltViewModel()
) {
    val savedLocations by viewModel.savedLocations.collectAsState(initial = emptyList())
    val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())
    val currentLocation by viewModel.currentLocation.collectAsState(initial = null)
    val isSearching by viewModel.isSearching.collectAsState(initial = false)
    val searchQuery by viewModel.searchQuery.collectAsState(initial = "")
    
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var showLocationDetailDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<DomainLocation?>(null) }
    
    // Default to Seattle if no current location
    val defaultLocation = LatLng(47.6062, -122.3321)
    val mapLocation = currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapLocation, 12f)
    }
    
    // Update camera position when current location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude), 12f
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locations") },
                actions = {
                    IconButton(onClick = { viewModel.getCurrentLocation() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddLocationDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // Current location marker
                    currentLocation?.let {
                        Marker(
                            state = MarkerState(LatLng(it.latitude, it.longitude)),
                            title = "Current Location",
                            snippet = "You are here"
                        )
                    }
                    
                    // Saved locations markers
                    savedLocations.forEach { location ->
                        Marker(
                            state = MarkerState(LatLng(location.latitude, location.longitude)),
                            title = location.name,
                            snippet = location.notes ?: "",
                            onClick = {
                                selectedLocation = location
                                showLocationDetailDialog = true
                                true
                            }
                        )
                    }
                }
            }
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search locations") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            
            // Location list
            if (isSearching) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(searchResults) { location ->
                        LocationItem(
                            location = location,
                            onClick = {
                                // Save the location
                                viewModel.saveLocation(location)
                                
                                // Update camera position
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                    LatLng(location.latitude, location.longitude), 12f
                                )
                                
                                // Clear search
                                viewModel.updateSearchQuery("")
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item {
                        Text(
                            text = "Saved Locations",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    items(savedLocations) { location ->
                        LocationItem(
                            location = location,
                            onClick = {
                                selectedLocation = location
                                showLocationDetailDialog = true
                                
                                // Update camera position
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                    LatLng(location.latitude, location.longitude), 12f
                                )
                            },
                            onDelete = {
                                viewModel.deleteLocation(location.id)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add Location Dialog
    if (showAddLocationDialog) {
        AddLocationDialog(
            currentLocation = currentLocation,
            onDismiss = { showAddLocationDialog = false },
            onSave = { name, notes, latitude, longitude ->
                viewModel.saveLocation(
                    DomainLocation(
                        id = java.util.UUID.randomUUID().toString(),
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        isSaved = true,
                        notes = notes.takeIf { it.isNotBlank() }
                    )
                )
                showAddLocationDialog = false
            }
        )
    }
    
    // Location Detail Dialog
    if (showLocationDetailDialog && selectedLocation != null) {
        LocationDetailDialog(
            location = selectedLocation!!,
            onDismiss = { showLocationDetailDialog = false },
            onDelete = {
                viewModel.deleteLocation(selectedLocation!!.id)
                showLocationDetailDialog = false
            },
            onSave = { name, notes ->
                viewModel.saveLocation(
                    selectedLocation!!.copy(
                        name = name,
                        notes = notes.takeIf { it.isNotBlank() }
                    )
                )
                showLocationDetailDialog = false
            }
        )
    }
}

@Composable
fun LocationItem(
    location: DomainLocation,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.h6
                )
                
                Text(
                    text = String.format("%.6f, %.6f", location.latitude, location.longitude),
                    style = MaterialTheme.typography.caption
                )
                
                if (!location.notes.isNullOrBlank()) {
                    Text(
                        text = location.notes,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun AddLocationDialog(
    currentLocation: Location?,
    onDismiss: () -> Unit,
    onSave: (name: String, notes: String, latitude: Double, longitude: Double) -> Unit
) {
    var locationName by remember { mutableStateOf("") }
    var locationNotes by remember { mutableStateOf("") }
    var useCurrentLocation by remember { mutableStateOf(true) }
    var customLatitude by remember { mutableStateOf("") }
    var customLongitude by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Initialize custom coordinates with current location if available
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            customLatitude = it.latitude.toString()
            customLongitude = it.longitude.toString()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Location") },
        text = {
            Column {
                TextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextField(
                    value = locationNotes,
                    onValueChange = { locationNotes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Use Current Location",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = useCurrentLocation,
                        onCheckedChange = { useCurrentLocation = it }
                    )
                }
                
                if (!useCurrentLocation) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = customLatitude,
                        onValueChange = { customLatitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextField(
                        value = customLongitude,
                        onValueChange = { customLongitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (locationName.isBlank()) {
                        showError = true
                        errorMessage = "Please enter a location name"
                        return@TextButton
                    }
                    
                    if (useCurrentLocation) {
                        if (currentLocation == null) {
                            showError = true
                            errorMessage = "Current location is not available"
                            return@TextButton
                        }
                        
                        onSave(
                            locationName,
                            locationNotes,
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                    } else {
                        try {
                            val latitude = customLatitude.toDouble()
                            val longitude = customLongitude.toDouble()
                            onSave(locationName, locationNotes, latitude, longitude)
                        } catch (e: NumberFormatException) {
                            showError = true
                            errorMessage = "Please enter valid coordinates"
                        }
                    }
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

@Composable
fun LocationDetailDialog(
    location: DomainLocation,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (name: String, notes: String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(location.name) }
    var editedNotes by remember { mutableStateOf(location.notes ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Location" else "Location Details") },
        text = {
            Column {
                if (isEditing) {
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Coordinates",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = String.format("%.6f, %.6f", location.latitude, location.longitude),
                        style = MaterialTheme.typography.body1
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = location.notes ?: "No notes",
                        style = MaterialTheme.typography.body1
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Delete Location")
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                TextButton(
                    onClick = {
                        onSave(editedName, editedNotes)
                    }
                ) {
                    Text("Save")
                }
            } else {
                TextButton(
                    onClick = { isEditing = true }
                ) {
                    Text("Edit")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = if (isEditing) {
                    { isEditing = false }
                } else {
                    onDismiss
                }
            ) {
                Text(if (isEditing) "Cancel" else "Close")
            }
        }
    )
}