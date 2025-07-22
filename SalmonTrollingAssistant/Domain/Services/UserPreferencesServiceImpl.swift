import Foundation
import Combine

class UserPreferencesServiceImpl: UserPreferencesService, ObservableObject {
    static let shared = UserPreferencesServiceImpl()
    
    private let userDefaults = UserDefaults.standard
    private let profileKey = "user_profile"
    private let isLoggedInKey = "is_logged_in"
    
    @Published private var currentProfile: UserProfile?
    private var cancellables = Set<AnyCancellable>()
    
    private init() {
        loadProfile()
    }
    
    func getUserProfile() -> AnyPublisher<UserProfile?, Never> {
        return Just(currentProfile).eraseToAnyPublisher()
    }
    
    func updateUserProfile(profile: UserProfile) -> AnyPublisher<Bool, Error> {
        return Future<Bool, Error> { [weak self] promise in
            guard let self = self else {
                promise(.failure(NSError(domain: "UserPreferencesService", code: 0, userInfo: [NSLocalizedDescriptionKey: "Service not available"])))
                return
            }
            
            do {
                let data = try JSONEncoder().encode(profile)
                self.userDefaults.set(data, forKey: self.profileKey)
                self.currentProfile = profile
                promise(.success(true))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }
    
    func getUserPreferences() -> UserPreferences {
        return currentProfile?.preferences ?? UserPreferences.defaultPreferences()
    }
    
    func updateUserPreferences(preferences: UserPreferences) -> AnyPublisher<Bool, Error> {
        return Future<Bool, Error> { [weak self] promise in
            guard let self = self else {
                promise(.failure(NSError(domain: "UserPreferencesService", code: 0, userInfo: [NSLocalizedDescriptionKey: "Service not available"])))
                return
            }
            
            if var profile = self.currentProfile {
                profile.preferences = preferences
                self.updateUserProfile(profile: profile)
                    .sink(receiveCompletion: { completion in
                        if case .failure(let error) = completion {
                            promise(.failure(error))
                        }
                    }, receiveValue: { success in
                        promise(.success(success))
                    })
                    .store(in: &self.cancellables)
            } else {
                let profile = UserProfile.defaultProfile()
                self.updateUserProfile(profile: profile)
                    .sink(receiveCompletion: { completion in
                        if case .failure(let error) = completion {
                            promise(.failure(error))
                        }
                    }, receiveValue: { success in
                        promise(.success(success))
                    })
                    .store(in: &self.cancellables)
            }
        }.eraseToAnyPublisher()
    }
    
    func isLoggedIn() -> Bool {
        return userDefaults.bool(forKey: isLoggedInKey)
    }
    
    func setLoggedIn(_ loggedIn: Bool) {
        userDefaults.set(loggedIn, forKey: isLoggedInKey)
    }
    
    func logout() -> AnyPublisher<Bool, Never> {
        return Future<Bool, Never> { [weak self] promise in
            guard let self = self else {
                promise(.success(false))
                return
            }
            
            self.userDefaults.removeObject(forKey: self.profileKey)
            self.userDefaults.set(false, forKey: self.isLoggedInKey)
            self.currentProfile = nil
            promise(.success(true))
        }.eraseToAnyPublisher()
    }
    
    // MARK: - Private Methods
    
    private func loadProfile() {
        if let data = userDefaults.data(forKey: profileKey) {
            do {
                currentProfile = try JSONDecoder().decode(UserProfile.self, from: data)
            } catch {
                print("Error decoding user profile: \(error)")
                currentProfile = UserProfile.defaultProfile()
            }
        } else {
            currentProfile = UserProfile.defaultProfile()
        }
    }
}