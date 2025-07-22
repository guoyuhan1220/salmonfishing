import SwiftUI

struct ProfileView: View {
    @StateObject private var authViewModel = AuthViewModel()
    @StateObject private var preferencesViewModel = UserPreferencesViewModel()
    @StateObject private var catchViewModel = CatchAnalyticsViewModel()
    
    @State private var showingEditProfile = false
    @State private var showingSettings = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Offline indicator
                    OfflineIndicatorView()
                    
                    // Profile header
                    profileHeader
                    
                    // Preferences section
                    preferencesSection
                    
                    // Catch history section
                    catchHistorySection
                    
                    // Settings section
                    settingsSection
                    
                    // Sign out button
                    if authViewModel.isAuthenticated {
                        Button(action: {
                            authViewModel.signOut()
                        }) {
                            Text("Sign Out")
                                .foregroundColor(.red)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color(.systemBackground))
                                .cornerRadius(10)
                                .shadow(radius: 2)
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
            .navigationTitle("Profile")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingEditProfile = true
                    }) {
                        Text("Edit")
                    }
                }
            }
            .sheet(isPresented: $showingEditProfile) {
                EditProfileView(viewModel: preferencesViewModel)
            }
            .sheet(isPresented: $showingSettings) {
                AppSettingsView()
            }
            .onAppear {
                if !authViewModel.isAuthenticated {
                    authViewModel.checkAuthStatus()
                }
                preferencesViewModel.loadUserPreferences()
                catchViewModel.loadCatchHistory()
            }
        }
    }
    
    // MARK: - UI Components
    
    private var profileHeader: some View {
        VStack(spacing: 15) {
            if authViewModel.isAuthenticated {
                // User avatar
                Image(systemName: "person.crop.circle.fill")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 100, height: 100)
                    .foregroundColor(.blue)
                
                // User name
                Text(authViewModel.currentUser?.name ?? "Angler")
                    .font(.title)
                    .bold()
                
                // User info
                Text(authViewModel.currentUser?.email ?? "")
                    .foregroundColor(.secondary)
                
                // Experience level
                Text("Experience: \(preferencesViewModel.userPreferences?.experienceLevel.rawValue ?? "Beginner")")
                    .font(.subheadline)
                    .padding(.vertical, 4)
                    .padding(.horizontal, 12)
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(20)
            } else {
                // Sign in prompt
                VStack(spacing: 15) {
                    Image(systemName: "person.crop.circle.badge.questionmark")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 80, height: 80)
                        .foregroundColor(.gray)
                    
                    Text("Sign in to access your profile")
                        .font(.headline)
                    
                    Button(action: {
                        // Navigate to auth screen
                    }) {
                        Text("Sign In")
                            .foregroundColor(.white)
                            .padding(.horizontal, 30)
                            .padding(.vertical, 10)
                            .background(Color.blue)
                            .cornerRadius(10)
                    }
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color(.systemBackground))
                .cornerRadius(10)
                .shadow(radius: 2)
                .padding(.horizontal)
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(radius: 2)
        .padding(.horizontal)
    }
    
    private var preferencesSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("Fishing Preferences")
                .font(.headline)
                .padding(.horizontal)
            
            VStack(spacing: 0) {
                // Preferred species
                preferenceRow(
                    title: "Preferred Species",
                    value: formatSpeciesList(preferencesViewModel.userPreferences?.preferredSpecies ?? [])
                )
                
                Divider()
                
                // Preferred equipment
                preferenceRow(
                    title: "Preferred Equipment",
                    value: "\(preferencesViewModel.userPreferences?.preferredEquipment.count ?? 0) items"
                )
                
                Divider()
                
                // Notification settings
                preferenceRow(
                    title: "Notifications",
                    value: preferencesViewModel.userPreferences?.notificationSettings.enableOptimalConditionAlerts == true ? "Enabled" : "Disabled"
                )
            }
            .background(Color(.systemBackground))
            .cornerRadius(10)
            .shadow(radius: 2)
            .padding(.horizontal)
            
            NavigationLink(destination: UserPreferencesView()) {
                Text("Edit Preferences")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(10)
                    .padding(.horizontal)
            }
        }
    }
    
    private var catchHistorySection: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("Catch History")
                .font(.headline)
                .padding(.horizontal)
            
            if catchViewModel.catchHistory.isEmpty {
                Text("No catch history recorded yet")
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(10)
                    .shadow(radius: 2)
                    .padding(.horizontal)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 15) {
                        ForEach(catchViewModel.catchHistory.prefix(5)) { catchData in
                            catchHistoryCard(catchData)
                        }
                    }
                    .padding(.horizontal)
                }
            }
            
            NavigationLink(destination: CatchAnalyticsView()) {
                Text("View All Catches")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(10)
                    .padding(.horizontal)
            }
        }
    }
    
    private var settingsSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("App Settings")
                .font(.headline)
                .padding(.horizontal)
            
            VStack(spacing: 0) {
                settingsRow(title: "Data Management", icon: "arrow.up.arrow.down") {
                    showingSettings = true
                }
                
                Divider()
                
                settingsRow(title: "Appearance", icon: "paintbrush") {
                    showingSettings = true
                }
                
                Divider()
                
                settingsRow(title: "Notifications", icon: "bell") {
                    showingSettings = true
                }
                
                Divider()
                
                settingsRow(title: "About", icon: "info.circle") {
                    showingSettings = true
                }
            }
            .background(Color(.systemBackground))
            .cornerRadius(10)
            .shadow(radius: 2)
            .padding(.horizontal)
        }
    }
    
    private func preferenceRow(title: String, value: String) -> some View {
        HStack {
            Text(title)
                .foregroundColor(.primary)
            
            Spacer()
            
            Text(value)
                .foregroundColor(.secondary)
        }
        .padding()
    }
    
    private func settingsRow(title: String, icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .frame(width: 30)
                    .foregroundColor(.blue)
                
                Text(title)
                    .foregroundColor(.primary)
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .foregroundColor(.secondary)
                    .font(.caption)
            }
            .padding()
        }
    }
    
    private func catchHistoryCard(_ catchData: CatchData) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Species and date
            HStack {
                Text(catchData.species.rawValue)
                    .font(.headline)
                
                Spacer()
                
                Text(formatDate(catchData.timestamp))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Divider()
            
            // Size and weight
            if let size = catchData.size {
                HStack {
                    Text("Size:")
                    Spacer()
                    Text("\(size, specifier: "%.1f") in")
                }
                .font(.caption)
            }
            
            if let weight = catchData.weight {
                HStack {
                    Text("Weight:")
                    Spacer()
                    Text("\(weight, specifier: "%.1f") lbs")
                }
                .font(.caption)
            }
            
            // Location
            Text(catchData.location.name)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(1)
        }
        .padding()
        .frame(width: 180, height: 150)
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(radius: 2)
    }
    
    // MARK: - Helper Functions
    
    private func formatSpeciesList(_ species: [FishSpecies]) -> String {
        if species.isEmpty {
            return "None"
        }
        
        return species.map { $0.rawValue }.joined(separator: ", ")
    }
    
    private func formatDate(_ timestamp: TimeInterval) -> String {
        let date = Date(timeIntervalSince1970: timestamp)
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        return formatter.string(from: date)
    }
}

struct EditProfileView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var viewModel: UserPreferencesViewModel
    
    @State private var name: String = ""
    @State private var selectedExperienceLevel: ExperienceLevel = .beginner
    
    let experienceLevels: [ExperienceLevel] = [.beginner, .intermediate, .advanced, .expert]
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Profile Information")) {
                    TextField("Name", text: $name)
                }
                
                Section(header: Text("Experience Level")) {
                    Picker("Experience", selection: $selectedExperienceLevel) {
                        ForEach(experienceLevels, id: \.self) { level in
                            Text(level.rawValue.capitalized).tag(level)
                        }
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }
            }
            .navigationTitle("Edit Profile")
            .navigationBarItems(
                leading: Button("Cancel") {
                    presentationMode.wrappedValue.dismiss()
                },
                trailing: Button("Save") {
                    saveProfile()
                }
            )
            .onAppear {
                // Load current values
                if let preferences = viewModel.userPreferences {
                    selectedExperienceLevel = preferences.experienceLevel
                }
                name = viewModel.userName ?? ""
            }
        }
    }
    
    private func saveProfile() {
        viewModel.updateUserName(name)
        viewModel.updateExperienceLevel(selectedExperienceLevel)
        presentationMode.wrappedValue.dismiss()
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileView()
    }
}