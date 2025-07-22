import SwiftUI

struct AuthView: View {
    @ObservedObject var viewModel: AuthViewModel
    var onAuthSuccess: () -> Void
    
    @State private var isSignUp = false
    @State private var email = ""
    @State private var password = ""
    @State private var name = ""
    
    var body: some View {
        VStack(spacing: 20) {
            Text(isSignUp ? "Create Account" : "Sign In")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            if isSignUp {
                TextField("Name (Optional)", text: $name)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .autocapitalization(.words)
                    .disableAutocorrection(true)
            }
            
            TextField("Email", text: $email)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .disableAutocorrection(true)
            
            SecureField("Password", text: $password)
                .textFieldStyle(RoundedBorderTextFieldStyle())
            
            switch viewModel.authState {
            case .error(let message):
                Text(message)
                    .foregroundColor(.red)
                    .padding(.vertical, 8)
            case .loading:
                ProgressView()
                    .padding(.vertical, 8)
            default:
                EmptyView()
            }
            
            Button(action: {
                if isSignUp {
                    viewModel.createAccount(
                        email: email,
                        password: password,
                        name: name.isEmpty ? nil : name
                    )
                } else {
                    viewModel.signIn(email: email, password: password)
                }
            }) {
                Text(isSignUp ? "Create Account" : "Sign In")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            .disabled(email.isEmpty || password.isEmpty || viewModel.authState == .loading)
            
            Button(action: {
                isSignUp.toggle()
                viewModel.resetAuthState()
            }) {
                Text(isSignUp ? "Already have an account? Sign In" : "Don't have an account? Sign Up")
                    .foregroundColor(.blue)
            }
            
            Divider()
                .padding(.vertical)
            
            Button(action: {
                viewModel.signInAnonymously()
            }) {
                Text("Continue as Guest")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.gray.opacity(0.2))
                    .foregroundColor(.primary)
                    .cornerRadius(8)
            }
            .disabled(viewModel.authState == .loading)
        }
        .padding()
        .onChange(of: viewModel.isSignedIn) { isSignedIn in
            if isSignedIn {
                onAuthSuccess()
            }
        }
    }
}

struct AuthView_Previews: PreviewProvider {
    static var previews: some View {
        let authService = AuthenticationServiceImpl()
        let viewModel = AuthViewModel(authService: authService)
        
        return AuthView(viewModel: viewModel) {
            print("Auth success")
        }
    }
}