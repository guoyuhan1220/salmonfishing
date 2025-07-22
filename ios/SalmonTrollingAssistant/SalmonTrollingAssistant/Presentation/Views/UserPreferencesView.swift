import SwiftUI

struct UserPreferencesView: View {
    @StateObject private var viewModel = UserPreferencesViewModel()
    @State private var selectedSpecies: [FishSpecies] = []
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Profile Section
                VStack(alignment: .leading, spacing: 10) {
                    Text("Profile")
                        .font(.headline)
                    
                    if let profile = viewModel.userProfile {
                        HStack {
                            Image(systemName: "person.circle.fill")
                                .font(.system(size: 50))
                                .foregroundColor(.blue)
                            
                            VStack(alignment: .leading) {
                                Text(profile.name ?? "Anonymous User")
                                    .font(.title3)
                                    .fontWeight(.semibold)
                                
                                Text("Experience Level: \(profile.preferences.experienceLevel.rawValue)")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                        }
                    } else {
                        Text("Not logged in")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Experience Level Section
                VStack(alignment: .leading, spacing: 10) {
                    Text("Experience Level")
                        .font(.headline)
                    
                    Picker("Experience Level", selection: $viewModel.preferences.experienceLevel) {
                        ForEach(ExperienceLevel.allCases, id: \.self) { level in
                            Text(level.rawValue).tag(level)
                        }
                    }
                    .pickerStyle(SegmentedPickerStyle())
                    .onChange(of: viewModel.preferences.experienceLevel) { newValue in
                        viewModel.updateExperienceLevel(newValue)
                    }
                    
                    Text("This helps us tailor recommendations to your skill level.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Preferred Species Section
                VStack(alignment: .leading, spacing: 10) {
                    Text("Preferred Species")
                        .font(.headline)
                    
                    ForEach(FishSpecies.allCases, id: \.self) { species in
                        Toggle(species.rawValue, isOn: Binding(
                            get: { viewModel.preferences.preferredSpecies.contains(species) },
                            set: { isOn in
                                if isOn {
                                    if !viewModel.preferences.preferredSpecies.contains(species) {
                                        viewModel.preferences.preferredSpecies.append(species)
                                    }
                                } else {
                                    viewModel.preferences.preferredSpecies.removeAll { $0 == species }
                                }
                                viewModel.updatePreferredSpecies(viewModel.preferences.preferredSpecies)
                            }
                        ))
                    }
                    
                    Text("We'll prioritize recommendations for your preferred species.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Notification Settings Section
                VStack(alignment: .leading, spacing: 10) {
                    Text("Notification Settings")
                        .font(.headline)
                    
                    Toggle("Weather Alerts", isOn: Binding(
                        get: { viewModel.preferences.notificationSettings.enableWeatherAlerts },
                        set: { viewModel.toggleNotificationSetting(weatherAlerts: $0) }
                    ))
                    
                    Toggle("Tide Alerts", isOn: Binding(
                        get: { viewModel.preferences.notificationSettings.enableTideAlerts },
                        set: { viewModel.toggleNotificationSetting(tideAlerts: $0) }
                    ))
                    
                    Toggle("Optimal Fishing Condition Alerts", isOn: Binding(
                        get: { viewModel.preferences.notificationSettings.enableOptimalConditionAlerts },
                        set: { viewModel.toggleNotificationSetting(optimalConditionAlerts: $0) }
                    ))
                    
                    Text("Receive notifications when conditions are favorable for fishing.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Display Settings Section
                VStack(alignment: .leading, spacing: 10) {
                    Text("Display Settings")
                        .font(.headline)
                    
                    Toggle("High Contrast Mode", isOn: Binding(
                        get: { viewModel.preferences.useHighContrastMode },
                        set: { _ in viewModel.toggleHighContrastMode() }
                    ))
                    
                    Toggle("Large Text", isOn: Binding(
                        get: { viewModel.preferences.useLargeText },
                        set: { _ in viewModel.toggleLargeText() }
                    ))
                    
                    Toggle("Use Metric Units", isOn: Binding(
                        get: { viewModel.preferences.useMetricUnits },
                        set: { _ in viewModel.toggleMetricUnits() }
                    ))
                    
                    Text("Adjust display settings for better visibility outdoors.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Logout Button
                if viewModel.isLoggedIn {
                    Button(action: {
                        viewModel.logout()
                    }) {
                        Text("Log Out")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.red)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                }
                
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .font(.caption)
                }
            }
            .padding()
        }
        .navigationTitle("Preferences")
        .onAppear {
            viewModel.loadUserProfile()
        }
    }
}

struct UserPreferencesView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            UserPreferencesView()
        }
    }
}