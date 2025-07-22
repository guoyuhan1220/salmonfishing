package com.example.salmontrollingassistant.domain.model

import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

enum class EquipmentType {
    FLASHER, LURE, LEADER
}

enum class FishSpecies {
    CHINOOK, COHO, SOCKEYE, PINK, CHUM
}

@JsonClass(generateAdapter = true)
data class EquipmentItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val type: EquipmentType,
    val imageUrl: String? = null,
    val specifications: Map<String, String> = mapOf(),
    val targetSpecies: List<FishSpecies>? = null,
    val waterClarityConditions: List<String>? = null,
    val lightConditions: List<String>? = null,
    val weatherConditions: List<String>? = null,
    val tideConditions: List<TideType>? = null
) {
    // Properties specific to equipment types
    val size: String?
        get() = specifications["size"]
    
    val color: String?
        get() = specifications["color"]
    
    val length: String?
        get() = specifications["length"]
    
    val material: String?
        get() = specifications["material"]
    
    val weight: String?
        get() = specifications["weight"]
}

@JsonClass(generateAdapter = true)
data class EquipmentRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val type: EquipmentType,
    val items: List<EquipmentItem>,
    val reasonForRecommendation: String,
    val confidenceScore: Float // 0.0 to 1.0
)

// User equipment preferences
@JsonClass(generateAdapter = true)
data class UserEquipment(
    val id: String = UUID.randomUUID().toString(),
    val equipmentId: String,
    val equipmentType: EquipmentType = EquipmentType.FLASHER,
    val name: String = "",
    val color: String? = null,
    val size: String? = null,
    val brand: String? = null,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

// Water clarity enum for recommendation filtering
enum class WaterClarity {
    CLEAR, MEDIUM, MURKY;
    
    companion object {
        // Determine water clarity based on visibility
        fun fromVisibility(visibility: Double): WaterClarity {
            return when {
                visibility > 5.0 -> CLEAR
                visibility > 2.0 -> MEDIUM
                else -> MURKY
            }
        }
    }
}

// Light condition enum for recommendation filtering
enum class LightCondition {
    BRIGHT, OVERCAST, LOW_LIGHT;
    
    companion object {
        // Determine light condition based on cloud cover and time of day
        fun fromWeatherData(weatherData: WeatherData): LightCondition {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = weatherData.timestamp
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            
            // Early morning or evening hours
            if (hour < 6 || hour > 18) {
                return LOW_LIGHT
            }
            
            // Based on cloud cover
            return if (weatherData.cloudCover > 70) {
                OVERCAST
            } else {
                BRIGHT
            }
        }
    }
}

// Weather condition enum for recommendation filtering
enum class WeatherCondition {
    CALM, WINDY, RAINY;
    
    companion object {
        // Determine weather condition based on wind speed and precipitation
        fun fromWeatherData(weatherData: WeatherData): WeatherCondition {
            return when {
                weatherData.precipitation > 1.0 -> RAINY
                weatherData.windSpeed > 15.0 -> WINDY
                else -> CALM
            }
        }
    }
}