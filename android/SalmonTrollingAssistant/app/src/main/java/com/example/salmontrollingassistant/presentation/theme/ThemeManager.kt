package com.example.salmontrollingassistant.presentation.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define theme modes
enum class ThemeMode {
    SYSTEM, LIGHT, DARK, HIGH_CONTRAST
}

// DataStore for theme preferences
val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

// Preference keys
object ThemePreferences {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val HIGH_CONTRAST_ENABLED = booleanPreferencesKey("high_contrast_enabled")
    val AUTO_BRIGHTNESS_DETECTION = booleanPreferencesKey("auto_brightness_detection")
}

// Theme colors
private val LightColors = lightColors(
    primary = Color(0xFF0277BD),
    primaryVariant = Color(0xFF014A75),
    secondary = Color(0xFF26A69A),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColors = darkColors(
    primary = Color(0xFF29B6F6),
    primaryVariant = Color(0xFF0288D1),
    secondary = Color(0xFF4DB6AC),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val HighContrastColors = darkColors(
    primary = Color.Yellow,
    primaryVariant = Color(0xFFFFD600),
    secondary = Color.Yellow,
    background = Color.Black,
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.Yellow
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Get theme mode from DataStore
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        val themeModeString = preferences[ThemePreferences.THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(themeModeString)
    }
    
    // Get high contrast mode from DataStore
    val isHighContrastEnabled: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[ThemePreferences.HIGH_CONTRAST_ENABLED] ?: false
    }
    
    // Get auto brightness detection from DataStore
    val isAutoBrightnessDetectionEnabled: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[ThemePreferences.AUTO_BRIGHTNESS_DETECTION] ?: false
    }
    
    // Set theme mode
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            context.themeDataStore.edit { preferences ->
                preferences[ThemePreferences.THEME_MODE] = mode.name
            }
        }
    }
    
    // Toggle high contrast mode
    fun setHighContrastEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.themeDataStore.edit { preferences ->
                preferences[ThemePreferences.HIGH_CONTRAST_ENABLED] = enabled
                if (enabled) {
                    preferences[ThemePreferences.THEME_MODE] = ThemeMode.HIGH_CONTRAST.name
                }
            }
        }
    }
    
    // Toggle auto brightness detection
    fun setAutoBrightnessDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.themeDataStore.edit { preferences ->
                preferences[ThemePreferences.AUTO_BRIGHTNESS_DETECTION] = enabled
            }
        }
    }
}

// Composable function to apply the theme
@Composable
fun SalmonTrollingAssistantTheme(
    themeViewModel: ThemeViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val themeMode by themeViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val isHighContrastEnabled by themeViewModel.isHighContrastEnabled.collectAsState(initial = false)
    val isAutoBrightnessDetectionEnabled by themeViewModel.isAutoBrightnessDetectionEnabled.collectAsState(initial = false)
    
    val context = LocalContext.current
    var currentLightLevel by remember { mutableStateOf(0f) }
    
    // Set up light sensor for auto brightness detection
    if (isAutoBrightnessDetectionEnabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        DisposableEffect(Unit) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    currentLightLevel = event.values[0]
                    
                    // If light level is very high (bright sunlight), enable high contrast mode
                    if (currentLightLevel > 10000 && !isHighContrastEnabled) {
                        themeViewModel.setHighContrastEnabled(true)
                    } else if (currentLightLevel <= 10000 && isHighContrastEnabled) {
                        themeViewModel.setHighContrastEnabled(false)
                    }
                }
                
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    // Not needed for this implementation
                }
            }
            
            // Register the sensor listener
            if (lightSensor != null) {
                sensorManager.registerListener(
                    listener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            
            // Clean up when the composable is disposed
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }
    
    // Determine which color scheme to use
    val colors = when (themeMode) {
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.HIGH_CONTRAST -> HighContrastColors
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
    }
    
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}