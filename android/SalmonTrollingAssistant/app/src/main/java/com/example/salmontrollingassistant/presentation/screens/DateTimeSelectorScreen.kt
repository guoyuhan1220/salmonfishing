package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.model.TideType
import com.example.salmontrollingassistant.domain.model.WeatherData
import com.example.salmontrollingassistant.presentation.viewmodels.WeatherForecastUiState
import com.example.salmontrollingassistant.presentation.viewmodels.WeatherForecastViewModel
import java.util.Calendar
import java.util.Date

@Composable
fun DateTimeSelectorScreen(
    viewModel: WeatherForecastViewModel,
    location: Location
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedWeather by viewModel.selectedWeather.collectAsState()
    val selectedTide by viewModel.selectedTide.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Select Date and Time",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DateTimePicker(
            selectedDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) },
            minDate = viewModel.minimumDate,
            maxDate = viewModel.maximumDate
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.loadWeatherForSelectedDate(location) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Forecast")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (uiState) {
            is WeatherForecastUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            }
            is WeatherForecastUiState.Success -> {
                selectedWeather?.let { weather ->
                    WeatherDetailCard(weather = weather)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                selectedTide?.let { tide ->
                    TideDetailCard(tide = tide)
                }
            }
            is WeatherForecastUiState.Error -> {
                val errorMessage = (uiState as WeatherForecastUiState.Error).message
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun DateTimePicker(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    minDate: Date,
    maxDate: Date
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance().apply {
        time = selectedDate
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { showDatePicker = true }) {
                Text("Select Date: ${formatDateForDisplay(selectedDate)}")
            }
            
            Button(onClick = { showTimePicker = true }) {
                Text("Select Time: ${formatTimeForDisplay(selectedDate)}")
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = selectedDate,
            minDate = minDate,
            maxDate = maxDate,
            onDateSelected = { date ->
                val newCalendar = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.YEAR, date.get(Calendar.YEAR))
                    set(Calendar.MONTH, date.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH))
                }
                onDateSelected(newCalendar.time)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedDate,
            onTimeSelected = { hour, minute ->
                val newCalendar = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                onDateSelected(newCalendar.time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun DatePickerDialog(
    initialDate: Date,
    minDate: Date,
    maxDate: Date,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    // In a real implementation, this would use a DatePicker from AndroidX
    // For this example, we'll use a simplified dialog
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Date", style = MaterialTheme.typography.h6)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simplified date selection - in a real app, use DatePicker
                val calendar = Calendar.getInstance().apply { time = initialDate }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                
                // This is a placeholder for a real date picker
                Text("Selected: ${month + 1}/$day/$year")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.padding(8.dp))
                    
                    Button(onClick = { onDateSelected(calendar) }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    initialTime: Date,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // In a real implementation, this would use a TimePicker from AndroidX
    // For this example, we'll use a simplified dialog
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Time", style = MaterialTheme.typography.h6)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simplified time selection - in a real app, use TimePicker
                val calendar = Calendar.getInstance().apply { time = initialTime }
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                
                // This is a placeholder for a real time picker
                Text("Selected: ${formatHour(hour)}:${formatMinute(minute)} ${if (hour < 12) "AM" else "PM"}")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.padding(8.dp))
                    
                    Button(onClick = { onTimeSelected(hour, minute) }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailCard(weather: WeatherData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Weather Details",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "Temperature", value = "${String.format("%.1f", weather.temperature)}°F")
                    DetailRow(label = "Wind", value = "${String.format("%.1f", weather.windSpeed)} mph ${weather.windDirection}")
                    DetailRow(label = "Precipitation", value = "${String.format("%.2f", weather.precipitation)} in")
                    DetailRow(label = "Cloud Cover", value = "${weather.cloudCover}%")
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "Visibility", value = "${String.format("%.1f", weather.visibility)} mi")
                    DetailRow(label = "Pressure", value = "${String.format("%.1f", weather.pressure)} hPa")
                    DetailRow(label = "Humidity", value = "${weather.humidity}%")
                    DetailRow(label = "UV Index", value = "${weather.uvIndex}")
                }
            }
            
            weather.waterTemperature?.let {
                DetailRow(label = "Water Temperature", value = "${String.format("%.1f", it)}°F")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(text = value)
    }
}

// Helper functions
private fun formatDateForDisplay(date: Date): String {
    val calendar = Calendar.getInstance().apply { time = date }
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = calendar.get(Calendar.YEAR)
    return "$month/$day/$year"
}

private fun formatTimeForDisplay(date: Date): String {
    val calendar = Calendar.getInstance().apply { time = date }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "${formatHour(hour12)}:${formatMinute(minute)} $amPm"
}

private fun formatHour(hour: Int): String = hour.toString()

private fun formatMinute(minute: Int): String = if (minute < 10) "0$minute" else minute.toString()

@Composable
fun TideDetailCard(tide: TideData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Tide Details",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "Current Height", value = "${String.format("%.2f", tide.height)} ft")
                    DetailRow(label = "Tide Type", value = getTideTypeDisplay(tide.type))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    tide.nextHighTide?.let {
                        DetailRow(
                            label = "Next High Tide",
                            value = "${formatTimeForDisplay(it.timestamp)} (${String.format("%.2f", it.height)} ft)"
                        )
                    }
                    
                    tide.nextLowTide?.let {
                        DetailRow(
                            label = "Next Low Tide",
                            value = "${formatTimeForDisplay(it.timestamp)} (${String.format("%.2f", it.height)} ft)"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Simple tide chart visualization
            TideChartVisualization(tide = tide)
        }
    }
}

@Composable
fun TideChartVisualization(tide: TideData) {
    // This is a simplified visualization - in a real app, use a proper chart library
    val maxHeight = 4.0 // Assuming max tide height of 4 feet for visualization
    val currentHeight = (tide.height / maxHeight).coerceIn(0.0, 1.0)
    
    Column {
        Text(
            text = "Tide Chart",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(4.dp)
        ) {
            // Water level
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((60 * currentHeight).dp)
                    .background(Color(0xFF4FC3F7))
                    .align(Alignment.BottomCenter)
            )
            
            // Current tide marker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.Red)
                    .align(Alignment.BottomStart)
                    .padding(top = (60 * currentHeight).dp)
            )
            
            // Tide type indicator
            Text(
                text = getTideTypeDisplay(tide.type),
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        
        // Tide scale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0 ft", style = MaterialTheme.typography.caption)
            Text(text = "${String.format("%.1f", maxHeight)} ft", style = MaterialTheme.typography.caption)
        }
    }
}

private fun getTideTypeDisplay(tideType: TideType): String {
    return when (tideType) {
        TideType.HIGH -> "High Tide"
        TideType.LOW -> "Low Tide"
        TideType.RISING -> "Rising Tide"
        TideType.FALLING -> "Falling Tide"
    }
}