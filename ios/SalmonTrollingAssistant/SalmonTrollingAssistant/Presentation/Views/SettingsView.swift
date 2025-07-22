import SwiftUI

struct SettingsView: View {
    @State private var highContrastMode = false
    @State private var enableWeatherAlerts = true
    @State private var enableTideAlerts = true
    @State private var enableOptimalConditionAlerts = true
    @State private var selectedExperienceLevel = 1 // Intermediate
    @State private var selectedSpecies: [Bool] = [true, true, false, false, false] // Chinook, Coho selected by default
    
    private let experienceLevels = ["Beginner", "Intermediate", "Advanced", "Expert"]
    private let fishSpecies = ["Chinook", "Coho", "Sockeye", "Pink", "Chum"]
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Display")) {
                    Toggle("High Contrast Mode", isOn: $highContrastMode)
                }
                
                Section(header: Text("Experience Level")) {
                    Picker("Experience Level", selection: $selectedExperienceLevel) {
                        ForEach(0..<experienceLevels.count, id: \.self) { index in
                            Text(experienceLevels[index]).tag(index)
                        }
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }
                
                Section(header: Text("Preferred Species")) {
                    ForEach(0..<fishSpecies.count, id: \.self) { index in
                        Toggle(fishSpecies[index], isOn: $selectedSpecies[index])
                    }
                }
                
                Section(header: Text("Notifications")) {
                    Toggle("Weather Alerts", isOn: $enableWeatherAlerts)
                    Toggle("Tide Alerts", isOn: $enableTideAlerts)
                    Toggle("Optimal Fishing Condition Alerts", isOn: $enableOptimalConditionAlerts)
                }
                
                Section(header: Text("Data Management")) {
                    NavigationLink(destination: DataManagementView()) {
                        Text("Manage Offline Data")
                    }
                    
                    Button(action: {
                        // Clear cache action
                    }) {
                        Text("Clear Cache")
                            .foregroundColor(.red)
                    }
                }
                
                Section(header: Text("Account")) {
                    Button(action: {
                        // Logout action
                    }) {
                        Text("Log Out")
                            .foregroundColor(.red)
                    }
                }
                
                Section(footer: Text("Salmon Trolling Assistant v1.0")) {
                    // Empty section for footer
                }
            }
            .navigationTitle("Settings")
        }
    }
}

struct DataManagementView: View {
    @State private var offlineMapDownloaded = false
    @State private var offlineDataSize: String = "245 MB"
    @State private var maxOfflineDataAge = 1 // 1 day
    @State private var wifiOnlyDownloads = true
    
    var body: some View {
        Form {
            Section(header: Text("Offline Maps")) {
                Toggle("Download Maps for Offline Use", isOn: $offlineMapDownloaded)
                
                if offlineMapDownloaded {
                    Button(action: {
                        // Update maps action
                    }) {
                        Text("Update Offline Maps")
                    }
                }
            }
            
            Section(header: Text("Data Usage")) {
                Toggle("Download Updates on Wi-Fi Only", isOn: $wifiOnlyDownloads)
                
                Picker("Keep Offline Data For", selection: $maxOfflineDataAge) {
                    Text("1 Day").tag(1)
                    Text("3 Days").tag(3)
                    Text("7 Days").tag(7)
                    Text("30 Days").tag(30)
                }
                .pickerStyle(SegmentedPickerStyle())
            }
            
            Section(header: Text("Storage")) {
                HStack {
                    Text("Offline Data Size")
                    Spacer()
                    Text(offlineDataSize)
                        .foregroundColor(.secondary)
                }
                
                Button(action: {
                    // Clear all offline data action
                }) {
                    Text("Clear All Offline Data")
                        .foregroundColor(.red)
                }
            }
        }
        .navigationTitle("Data Management")
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}