package com.example.salmontrollingassistant.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

class EquipmentModelsTest {

    @Test
    fun `EquipmentItem should return correct specification values`() {
        // Given
        val specifications = mapOf(
            "size" to "Large",
            "color" to "Green",
            "length" to "12 inches",
            "material" to "Plastic",
            "weight" to "2 oz"
        )
        
        val equipmentItem = EquipmentItem(
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER,
            specifications = specifications
        )
        
        // Then
        assertThat(equipmentItem.size).isEqualTo("Large")
        assertThat(equipmentItem.color).isEqualTo("Green")
        assertThat(equipmentItem.length).isEqualTo("12 inches")
        assertThat(equipmentItem.material).isEqualTo("Plastic")
        assertThat(equipmentItem.weight).isEqualTo("2 oz")
    }
    
    @Test
    fun `EquipmentItem should return null for missing specifications`() {
        // Given
        val specifications = mapOf(
            "size" to "Large",
            "color" to "Green"
        )
        
        val equipmentItem = EquipmentItem(
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER,
            specifications = specifications
        )
        
        // Then
        assertThat(equipmentItem.size).isEqualTo("Large")
        assertThat(equipmentItem.color).isEqualTo("Green")
        assertThat(equipmentItem.length).isNull()
        assertThat(equipmentItem.material).isNull()
        assertThat(equipmentItem.weight).isNull()
    }
    
    @Test
    fun `WaterClarity should be determined correctly from visibility`() {
        // Given
        val highVisibility = 6.0
        val mediumVisibility = 3.0
        val lowVisibility = 1.0
        
        // When
        val highClarity = WaterClarity.fromVisibility(highVisibility)
        val mediumClarity = WaterClarity.fromVisibility(mediumVisibility)
        val lowClarity = WaterClarity.fromVisibility(lowVisibility)
        
        // Then
        assertThat(highClarity).isEqualTo(WaterClarity.CLEAR)
        assertThat(mediumClarity).isEqualTo(WaterClarity.MEDIUM)
        assertThat(lowClarity).isEqualTo(WaterClarity.MURKY)
    }
    
    @Test
    fun `LightCondition should be determined correctly from weather data`() {
        // Create test weather data for different times and cloud cover
        val morningClearWeather = WeatherData(
            timestamp = Date(2023, 7, 15, 7, 0), // 7 AM
            temperature = 20.0,
            windSpeed = 5.0,
            windDirection = "N",
            precipitation = 0.0,
            cloudCover = 10, // Clear
            visibility = 10.0,
            pressure = 1013.0,
            humidity = 60,
            uvIndex = 5
        )
        
        val middayOvercastWeather = WeatherData(
            timestamp = Date(2023, 7, 15, 12, 0), // 12 PM
            temperature = 25.0,
            windSpeed = 8.0,
            windDirection = "NW",
            precipitation = 0.0,
            cloudCover = 80, // Overcast
            visibility = 8.0,
            pressure = 1010.0,
            humidity = 70,
            uvIndex = 3
        )
        
        val eveningWeather = WeatherData(
            timestamp = Date(2023, 7, 15, 20, 0), // 8 PM
            temperature = 18.0,
            windSpeed = 3.0,
            windDirection = "W",
            precipitation = 0.0,
            cloudCover = 20,
            visibility = 7.0,
            pressure = 1012.0,
            humidity = 75,
            uvIndex = 1
        )
        
        // When
        val morningCondition = LightCondition.fromWeatherData(morningClearWeather)
        val middayCondition = LightCondition.fromWeatherData(middayOvercastWeather)
        val eveningCondition = LightCondition.fromWeatherData(eveningWeather)
        
        // Then
        assertThat(morningCondition).isEqualTo(LightCondition.BRIGHT)
        assertThat(middayCondition).isEqualTo(LightCondition.OVERCAST)
        assertThat(eveningCondition).isEqualTo(LightCondition.LOW_LIGHT)
    }
    
    @Test
    fun `WeatherCondition should be determined correctly from weather data`() {
        // Create test weather data for different conditions
        val calmWeather = WeatherData(
            timestamp = Date(),
            temperature = 22.0,
            windSpeed = 5.0,
            windDirection = "N",
            precipitation = 0.0,
            cloudCover = 20,
            visibility = 10.0,
            pressure = 1013.0,
            humidity = 60,
            uvIndex = 5
        )
        
        val windyWeather = WeatherData(
            timestamp = Date(),
            temperature = 20.0,
            windSpeed = 20.0,
            windDirection = "NW",
            precipitation = 0.0,
            cloudCover = 30,
            visibility = 8.0,
            pressure = 1008.0,
            humidity = 65,
            uvIndex = 4
        )
        
        val rainyWeather = WeatherData(
            timestamp = Date(),
            temperature = 18.0,
            windSpeed = 10.0,
            windDirection = "W",
            precipitation = 2.5,
            cloudCover = 90,
            visibility = 5.0,
            pressure = 1005.0,
            humidity = 85,
            uvIndex = 2
        )
        
        // When
        val calmCondition = WeatherCondition.fromWeatherData(calmWeather)
        val windyCondition = WeatherCondition.fromWeatherData(windyWeather)
        val rainyCondition = WeatherCondition.fromWeatherData(rainyWeather)
        
        // Then
        assertThat(calmCondition).isEqualTo(WeatherCondition.CALM)
        assertThat(windyCondition).isEqualTo(WeatherCondition.WINDY)
        assertThat(rainyCondition).isEqualTo(WeatherCondition.RAINY)
    }
}