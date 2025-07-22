package com.example.salmontrollingassistant.domain.model

/**
 * Represents user preferences for the application
 */
data class UserPreferences(
    val preferredSpecies: List<FishSpecies> = emptyList(),
    val preferredEquipment: List<String> = emptyList(),
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val displaySettings: DisplaySettings = DisplaySettings(),
    val dataSettings: DataSettings = DataSettings()
)

/**
 * Represents notification settings for the user
 */
data class NotificationSettings(
    val enableWeatherAlerts: Boolean = false,
    val enableTideAlerts: Boolean = false,
    val enableOptimalConditionAlerts: Boolean = false
)

/**
 * Represents display settings for the user
 */
data class DisplaySettings(
    val useDarkMode: Boolean = false,
    val useHighContrastMode: Boolean = false,
    val useMetricSystem: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM
)

/**
 * Represents data settings for the user
 */
data class DataSettings(
    val dataRefreshInterval: Int = 30, // minutes
    val wifiOnlyDownloads: Boolean = true,
    val imageQuality: ImageQuality = ImageQuality.MEDIUM,
    val prefetchData: Boolean = true,
    val locationUpdateFrequency: Int = 5 // minutes
)

/**
 * Represents user equipment
 */
data class UserEquipment(
    val id: String = "",
    val equipmentId: String,
    val equipmentType: EquipmentType,
    val name: String = "",
    val color: String? = null,
    val size: String? = null,
    val brand: String? = null,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * Represents the user's experience level
 */
enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

/**
 * Represents fish species
 */
enum class FishSpecies {
    CHINOOK,
    COHO,
    SOCKEYE,
    PINK,
    CHUM,
    STEELHEAD,
    ATLANTIC
}

/**
 * Represents equipment types
 */
enum class EquipmentType {
    FLASHER,
    LURE,
    HOOCHIE,
    SPOON,
    PLUG,
    BAIT,
    ROD,
    REEL,
    LINE,
    LEADER,
    WEIGHT,
    OTHER
}

/**
 * Represents font size options
 */
enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

/**
 * Represents image quality options
 */
enum class ImageQuality {
    LOW,
    MEDIUM,
    HIGH
}