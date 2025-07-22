package com.example.salmontrollingassistant.domain.model

import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

@JsonClass(generateAdapter = true)
data class WeatherData(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date,
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: String,
    val precipitation: Double,
    val cloudCover: Int,
    val visibility: Double,
    val pressure: Double,
    val humidity: Int,
    val uvIndex: Int,
    val waterTemperature: Double? = null
)