import Foundation
import Combine

protocol UserPreferencesService {
    func getUserPreferences() -> AnyPublisher<UserPreferences?, Never>
    func updateUserPreferences(_ preferences: UserPreferences) -> AnyPublisher<Bool, Error>
}

class UserPreferencesServiceImpl: UserPreferencesService {
    private let preferencesSubject = CurrentValueSubject<UserPreferences?, Never>(nil)
    
    init() {
        loadPreferences()
    }
    
    func getUserPreferences() -> AnyPublisher<UserPreferences?, Never> {
        return preferencesSubject.eraseToAnyPublisher()
    }
    
    func updateUserPreferences(_ preferences: UserPreferences) -> AnyPublisher<Bool, Error> {
        let subject = PassthroughSubject<Bool, Error>()
        
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(preferences)
            UserDefaults.standard.set(data, forKey: "userPreferences")
            preferencesSubject.send(preferences)
            subject.send(true)
            subject.send(completion: .finished)
        } catch {
            subject.send(completion: .failure(error))
        }
        
        return subject.eraseToAnyPublisher()
    }
    
    private func loadPreferences() {
        if let data = UserDefaults.standard.data(forKey: "userPreferences") {
            do {
                let decoder = JSONDecoder()
                let preferences = try decoder.decode(UserPreferences.self, from: data)
                preferencesSubject.send(preferences)
            } catch {
                print("Error loading user preferences: \(error.localizedDescription)")
                // Use default preferences if loading fails
                preferencesSubject.send(UserPreferences.defaultPreferences())
            }
        } else {
            // No saved preferences, use defaults
            preferencesSubject.send(UserPreferences.defaultPreferences())
        }
    }
}