package com.example.salmontrollingassistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.salmontrollingassistant.domain.model.WeatherData
import java.util.Date

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val id: String,
    val locationId: String,
    val timestamp: Long,
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: String,
    val precipitation: Double,
    val cloudCover: Int,
    val visibility: Double,
    val pressure: Double,
    val humidity: Int,
    val uvIndex: Int,
    val waterTemperature: Double?,
    val isForecast: Boolean,
    val cacheTimestamp: Long = System.currentTimeMillis()
) {
    fun toWeatherData(): WeatherData {
        return WeatherData(
            id = id,
            timestamp = Date(timestamp),
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
    }
    
    companion object {
        fun fromWeatherData(weatherData: WeatherData, locationId: String, isForecast: Boolean): WeatherEntity {
            return WeatherEntity(
                id = weatherData.id,
                locationId = locationId,
                timestamp = weatherData.timestamp.time,
                temperature = weatherData.temperature,
                windSpeed = weatherData.windSpeed,
                windDirection = weatherData.windDirection,
                precipitation = weatherData.precipitation,
                cloudCover = weatherData.cloudCover,
                visibility = weatherData.visibility,
                pressure = weatherData.pressure,
                humidity = weatherData.humidity,
                uvIndex = weatherData.uvIndex,
                waterTemperature = weatherData.waterTemperature,
                isForecast = isForecast
            )
        }
    }
}