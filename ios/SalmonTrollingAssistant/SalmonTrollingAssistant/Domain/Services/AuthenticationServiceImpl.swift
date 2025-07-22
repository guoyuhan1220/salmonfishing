import Foundation
import Combine
import KeychainAccess

class AuthenticationServiceImpl: AuthenticationService {
    private let keychain = Keychain(service: "com.example.SalmonTrollingAssistant")
    private let userDefaults = UserDefaults.standard
    
    private let userProfileKey = "userProfile"
    private let authTokenKey = "authToken"
    
    let currentUser = CurrentValueSubject<UserProfile?, Never>(nil)
    
    var isSignedIn: AnyPublisher<Bool, Never> {
        return currentUser
            .map { $0 != nil }
            .eraseToAnyPublisher()
    }
    
    var isAnonymousUser: AnyPublisher<Bool, Never> {
        return currentUser
            .map { $0?.isAnonymous ?? false }
            .eraseToAnyPublisher()
    }
    
    init() {
        // Load the current user on initialization
        if let userData = userDefaults.data(forKey: userProfileKey),
           let userProfile = try? JSONDecoder().decode(UserProfile.self, from: userData) {
            currentUser.send(userProfile)
        }
    }
    
    func createAccount(credentials: AuthCredentials, name: String?) async -> AuthResult {
        // In a real app, this would make an API call to create an account
        // For now, we'll simulate account creation locally
        
        // Check if email already exists
        if emailExists(credentials.email) {
            return AuthResult(
                success: false,
                error: "Email already in use"
            )
        }
        
        let userId = UUID().uuidString
        let token = UUID().uuidString
        
        // Create and save the user profile
        let userProfile = UserProfile(
            id: userId,
            name: name,
            email: credentials.email,
            isAnonymous: false
        )
        
        saveCurrentUser(userProfile)
        saveCredentials(credentials)
        saveAuthToken(token)
        
        return AuthResult(
            success: true,
            userId: userId,
            token: token
        )
    }
    
    func signIn(credentials: AuthCredentials) async -> AuthResult {
        // In a real app, this would make an API call to authenticate
        // For now, we'll simulate authentication locally
        
        guard let savedPassword = getSavedPassword(for: credentials.email),
              savedPassword == credentials.password else {
            return AuthResult(
                success: false,
                error: "Invalid email or password"
            )
        }
        
        // Load or create user profile
        let currentUserValue = currentUser.value
        let userId = currentUserValue?.id ?? UUID().uuidString
        let token = UUID().uuidString
        
        let userProfile: UserProfile
        if var existingUser = currentUserValue {
            existingUser.email = credentials.email
            existingUser.isAnonymous = false
            userProfile = existingUser
        } else {
            userProfile = UserProfile(
                id: userId,
                email: credentials.email,
                isAnonymous: false
            )
        }
        
        saveCurrentUser(userProfile)
        saveAuthToken(token)
        
        return AuthResult(
            success: true,
            userId: userId,
            token: token
        )
    }
    
    func signOut() async -> Bool {
        saveAuthToken(nil)
        saveCurrentUser(nil)
        return true
    }
    
    func signInAnonymously() async -> AuthResult {
        let userId = UUID().uuidString
        let token = UUID().uuidString
        
        let userProfile = UserProfile(
            id: userId,
            isAnonymous: true
        )
        
        saveCurrentUser(userProfile)
        saveAuthToken(token)
        
        return AuthResult(
            success: true,
            userId: userId,
            token: token
        )
    }
    
    func convertAnonymousAccount(credentials: AuthCredentials, name: String?) async -> AuthResult {
        guard var currentUserValue = currentUser.value else {
            return AuthResult(
                success: false,
                error: "No anonymous user to convert"
            )
        }
        
        if !currentUserValue.isAnonymous {
            return AuthResult(
                success: false,
                error: "Current user is not anonymous"
            )
        }
        
        // Check if email already exists
        if emailExists(credentials.email) {
            return AuthResult(
                success: false,
                error: "Email already in use"
            )
        }
        
        let token = UUID().uuidString
        
        currentUserValue.name = name
        currentUserValue.email = credentials.email
        currentUserValue.isAnonymous = false
        
        saveCurrentUser(currentUserValue)
        saveCredentials(credentials)
        saveAuthToken(token)
        
        return AuthResult(
            success: true,
            userId: currentUserValue.id,
            token: token
        )
    }
    
    // MARK: - Helper Methods
    
    private func saveCurrentUser(_ userProfile: UserProfile?) {
        if let userProfile = userProfile,
           let userData = try? JSONEncoder().encode(userProfile) {
            userDefaults.set(userData, forKey: userProfileKey)
        } else {
            userDefaults.removeObject(forKey: userProfileKey)
        }
        
        currentUser.send(userProfile)
    }
    
    private func emailExists(_ email: String) -> Bool {
        return (try? keychain.get("email_\(email)")) != nil
    }
    
    private func saveCredentials(_ credentials: AuthCredentials) {
        try? keychain.set(credentials.password, key: "email_\(credentials.email)")
    }
    
    private func getSavedPassword(for email: String) -> String? {
        return try? keychain.get("email_\(email)")
    }
    
    private func saveAuthToken(_ token: String?) {
        if let token = token {
            try? keychain.set(token, key: authTokenKey)
        } else {
            try? keychain.remove(authTokenKey)
        }
    }
    
    private func getAuthToken() -> String? {
        return try? keychain.get(authTokenKey)
    }
}