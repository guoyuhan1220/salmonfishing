package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.presentation.viewmodels.CatchAnalyticsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchAnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CatchAnalyticsViewModel = hiltViewModel()
) {
    val catchCountBySpecies by viewModel.catchCountBySpecies.collectAsState()
    val catchCountByLocation by viewModel.catchCountByLocation.collectAsState()
    val catchCountByMonth by viewModel.catchCountByMonth.collectAsState()
    val averageSizeBySpecies by viewModel.averageSizeBySpecies.collectAsState()
    val averageWeightBySpecies by viewModel.averageWeightBySpecies.collectAsState()
    val mostSuccessfulEquipment by viewModel.mostSuccessfulEquipment.collectAsState()
    val mostSuccessfulLocations by viewModel.mostSuccessfulLocations.collectAsState()
    val catchTrendOverTime by viewModel.catchTrendOverTime.collectAsState()
    val personalizedRecommendations by viewModel.personalizedRecommendations.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catch Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAnalytics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Personalized Recommendations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Personalized Recommendations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (personalizedRecommendations.isEmpty()) {
                            Text(
                                text = "Start logging your catches to get personalized recommendations!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            personalizedRecommendations.forEach { recommendation ->
                                Text(
                                    text = "• $recommendation",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Catch Count by Species
            item {
                AnalyticsCard(title = "Catch Count by Species") {
                    if (catchCountBySpecies.isEmpty()) {
                        Text(
                            text = "No catch data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            catchCountBySpecies.entries.sortedByDescending { it.value }.forEach { (species, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = species.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Average Size by Species
            item {
                AnalyticsCard(title = "Average Size by Species (inches)") {
                    if (averageSizeBySpecies.isEmpty()) {
                        Text(
                            text = "No size data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            averageSizeBySpecies.entries.sortedByDescending { it.value }.forEach { (species, size) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = species.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = String.format("%.1f", size),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Average Weight by Species
            item {
                AnalyticsCard(title = "Average Weight by Species (lbs)") {
                    if (averageWeightBySpecies.isEmpty()) {
                        Text(
                            text = "No weight data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            averageWeightBySpecies.entries.sortedByDescending { it.value }.forEach { (species, weight) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = species.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = String.format("%.1f", weight),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Most Successful Equipment
            item {
                AnalyticsCard(title = "Most Successful Equipment") {
                    if (mostSuccessfulEquipment.isEmpty()) {
                        Text(
                            text = "No equipment data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            mostSuccessfulEquipment.take(5).forEach { (equipment, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = equipment,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Most Successful Locations
            item {
                AnalyticsCard(title = "Most Successful Locations") {
                    if (mostSuccessfulLocations.isEmpty()) {
                        Text(
                            text = "No location data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            mostSuccessfulLocations.take(5).forEach { (location, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = location,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Catch Count by Month
            item {
                AnalyticsCard(title = "Catch Count by Month") {
                    if (catchCountByMonth.isEmpty()) {
                        Text(
                            text = "No monthly data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column {
                            val monthNames = listOf(
                                "January", "February", "March", "April", "May", "June",
                                "July", "August", "September", "October", "November", "December"
                            )
                            
                            for (month in 0..11) {
                                val count = catchCountByMonth[month] ?: 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = monthNames[month],
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Catch Trend Over Time
            item {
                AnalyticsCard(title = "Catch Trend Over Time") {
                    if (catchTrendOverTime.isEmpty()) {
                        Text(
                            text = "No trend data available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Column {
                            catchTrendOverTime.forEach { (date, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dateFormat.format(date),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            content()
        }
    }
}