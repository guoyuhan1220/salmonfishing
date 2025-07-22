package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.presentation.components.OfflineIndicator
import com.example.salmontrollingassistant.presentation.viewmodels.WeatherForecastViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: WeatherForecastViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var showDateTimePicker by remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf(Date()) }
    
    // Observe weather and tide data
    val weatherData by viewModel.currentWeather.collectAsState()
    val tideData by viewModel.currentTide.collectAsState()
    
    // Mock saved locations for now
    val savedLocations = remember {
        listOf(
            Location("1", "Seattle Bay", 47.6062, -122.3321, true),
            Location("2", "Puget Sound", 47.7237, -122.4713, true),
            Location("3", "Hood Canal", 47.6477, -122.9637, true),
            Location("4", "San Juan Islands", 48.5513, -123.0781, true)
        )
    }
    
    // Mock recommendations for now
    val recommendations = remember {
        listOf(
            "Green Flasher" to "Perfect for current low light",
            "Blue Hoochie" to "Works well with today's tide",
            "Silver Spoon" to "Ideal for clear water conditions"
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salmon Trolling") },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Offline indicator
            OfflineIndicator()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current conditions section
            CurrentConditionsSection(weatherData, tideData)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick recommendations section
            QuickRecommendationsSection(recommendations)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Saved locations carousel
            SavedLocationsSection(savedLocations)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Date/time selector
            DateTimeSelectorSection(
                selectedDate = selectedDate.value,
                onSelectDateTime = { showDateTimePicker = true }
            )
        }
    }
    
    // Show date time picker dialog if needed
    if (showDateTimePicker) {
        DateTimeSelectorScreen(
            initialDate = selectedDate.value,
            onDateSelected = { date ->
                selectedDate.value = date
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false }
        )
    }
    
    // Load data when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadCurrentWeather()
        viewModel.loadCurrentTide()
    }
}

@Composable
fun CurrentConditionsSection(weatherData: com.example.salmontrollingassistant.domain.model.WeatherData?, tideData: com.example.salmontrollingassistant.domain.model.TideData?) {
    Column {
        Text(
            text = "Current Conditions",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Weather info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (weatherData?.cloudCover ?: 0 > 50) 
                            Icons.Filled.Cloud else Icons.Filled.WbSunny,
                        contentDescription = "Weather",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "${weatherData?.temperature?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.h5
                    )
                    Text(
                        text = "Feels like ${weatherData?.temperature?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.caption
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height(70.dp)
                        .width(1.dp)
                        .padding(horizontal = 8.dp)
                )
                
                // Tide info
                Column {
                    Text(
                        text = "Tide: ${tideData?.type?.name ?: "Unknown"}",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = "Height: ${String.format("%.1f", tideData?.height ?: 0.0)} ft",
                        style = MaterialTheme.typography.body1
                    )
                    tideData?.nextHighTide?.let {
                        Text(
                            text = "Next High: ${formatTime(it.timestamp)}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickRecommendationsSection(recommendations: List<Pair<String, String>>) {
    Column {
        Text(
            text = "Quick Recommendations",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recommendations) { (title, description) ->
                RecommendationCard(title, description)
            }
        }
    }
}

@Composable
fun RecommendationCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Waves,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
}

@Composable
fun SavedLocationsSection(locations: List<Location>) {
    Column {
        Text(
            text = "Saved Locations",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(locations) { location ->
                LocationCard(location)
            }
        }
    }
}

@Composable
fun LocationCard(location: Location) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable { /* Select this location */ },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(30.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = location.name,
                style = MaterialTheme.typography.subtitle2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DateTimeSelectorSection(selectedDate: Date, onSelectDateTime: () -> Unit) {
    Column {
        Text(
            text = "Plan Your Trip",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectDateTime() },
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Calendar"
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = formatDate(selectedDate),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Select"
                )
            }
        }
    }
}

// Helper functions
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return formatter.format(date)
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp * 1000) // Convert seconds to milliseconds
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(date)
}