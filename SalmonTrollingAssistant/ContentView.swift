import SwiftUI
import MapKit
import CoreLocation

// Simple data structures for our app
struct SimpleLocation: Identifiable, Hashable {
    let id: String
    let name: String
    let latitude: Double
    let longitude: Double
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
    
    static func == (lhs: SimpleLocation, rhs: SimpleLocation) -> Bool {
        return lhs.id == rhs.id
    }
}

// Fishing gear model
struct FishingGear: Identifiable, Hashable {
    let id = UUID()
    let name: String
    let type: GearType
    let image: UIImage
    let color: String
    let size: String
    let brand: String
    let confidence: Int // AI confidence in identification
    
    // For preview and demo purposes
    static func mockGear() -> [FishingGear] {
        return [
            FishingGear(name: "Hot Spot Flasher", type: .flasher, image: UIImage(systemName: "circle")!, color: "Green", size: "Standard", brand: "Hot Spot", confidence: 98),
            FishingGear(name: "Coho Killer Spoon", type: .lure, image: UIImage(systemName: "oval")!, color: "Blue/Silver", size: "3.5\"", brand: "Silver Horde", confidence: 95),
            FishingGear(name: "Ace Hi Fly", type: .lure, image: UIImage(systemName: "oval")!, color: "Purple", size: "Small", brand: "Ace", confidence: 92)
        ]
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
    
    static func == (lhs: FishingGear, rhs: FishingGear) -> Bool {
        return lhs.id == rhs.id
    }
}

enum GearType: String, CaseIterable {
    case flasher = "Flasher"
    case lure = "Lure"
    case leader = "Leader"
    case rod = "Rod"
    case reel = "Reel"
    case line = "Line"
    case hook = "Hook"
    case weight = "Weight"
    case other = "Other"
    
    var icon: String {
        switch self {
        case .flasher: return "circle"
        case .lure: return "oval"
        case .leader: return "line.diagonal"
        case .rod: return "arrow.up"
        case .reel: return "circle"
        case .line: return "line.horizontal.3"
        case .hook: return "curlybraces"
        case .weight: return "circle.fill"
        case .other: return "questionmark"
        }
    }
}

// Sample data
let sampleLocations = [
    SimpleLocation(id: "1", name: "Puget Sound", latitude: 47.6062, longitude: -122.3321),
    SimpleLocation(id: "2", name: "Hood Canal", latitude: 47.6792, longitude: -122.9006),
    SimpleLocation(id: "3", name: "San Juan Islands", latitude: 48.5513, longitude: -123.0781),
    SimpleLocation(id: "4", name: "Strait of Juan de Fuca", latitude: 48.2332, longitude: -124.1355)
]

struct ContentView: View {
    @State private var isLoggedIn = false
    @State private var username = ""
    @State private var password = ""
    @State private var showError = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                // App logo
                Image(systemName: "water.waves")
                    .font(.system(size: 60))
                    .foregroundColor(.blue)
                    .padding(.bottom, 20)
                
                Text("Salmon Trolling Assistant")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                Text("Your fishing companion")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .padding(.bottom, 40)
                
                // Login form
                TextField("Username", text: $username)
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                    .autocapitalization(.none)
                
                SecureField("Password", text: $password)
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                
                if showError {
                    Text("Invalid username or password")
                        .foregroundColor(.red)
                        .font(.caption)
                }
                
                Button(action: {
                    // For demo purposes, any non-empty username/password will work
                    if !username.isEmpty && !password.isEmpty {
                        isLoggedIn = true
                    } else {
                        showError = true
                    }
                }) {
                    Text("Log In")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(10)
                }
                .padding(.top, 20)
                
                // Demo login shortcut
                Button(action: {
                    username = "demo"
                    password = "password"
                    isLoggedIn = true
                }) {
                    Text("Quick Demo Login")
                        .foregroundColor(.blue)
                }
                .padding(.top, 10)
                
                Spacer()
                
                // Version info
                Text("Version 1.0")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding()
            .navigationBarHidden(true)
            .fullScreenCover(isPresented: $isLoggedIn) {
                AppTabView()
            }
        }
    }
}

// Shared app state
class AppState: ObservableObject {
    @Published var userGear: [FishingGear] = []
}

// LocationsView is now in a separate file

struct LocationPin: View {
    let location: SimpleLocation
    let action: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            Image(systemName: "mappin.circle.fill")
                .font(.title)
                .foregroundColor(.red)
            
            Image(systemName: "arrowtriangle.down.fill")
                .font(.caption)
                .foregroundColor(.red)
                .offset(x: 0, y: -5)
            
            Text(location.name)
                .font(.caption)
                .padding(5)
                .background(Color.white.opacity(0.8))
                .cornerRadius(5)
                .padding(.top, -5)
        }
        .onTapGesture {
            action()
        }
    }
}

struct LocationRow: View {
    let location: SimpleLocation
    
    var body: some View {
        HStack(spacing: 15) {
            Image(systemName: "mappin.circle.fill")
                .font(.title2)
                .foregroundColor(.red)
                .frame(width: 40, height: 40)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(location.name)
                    .font(.headline)
                
                Text("Lat: \(String(format: "%.4f", location.latitude)), Long: \(String(format: "%.4f", location.longitude))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 8)
    }
}

struct EnhancedForecastView: View {
    let location: SimpleLocation
    @State private var selectedDayIndex = 0
    @Environment(\.presentationMode) var presentationMode
    
    // Mock forecast data
    private let days = ["Today", "Tomorrow", "Wednesday", "Thursday", "Friday"]
    private let dates = ["Jul 21", "Jul 22", "Jul 23", "Jul 24", "Jul 25"]
    private let temperatures = [72, 68, 70, 75, 73]
    private let conditions = ["Sunny", "Partly Cloudy", "Cloudy", "Sunny", "Partly Cloudy"]
    private let windSpeeds = [8, 10, 12, 7, 9]
    private let windDirections = ["NW", "N", "NE", "E", "SE"]
    private let tideHeights = [3.2, 2.8, 3.5, 3.0, 2.5]
    private let tideTypes = ["Rising", "High", "Falling", "Low", "Rising"]
    private let precipChance = [10, 30, 60, 5, 20]
    
    // Hourly data for the selected day
    private let hourlyTimes = ["6AM", "9AM", "12PM", "3PM", "6PM", "9PM"]
    private let hourlyTemps = [
        [62, 68, 72, 70, 67, 64], // Today
        [60, 64, 68, 67, 65, 62], // Tomorrow
        [63, 66, 70, 69, 66, 63], // Wednesday
        [65, 70, 75, 73, 70, 67], // Thursday
        [64, 68, 73, 71, 68, 65]  // Friday
    ]
    private let hourlyConditions = [
        ["Sunny", "Sunny", "Sunny", "Sunny", "Partly Cloudy", "Clear"],
        ["Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Cloudy", "Cloudy", "Partly Cloudy"],
        ["Cloudy", "Cloudy", "Cloudy", "Cloudy", "Cloudy", "Cloudy"],
        ["Clear", "Sunny", "Sunny", "Sunny", "Sunny", "Clear"],
        ["Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Clear"]
    ]
    
    // Tide times for the selected day
    private let tideTimes = [
        ["4:32 AM (Low)", "10:45 AM (High)", "4:56 PM (Low)", "11:08 PM (High)"],
        ["5:18 AM (Low)", "11:30 AM (High)", "5:42 PM (Low)", "11:54 PM (High)"],
        ["6:04 AM (Low)", "12:15 PM (High)", "6:28 PM (Low)", ""],
        ["12:40 AM (High)", "6:50 AM (Low)", "1:00 PM (High)", "7:14 PM (Low)"],
        ["1:26 AM (High)", "7:36 AM (Low)", "1:45 PM (High)", "8:00 PM (Low)"]
    ]
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Day selector
                    VStack(spacing: 5) {
                        Picker("Day", selection: $selectedDayIndex) {
                            ForEach(0..<days.count, id: \.self) { index in
                                Text(days[index]).tag(index)
                            }
                        }
                        .pickerStyle(SegmentedPickerStyle())
                        
                        Text(dates[selectedDayIndex])
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal)
                    
                    // Daily summary card
                    VStack(alignment: .leading, spacing: 15) {
                        Text("Daily Summary")
                            .font(.headline)
                            .fontWeight(.bold)
                        
                        HStack(spacing: 20) {
                            // Weather icon
                            Image(systemName: weatherIcon(for: conditions[selectedDayIndex]))
                                .font(.system(size: 40))
                                .foregroundColor(.blue)
                                .frame(width: 60, height: 60)
                                .background(Color.blue.opacity(0.1))
                                .clipShape(Circle())
                            
                            VStack(alignment: .leading, spacing: 5) {
                                Text("\(temperatures[selectedDayIndex])°F")
                                    .font(.system(size: 32, weight: .bold))
                                
                                Text(conditions[selectedDayIndex])
                                    .font(.headline)
                                
                                Text("Precipitation: \(precipChance[selectedDayIndex])%")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                        }
                        
                        Divider()
                        
                        // Weather details
                        HStack {
                            VStack(alignment: .leading, spacing: 5) {
                                Text("Wind")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text("\(windSpeeds[selectedDayIndex]) mph \(windDirections[selectedDayIndex])")
                                    .font(.headline)
                            }
                            
                            Spacer()
                            
                            VStack(alignment: .leading, spacing: 5) {
                                Text("Humidity")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text("\(60 + selectedDayIndex * 5)%")
                                    .font(.headline)
                            }
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                    .padding(.horizontal)
                    
                    // Hourly forecast
                    VStack(alignment: .leading, spacing: 15) {
                        Text("Hourly Forecast")
                            .font(.headline)
                            .fontWeight(.bold)
                            .padding(.horizontal)
                        
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 20) {
                                ForEach(0..<hourlyTimes.count, id: \.self) { index in
                                    VStack(spacing: 8) {
                                        Text(hourlyTimes[index])
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                        
                                        Image(systemName: weatherIcon(for: hourlyConditions[selectedDayIndex][index]))
                                            .font(.system(size: 24))
                                            .foregroundColor(.blue)
                                        
                                        Text("\(hourlyTemps[selectedDayIndex][index])°")
                                            .font(.headline)
                                    }
                                    .frame(width: 60)
                                }
                            }
                            .padding()
                        }
                    }
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                    .padding(.horizontal)
                    
                    // Tide information
                    VStack(alignment: .leading, spacing: 15) {
                        Text("Tide")
                            .font(.headline)
                            .fontWeight(.bold)
                        
                        HStack(spacing: 20) {
                            // Tide icon
                            Image(systemName: tideIcon(for: tideTypes[selectedDayIndex]))
                                .font(.system(size: 30))
                                .foregroundColor(.blue)
                                .frame(width: 60, height: 60)
                                .background(Color.blue.opacity(0.1))
                                .clipShape(Circle())
                            
                            VStack(alignment: .leading, spacing: 5) {
                                Text("\(String(format: "%.1f", tideHeights[selectedDayIndex])) ft")
                                    .font(.system(size: 32, weight: .bold))
                                
                                Text(tideTypes[selectedDayIndex])
                                    .font(.headline)
                            }
                            
                            Spacer()
                        }
                        
                        Divider()
                        
                        // Tide times
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Tide Schedule")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            
                            ForEach(tideTimes[selectedDayIndex].indices, id: \.self) { index in
                                if !tideTimes[selectedDayIndex][index].isEmpty {
                                    HStack {
                                        Image(systemName: index % 2 == 0 ? "arrow.down" : "arrow.up")
                                            .foregroundColor(index % 2 == 0 ? .red : .blue)
                                        
                                        Text(tideTimes[selectedDayIndex][index])
                                            .font(.body)
                                        
                                        Spacer()
                                    }
                                }
                            }
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                    .padding(.horizontal)
                    
                    // Fishing recommendation
                    VStack(alignment: .leading, spacing: 15) {
                        Text("Fishing Conditions")
                            .font(.headline)
                            .fontWeight(.bold)
                        
                        HStack {
                            let rating = getFishingRating(dayIndex: selectedDayIndex)
                            ForEach(0..<5) { index in
                                Image(systemName: index < rating ? "star.fill" : "star")
                                    .foregroundColor(.yellow)
                            }
                            
                            Spacer()
                            
                            Text(getFishingRatingText(dayIndex: selectedDayIndex))
                                .font(.headline)
                                .foregroundColor(getFishingRatingColor(dayIndex: selectedDayIndex))
                        }
                        
                        Text(getFishingAdvice(dayIndex: selectedDayIndex))
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                    .padding(.horizontal)
                    
                    // See Recommended Gear Button
                    Button(action: {
                        // Dismiss this sheet
                        presentationMode.wrappedValue.dismiss()
                        
                        // We'll use NotificationCenter to communicate with the AppTabView
                        NotificationCenter.default.post(
                            name: Notification.Name("SwitchToRecommendationsTab"),
                            object: nil,
                            userInfo: ["location": location]
                        )
                    }) {
                        HStack {
                            Image(systemName: "star.fill")
                            Text("See Recommended Gear")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    Spacer()
                }
                .padding(.top)
            }
            .navigationBarTitle("\(location.name) Forecast", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    // Helper functions for fishing conditions
    private func getFishingRating(dayIndex: Int) -> Int {
        let ratings = [4, 3, 2, 5, 3]
        return ratings[dayIndex]
    }
    
    private func getFishingRatingText(dayIndex: Int) -> String {
        let ratings = ["Very Good", "Good", "Fair", "Excellent", "Good"]
        return ratings[dayIndex]
    }
    
    private func getFishingRatingColor(dayIndex: Int) -> Color {
        let rating = getFishingRating(dayIndex: dayIndex)
        switch rating {
        case 5:
            return .green
        case 4:
            return .blue
        case 3:
            return .orange
        default:
            return .red
        }
    }
    
    private func getFishingAdvice(dayIndex: Int) -> String {
        let advice = [
            "Morning and evening hours will be most productive. Focus on deeper waters during midday.",
            "Changing tide conditions may affect fish activity. Best fishing during incoming tide.",
            "Cloudy conditions might reduce visibility. Try brighter lures and flashers.",
            "Perfect conditions all day. Early morning will be especially productive.",
            "Good overall conditions. Watch for changing winds in the afternoon."
        ]
        return advice[dayIndex]
    }
}

// Create a coordinator class to handle notifications
class AppTabCoordinator: ObservableObject {
    @Published var selectedTab: Int = 0
    @Published var selectedLocation: SimpleLocation = sampleLocations[0]
    @Published var weatherConditions: String = "Partly Cloudy"
    @Published var windSpeed: Int = 8
    @Published var tideType: String = "Rising"
    
    init() {
        setupNotificationObserver()
    }
    
    func setupNotificationObserver() {
        NotificationCenter.default.addObserver(
            forName: Notification.Name("SwitchToRecommendationsTab"),
            object: nil,
            queue: .main) { [weak self] notification in
                guard let self = self else { return }
                
                if let location = notification.userInfo?["location"] as? SimpleLocation {
                    // Update the location and other parameters based on the selected location
                    self.selectedLocation = location
                    
                    // Get weather data for this location (in a real app, this would come from a service)
                    // For now, we'll use mock data based on the location name's hash
                    let hash = abs(location.name.hashValue)
                    let conditions = ["Sunny", "Partly Cloudy", "Cloudy", "Clear", "Rainy"]
                    let tides = ["Rising", "High", "Falling", "Low"]
                    
                    self.weatherConditions = conditions[hash % conditions.count]
                    self.windSpeed = 5 + (hash % 15) // Wind speed between 5-20 mph
                    self.tideType = tides[hash % tides.count]
                    
                    // Switch to the recommendations tab
                    self.selectedTab = 2
                }
            }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

struct AppTabView: View {
    @StateObject private var appState = AppState()
    @StateObject private var coordinator = AppTabCoordinator()
    
    var body: some View {
        TabView(selection: $coordinator.selectedTab) {
            // Map & Locations Tab
            MapView()
                .tabItem {
                    Label("Locations", systemImage: "map")
                }
                .tag(0)
            
            // My Gear Tab
            MyGearView(userGear: $appState.userGear)
                .tabItem {
                    Label("My Gear", systemImage: "camera")
                }
                .tag(1)
            
            // Recommendations Tab
            RecommendationsView(userGear: appState.userGear, 
                         selectedLocation: coordinator.selectedLocation,
                         weatherConditions: coordinator.weatherConditions,
                         windSpeed: coordinator.windSpeed,
                         tideType: coordinator.tideType)
                .tabItem {
                    Label("Recommendations", systemImage: "star")
                }
                .tag(2)
            
            // Settings Tab
            Text("Settings")
                .font(.largeTitle)
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
                .tag(3)
        }
        .environmentObject(appState)
    }
}

// Helper functions
private func weatherIcon(for condition: String) -> String {
    let condition = condition.lowercased()
    if condition.contains("clear") || condition.contains("sunny") {
        return "sun.max"
    } else if condition.contains("partly cloudy") {
        return "cloud.sun"
    } else if condition.contains("cloudy") {
        return "cloud"
    } else if condition.contains("rain") {
        return "cloud.rain"
    } else if condition.contains("thunder") {
        return "cloud.bolt"
    } else if condition.contains("snow") {
        return "cloud.snow"
    } else {
        return "cloud"
    }
}

private func tideIcon(for tideType: String) -> String {
    let type = tideType.lowercased()
    if type.contains("rising") {
        return "arrow.up.right"
    } else if type.contains("high") {
        return "arrow.up"
    } else if type.contains("falling") {
        return "arrow.down.right"
    } else if type.contains("low") {
        return "arrow.down"
    } else {
        return "circle"
    }
}

struct MapView: View {
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    @State private var showingForecast = false
    @State private var selectedLocation: SimpleLocation?
    @State private var pins: [SimpleLocation] = sampleLocations
    @State private var newPinName: String = ""
    @State private var showingNamePrompt = false
    
    var body: some View {
        NavigationView {
            ZStack {
                ZStack {
                    Map(coordinateRegion: $region, annotationItems: pins) { pin in
                        MapAnnotation(coordinate: CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude)) {
                            VStack {
                                ZStack {
                                    // Fish body
                                    Image(systemName: "fish.fill")
                                        .font(.title)
                                        .foregroundColor(.red)
                                        .rotationEffect(.degrees(-10))
                                    
                                    // Small bubble for effect
                                    Circle()
                                        .fill(Color.white.opacity(0.7))
                                        .frame(width: 6, height: 6)
                                        .offset(x: -10, y: -5)
                                }
                                
                                Text(pin.name)
                                    .font(.caption)
                                    .padding(5)
                                    .background(Color.white.opacity(0.8))
                                    .cornerRadius(5)
                            }
                            .onTapGesture {
                                selectedLocation = pin
                                showingForecast = true
                            }
                        }
                    }
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                // This empty gesture handler allows the map's native panning to work
                                // by preventing other gestures from capturing the drag events
                            }
                    )
                    
                    // Long press gesture for adding new pins
                    // Using a separate overlay to not interfere with map panning
                    Color.clear
                        .contentShape(Rectangle())
                        .allowsHitTesting(false) // This allows touches to pass through to the map
                        .onLongPressGesture(minimumDuration: 0.5) {
                            // Show prompt to add a new pin at the current map center
                            showingNamePrompt = true
                        }
                }
                
                VStack {
                    Spacer()
                    
                    // Tip banner
                    Text("Long press on map to add a new location")
                        .font(.caption)
                        .padding(8)
                        .background(Color.black.opacity(0.7))
                        .foregroundColor(.white)
                        .cornerRadius(20)
                        .padding(.bottom, 10)
                    
                    HStack {
                        Spacer()
                        
                        Button(action: {
                            // Get current location
                            region = MKCoordinateRegion(
                                center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
                                span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                            )
                        }) {
                            Image(systemName: "location")
                                .padding()
                                .background(Color.white)
                                .clipShape(Circle())
                                .shadow(radius: 2)
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Fishing Locations")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingNamePrompt = true
                    }) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
        .sheet(isPresented: $showingForecast) {
            if let location = selectedLocation {
                ForecastView(location: location)
            }
        }
        .alert("Add Location", isPresented: $showingNamePrompt) {
            TextField("Location name", text: $newPinName)
            Button("Cancel", role: .cancel) {
                newPinName = ""
            }
            Button("Add") {
                let newPin = SimpleLocation(
                    id: UUID().uuidString,
                    name: newPinName.isEmpty ? "New Location" : newPinName,
                    latitude: region.center.latitude,
                    longitude: region.center.longitude
                )
                pins.append(newPin)
                newPinName = ""
            }
        } message: {
            Text("Enter a name for this location")
        }
    }
}

// MyGearView is now imported from MyGearView.swift

struct RecommendationsView: View {
    let userGear: [FishingGear]
    let selectedLocation: SimpleLocation
    let weatherConditions: String
    let windSpeed: Int
    let tideType: String
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Location and conditions summary
                VStack(spacing: 5) {
                    Text(selectedLocation.name)
                        .font(.headline)
                    
                    HStack(spacing: 15) {
                        Label(weatherConditions, systemImage: weatherIcon(for: weatherConditions))
                            .font(.caption)
                        
                        Label("\(windSpeed) mph", systemImage: "wind")
                            .font(.caption)
                        
                        Label(tideType, systemImage: tideIcon(for: tideType))
                            .font(.caption)
                    }
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color(.secondarySystemBackground))
                
                // Recommendations list
                List {
                    Section(header: Text("Recommended Flashers")) {
                        recommendationRow(
                            name: "Hot Spot Flasher - UV Green",
                            description: "Perfect for today's light conditions",
                            matchScore: 95,
                            icon: "circle"
                        )
                        
                        recommendationRow(
                            name: "Pro-Troll Flasher - UV Purple",
                            description: "Good alternative in current conditions",
                            matchScore: 87,
                            icon: "circle"
                        )
                    }
                    
                    Section(header: Text("Recommended Lures")) {
                        recommendationRow(
                            name: "Coho Killer - Green Pirate",
                            description: "Top choice for today",
                            matchScore: 92,
                            icon: "oval"
                        )
                        
                        recommendationRow(
                            name: "Ace Hi Fly - Blue/Silver",
                            description: "Good choice with current conditions",
                            matchScore: 85,
                            icon: "oval"
                        )
                    }
                    
                    Section(header: Text("Recommended Leaders")) {
                        recommendationRow(
                            name: "Fluorocarbon Leader - 40lb test",
                            description: "Optimal leader for today's conditions",
                            matchScore: 90,
                            icon: "line.diagonal"
                        )
                    }
                }
                .listStyle(InsetGroupedListStyle())
            }
            .navigationTitle("Recommendations")
        }
    }
    
    private func recommendationRow(name: String, description: String, matchScore: Int, icon: String) -> some View {
        HStack(spacing: 15) {
            // Icon with match score
            ZStack {
                Circle()
                    .fill(matchScoreColor(for: matchScore).opacity(0.2))
                    .frame(width: 50, height: 50)
                
                Image(systemName: icon)
                    .font(.system(size: 24))
                    .foregroundColor(matchScoreColor(for: matchScore))
                
                // Match score indicator
                Text("\(matchScore)%")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
                    .padding(4)
                    .background(matchScoreColor(for: matchScore))
                    .clipShape(Circle())
                    .offset(x: 15, y: -15)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(name)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            // Indicator if user has this gear
            if userGear.contains(where: { $0.name.contains(name.split(separator: " ").first ?? "") }) {
                Image(systemName: "checkmark.circle")
                    .foregroundColor(.green)
                    .font(.title3)
            }
        }
        .padding(.vertical, 8)
    }
    
    private func matchScoreColor(for score: Int) -> Color {
        if score >= 90 {
            return .green
        } else if score >= 80 {
            return .blue
        } else if score >= 70 {
            return .orange
        } else {
            return .red
        }
    }
}

struct ForecastView: View {
    let location: SimpleLocation
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                // Location header with fish icon
                HStack {
                    Image(systemName: "fish.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.blue)
                        .rotationEffect(.degrees(-10))
                    
                    Text(location.name)
                        .font(.title2)
                        .fontWeight(.bold)
                }
                .padding(.top)
                
                // Weather card
                VStack(alignment: .leading, spacing: 15) {
                    Text("Weather")
                        .font(.headline)
                        .fontWeight(.bold)
                    
                    HStack(spacing: 20) {
                        // Weather icon
                        Image(systemName: "sun.max")
                            .font(.system(size: 40))
                            .foregroundColor(.blue)
                            .frame(width: 60, height: 60)
                            .background(Color.blue.opacity(0.1))
                            .clipShape(Circle())
                        
                        VStack(alignment: .leading, spacing: 5) {
                            Text("72°F")
                                .font(.system(size: 32, weight: .bold))
                            
                            Text("Sunny")
                                .font(.headline)
                            
                            Text("Precipitation: 10%")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        
                        Spacer()
                    }
                    
                    Divider()
                    
                    // Weather details
                    HStack {
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Wind")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("8 mph NW")
                                .font(.headline)
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Humidity")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("60%")
                                .font(.headline)
                        }
                    }
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                .padding(.horizontal)
                
                // Tide card
                VStack(alignment: .leading, spacing: 15) {
                    Text("Tide")
                        .font(.headline)
                        .fontWeight(.bold)
                    
                    HStack(spacing: 20) {
                        // Tide icon
                        Image(systemName: "arrow.up.right.circle.fill")
                            .font(.system(size: 30))
                            .foregroundColor(.blue)
                            .frame(width: 60, height: 60)
                            .background(Color.blue.opacity(0.1))
                            .clipShape(Circle())
                        
                        VStack(alignment: .leading, spacing: 5) {
                            Text("3.2 ft")
                                .font(.system(size: 32, weight: .bold))
                            
                            Text("Rising")
                                .font(.headline)
                        }
                        
                        Spacer()
                    }
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                .padding(.horizontal)
                
                // Fishing conditions
                VStack(alignment: .leading, spacing: 15) {
                    Text("Fishing Conditions")
                        .font(.headline)
                        .fontWeight(.bold)
                    
                    HStack {
                        ForEach(0..<4) { index in
                            Image(systemName: "star.fill")
                                .foregroundColor(.yellow)
                        }
                        Image(systemName: "star")
                            .foregroundColor(.yellow)
                        
                        Spacer()
                        
                        Text("Very Good")
                            .font(.headline)
                            .foregroundColor(.blue)
                    }
                    
                    Text("Morning and evening hours will be most productive. Focus on deeper waters during midday.")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(10)
                .padding(.horizontal)
                
                Spacer()
            }
            .padding(.top)
            .navigationBarTitle("\(location.name) Forecast", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}
struct TabSelectorView: View {
    @Binding var selectedTab: Int
    
    var body: some View {
        HStack(spacing: 0) {
            Button(action: {
                selectedTab = 0
            }) {
                Text("Map")
                    .padding(.vertical, 6)
                    .padding(.horizontal, 12)
                    .background(selectedTab == 0 ? Color.blue : Color.clear)
                    .foregroundColor(selectedTab == 0 ? .white : .blue)
                    .cornerRadius(8)
            }
            
            Button(action: {
                selectedTab = 1
            }) {
                Text("List")
                    .padding(.vertical, 6)
                    .padding(.horizontal, 12)
                    .background(selectedTab == 1 ? Color.blue : Color.clear)
                    .foregroundColor(selectedTab == 1 ? .white : .blue)
                    .cornerRadius(8)
            }
        }
        .background(Color(.secondarySystemBackground))
        .cornerRadius(8)
        .frame(width: 150)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
