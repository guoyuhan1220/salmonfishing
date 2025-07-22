import SwiftUI
import MapKit
import CoreLocation
import PhotosUI
import Combine

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

// Extension for safe array access
extension Array {
    subscript(safe index: Int) -> Element? {
        return indices.contains(index) ? self[index] : nil
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
            FishingGear(name: "Hot Spot Flasher", type: .flasher, image: UIImage(systemName: "circle.hexagongrid.fill")!, color: "Green", size: "Standard", brand: "Hot Spot", confidence: 98),
            FishingGear(name: "Coho Killer Spoon", type: .lure, image: UIImage(systemName: "fish.fill")!, color: "Blue/Silver", size: "3.5\"", brand: "Silver Horde", confidence: 95),
            FishingGear(name: "Ace Hi Fly", type: .lure, image: UIImage(systemName: "fish.fill")!, color: "Purple", size: "Small", brand: "Ace", confidence: 92)
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
        case .flasher: return "circle.hexagongrid.fill"
        case .lure: return "fish.fill"
        case .leader: return "line.diagonal"
        case .rod: return "arrow.up.to.line"
        case .reel: return "dial.min"
        case .line: return "circle.dashed"
        case .hook: return "curlybraces"
        case .weight: return "scalemass.fill"
        case .other: return "questionmark.circle"
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
                    .font(.system(size: 80))
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

struct AppTabView: View {
    @StateObject private var appState = AppState()
    
    var body: some View {
        TabView {
            // Map & Locations Tab (Combined)
            MapView()
                .tabItem {
                    Label("Locations", systemImage: "map.fill")
                }
                .tag(0)
            
            // My Gear Tab
            GearScanView(userGear: $appState.userGear)
                .tabItem {
                    Label("My Gear", systemImage: "camera.fill")
                }
                .tag(1)
            
            // Recommendations Tab
            SmartRecsView(userGear: appState.userGear, 
                         selectedLocation: sampleLocations[0],
                         weatherConditions: "Partly Cloudy",
                         windSpeed: 8,
                         tideType: "Rising")
                .tabItem {
                    Label("Smart Recs", systemImage: "wand.and.stars")
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

// Weather icon helper function
private func weatherIcon(for condition: String) -> String {
    let condition = condition.lowercased()
    if condition.contains("clear") || condition.contains("sunny") {
        return "sun.max.fill"
    } else if condition.contains("partly cloudy") {
        return "cloud.sun.fill"
    } else if condition.contains("cloudy") {
        return "cloud.fill"
    } else if condition.contains("rain") {
        return "cloud.rain.fill"
    } else if condition.contains("thunder") {
        return "cloud.bolt.fill"
    } else if condition.contains("snow") {
        return "cloud.snow.fill"
    } else {
        return "cloud.fill"
    }
}

// Tide icon helper function
private func tideIcon(for tideType: String) -> String {
        let type = tideType.lowercased()
        if type.contains("rising") {
            return "arrow.up.right.circle.fill"
        } else if type.contains("high") {
            return "arrow.up.circle.fill"
        } else if type.contains("falling") {
            return "arrow.down.right.circle.fill"
        } else if type.contains("low") {
            return "arrow.down.circle.fill"
        } else {
            return "circle.fill"
        }
    }


struct MapView: View {
    @State private var viewMode: ViewMode = .map
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    @State private var showingAlert = false
    @State private var showingForecast = false
    @State private var selectedLocation: SimpleLocation?
    @State private var pins: [SimpleLocation] = sampleLocations
    @State private var newPinCoordinate: CLLocationCoordinate2D?
    @State private var newPinName: String = ""
    @State private var showingNamePrompt = false
    @State private var searchText = ""
    @State private var appTabIndex = 0
    
    enum ViewMode {
        case map
        case list
    }
    
    var body: some View {
        NavigationView {
            ZStack(alignment: .top) {
                // Main content
                if viewMode == .map {
                    mapView
                } else {
                    listView
                }
                
                // Floating view mode selector
                VStack {
                    HStack {
                        Picker("View Mode", selection: $viewMode) {
                            Image(systemName: "map").tag(ViewMode.map)
                            Image(systemName: "list.bullet").tag(ViewMode.list)
                        }
                        .pickerStyle(SegmentedPickerStyle())
                        .frame(width: 120)
                        .background(Color.white.opacity(0.9))
                        .cornerRadius(8)
                        .shadow(radius: 2)
                    }
                    .padding(.top, 8)
                    
                    Spacer()
                }
            }
            .navigationTitle("Fishing Spots")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        // Add new location
                        showingNamePrompt = true
                    }) {
                        Image(systemName: "plus")
                    }
                }
                
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        // Get current location
                        region = MKCoordinateRegion(
                            center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
                            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                        )
                    }) {
                        Image(systemName: "location")
                    }
                }
            }
        }
        .sheet(isPresented: $showingForecast) {
            if let location = selectedLocation {
                ForecastView(location: location, selectedTabIndex: $appTabIndex)
            }
        }
        .overlay(
            Group {
                if showingNamePrompt {
                    // Custom text field alert
                    ZStack {
                        Color.black.opacity(0.4)
                            .edgesIgnoringSafeArea(.all)
                            .onTapGesture {
                                showingNamePrompt = false
                            }
                        
                        VStack(spacing: 20) {
                            Text("Name this location")
                                .font(.headline)
                            
                            TextField("Location name", text: $newPinName)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                                .padding(.horizontal)
                            
                            HStack {
                                Button("Cancel") {
                                    showingNamePrompt = false
                                    newPinName = ""
                                }
                                .foregroundColor(.red)
                                
                                Spacer()
                                
                                Button("Save") {
                                    // Add new pin
                                    let newPin = SimpleLocation(
                                        id: UUID().uuidString,
                                        name: newPinName.isEmpty ? "New Location" : newPinName,
                                        latitude: region.center.latitude,
                                        longitude: region.center.longitude
                                    )
                                    pins.append(newPin)
                                    selectedLocation = newPin
                                    showingNamePrompt = false
                                    newPinName = ""
                                    
                                    // Show forecast for new pin
                                    showingForecast = true
                                }
                                .foregroundColor(.blue)
                            }
                            .padding(.horizontal)
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(15)
                        .shadow(radius: 10)
                        .padding(30)
                    }
                }
            }
        )
    }
    
    private var mapView: some View {
        ZStack {
            // Map with pins
            Map(coordinateRegion: $region, annotationItems: pins) { pin in
                MapAnnotation(coordinate: CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude)) {
                    VStack {
                        Image(systemName: "mappin.circle.fill")
                            .font(.title)
                            .foregroundColor(.red)
                        
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
            .ignoresSafeArea()
            .gesture(
                LongPressGesture(minimumDuration: 0.5)
                    .sequenced(before: DragGesture(minimumDistance: 0))
                    .onEnded { value in
                        switch value {
                        case .second(true, let drag):
                            if let location = drag?.location {
                                // Convert screen point to map coordinate
                                let coordinate = convertToCoordinate(location)
                                newPinCoordinate = coordinate
                                showingNamePrompt = true
                            }
                        default:
                            break
                        }
                    }
            )
            
            // Floating controls
            VStack {
                Spacer()
                
                HStack {
                    Spacer()
                    
                    Button(action: {
                        // Get current location
                        region = MKCoordinateRegion(
                            center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
                            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                        )
                    }) {
                        Image(systemName: "location.fill")
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                            .background(Color.blue)
                            .clipShape(Circle())
                            .shadow(radius: 3)
                    }
                    .padding(.trailing)
                }
                .padding(.bottom, 60)
                
                // Instructions overlay
                Text("Long press to drop a pin")
                    .font(.caption)
                    .padding(8)
                    .background(Color.white.opacity(0.8))
                    .cornerRadius(8)
                    .padding(.bottom)
            }
        }
    }
    
    private var listView: some View {
        VStack {
            // Search bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                
                TextField("Search locations", text: $searchText)
                
                if !searchText.isEmpty {
                    Button(action: {
                        searchText = ""
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(10)
            .background(Color(.systemGray6))
            .cornerRadius(10)
            .padding(.horizontal)
            
            // Location list
            List {
                let filteredPins = searchText.isEmpty ? pins : pins.filter { $0.name.lowercased().contains(searchText.lowercased()) }
                
                ForEach(filteredPins, id: \.id) { pin in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(pin.name)
                            .font(.headline)
                        
                        HStack {
                            Image(systemName: "mappin")
                                .foregroundColor(.red)
                                .font(.caption)
                            
                            Text("\(pin.latitude.formatted(.number.precision(.fractionLength(4)))), \(pin.longitude.formatted(.number.precision(.fractionLength(4))))")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            Image(systemName: "cloud.sun.fill")
                                .foregroundColor(.blue)
                                .font(.caption)
                            
                            Text("Tap to view forecast")
                                .font(.caption)
                                .foregroundColor(.blue)
                        }
                        .padding(.top, 4)
                    }
                    .padding(.vertical, 4)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        selectedLocation = pin
                        showingForecast = true
                    }
                }
                .onDelete { indexSet in
                    pins.remove(atOffsets: indexSet)
                }
            }
        }
    }
    
    private func convertToCoordinate(_ point: CGPoint) -> CLLocationCoordinate2D {
        let mapSize = UIScreen.main.bounds.size
        
        let normalizedPoint = CGPoint(
            x: point.x / mapSize.width,
            y: point.y / mapSize.height
        )
        
        let span = region.span
        let center = region.center
        
        let xOffset = span.longitudeDelta * (normalizedPoint.x - 0.5)
        let yOffset = span.latitudeDelta * (0.5 - normalizedPoint.y)
        
        return CLLocationCoordinate2D(
            latitude: center.latitude + yOffset,
            longitude: center.longitude + xOffset
        )
    }
}

struct ForecastView: View {
    let location: SimpleLocation
    @State private var selectedDayIndex = 0
    @State private var selectedTab = 0 // 0: Overview, 1: Hourly
    @Environment(\.presentationMode) var presentationMode
    @Binding var selectedTabIndex: Int
    
    // Mock forecast data
    private let days = ["Today", "Tomorrow", "Wednesday", "Thursday", "Friday"]
    private let temperatures = [72, 68, 70, 75, 73]
    private let conditions = ["Sunny", "Partly Cloudy", "Cloudy", "Sunny", "Partly Cloudy"]
    private let windSpeeds = [8, 10, 12, 7, 9]
    private let windDirections = ["NW", "N", "NE", "E", "SE"]
    private let tideHeights = [3.2, 2.8, 3.5, 3.0, 2.5]
    private let tideTypes = ["Rising", "High", "Falling", "Low", "Rising"]
    private let tideIcons = ["arrow.up.right.circle.fill", "arrow.up.circle.fill", "arrow.down.right.circle.fill", "arrow.down.circle.fill", "arrow.up.right.circle.fill"]
    
    init(location: SimpleLocation, selectedTabIndex: Binding<Int> = .constant(0)) {
        self.location = location
        self._selectedTabIndex = selectedTabIndex
    }
    
    // Hourly data
    private let hours = ["6 AM", "9 AM", "12 PM", "3 PM", "6 PM", "9 PM"]
    private let hourlyTemps = [
        [64, 68, 72, 71, 69, 65], // Today
        [60, 64, 68, 67, 65, 62], // Tomorrow
        [62, 66, 70, 69, 67, 64], // Wednesday
        [66, 70, 75, 74, 71, 68], // Thursday
        [65, 69, 73, 72, 70, 67]  // Friday
    ]
    private let hourlyConditions = [
        ["Sunny", "Sunny", "Sunny", "Sunny", "Partly Cloudy", "Clear"],
        ["Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Cloudy", "Cloudy"],
        ["Cloudy", "Cloudy", "Cloudy", "Partly Cloudy", "Partly Cloudy", "Clear"],
        ["Sunny", "Sunny", "Sunny", "Sunny", "Clear", "Clear"],
        ["Partly Cloudy", "Partly Cloudy", "Sunny", "Sunny", "Partly Cloudy", "Clear"]
    ]
    private let hourlyWindSpeeds = [
        [5, 7, 8, 8, 6, 4], // Today
        [7, 9, 10, 10, 8, 6], // Tomorrow
        [9, 11, 12, 12, 10, 8], // Wednesday
        [4, 6, 7, 7, 5, 3], // Thursday
        [6, 8, 9, 9, 7, 5]  // Friday
    ]
    private let hourlyWindDirections = [
        ["NW", "NW", "NW", "NW", "NW", "NW"],
        ["N", "N", "N", "N", "N", "N"],
        ["NE", "NE", "NE", "NE", "NE", "NE"],
        ["E", "E", "E", "E", "E", "E"],
        ["SE", "SE", "SE", "SE", "SE", "SE"]
    ]
    private let hourlyTideHeights = [
        [2.5, 3.0, 3.2, 3.1, 2.8, 2.4], // Today
        [2.0, 2.5, 2.8, 2.7, 2.4, 2.0], // Tomorrow
        [2.8, 3.2, 3.5, 3.4, 3.1, 2.7], // Wednesday
        [2.3, 2.7, 3.0, 2.9, 2.6, 2.2], // Thursday
        [1.8, 2.2, 2.5, 2.4, 2.1, 1.7]  // Friday
    ]
    private let hourlyTideTypes = [
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"]
    ]
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Location header
                HStack {
                    VStack(alignment: .leading) {
                        Text(location.name)
                            .font(.title)
                            .fontWeight(.bold)
                        
                        Text("5-Day Forecast")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }
                .padding()
                
                // Day selector
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 15) {
                        ForEach(0..<5) { index in
                            Button(action: {
                                selectedDayIndex = index
                            }) {
                                VStack {
                                    Text(days[index])
                                        .font(.caption)
                                    
                                    Text("\(Calendar.current.component(.day, from: Date().addingTimeInterval(TimeInterval(86400 * index))))")
                                        .font(.title3)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 8)
                                .padding(.horizontal, 12)
                                .background(selectedDayIndex == index ? Color.blue : Color.clear)
                                .foregroundColor(selectedDayIndex == index ? .white : .primary)
                                .cornerRadius(10)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                
                // Tab selector
                Picker("View", selection: $selectedTab) {
                    Text("Overview").tag(0)
                    Text("Hourly").tag(1)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                ScrollView {
                    if selectedTab == 0 {
                        // Overview tab
                        VStack(alignment: .leading, spacing: 20) {
                            // Weather card
                            VStack(alignment: .leading, spacing: 15) {
                                HStack {
                                    Text("Weather")
                                        .font(.headline)
                                    Spacer()
                                }
                                
                                HStack(spacing: 20) {
                                    // Weather icon
                                    Image(systemName: weatherIcon(for: conditions[selectedDayIndex]))
                                        .font(.system(size: 50))
                                        .foregroundColor(.blue)
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text("\(temperatures[selectedDayIndex])°F")
                                            .font(.system(size: 36, weight: .bold))
                                        
                                        Text(conditions[selectedDayIndex])
                                            .font(.headline)
                                    }
                                    
                                    Spacer()
                                }
                                
                                Divider()
                                
                                // Weather details
                                HStack {
                                    VStack(alignment: .leading, spacing: 5) {
                                        HStack {
                                            Image(systemName: "wind")
                                            Text("Wind")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                        Text("\(windSpeeds[selectedDayIndex]) mph \(windDirections[selectedDayIndex])")
                                            .font(.headline)
                                    }
                                    
                                    Spacer()
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        HStack {
                                            Image(systemName: "humidity")
                                            Text("Humidity")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                        Text("\(60 + selectedDayIndex * 5)%")
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
                                HStack {
                                    Text("Tide")
                                        .font(.headline)
                                    Spacer()
                                }
                                
                                HStack(spacing: 20) {
                                    // Tide icon
                                    Image(systemName: tideIcons[selectedDayIndex])
                                        .font(.system(size: 50))
                                        .foregroundColor(.blue)
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text("\(String(format: "%.1f", tideHeights[selectedDayIndex])) ft")
                                            .font(.system(size: 36, weight: .bold))
                                        
                                        Text(tideTypes[selectedDayIndex])
                                            .font(.headline)
                                    }
                                    
                                    Spacer()
                                }
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                            
                            // Fishing conditions summary
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Fishing Conditions")
                                    .font(.headline)
                                
                                HStack {
                                    Image(systemName: "fish.fill")
                                        .foregroundColor(.green)
                                    Text("Good conditions for Coho and Chinook")
                                        .font(.subheadline)
                                }
                                
                                Text("Rising tide with moderate winds creates favorable conditions for salmon trolling.")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                            
                            // Smart Recommendations button
                            Button(action: {
                                // Navigate to Smart Recommendations tab (index 2)
                                selectedTabIndex = 2
                                presentationMode.wrappedValue.dismiss()
                            }) {
                                HStack {
                                    Image(systemName: "wand.and.stars")
                                        .font(.title2)
                                    Text("Get Smart Recommendations")
                                        .fontWeight(.semibold)
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }
                            .padding(.horizontal)
                            .padding(.top, 10)
                        }
                    } else {
                        // Hourly tab
                        VStack(alignment: .leading, spacing: 20) {
                            // Hourly weather forecast
                            VStack(alignment: .leading, spacing: 15) {
                                Text("Hourly Weather")
                                    .font(.headline)
                                    .padding(.horizontal)
                                
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 20) {
                                        ForEach(0..<hours.count, id: \.self) { hourIndex in
                                            VStack(spacing: 10) {
                                                Text(hours[hourIndex])
                                                    .font(.caption)
                                                    .foregroundColor(.secondary)
                                                
                                                Image(systemName: weatherIcon(for: hourlyConditions[selectedDayIndex][hourIndex]))
                                                    .font(.system(size: 24))
                                                    .foregroundColor(.blue)
                                                
                                                Text("\(hourlyTemps[selectedDayIndex][hourIndex])°F")
                                                    .font(.headline)
                                                
                                                HStack(spacing: 2) {
                                                    Image(systemName: "wind")
                                                        .font(.caption)
                                                    Text("\(hourlyWindSpeeds[selectedDayIndex][hourIndex])")
                                                        .font(.caption)
                                                }
                                            }
                                            .frame(width: 70)
                                            .padding(.vertical)
                                            .background(Color(.secondarySystemBackground))
                                            .cornerRadius(10)
                                        }
                                    }
                                    .padding(.horizontal)
                                }
                            }
                            
                            // Hourly wind details
                            VStack(alignment: .leading, spacing: 15) {
                                Text("Wind Details")
                                    .font(.headline)
                                    .padding(.horizontal)
                                
                                VStack(spacing: 15) {
                                    ForEach(0..<hours.count, id: \.self) { hourIndex in
                                        HStack {
                                            Text(hours[hourIndex])
                                                .font(.subheadline)
                                                .frame(width: 60, alignment: .leading)
                                            
                                            Image(systemName: "wind")
                                                .foregroundColor(.blue)
                                            
                                            Text("\(hourlyWindSpeeds[selectedDayIndex][hourIndex]) mph")
                                                .font(.subheadline)
                                            
                                            Text(hourlyWindDirections[selectedDayIndex][hourIndex])
                                                .font(.subheadline)
                                                .foregroundColor(.secondary)
                                            
                                            Spacer()
                                            
                                            // Wind direction arrow
                                            Image(systemName: "arrow.up")
                                                .rotationEffect(windDirectionAngle(for: hourlyWindDirections[selectedDayIndex][hourIndex]))
                                                .foregroundColor(.blue)
                                        }
                                        .padding(.horizontal)
                                        
                                        if hourIndex < hours.count - 1 {
                                            Divider()
                                                .padding(.horizontal)
                                        }
                                    }
                                }
                                .padding(.vertical)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(10)
                                .padding(.horizontal)
                            }
                            
                            // Hourly tide details
                            VStack(alignment: .leading, spacing: 15) {
                                Text("Tide Details")
                                    .font(.headline)
                                    .padding(.horizontal)
                                
                                VStack(spacing: 15) {
                                    ForEach(0..<hours.count, id: \.self) { hourIndex in
                                        HStack {
                                            Text(hours[hourIndex])
                                                .font(.subheadline)
                                                .frame(width: 60, alignment: .leading)
                                            
                                            Image(systemName: tideIcon(for: hourlyTideTypes[selectedDayIndex][hourIndex]))
                                                .foregroundColor(.blue)
                                            
                                            Text("\(String(format: "%.1f", hourlyTideHeights[selectedDayIndex][hourIndex])) ft")
                                                .font(.subheadline)
                                            
                                            Spacer()
                                            
                                            Text(hourlyTideTypes[selectedDayIndex][hourIndex])
                                                .font(.subheadline)
                                                .foregroundColor(.secondary)
                                        }
                                        .padding(.horizontal)
                                        
                                        if hourIndex < hours.count - 1 {
                                            Divider()
                                                .padding(.horizontal)
                                        }
                                    }
                                }
                                .padding(.vertical)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(10)
                                .padding(.horizontal)
                            }
                            
                            // Smart Recommendations button
                            Button(action: {
                                // Navigate to Smart Recommendations tab (index 2)
                                selectedTabIndex = 2
                                presentationMode.wrappedValue.dismiss()
                            }) {
                                HStack {
                                    Image(systemName: "wand.and.stars")
                                        .font(.title2)
                                    Text("Get Smart Recommendations")
                                        .fontWeight(.semibold)
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }
                            .padding(.horizontal)
                            .padding(.top, 10)
                        }
                        .padding(.bottom)
                    }
                }
            }
            .navigationBarTitle("Forecast", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    private func weatherIcon(for condition: String) -> String {
        let condition = condition.lowercased()
        if condition.contains("clear") || condition.contains("sunny") {
            return "sun.max.fill"
        } else if condition.contains("partly cloudy") {
            return "cloud.sun.fill"
        } else if condition.contains("cloudy") {
            return "cloud.fill"
        } else if condition.contains("rain") {
            return "cloud.rain.fill"
        } else if condition.contains("thunder") {
            return "cloud.bolt.fill"
        } else if condition.contains("snow") {
            return "cloud.snow.fill"
        } else {
            return "cloud.fill"
        }
    }
    
    private func tideIcon(for tideType: String) -> String {
        let type = tideType.lowercased()
        if type.contains("rising") {
            return "arrow.up.right.circle.fill"
        } else if type.contains("high") {
            return "arrow.up.circle.fill"
        } else if type.contains("falling") {
            return "arrow.down.right.circle.fill"
        } else if type.contains("low") {
            return "arrow.down.circle.fill"
        } else {
            return "circle.fill"
        }
    }
    
    private func windDirectionAngle(for direction: String) -> Angle {
        switch direction {
        case "N": return .degrees(0)
        case "NE": return .degrees(45)
        case "E": return .degrees(90)
        case "SE": return .degrees(135)
        case "S": return .degrees(180)
        case "SW": return .degrees(225)
        case "W": return .degrees(270)
        case "NW": return .degrees(315)
        default: return .degrees(0)
        }
    }
}

struct RecommendedItem: Identifiable, Hashable {
    let id = UUID()
    let name: String
    let image: String // System image name
    let description: String
    let confidenceScore: Int
}

struct ContentRecommendationsView: View {
    // Mock recommendation data
    private let categories = ["Flashers", "Lures", "Leaders"]
    
    private let recommendations: [[RecommendedItem]] = [
        // Flashers
        [
            RecommendedItem(
                name: "Hot Spot Flasher - UV Green",
                image: "circle.hexagongrid.fill",
                description: "Bright flashers work well in current water clarity and light conditions",
                confidenceScore: 85
            ),
            RecommendedItem(
                name: "Silver Horde Kingfisher - Chrome/Blue",
                image: "circle.hexagongrid.fill",
                description: "Reflective surface creates optimal flash in deeper waters",
                confidenceScore: 82
            ),
            RecommendedItem(
                name: "Pro-Troll Flasher - UV Purple",
                image: "circle.hexagongrid.fill",
                description: "UV reactive colors perform well in low light conditions",
                confidenceScore: 78
            )
        ],
        // Lures
        [
            RecommendedItem(
                name: "Coho Killer - Green Pirate",
                image: "fish.fill",
                description: "Green patterns match current baitfish and stand out in today's water conditions",
                confidenceScore: 88
            ),
            RecommendedItem(
                name: "Silver Horde Kingfisher Lite - Army Truck",
                image: "fish.fill",
                description: "Proven pattern for Chinook in current water conditions",
                confidenceScore: 75
            ),
            RecommendedItem(
                name: "Ace Hi Fly - Blue/Silver",
                image: "fish.fill",
                description: "Excellent choice for trolling in clear water conditions",
                confidenceScore: 70
            )
        ],
        // Leaders
        [
            RecommendedItem(
                name: "Fluorocarbon Leader - 40lb test, 42-48\" length",
                image: "line.diagonal",
                description: "Longer leader recommended due to clear water conditions and cautious fish",
                confidenceScore: 92
            ),
            RecommendedItem(
                name: "Monofilament Leader - 30lb test, 36\" length",
                image: "line.diagonal",
                description: "Good alternative when fish are less leader shy",
                confidenceScore: 78
            )
        ]
    ]
    
    @State private var selectedItem: RecommendedItem? = nil
    @State private var showingDetail = false
    
    var body: some View {
        NavigationView {
            List {
                ForEach(0..<categories.count, id: \.self) { categoryIndex in
                    Section(header: Text(categories[categoryIndex])) {
                        ForEach(recommendations[categoryIndex]) { item in
                            Button(action: {
                                selectedItem = item
                                showingDetail = true
                            }) {
                                HStack(spacing: 15) {
                                    // Tool image
                                    ZStack {
                                        Circle()
                                            .fill(Color.blue.opacity(0.2))
                                            .frame(width: 60, height: 60)
                                        
                                        Image(systemName: item.image)
                                            .font(.system(size: 30))
                                            .foregroundColor(.blue)
                                    }
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text(item.name)
                                            .font(.headline)
                                            .foregroundColor(.primary)
                                        
                                        Text(item.description)
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                            .lineLimit(2)
                                        
                                        HStack {
                                            Text("Confidence:")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                            
                                            // Confidence bar
                                            ZStack(alignment: .leading) {
                                                Rectangle()
                                                    .frame(width: 100, height: 6)
                                                    .opacity(0.3)
                                                    .foregroundColor(Color(.systemGray5))
                                                
                                                Rectangle()
                                                    .frame(width: CGFloat(item.confidenceScore), height: 6)
                                                    .foregroundColor(confidenceColor(for: item.confidenceScore))
                                            }
                                            .cornerRadius(3)
                                            
                                            Text("\(item.confidenceScore)%")
                                                .font(.caption)
                                                .foregroundColor(confidenceColor(for: item.confidenceScore))
                                        }
                                    }
                                    
                                    Spacer()
                                    
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.gray)
                                        .font(.caption)
                                }
                                .padding(.vertical, 8)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Recommendations")
            .sheet(isPresented: $showingDetail) {
                if let item = selectedItem {
                    RecommendationDetailView(item: item)
                }
            }
        }
    }
    
    private func confidenceColor(for score: Int) -> Color {
        if score >= 80 {
            return .green
        } else if score >= 60 {
            return .blue
        } else if score >= 40 {
            return .yellow
        } else {
            return .orange
        }
    }
}

struct RecommendationDetailView: View {
    let item: RecommendedItem
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Hero image
                    ZStack {
                        Rectangle()
                            .fill(Color.blue.opacity(0.2))
                            .frame(height: 200)
                        
                        Image(systemName: item.image)
                            .font(.system(size: 80))
                            .foregroundColor(.blue)
                    }
                    
                    VStack(alignment: .leading, spacing: 15) {
                        // Title and confidence score
                        HStack {
                            Text(item.name)
                                .font(.title2)
                                .fontWeight(.bold)
                            
                            Spacer()
                            
                            Text("\(item.confidenceScore)% Match")
                                .font(.subheadline)
                                .padding(8)
                                .background(confidenceColor(for: item.confidenceScore).opacity(0.2))
                                .foregroundColor(confidenceColor(for: item.confidenceScore))
                                .cornerRadius(8)
                        }
                        .padding(.horizontal)
                        
                        // Description
                        Text("Description")
                            .font(.headline)
                            .padding(.horizontal)
                        
                        Text(item.description)
                            .padding(.horizontal)
                        
                        // Detailed specifications
                        Text("Specifications")
                            .font(.headline)
                            .padding(.top)
                            .padding(.horizontal)
                        
                        VStack(alignment: .leading, spacing: 10) {
                            specificationRow(label: "Type", value: getItemType(from: item.name))
                            specificationRow(label: "Color", value: getItemColor(from: item.name))
                            specificationRow(label: "Size", value: getItemSize())
                            specificationRow(label: "Material", value: getItemMaterial())
                            specificationRow(label: "Best For", value: "Coho, Chinook")
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(10)
                        .padding(.horizontal)
                        
                        // Usage tips
                        Text("Usage Tips")
                            .font(.headline)
                            .padding(.top)
                            .padding(.horizontal)
                        
                        VStack(alignment: .leading, spacing: 10) {
                            tipRow(icon: "speedometer", tip: "Trolling speed: 2.2-2.8 knots")
                            tipRow(icon: "arrow.down.to.line", tip: "Depth: 30-60 feet")
                            tipRow(icon: "water.waves", tip: "Best in moderate chop conditions")
                            tipRow(icon: "sun.max.fill", tip: "Performs well in bright sunlight")
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(10)
                        .padding(.horizontal)
                        
                        // Action buttons
                        HStack {
                            Button(action: {
                                // Save to favorites
                            }) {
                                Label("Save", systemImage: "heart")
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color.blue)
                                    .foregroundColor(.white)
                                    .cornerRadius(10)
                            }
                            
                            Button(action: {
                                // Share
                            }) {
                                Label("Share", systemImage: "square.and.arrow.up")
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color.gray.opacity(0.2))
                                    .foregroundColor(.primary)
                                    .cornerRadius(10)
                            }
                        }
                        .padding(.horizontal)
                        .padding(.top)
                    }
                    .padding(.bottom, 30)
                }
            }
            .navigationBarTitle("Recommendation Details", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    private func specificationRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(width: 80, alignment: .leading)
            
            Text(value)
                .font(.subheadline)
            
            Spacer()
        }
    }
    
    private func tipRow(icon: String, tip: String) -> some View {
        HStack(spacing: 15) {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 20)
            
            Text(tip)
                .font(.subheadline)
            
            Spacer()
        }
    }
    
    private func getItemType(from name: String) -> String {
        if name.contains("Flasher") {
            return "Rotating Flasher"
        } else if name.contains("Lure") || name.contains("Killer") {
            return "Spoon Lure"
        } else if name.contains("Leader") {
            return "Fishing Leader"
        } else {
            return "Fishing Equipment"
        }
    }
    
    private func getItemColor(from name: String) -> String {
        if name.contains("Green") {
            return "Green"
        } else if name.contains("Blue") {
            return "Blue"
        } else if name.contains("Chrome") {
            return "Chrome/Silver"
        } else if name.contains("Purple") {
            return "Purple"
        } else {
            return "Multi-color"
        }
    }
    
    private func getItemSize() -> String {
        if item.name.contains("Leader") {
            if item.name.contains("36\"") {
                return "36 inches"
            } else {
                return "42-48 inches"
            }
        } else {
            return "Standard"
        }
    }
    
    private func getItemMaterial() -> String {
        if item.name.contains("Fluorocarbon") {
            return "Fluorocarbon"
        } else if item.name.contains("Monofilament") {
            return "Monofilament"
        } else {
            return "Metal/Plastic"
        }
    }
    
    private func confidenceColor(for score: Int) -> Color {
        if score >= 80 {
            return .green
        } else if score >= 60 {
            return .blue
        } else if score >= 40 {
            return .yellow
        } else {
            return .orange
        }
    }
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

// Placeholder for MyGearView
struct GearScanView: View {
    @Binding var userGear: [FishingGear]
    @State private var showingImagePicker = false
    @State private var showingCamera = false
    @State private var inputImage: UIImage?
    @State private var showingScanningOverlay = false
    @State private var scanProgress: CGFloat = 0.0
    @State private var showingNewGearForm = false
    @State private var detectedGear: FishingGear?
    @State private var isMultiScanMode = false
    @State private var multiScanImages: [UIImage] = []
    
    var body: some View {
        NavigationView {
            VStack {
                if userGear.isEmpty {
                    emptyStateView
                } else {
                    gearListView
                }
            }
            .navigationTitle("My Gear")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: {
                            showingCamera = true
                        }) {
                            Label("Take Photo", systemImage: "camera")
                        }
                        
                        Button(action: {
                            showingImagePicker = true
                        }) {
                            Label("Choose Photo", systemImage: "photo.on.rectangle")
                        }
                        
                        Button(action: {
                            isMultiScanMode = true
                            showingCamera = true
                        }) {
                            Label("Scan Multiple Items", systemImage: "square.stack.3d.up")
                        }
                        
                        Button(action: {
                            showingNewGearForm = true
                        }) {
                            Label("Add Manually", systemImage: "plus")
                        }
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
                    }
                }
            }
            .sheet(isPresented: $showingImagePicker) {
                VStack {
                    HStack {
                        Spacer()
                        Button(action: {
                            showingImagePicker = false
                        }) {
                            Text("Done")
                                .fontWeight(.semibold)
                                .padding()
                        }
                    }
                    .padding(.horizontal)
                    
                    Text("Photo Picker Placeholder")
                        .font(.title2)
                        .padding()
                    
                    Text("In a real app, this would show your photo library")
                        .foregroundColor(.secondary)
                        .padding()
                    
                    Spacer()
                    
                    Button(action: {
                        // Simulate selecting a photo
                        inputImage = UIImage(systemName: "photo")
                        showingImagePicker = false
                        
                        // Trigger the scanning process
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            showingScanningOverlay = true
                            
                            // Simulate AI processing
                            var progress: CGFloat = 0.0
                            let timer = Timer.scheduledTimer(withTimeInterval: 0.05, repeats: true) { timer in
                                progress += 0.01
                                scanProgress = progress
                                
                                if progress >= 1.0 {
                                    timer.invalidate()
                                    showingScanningOverlay = false
                                    showingNewGearForm = true
                                }
                            }
                            
                            RunLoop.current.add(timer, forMode: .common)
                        }
                    }) {
                        Text("Select Photo")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                    .padding()
                }
            }
            .sheet(isPresented: $showingCamera) {
                CameraPlaceholderView(image: $inputImage, isMultiScanMode: $isMultiScanMode, multiScanImages: $multiScanImages)
            }
            .sheet(isPresented: $showingNewGearForm) {
                NewGearFormPlaceholderView(userGear: $userGear)
            }
            .overlay(
                ZStack {
                    if showingScanningOverlay {
                        Color.black.opacity(0.7)
                            .edgesIgnoringSafeArea(.all)
                        
                        VStack(spacing: 20) {
                            Text("Scanning Gear...")
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                            
                            ZStack {
                                Circle()
                                    .stroke(lineWidth: 8)
                                    .opacity(0.3)
                                    .foregroundColor(.white)
                                
                                Circle()
                                    .trim(from: 0, to: scanProgress)
                                    .stroke(style: StrokeStyle(lineWidth: 8, lineCap: .round, lineJoin: .round))
                                    .foregroundColor(.blue)
                                    .rotationEffect(Angle(degrees: 270))
                                
                                Image(systemName: "camera.viewfinder")
                                    .font(.system(size: 40))
                                    .foregroundColor(.white)
                            }
                            .frame(width: 150, height: 150)
                            
                            Text("Using AI to identify your fishing gear...")
                                .foregroundColor(.white)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal)
                        }
                        .padding()
                        .background(Color.black.opacity(0.5))
                        .cornerRadius(20)
                    }
                }
            )
        }
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Spacer()
            
            Image(systemName: "camera.viewfinder")
                .font(.system(size: 80))
                .foregroundColor(.blue)
            
            Text("Scan Your Fishing Gear")
                .font(.title2)
                .fontWeight(.bold)
            
            Text("Take photos of your fishing gear and our AI will identify and catalog them for personalized recommendations.")
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
                .padding(.horizontal, 40)
            
            Button(action: {
                showingCamera = true
            }) {
                HStack {
                    Image(systemName: "camera.fill")
                    Text("Scan Gear")
                }
                .frame(minWidth: 200)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
            .padding(.top)
            
            Button(action: {
                showingImagePicker = true
            }) {
                HStack {
                    Image(systemName: "photo.on.rectangle")
                    Text("Choose from Photos")
                }
                .frame(minWidth: 200)
                .padding()
                .background(Color.gray.opacity(0.2))
                .foregroundColor(.primary)
                .cornerRadius(10)
            }
            
            Spacer()
        }
    }
    
    private var gearListView: some View {
        List {
            ForEach(GearType.allCases, id: \.self) { gearType in
                let gearOfType = userGear.filter { $0.type == gearType }
                
                if !gearOfType.isEmpty {
                    Section(header: Text(gearType.rawValue + "s")) {
                        ForEach(gearOfType) { gear in
                            HStack {
                                ZStack {
                                    Circle()
                                        .fill(Color.blue.opacity(0.2))
                                        .frame(width: 60, height: 60)
                                    
                                    Image(systemName: gearType.icon)
                                        .font(.system(size: 30))
                                        .foregroundColor(.blue)
                                }
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(gear.name)
                                        .font(.headline)
                                    
                                    Text("\(gear.brand) • \(gear.color) • \(gear.size)")
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                    
                                    HStack {
                                        Text("AI Confidence:")
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                        
                                        Text("\(gear.confidence)%")
                                            .font(.caption)
                                            .foregroundColor(confidenceColor(for: gear.confidence))
                                    }
                                }
                                
                                Spacer()
                                
                                Image(systemName: "chevron.right")
                                    .foregroundColor(.gray)
                                    .font(.caption)
                            }
                            .padding(.vertical, 8)
                        }
                    }
                }
            }
        }
        .listStyle(InsetGroupedListStyle())
    }
    
    private func confidenceColor(for score: Int) -> Color {
        if score >= 90 {
            return .green
        } else if score >= 75 {
            return .blue
        } else if score >= 60 {
            return .yellow
        } else {
            return .orange
        }
    }
}

struct CameraPlaceholderView: View {
    @Binding var image: UIImage?
    @Binding var isMultiScanMode: Bool
    @Binding var multiScanImages: [UIImage]
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)
            
            VStack {
                Text("Camera Placeholder")
                    .font(.title)
                    .foregroundColor(.white)
                    .padding()
                
                Text("In a real app, this would be a camera view")
                    .foregroundColor(.white)
                    .padding()
                
                Button("Simulate Taking Photo") {
                    // Simulate taking a photo
                    image = UIImage(systemName: "camera.fill")
                    presentationMode.wrappedValue.dismiss()
                }
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                .padding()
                
                Button("Cancel") {
                    presentationMode.wrappedValue.dismiss()
                }
                .foregroundColor(.white)
                .padding()
            }
        }
    }
}

struct NewGearFormPlaceholderView: View {
    @Binding var userGear: [FishingGear]
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("AI Detection Results")) {
                    Text("Item successfully identified")
                        .font(.headline)
                }
                
                Section(header: Text("Gear Details")) {
                    Text("Name: Pro-Troll Flasher")
                    Text("Type: Flasher")
                    Text("Color: Green")
                }
                
                Section {
                    Button(action: {
                        // Add a sample gear item
                        let newGear = FishingGear(
                            name: "Pro-Troll Flasher",
                            type: .flasher,
                            image: UIImage(systemName: "circle.hexagongrid.fill")!,
                            color: "Green",
                            size: "Standard",
                            brand: "Pro-Troll",
                            confidence: 95
                        )
                        
                        userGear.append(newGear)
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("Save to My Gear")
                            .frame(maxWidth: .infinity)
                            .fontWeight(.semibold)
                    }
                }
            }
            .navigationTitle("New Gear")
            .navigationBarItems(trailing: Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}

// Placeholder for SmartRecommendationsView
struct SmartRecsView: View {
    let userGear: [FishingGear]
    let selectedLocation: SimpleLocation
    let weatherConditions: String
    let windSpeed: Int
    let tideType: String
    
    @State private var recommendationMode = 0 // 0: Optimal, 1: From Your Gear
    
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
                
                // Recommendation mode selector
                Picker("Mode", selection: $recommendationMode) {
                    Text("Optimal Gear").tag(0)
                    Text("From Your Gear").tag(1)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                if userGear.isEmpty && recommendationMode == 1 {
                    // Empty state for "From Your Gear" when no gear is added
                    VStack(spacing: 20) {
                        Spacer()
                        
                        Image(systemName: "camera.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.blue)
                        
                        Text("No Gear Found")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text("Add your fishing gear in the My Gear tab to get personalized recommendations based on your collection.")
                            .multilineTextAlignment(.center)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 40)
                        
                        Button(action: {}) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Add Your Gear")
                            }
                            .frame(minWidth: 200)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }
                        .padding(.top)
                        
                        Spacer()
                    }
                } else {
                    // Sample recommendations list
                    List {
                        Section(header: Text("Flashers")) {
                            recommendationRow(
                                name: "Hot Spot Flasher - UV Green",
                                description: "Perfect for today's light conditions",
                                matchScore: 95,
                                icon: "circle.hexagongrid.fill",
                                reason: "Current \(weatherConditions.lowercased()) conditions"
                            )
                            
                            recommendationRow(
                                name: "Pro-Troll Flasher - UV Purple",
                                description: "Good alternative for current conditions",
                                matchScore: 87,
                                icon: "circle.hexagongrid.fill",
                                reason: "Works well in \(weatherConditions.lowercased()) conditions"
                            )
                        }
                        
                        Section(header: Text("Lures")) {
                            recommendationRow(
                                name: "Coho Killer - Green Pirate",
                                description: "Top choice for \(selectedLocation.name) today",
                                matchScore: 92,
                                icon: "fish.fill",
                                reason: "Matches local baitfish patterns"
                            )
                        }
                    }
                    .listStyle(InsetGroupedListStyle())
                }
            }
            .navigationTitle("Smart Recommendations")
        }
    }
    
    private func recommendationRow(name: String, description: String, matchScore: Int, icon: String, reason: String) -> some View {
        HStack(spacing: 15) {
            // Recommendation icon with match score
            ZStack {
                Circle()
                    .fill(matchScoreColor(for: matchScore).opacity(0.2))
                    .frame(width: 60, height: 60)
                
                Image(systemName: icon)
                    .font(.system(size: 30))
                    .foregroundColor(matchScoreColor(for: matchScore))
                
                // Match score indicator
                Text("\(matchScore)%")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
                    .padding(4)
                    .background(matchScoreColor(for: matchScore))
                    .clipShape(Circle())
                    .offset(x: 20, y: -20)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(name)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                
                // Match reason
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                        .font(.caption)
                    
                    Text(reason)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            }
            
            Spacer()
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
    
    private func weatherIcon(for condition: String) -> String {
        let condition = condition.lowercased()
        if condition.contains("clear") || condition.contains("sunny") {
            return "sun.max.fill"
        } else if condition.contains("partly cloudy") {
            return "cloud.sun.fill"
        } else if condition.contains("cloudy") {
            return "cloud.fill"
        } else if condition.contains("rain") {
            return "cloud.rain.fill"
        } else {
            return "cloud.fill"
        }
    }
    
    private func tideIcon(for tideType: String) -> String {
        let type = tideType.lowercased()
        if type.contains("rising") {
            return "arrow.up.right.circle.fill"
        } else if type.contains("high") {
            return "arrow.up.circle.fill"
        } else if type.contains("falling") {
            return "arrow.down.right.circle.fill"
        } else if type.contains("low") {
            return "arrow.down.circle.fill"
        } else {
            return "circle.fill"
        }
    }
}
