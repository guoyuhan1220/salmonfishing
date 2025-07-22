package com.example.salmontrollingassistant.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

class WeatherDataTest {

    @Test
    fun `WeatherData should be created with correct values`() {
        // Given
        val timestamp = Date()
        val temperature = 22.5
        val windSpeed = 10.0
        val windDirection = "NE"
        val precipitation = 0.5
        val cloudCover = 30
        val visibility = 8.0
        val pressure = 1013.0
        val humidity = 65
        val uvIndex = 4
        val waterTemperature = 18.0
        
        // When
        val weatherData = WeatherData(
            timestamp = timestamp,
            temperature = temperature,
            windSpeed = windSpeed,
            windDirection = windDirection,
            precipitation = precipitation,
            cloudCover = cloudCover,
            visibility = visibility,
            pressure = pressure,
            humidity = humidity,
            uvIndex = uvIndex,
            waterTemperature = waterTemperature
        )
        
        // Then
        assertThat(weatherData.timestamp).isEqualTo(timestamp)
        assertThat(weatherData.temperature).isEqualTo(temperature)
        assertThat(weatherData.windSpeed).isEqualTo(windSpeed)
        assertThat(weatherData.windDirection).isEqualTo(windDirection)
        assertThat(weatherData.precipitation).isEqualTo(precipitation)
        assertThat(weatherData.cloudCover).isEqualTo(cloudCover)
        assertThat(weatherData.visibility).isEqualTo(visibility)
        assertThat(weatherData.pressure).isEqualTo(pressure)
        assertThat(weatherData.humidity).isEqualTo(humidity)
        assertThat(weatherData.uvIndex).isEqualTo(uvIndex)
        assertThat(weatherData.waterTemperature).isEqualTo(waterTemperature)
    }
    
    @Test
    fun `WeatherData should handle null waterTemperature`() {
        // Given
        val timestamp = Date()
        
        // When
        val weatherData = WeatherData(
            timestamp = timestamp,
            temperature = 22.5,
            windSpeed = 10.0,
            windDirection = "NE",
            precipitation = 0.5,
            cloudCover = 30,
            visibility = 8.0,
            pressure = 1013.0,
            humidity = 65,
            uvIndex = 4,
            waterTemperature = null
        )
        
        // Then
        assertThat(weatherData.waterTemperature).isNull()
    }
    
    @Test
    fun `WeatherData instances should be comparable`() {
        // Given
        val timestamp = Date()
        val weatherData1 = WeatherData(
            timestamp = timestamp,
            temperature = 22.5,
            windSpeed = 10.0,
            windDirection = "NE",
            precipitation = 0.5,
            cloudCover = 30,
            visibility = 8.0,
            pressure = 1013.0,
            humidity = 65,
            uvIndex = 4
        )
        
        val weatherData2 = WeatherData(
            timestamp = timestamp,
            temperature = 22.5,
            windSpeed = 10.0,
            windDirection = "NE",
            precipitation = 0.5,
            cloudCover = 30,
            visibility = 8.0,
            pressure = 1013.0,
            humidity = 65,
            uvIndex = 4
        )
        
        val weatherData3 = WeatherData(
            timestamp = timestamp,
            temperature = 25.0, // Different temperature
            windSpeed = 10.0,
            windDirection = "NE",
            precipitation = 0.5,
            cloudCover = 30,
            visibility = 8.0,
            pressure = 1013.0,
            humidity = 65,
            uvIndex = 4
        )
        
        // Then
        assertThat(weatherData1).isEqualTo(weatherData1) // Same instance
        assertThat(weatherData1).isNotEqualTo(weatherData3) // Different values
        
        // Different instances with same values should be equal due to data class equality
        assertThat(weatherData1).isEqualTo(weatherData2)
    }
}