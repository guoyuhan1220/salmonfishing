import SwiftUI

struct ThemeSettingsView: View {
    @ObservedObject var themeManager: ThemeManager
    @Environment(\.presentationMode) var presentationMode
    @AppStorage("oneHandedModeEnabled") private var isOneHandedModeEnabled = false
    @AppStorage("useLargeTargets") private var useLargeTargets = true
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Appearance")) {
                    Picker("Theme", selection: $themeManager.currentTheme) {
                        Text("System").tag(ThemeMode.system)
                        Text("Light").tag(ThemeMode.light)
                        Text("Dark").tag(ThemeMode.dark)
                        Text("High Contrast").tag(ThemeMode.highContrast)
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }
                
                Section(header: Text("Accessibility"), footer: Text("High contrast mode uses colors that are easier to see in bright sunlight.")) {
                    Toggle("High Contrast Mode", isOn: $themeManager.isHighContrastEnabled)
                        .onChange(of: themeManager.isHighContrastEnabled) { newValue in
                            if newValue {
                                themeManager.currentTheme = .highContrast
                            } else if themeManager.currentTheme == .highContrast {
                                themeManager.currentTheme = .system
                            }
                        }
                    
                    Toggle("Auto Brightness Detection", isOn: $themeManager.isAutoBrightnessDetectionEnabled)
                }
                
                Section(header: Text("Mobile Optimization"), footer: Text("These settings help make the app easier to use while on the water.")) {
                    Toggle("One-Handed Mode", isOn: $isOneHandedModeEnabled)
                        .onChange(of: isOneHandedModeEnabled) { newValue in
                            // This would be saved to UserDefaults in a real app
                        }
                    
                    Toggle("Large Touch Targets", isOn: $useLargeTargets)
                        .onChange(of: useLargeTargets) { newValue in
                            // This would be saved to UserDefaults in a real app
                        }
                }
                
                if themeManager.currentTheme == .highContrast {
                    Section(header: Text("Preview")) {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("High Contrast Mode")
                                .font(.headline)
                                .foregroundColor(Color.themeForeground(for: .highContrast))
                            
                            Text("This text is easier to read in bright sunlight.")
                                .foregroundColor(Color.themeForeground(for: .highContrast))
                            
                            HStack {
                                Text("Important information")
                                    .foregroundColor(Color.themeAccent(for: .highContrast))
                                
                                Spacer()
                                
                                Text("Details")
                                    .foregroundColor(Color.themeSecondary(for: .highContrast))
                            }
                            
                            Button(action: {}) {
                                Text("Action Button")
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 10)
                                    .background(Color.themeAccent(for: .highContrast))
                                    .foregroundColor(Color.black)
                                    .cornerRadius(8)
                            }
                        }
                        .padding()
                        .background(Color.themeBackground(for: .highContrast))
                        .cornerRadius(10)
                    }
                }
            }
            .navigationTitle("Theme Settings")
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}

struct ThemeSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        ThemeSettingsView(themeManager: ThemeManager())
    }
}