import Foundation
import Combine

class UserPreferencesViewModel: ObservableObject {
    private let userPreferencesService: UserPreferencesService
    private var cancellables = Set<AnyCancellable>()
    
    @Published var userProfile: UserProfile?
    @Published var preferences: UserPreferences
    @Published var isSaving = false
    @Published var errorMessage: String?
    @Published var isLoggedIn: Bool
    
    init(userPreferencesService: UserPreferencesService = UserPreferencesServiceImpl.shared) {
        self.userPreferencesService = userPreferencesService
        self.preferences = userPreferencesService.getUserPreferences()
        self.isLoggedIn = userPreferencesService.isLoggedIn()
        
        loadUserProfile()
    }
    
    func loadUserProfile() {
        userPreferencesService.getUserProfile()
            .sink { [weak self] profile in
                self?.userProfile = profile
                if let preferences = profile?.preferences {
                    self?.preferences = preferences
                }
            }
            .store(in: &cancellables)
    }
    
    func savePreferences() {
        isSaving = true
        errorMessage = nil
        
        userPreferencesService.updateUserPreferences(preferences: preferences)
            .sink(receiveCompletion: { [weak self] completion in
                self?.isSaving = false
                if case .failure(let error) = completion {
                    self?.errorMessage = error.localizedDescription
                }
            }, receiveValue: { _ in
                // Success
            })
            .store(in: &cancellables)
    }
    
    func logout() {
        userPreferencesService.logout()
            .sink { [weak self] success in
                if success {
                    self?.isLoggedIn = false
                    self?.userProfile = nil
                    self?.preferences = UserPreferences.defaultPreferences()
                }
            }
            .store(in: &cancellables)
    }
    
    func toggleHighContrastMode() {
        preferences.useHighContrastMode.toggle()
        savePreferences()
    }
    
    func toggleLargeText() {
        preferences.useLargeText.toggle()
        savePreferences()
    }
    
    func toggleMetricUnits() {
        preferences.useMetricUnits.toggle()
        savePreferences()
    }
    
    func updateExperienceLevel(_ level: ExperienceLevel) {
        preferences.experienceLevel = level
        savePreferences()
    }
    
    func updatePreferredSpecies(_ species: [FishSpecies]) {
        preferences.preferredSpecies = species
        savePreferences()
    }
    
    func toggleNotificationSetting(weatherAlerts: Bool? = nil, tideAlerts: Bool? = nil, optimalConditionAlerts: Bool? = nil) {
        if let weatherAlerts = weatherAlerts {
            preferences.notificationSettings.enableWeatherAlerts = weatherAlerts
        }
        
        if let tideAlerts = tideAlerts {
            preferences.notificationSettings.enableTideAlerts = tideAlerts
        }
        
        if let optimalConditionAlerts = optimalConditionAlerts {
            preferences.notificationSettings.enableOptimalConditionAlerts = optimalConditionAlerts
        }
        
        savePreferences()
    }
}