import SwiftUI

// Mock classes for preview
class MockOfflineDataManager: ObservableObject {
    @Published var offlineModeEnabled = false
    
    func setOfflineMode(enabled: Bool) {
        offlineModeEnabled = enabled
    }
    
    func isOfflineModeEnabled() -> Bool {
        return offlineModeEnabled
    }
    
    func clearAllCachedData() {
        // Mock implementation
    }
}

class MockOfflineDataAccessLayer: ObservableObject {
    enum CachePriority: Int {
        case low = 0
        case medium = 1
        case high = 2
    }
    
    @Published var cacheSizeMB = 100
    @Published var cachePriority = CachePriority.medium
    @Published var prefetchEnabled = true
    @Published var prefetchDays = 3
    @Published var totalCacheSizeMB: Float = 25.0
    
    static let shared = MockOfflineDataAccessLayer()
    
    func getMaxCacheSizeMB() -> Int {
        return cacheSizeMB
    }
    
    func setMaxCacheSizeMB(_ size: Int) {
        cacheSizeMB = size
    }
    
    func getCachePriority() -> CachePriority {
        return cachePriority
    }
    
    func setCachePriority(_ priority: CachePriority) {
        cachePriority = priority
        
        switch priority {
        case .low:
            cacheSizeMB = 50
            prefetchDays = 1
        case .medium:
            cacheSizeMB = 100
            prefetchDays = 3
        case .high:
            cacheSizeMB = 250
            prefetchDays = 7
        }
    }
    
    func isPrefetchEnabled() -> Bool {
        return prefetchEnabled
    }
    
    func setPrefetchEnabled(_ enabled: Bool) {
        prefetchEnabled = enabled
    }
    
    func getPrefetchDays() -> Int {
        return prefetchDays
    }
    
    func setPrefetchDays(_ days: Int) {
        prefetchDays = days
    }
    
    func getTotalCacheSizeMB() -> Float {
        return totalCacheSizeMB
    }
    
    func clearAllCachedData() {
        totalCacheSizeMB = 0
    }
    
    func cleanupCache() {
        totalCacheSizeMB *= 0.8
    }
}

struct DataManagementView: View {
    @Environment(\.presentationMode) var presentationMode
    
    // Use mock objects for preview
    #if DEBUG
    @StateObject private var offlineDataAccessLayer = MockOfflineDataAccessLayer.shared
    @StateObject private var offlineDataManager = MockOfflineDataManager()
    #else
    @ObservedObject private var offlineDataAccessLayer = OfflineDataAccessLayer.shared
    @ObservedObject private var offlineDataManager = OfflineDataManager.shared
    #endif
    
    @State private var showClearDataAlert = false
    @State private var showClearCacheAlert = false
    @State private var showExportAlert = false
    @State private var showImportAlert = false
    
    @State private var cacheSizeMB: Int = 100
    @State private var cachePriority: MockOfflineDataAccessLayer.CachePriority = .medium
    @State private var prefetchEnabled: Bool = true
    @State private var prefetchDays: Int = 3
    @State private var totalCacheSizeMB: Float = 0
    @State private var offlineModeEnabled: Bool = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Backup & Restore Card
                VStack(alignment: .leading, spacing: 10) {
                    Text("Backup & Restore")
                        .font(.headline)
                    
                    Text("Export your data to a file or import from a previous backup")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    HStack(spacing: 10) {
                        Button(action: {
                            showExportAlert = true
                        }) {
                            Text("Export Data")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        
                        Button(action: {
                            showImportAlert = true
                        }) {
                            Text("Import Data")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Storage Usage Card
                VStack(alignment: .leading, spacing: 10) {
                    Text("Storage Usage")
                        .font(.headline)
                    
                    HStack {
                        Text("Weather Data")
                        Spacer()
                        Text("\(Int(totalCacheSizeMB * 0.4)) MB")
                    }
                    
                    HStack {
                        Text("Tide Data")
                        Spacer()
                        Text("\(Int(totalCacheSizeMB * 0.3)) MB")
                    }
                    
                    HStack {
                        Text("Location Data")
                        Spacer()
                        Text("\(Int(totalCacheSizeMB * 0.1)) MB")
                    }
                    
                    HStack {
                        Text("Other Data")
                        Spacer()
                        Text("\(Int(totalCacheSizeMB * 0.2)) MB")
                    }
                    
                    Divider()
                    
                    HStack {
                        Text("Total")
                            .font(.headline)
                        Spacer()
                        Text("\(Int(totalCacheSizeMB)) MB")
                            .font(.headline)
                    }
                    
                    Button(action: {
                        showClearCacheAlert = true
                    }) {
                        HStack {
                            Image(systemName: "trash")
                            Text("Clear Cache")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Offline Data Settings Card
                VStack(alignment: .leading, spacing: 10) {
                    Text("Offline Data Settings")
                        .font(.headline)
                    
                    Toggle("Offline Mode", isOn: $offlineModeEnabled)
                        .onChange(of: offlineModeEnabled) { newValue in
                            offlineDataManager.setOfflineMode(enabled: newValue)
                        }
                    
                    Text("When offline mode is enabled, the app will not attempt to connect to the internet and will use cached data only.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Text("Cache Priority")
                        .padding(.top, 10)
                    
                    Picker("Cache Priority", selection: $cachePriority) {
                        Text("Low").tag(MockOfflineDataAccessLayer.CachePriority.low)
                        Text("Medium").tag(MockOfflineDataAccessLayer.CachePriority.medium)
                        Text("High").tag(MockOfflineDataAccessLayer.CachePriority.high)
                    }
                    .pickerStyle(SegmentedPickerStyle())
                    .onChange(of: cachePriority) { newValue in
                        offlineDataAccessLayer.setCachePriority(newValue)
                        cacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
                        prefetchDays = offlineDataAccessLayer.getPrefetchDays()
                    }
                    
                    Text(cachePriorityDescription)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Toggle("Prefetch Data", isOn: $prefetchEnabled)
                        .padding(.top, 10)
                        .onChange(of: prefetchEnabled) { newValue in
                            offlineDataAccessLayer.setPrefetchEnabled(newValue)
                        }
                    
                    Text("Automatically download data for saved locations to ensure offline availability.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    if prefetchEnabled {
                        Text("Prefetch Days: \(prefetchDays)")
                            .padding(.top, 10)
                        
                        Slider(value: Binding(
                            get: { Double(prefetchDays) },
                            set: { prefetchDays = Int($0) }
                        ), in: 1...7, step: 1)
                        .onChange(of: prefetchDays) { newValue in
                            offlineDataAccessLayer.setPrefetchDays(newValue)
                        }
                    }
                    
                    Text("Maximum Cache Size: \(cacheSizeMB) MB")
                        .padding(.top, 10)
                    
                    Slider(value: Binding(
                        get: { Double(cacheSizeMB) },
                        set: { cacheSizeMB = Int($0) }
                    ), in: 10...500, step: 10)
                    .onChange(of: cacheSizeMB) { newValue in
                        offlineDataAccessLayer.setMaxCacheSizeMB(newValue)
                    }
                    
                    Button(action: {
                        offlineDataAccessLayer.cleanupCache()
                        totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
                    }) {
                        HStack {
                            Image(systemName: "arrow.clockwise")
                            Text("Clean Up Cache")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                
                // Clear Data Card
                VStack(alignment: .leading, spacing: 10) {
                    Text("Clear Data")
                        .font(.headline)
                    
                    Text("Remove all your data from this device. This action cannot be undone.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Button(action: {
                        showClearDataAlert = true
                    }) {
                        Text("Clear All Data")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.red)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
            }
            .padding()
        }
        .navigationTitle("Data Management")
        .onAppear {
            // Load initial values
            cacheSizeMB = offlineDataAccessLayer.getMaxCacheSizeMB()
            cachePriority = offlineDataAccessLayer.getCachePriority()
            prefetchEnabled = offlineDataAccessLayer.isPrefetchEnabled()
            prefetchDays = offlineDataAccessLayer.getPrefetchDays()
            totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
            offlineModeEnabled = offlineDataManager.isOfflineModeEnabled()
        }
        .alert(isPresented: $showClearDataAlert) {
            Alert(
                title: Text("Clear All Data"),
                message: Text("Are you sure you want to clear all your data? This action cannot be undone."),
                primaryButton: .destructive(Text("Clear")) {
                    offlineDataAccessLayer.clearAllCachedData()
                    totalCacheSizeMB = offlineDataAccessLayer.getTotalCacheSizeMB()
                },
                secondaryButton: .cancel()
            )
        }
    }
    
    private var cachePriorityDescription: String {
        switch cachePriority {
        case .low:
            return "Minimal caching to save storage space. Limited offline functionality."
        case .medium:
            return "Balanced caching for moderate offline functionality."
        case .high:
            return "Maximum caching for best offline experience."
        }
    }
}

struct DataManagementView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            DataManagementView()
        }
    }
}