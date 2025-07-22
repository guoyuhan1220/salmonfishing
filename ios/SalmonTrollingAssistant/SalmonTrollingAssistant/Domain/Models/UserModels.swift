import Foundation

enum ExperienceLevel: String, Codable, CaseIterable {
    case beginner = "BEGINNER"
    case intermediate = "INTERMEDIATE"
    case advanced = "ADVANCED"
    case expert = "EXPERT"
}

struct UserProfile: Identifiable, Codable {
    let id: String
    var name: String?
    var email: String?
    var preferences: UserPreferences
    var equipmentInventory: [UserEquipment]
    var isAnonymous: Bool
    
    init(id: String = UUID().uuidString,
         name: String? = nil,
         email: String? = nil,
         preferences: UserPreferences = UserPreferences(),
         equipmentInventory: [UserEquipment] = [],
         isAnonymous: Bool = true) {
        self.id = id
        self.name = name
        self.email = email
        self.preferences = preferences
        self.equipmentInventory = equipmentInventory
        self.isAnonymous = isAnonymous
    }
}

struct UserPreferences: Codable {
    var preferredSpecies: [FishSpecies]
    var preferredEquipment: [String] // IDs of preferred equipment
    var experienceLevel: ExperienceLevel
    var notificationSettings: NotificationSettings
    
    init(preferredSpecies: [FishSpecies] = [],
         preferredEquipment: [String] = [],
         experienceLevel: ExperienceLevel = .beginner,
         notificationSettings: NotificationSettings = NotificationSettings()) {
        self.preferredSpecies = preferredSpecies
        self.preferredEquipment = preferredEquipment
        self.experienceLevel = experienceLevel
        self.notificationSettings = notificationSettings
    }
}

struct NotificationSettings: Codable {
    var enableWeatherAlerts: Bool
    var enableTideAlerts: Bool
    var enableOptimalConditionAlerts: Bool
    
    init(enableWeatherAlerts: Bool = false,
         enableTideAlerts: Bool = false,
         enableOptimalConditionAlerts: Bool = false) {
        self.enableWeatherAlerts = enableWeatherAlerts
        self.enableTideAlerts = enableTideAlerts
        self.enableOptimalConditionAlerts = enableOptimalConditionAlerts
    }
}

struct CatchData: Identifiable, Codable {
    let id: String
    let timestamp: Date
    let locationId: String
    let species: FishSpecies
    let size: Double? // in inches
    let weight: Double? // in pounds
    let equipmentUsed: [String] // IDs of equipment used
    let weatherConditionsId: String?
    let tideConditionsId: String?
    let notes: String?
    let photoUrls: [String]
    
    init(id: String = UUID().uuidString,
         timestamp: Date = Date(),
         locationId: String,
         species: FishSpecies,
         size: Double? = nil,
         weight: Double? = nil,
         equipmentUsed: [String] = [],
         weatherConditionsId: String? = nil,
         tideConditionsId: String? = nil,
         notes: String? = nil,
         photoUrls: [String] = []) {
        self.id = id
        self.timestamp = timestamp
        self.locationId = locationId
        self.species = species
        self.size = size
        self.weight = weight
        self.equipmentUsed = equipmentUsed
        self.weatherConditionsId = weatherConditionsId
        self.tideConditionsId = tideConditionsId
        self.notes = notes
        self.photoUrls = photoUrls
    }
}

struct AuthCredentials: Codable {
    let email: String
    let password: String
}

struct AuthResult: Codable {
    let success: Bool
    let userId: String?
    let token: String?
    let error: String?
    
    init(success: Bool, userId: String? = nil, token: String? = nil, error: String? = nil) {
        self.success = success
        self.userId = userId
        self.token = token
        self.error = error
    }
}