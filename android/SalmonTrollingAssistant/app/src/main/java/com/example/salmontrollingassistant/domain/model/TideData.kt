package com.example.salmontrollingassistant.domain.model

import com.squareup.moshi.JsonClass
import java.util.Date
import java.util.UUID

enum class TideType {
    HIGH, LOW, RISING, FALLING
}

@JsonClass(generateAdapter = true)
data class TideEvent(
    val timestamp: Date,
    val height: Double
)

@JsonClass(generateAdapter = true)
data class TideData(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date,
    val height: Double,
    val type: TideType,
    val nextHighTide: TideEvent? = null,
    val nextLowTide: TideEvent? = null
)