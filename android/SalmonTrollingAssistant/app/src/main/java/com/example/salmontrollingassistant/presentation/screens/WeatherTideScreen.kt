package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.presentation.components.OfflineIndicator
import com.example.salmontrollingassistant.presentation.viewmodels.WeatherForecastViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherTideScreen(
    viewModel: WeatherForecastViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("Weather", "Tide", "Combined")
    
    // Observe weather and tide data
    val weatherData by viewModel.currentWeather.collectAsState()
    val tideData by viewModel.currentTide.collectAsState()
    val forecastWeather by viewModel.forecastWeather.collectAsState()
    val forecastTides by viewModel.forecastTides.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather & Tide") },
                actions = {
                    IconButton(onClick = {
                        // Refresh data
                        viewModel.loadCurrentWeather()
                        viewModel.loadCurrentTide()
                        viewModel.loadForecast()
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
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
            
            // Date selector
            DateSelector(
                selectedDate = selectedDate,
                onSelectDate = { showDateTimePicker = true }
            )
            
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content based on selected tab with swipe navigation
            SwipeNavigation(
                onSwipeLeft = {
                    if (selectedTabIndex < 2) {
                        selectedTabIndex++
                    }
                },
                onSwipeRight = {
                    if (selectedTabIndex > 0) {
                        selectedTabIndex--
                    }
                }
            ) {
                // Pull to refresh container
                var isRefreshing by remember { mutableStateOf(false) }
                
                PullToRefreshContainer(
                    onRefresh = {
                        isRefreshing = true
                        // Refresh data
                        viewModel.loadCurrentWeather()
                        viewModel.loadCurrentTide()
                        viewModel.loadForecast()
                        // Simulate network delay
                        viewModel.viewModelScope.launch {
                            delay(1000)
                            isRefreshing = false
                        }
                    },
                    refreshing = isRefreshing
                ) {
                    when (selectedTabIndex) {
                        0 -> WeatherContent(weatherData, forecastWeather)
                        1 -> TideContent(tideData, forecastTides)
                        2 -> CombinedContent(weatherData, tideData)
                    }
                }
            }
        }
    }
    
    // Show date time picker dialog if needed
    if (showDateTimePicker) {
        DateTimeSelectorScreen(
            initialDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDateTimePicker = false
                // Update weather and tide data for selected date
                viewModel.getWeatherForDateTime(date)
            },
            onDismiss = { showDateTimePicker = false }
        )
    }
    
    // Load data when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadCurrentWeather()
        viewModel.loadCurrentTide()
        viewModel.loadForecast()
    }
}

@Composable
fun DateSelector(selectedDate: Date, onSelectDate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onSelectDate() },
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
                    style = MaterialTheme.typography.h6
                )
            }
            
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Select Date"
            )
        }
    }
}

@Composable
fun WeatherContent(
    currentWeather: com.example.salmontrollingassistant.domain.model.WeatherData?,
    forecastWeather: List<com.example.salmontrollingassistant.domain.model.WeatherData>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current weather card
        item {
            CurrentWeatherCard(currentWeather)
        }
        
        // Hourly forecast
        item {
            HourlyForecastSection()
        }
        
        // Daily forecast
        item {
            DailyForecastSection()
        }
        
        // Weather details
        item {
            WeatherDetailsSection(currentWeather)
        }
    }
}

@Composable
fun CurrentWeatherCard(weatherData: com.example.salmontrollingassistant.domain.model.WeatherData?) {
    Column {
        Text(
            text = "Current Weather",
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (weatherData?.cloudCover ?: 0 > 50) 
                            Icons.Filled.Cloud else Icons.Filled.WbSunny,
                        contentDescription = "Weather",
                        modifier = Modifier.size(50.dp)
                    )
                    
                    Text(
                        text = "${weatherData?.temperature?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.h3
                    )
                    
                    Text(
                        text = "Feels like ${weatherData?.temperature?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.caption
                    )
                }
                
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    WeatherDetailRow(
                        icon = Icons.Filled.Air,
                        title = "Wind",
                        value = "${weatherData?.windSpeed?.toInt() ?: 0} mph ${weatherData?.windDirection ?: ""}"
                    )
                    
                    WeatherDetailRow(
                        icon = Icons.Filled.Opacity,
                        title = "Humidity",
                        value = "${weatherData?.humidity ?: 0}%"
                    )
                    
                    WeatherDetailRow(
                        icon = Icons.Filled.Visibility,
                        title = "Visibility",
                        value = "${weatherData?.visibility ?: 0} mi"
                    )
                    
                    WeatherDetailRow(
                        icon = Icons.Filled.Thermostat,
                        title = "Water Temp",
                        value = "${weatherData?.waterTemperature?.toInt() ?: 0}°"
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HourlyForecastSection() {
    Column {
        Text(
            text = "Hourly Forecast",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(24) { hour ->
                    HourlyForecastItem(hour)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(hour: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = formatHour(hour),
            style = MaterialTheme.typography.caption
        )
        
        Icon(
            imageVector = if (hour % 3 == 0) Icons.Filled.Cloud else Icons.Filled.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = "${60 + (hour % 5)}°",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DailyForecastSection() {
    Column {
        Text(
            text = "7-Day Forecast",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column {
                for (day in 0..6) {
                    DailyForecastRow(day)
                    
                    if (day < 6) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastRow(day: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDay(day),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.width(100.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = if (day % 2 == 0) Icons.Filled.Cloud else Icons.Filled.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "${55 + (day % 10)}°",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "${65 + (day % 8)}°",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun WeatherDetailsSection(weatherData: com.example.salmontrollingassistant.domain.model.WeatherData?) {
    Column {
        Text(
            text = "Weather Details",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherDetailCard(
                icon = Icons.Filled.WbTwilight,
                title = "Sunrise",
                value = "6:32 AM",
                modifier = Modifier.weight(1f)
            )
            
            WeatherDetailCard(
                icon = Icons.Filled.WbTwilightEnd,
                title = "Sunset",
                value = "8:15 PM",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherDetailCard(
                icon = Icons.Filled.Speed,
                title = "Pressure",
                value = "${weatherData?.pressure?.toInt() ?: 0} hPa",
                modifier = Modifier.weight(1f)
            )
            
            WeatherDetailCard(
                icon = Icons.Filled.WbSunny,
                title = "UV Index",
                value = "${weatherData?.uvIndex ?: 0}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WeatherDetailCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF57C00), // Orange
                modifier = Modifier.size(30.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.h6
            )
        }
    }
}

@Composable
fun TideContent(
    currentTide: com.example.salmontrollingassistant.domain.model.TideData?,
    forecastTides: List<com.example.salmontrollingassistant.domain.model.TideData>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current tide card
        item {
            CurrentTideCard(currentTide)
        }
        
        // Tide chart
        item {
            TideChartSection()
        }
        
        // Tide events
        item {
            TideEventsSection()
        }
        
        // Historical comparison
        item {
            HistoricalComparisonSection()
        }
    }
}

@Composable
fun CurrentTideCard(tideData: com.example.salmontrollingassistant.domain.model.TideData?) {
    Column {
        Text(
            text = "Current Tide",
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Waves,
                        contentDescription = "Tide",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(50.dp)
                    )
                    
                    Text(
                        text = tideData?.type?.name ?: "Unknown",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${String.format("%.1f", tideData?.height ?: 0.0)} ft",
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    tideData?.nextHighTide?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Next High:",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "${formatTime(it.timestamp)} (${String.format("%.1f", it.height)} ft)",
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                    
                    tideData?.nextLowTide?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Next Low:",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "${formatTime(it.timestamp)} (${String.format("%.1f", it.height)} ft)",
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TideChartSection() {
    Column {
        Text(
            text = "Tide Chart",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // This would be replaced with an actual chart view with pinch-to-zoom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    PinchToZoom {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            // Sample tide curve
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = Path().apply {
                                    val width = size.width
                                    val height = size.height
                                    
                                    moveTo(0f, height * 0.75f)
                                    
                                    cubicTo(
                                        width * 0.25f, height * 0.25f,
                                        width * 0.75f, height * 1.25f,
                                        width, height * 0.75f
                                    )
                                }
                                
                                drawPath(
                                    path = path,
                                    color = MaterialTheme.colors.primary,
                                    style = Stroke(width = 3f)
                                )
                                
                                // Current time indicator
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(width * 0.3f, 0f),
                                    end = Offset(width * 0.3f, height),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                    
                    // Legend
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colors.primary, CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Tide Height",
                            style = MaterialTheme.typography.caption
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Current Time",
                            style = MaterialTheme.typography.caption
                        )
                    }
                    
                    // Pinch to zoom hint
                    Text(
                        text = "Pinch to zoom",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                }
                
                // Time labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "12 AM",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "6 AM",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "12 PM",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "6 PM",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "12 AM",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TideEventsSection() {
    Column {
        Text(
            text = "Tide Events",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column {
                for (index in 0..3) {
                    TideEventRow(
                        time = if (index % 2 == 0) "8:32 AM" else "2:45 PM",
                        type = if (index % 2 == 0) "High Tide" else "Low Tide",
                        height = if (index % 2 == 0) 5.2 else 1.3
                    )
                    
                    if (index < 3) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TideEventRow(time: String, type: String, height: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.width(80.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = type,
            style = MaterialTheme.typography.body1,
            color = if (type.contains("High")) Color.Green else Color.Red
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "${String.format("%.1f", height)} ft",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun HistoricalComparisonSection() {
    Column {
        Text(
            text = "Historical Comparison",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HistoricalComparisonCard(
                title = "Average Tide",
                current = "3.2 ft",
                historical = "3.5 ft",
                difference = "-0.3 ft",
                modifier = Modifier.weight(1f)
            )
            
            HistoricalComparisonCard(
                title = "Tide Range",
                current = "4.1 ft",
                historical = "4.3 ft",
                difference = "-0.2 ft",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HistoricalComparisonCard(
    title: String,
    current: String,
    historical: String,
    difference: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current:",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                
                Text(
                    text = current,
                    style = MaterialTheme.typography.body2
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Historical:",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                
                Text(
                    text = historical,
                    style = MaterialTheme.typography.body2
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Difference:",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                
                Text(
                    text = difference,
                    style = MaterialTheme.typography.body2,
                    color = if (difference.contains("-")) Color.Red else Color.Green
                )
            }
        }
    }
}

@Composable
fun CombinedContent(
    weatherData: com.example.salmontrollingassistant.domain.model.WeatherData?,
    tideData: com.example.salmontrollingassistant.domain.model.TideData?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Combined overview card
        item {
            CombinedOverviewCard(weatherData, tideData)
        }
        
        // Combined timeline
        item {
            CombinedTimelineSection()
        }
        
        // Fishing conditions
        item {
            FishingConditionsSection()
        }
    }
}

@Composable
fun CombinedOverviewCard(
    weatherData: com.example.salmontrollingassistant.domain.model.WeatherData?,
    tideData: com.example.salmontrollingassistant.domain.model.TideData?
) {
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Weather summary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (weatherData?.cloudCover ?: 0 > 50) 
                            Icons.Filled.Cloud else Icons.Filled.WbSunny,
                        contentDescription = "Weather",
                        modifier = Modifier.size(30.dp)
                    )
                    
                    Text(
                        text = "${weatherData?.temperature?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Wind: ${weatherData?.windSpeed?.toInt() ?: 0} mph",
                        style = MaterialTheme.typography.caption
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height(80.dp)
                        .width(1.dp)
                )
                
                // Tide summary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Waves,
                        contentDescription = "Tide",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(30.dp)
                    )
                    
                    Text(
                        text = tideData?.type?.name ?: "Unknown",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${String.format("%.1f", tideData?.height ?: 0.0)} ft",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

@Composable
fun CombinedTimelineSection() {
    Column {
        Text(
            text = "Today's Timeline",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // This would be replaced with an actual chart view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Weather line
                        val weatherPath = Path().apply {
                            moveTo(0f, height * 0.4f)
                            
                            cubicTo(
                                width * 0.25f, height * 0.3f,
                                width * 0.75f, height * 0.5f,
                                width, height * 0.4f
                            )
                        }
                        
                        drawPath(
                            path = weatherPath,
                            color = Color(0xFFF57C00), // Orange
                            style = Stroke(width = 2f)
                        )
                        
                        // Tide line
                        val tidePath = Path().apply {
                            moveTo(0f, height * 0.75f)
                            
                            cubicTo(
                                width * 0.25f, height * 0.25f,
                                width * 0.75f, height * 1.25f,
                                width, height * 0.75f
                            )
                        }
                        
                        drawPath(
                            path = tidePath,
                            color = MaterialTheme.colors.primary,
                            style = Stroke(width = 2f)
                        )
                    }
                    
                    // Legend
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFF57C00), CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Weather",
                            style = MaterialTheme.typography.caption
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colors.primary, CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Tide",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FishingConditionsSection() {
    Column {
        Text(
            text = "Fishing Conditions",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Today's conditions are favorable for salmon trolling.",
                    style = MaterialTheme.typography.body1
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ConditionIndicator(
                        title = "Water Clarity",
                        value = "Good",
                        color = Color.Green
                    )
                    
                    ConditionIndicator(
                        title = "Tide Movement",
                        value = "Excellent",
                        color = Color.Green
                    )
                    
                    ConditionIndicator(
                        title = "Wind",
                        value = "Fair",
                        color = Color(0xFFF57C00) // Orange
                    )
                }
                
                Text(
                    text = "Best fishing time today: 6:30 AM - 9:30 AM",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConditionIndicator(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper functions
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp * 1000) // Convert seconds to milliseconds
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(date)
}

private fun formatHour(hour: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, 0)
    
    val formatter = SimpleDateFormat("h a", Locale.getDefault())
    return formatter.format(calendar.time)
}

private fun formatDay(dayOffset: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
    
    return when (dayOffset) {
        0 -> "Today"
        1 -> "Tomorrow"
        else -> {
            val formatter = SimpleDateFormat("EEE", Locale.getDefault())
            formatter.format(calendar.time)
        }
    }
}