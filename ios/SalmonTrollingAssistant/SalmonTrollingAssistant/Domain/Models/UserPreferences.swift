import Foundation

enum ExperienceLevel: String, Codable, CaseIterable {
    case beginner = "Beginner"
    case intermediate = "Intermediate"
    case advanced = "Advanced"
    case expert = "Expert"
}

struct UserPreferences: Codable {
    let preferredSpecies: [FishSpecies]
    let preferredEquipment: [String] // IDs of preferred equipment
    let experienceLevel: ExperienceLevel
    let notificationSettings: NotificationSettings
    
    struct NotificationSettings: Codable {
        let enableWeatherAlerts: Bool
        let enableTideAlerts: Bool
        let enableOptimalConditionAlerts: Bool
    }
    
    static func defaultPreferences() -> UserPreferences {
        return UserPreferences(
            preferredSpecies: [.chinook, .coho],
            preferredEquipment: [],
            experienceLevel: .intermediate,
            notificationSettings: NotificationSettings(
                enableWeatherAlerts: true,
                enableTideAlerts: true,
                enableOptimalConditionAlerts: true
            )
        )
    }
}