package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.salmontrollingassistant.presentation.components.OfflineIndicator
import java.util.*

data class RecommendationItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val name: String,
    val description: String,
    val confidenceScore: Float,
    val icon: ImageVector
)

@Composable
fun RecommendationsScreen() {
    var selectedSpecies by remember { mutableStateOf("All Species") }
    var selectedRecommendation by remember { mutableStateOf<RecommendationItem?>(null) }
    
    val speciesOptions = listOf("All Species", "Chinook", "Coho", "Sockeye", "Pink", "Chum")
    
    // Sample recommendations data - in a real app, this would come from a ViewModel
    val recommendations = remember {
        listOf(
            RecommendationItem(
                type = "Flasher",
                name = "Green Glow Flasher",
                description = "This flasher works well in low light conditions and cloudy water. The green glow attracts salmon from a distance.",
                confidenceScore = 0.85f,
                icon = Icons.Default.RadioButtonChecked
            ),
            RecommendationItem(
                type = "Lure",
                name = "Blue Hoochie",
                description = "This lure mimics small squid and is effective in clear water conditions with moderate current.",
                confidenceScore = 0.75f,
                icon = Icons.Default.Water
            ),
            RecommendationItem(
                type = "Leader",
                name = "42-inch Leader",
                description = "This leader length provides optimal action for your lure in the current tide conditions.",
                confidenceScore = 0.9f,
                icon = Icons.Default.LinearScale
            ),
            RecommendationItem(
                type = "Flasher",
                name = "Silver UV Flasher",
                description = "The UV coating on this flasher makes it highly visible in deeper water and overcast conditions.",
                confidenceScore = 0.7f,
                icon = Icons.Default.RadioButtonChecked
            ),
            RecommendationItem(
                type = "Lure",
                name = "Glow Spoon",
                description = "This spoon's glow feature makes it effective in early morning or evening fishing.",
                confidenceScore = 0.8f,
                icon = Icons.Default.Water
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommendations") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Offline indicator
            OfflineIndicator()
            
            // Filter controls
            FilterSection(
                speciesOptions = speciesOptions,
                selectedSpecies = selectedSpecies,
                onSpeciesSelected = { selectedSpecies = it }
            )
            
            // Recommendations list
            RecommendationsList(
                recommendations = recommendations,
                onRecommendationSelected = { selectedRecommendation = it }
            )
        }
    }
    
    // Show recommendation detail dialog when a recommendation is selected
    selectedRecommendation?.let { recommendation ->
        RecommendationDetailDialog(
            recommendation = recommendation,
            onDismiss = { selectedRecommendation = null }
        )
    }
}

@Composable
fun FilterSection(
    speciesOptions: List<String>,
    selectedSpecies: String,
    onSpeciesSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filter by Species",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(speciesOptions) { species ->
                FilterChip(
                    selected = species == selectedSpecies,
                    onClick = { onSpeciesSelected(species) },
                    label = species
                )
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colors.primary else Color.LightGray.copy(alpha = 0.3f),
        elevation = if (selected) 4.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) Color.White else Color.Black
        )
    }
}

@Composable
fun RecommendationsList(
    recommendations: List<RecommendationItem>,
    onRecommendationSelected: (RecommendationItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recommendations) { recommendation ->
            RecommendationCard(
                recommendation = recommendation,
                onClick = { onRecommendationSelected(recommendation) }
            )
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: RecommendationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon for the recommendation type
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = recommendation.icon,
                    contentDescription = null,
                    tint = when (recommendation.type) {
                        "Flasher" -> Color.Green
                        "Lure" -> Color.Blue
                        else -> Color(0xFFF57C00) // Orange
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recommendation.type,
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    
                    // Confidence indicator
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < (recommendation.confidenceScore * 5).toInt())
                                    Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = recommendation.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun RecommendationDetailDialog(
    recommendation: RecommendationItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recommendation.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Type and confidence score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (recommendation.type) {
                            "Flasher" -> Color.Green.copy(alpha = 0.2f)
                            "Lure" -> Color.Blue.copy(alpha = 0.2f)
                            else -> Color(0xFFF57C00).copy(alpha = 0.2f) // Orange
                        }
                    ) {
                        Text(
                            text = recommendation.type,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Confidence",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "${(recommendation.confidenceScore * 100).toInt()}%",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Description
                Column {
                    Text(
                        text = "Why this works",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = recommendation.description,
                        style = MaterialTheme.typography.body1
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Visual explanation
                Column {
                    Text(
                        text = "Conditions Match",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ConditionMatchItem(
                            icon = Icons.Default.WbSunny,
                            title = "Weather",
                            match = "Good",
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        ConditionMatchItem(
                            icon = Icons.Default.Waves,
                            title = "Tide",
                            match = "Excellent",
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        ConditionMatchItem(
                            icon = Icons.Default.Opacity,
                            title = "Water Clarity",
                            match = "Fair",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConditionMatchItem(
    icon: ImageVector,
    title: String,
    match: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.LightGray.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
            
            Text(
                text = match,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = when (match) {
                    "Excellent" -> Color.Green
                    "Good" -> MaterialTheme.colors.primary
                    "Fair" -> Color(0xFFF57C00) // Orange
                    else -> Color.Red
                }
            )
        }
    }
}