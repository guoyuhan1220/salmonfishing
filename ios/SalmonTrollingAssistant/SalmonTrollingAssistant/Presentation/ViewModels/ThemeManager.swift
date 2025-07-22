import Foundation
import SwiftUI
import Combine

enum ThemeMode: String, CaseIterable {
    case system
    case light
    case dark
    case highContrast
}

class ThemeManager: ObservableObject {
    @Published var currentTheme: ThemeMode
    @Published var isHighContrastEnabled: Bool
    @Published var isAutoBrightnessDetectionEnabled: Bool
    
    private var cancellables = Set<AnyCancellable>()
    private let userDefaults: UserDefaults
    
    private let themeKey = "app_theme_mode"
    private let highContrastKey = "high_contrast_enabled"
    private let autoBrightnessKey = "auto_brightness_detection_enabled"
    
    init(userDefaults: UserDefaults = .standard) {
        self.userDefaults = userDefaults
        
        // Load saved preferences
        let savedTheme = userDefaults.string(forKey: themeKey)
        self.currentTheme = ThemeMode(rawValue: savedTheme ?? "") ?? .system
        
        self.isHighContrastEnabled = userDefaults.bool(forKey: highContrastKey)
        self.isAutoBrightnessDetectionEnabled = userDefaults.bool(forKey: autoBrightnessKey)
        
        // Set up auto brightness detection if enabled
        if isAutoBrightnessDetectionEnabled {
            setupBrightnessDetection()
        }
        
        // Save changes to user defaults
        $currentTheme
            .sink { [weak self] theme in
                self?.userDefaults.set(theme.rawValue, forKey: self?.themeKey ?? "")
            }
            .store(in: &cancellables)
        
        $isHighContrastEnabled
            .sink { [weak self] isEnabled in
                self?.userDefaults.set(isEnabled, forKey: self?.highContrastKey ?? "")
            }
            .store(in: &cancellables)
        
        $isAutoBrightnessDetectionEnabled
            .sink { [weak self] isEnabled in
                self?.userDefaults.set(isEnabled, forKey: self?.autoBrightnessKey ?? "")
                
                if isEnabled {
                    self?.setupBrightnessDetection()
                }
            }
            .store(in: &cancellables)
    }
    
    func setTheme(_ theme: ThemeMode) {
        currentTheme = theme
    }
    
    func toggleHighContrast() {
        isHighContrastEnabled.toggle()
        
        if isHighContrastEnabled {
            currentTheme = .highContrast
        } else {
            currentTheme = .system
        }
    }
    
    func toggleAutoBrightnessDetection() {
        isAutoBrightnessDetectionEnabled.toggle()
    }
    
    private func setupBrightnessDetection() {
        // In a real app, we would use UIScreen.brightness notifications
        // to detect changes in screen brightness and adjust the theme accordingly
        // For this implementation, we'll just simulate it
        
        // Example of how it would be implemented:
        // NotificationCenter.default.publisher(for: UIScreen.brightnessDidChangeNotification)
        //     .sink { [weak self] _ in
        //         let brightness = UIScreen.main.brightness
        //         if brightness < 0.3 && self?.currentTheme != .highContrast {
        //             self?.currentTheme = .highContrast
        //         } else if brightness >= 0.3 && self?.currentTheme == .highContrast {
        //             self?.currentTheme = .system
        //         }
        //     }
        //     .store(in: &cancellables)
    }
}

// Extension to provide theme colors
extension Color {
    static func themeBackground(for theme: ThemeMode) -> Color {
        switch theme {
        case .light:
            return Color(.systemBackground)
        case .dark:
            return Color(.systemBackground)
        case .highContrast:
            return Color.black
        case .system:
            return Color(.systemBackground)
        }
    }
    
    static func themeForeground(for theme: ThemeMode) -> Color {
        switch theme {
        case .light:
            return Color(.label)
        case .dark:
            return Color(.label)
        case .highContrast:
            return Color.white
        case .system:
            return Color(.label)
        }
    }
    
    static func themeAccent(for theme: ThemeMode) -> Color {
        switch theme {
        case .light, .dark, .system:
            return Color.blue
        case .highContrast:
            return Color.yellow
        }
    }
    
    static func themeSecondary(for theme: ThemeMode) -> Color {
        switch theme {
        case .light:
            return Color(.secondaryLabel)
        case .dark:
            return Color(.secondaryLabel)
        case .highContrast:
            return Color.yellow.opacity(0.8)
        case .system:
            return Color(.secondaryLabel)
        }
    }
}

// Theme modifier for views
struct ThemeModifier: ViewModifier {
    @ObservedObject var themeManager: ThemeManager
    
    func body(content: Content) -> some View {
        content
            .preferredColorScheme(colorScheme)
            .accentColor(Color.themeAccent(for: themeManager.currentTheme))
    }
    
    private var colorScheme: ColorScheme? {
        switch themeManager.currentTheme {
        case .light:
            return .light
        case .dark, .highContrast:
            return .dark
        case .system:
            return nil
        }
    }
}

extension View {
    func withTheme(_ themeManager: ThemeManager) -> some View {
        self.modifier(ThemeModifier(themeManager: themeManager))
    }
}