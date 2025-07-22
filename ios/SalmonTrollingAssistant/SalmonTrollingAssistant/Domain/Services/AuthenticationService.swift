import Foundation
import Combine

protocol AuthenticationService {
    /**
     * Create a new user account with email and password
     */
    func createAccount(credentials: AuthCredentials, name: String?) async -> AuthResult
    
    /**
     * Sign in with existing credentials
     */
    func signIn(credentials: AuthCredentials) async -> AuthResult
    
    /**
     * Sign out the current user
     */
    func signOut() async -> Bool
    
    /**
     * Create or get an anonymous user account
     */
    func signInAnonymously() async -> AuthResult
    
    /**
     * Convert an anonymous account to a permanent account
     */
    func convertAnonymousAccount(credentials: AuthCredentials, name: String?) async -> AuthResult
    
    /**
     * Get the current user profile
     */
    var currentUser: CurrentValueSubject<UserProfile?, Never> { get }
    
    /**
     * Check if the user is signed in
     */
    var isSignedIn: AnyPublisher<Bool, Never> { get }
    
    /**
     * Check if the current user is anonymous
     */
    var isAnonymousUser: AnyPublisher<Bool, Never> { get }
}