package com.example.salmontrollingassistant.domain.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class ExperienceLevel {
    BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
}

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String? = null,
    val email: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val equipmentInventory: List<UserEquipment> = emptyList(),
    val isAnonymous: Boolean = true
)

@JsonClass(generateAdapter = true)
data class UserPreferences(
    val preferredSpecies: List<FishSpecies> = emptyList(),
    val preferredEquipment: List<String> = emptyList(), // IDs of preferred equipment
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val notificationSettings: NotificationSettings = NotificationSettings()
)

@JsonClass(generateAdapter = true)
data class NotificationSettings(
    val enableWeatherAlerts: Boolean = false,
    val enableTideAlerts: Boolean = false,
    val enableOptimalConditionAlerts: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CatchData(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val locationId: String,
    val species: FishSpecies,
    val size: Double? = null, // in inches
    val weight: Double? = null, // in pounds
    val equipmentUsed: List<String> = emptyList(), // IDs of equipment used
    val weatherConditionsId: String? = null,
    val tideConditionsId: String? = null,
    val notes: String? = null,
    val photoUrls: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AuthCredentials(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResult(
    val success: Boolean,
    val userId: String? = null,
    val token: String? = null,
    val error: String? = null
)