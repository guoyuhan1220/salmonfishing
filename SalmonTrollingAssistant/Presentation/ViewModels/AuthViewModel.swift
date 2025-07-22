import Foundation
import Combine

class AuthViewModel: ObservableObject {
    private let authService: AuthenticationService
    private var cancellables = Set<AnyCancellable>()
    
    @Published var authState: AuthState = .initial
    @Published var currentUser: UserProfile?
    @Published var isSignedIn: Bool = false
    @Published var isAnonymous: Bool = false
    
    init(authService: AuthenticationService) {
        self.authService = authService
        
        // Subscribe to user changes
        authService.currentUser
            .assign(to: \.currentUser, on: self)
            .store(in: &cancellables)
        
        authService.isSignedIn
            .assign(to: \.isSignedIn, on: self)
            .store(in: &cancellables)
        
        authService.isAnonymousUser
            .assign(to: \.isAnonymous, on: self)
            .store(in: &cancellables)
    }
    
    func createAccount(email: String, password: String, name: String?) {
        guard validateCredentials(email: email, password: password) else {
            return
        }
        
        authState = .loading
        
        Task {
            let result = await authService.createAccount(
                credentials: AuthCredentials(email: email, password: password),
                name: name
            )
            
            await MainActor.run {
                if result.success {
                    authState = .success(result)
                } else {
                    authState = .error(result.error ?? "Unknown error")
                }
            }
        }
    }
    
    func signIn(email: String, password: String) {
        guard validateCredentials(email: email, password: password) else {
            return
        }
        
        authState = .loading
        
        Task {
            let result = await authService.signIn(
                credentials: AuthCredentials(email: email, password: password)
            )
            
            await MainActor.run {
                if result.success {
                    authState = .success(result)
                } else {
                    authState = .error(result.error ?? "Unknown error")
                }
            }
        }
    }
    
    func signOut() {
        authState = .loading
        
        Task {
            let success = await authService.signOut()
            
            await MainActor.run {
                if success {
                    authState = .initial
                } else {
                    authState = .error("Failed to sign out")
                }
            }
        }
    }
    
    func signInAnonymously() {
        authState = .loading
        
        Task {
            let result = await authService.signInAnonymously()
            
            await MainActor.run {
                if result.success {
                    authState = .success(result)
                } else {
                    authState = .error(result.error ?? "Unknown error")
                }
            }
        }
    }
    
    func convertAnonymousAccount(email: String, password: String, name: String?) {
        guard validateCredentials(email: email, password: password) else {
            return
        }
        
        authState = .loading
        
        Task {
            let result = await authService.convertAnonymousAccount(
                credentials: AuthCredentials(email: email, password: password),
                name: name
            )
            
            await MainActor.run {
                if result.success {
                    authState = .success(result)
                } else {
                    authState = .error(result.error ?? "Unknown error")
                }
            }
        }
    }
    
    func resetAuthState() {
        authState = .initial
    }
    
    private func validateCredentials(email: String, password: String) -> Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        
        guard !email.isEmpty, emailPredicate.evaluate(with: email) else {
            authState = .error("Invalid email address")
            return false
        }
        
        guard password.count >= 6 else {
            authState = .error("Password must be at least 6 characters")
            return false
        }
        
        return true
    }
    
    enum AuthState {
        case initial
        case loading
        case success(AuthResult)
        case error(String)
    }
}