import SwiftUI
import MapKit
import CoreLocation

struct LocationsView: View {
    @State private var selectedTab = 0
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    @State private var showingForecast = false
    @State private var selectedLocation: SimpleLocation?
    @State private var locations: [SimpleLocation] = sampleLocations
    @State private var newPinCoordinate: CLLocationCoordinate2D?
    @State private var newPinName: String = ""
    @State private var showingNamePrompt = false
    @State private var searchText = ""
    
    var filteredLocations: [SimpleLocation] {
        if searchText.isEmpty {
            return locations
        } else {
            return locations.filter { $0.name.localizedCaseInsensitiveContains(searchText) }
        }
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                // Main content based on selected tab
                if selectedTab == 0 {
                    // Map View - Full Screen
                    mapView
                } else {
                    // List View
                    listView
                }
                
                // Floating tab selector at the top
                VStack {
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
                    .shadow(radius: 2)
                    .padding(.top, 10)
                    
                    Spacer()
                }
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingNamePrompt = true
                        if selectedTab == 1 {
                            // When in list view, show prompt directly
                            newPinCoordinate = CLLocationCoordinate2D(
                                latitude: region.center.latitude,
                                longitude: region.center.longitude
                            )
                        }
                    }) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
        .sheet(isPresented: $showingForecast) {
            if let location = selectedLocation {
                EnhancedForecastView(location: location)
            }
        }
        .alert("Add Location", isPresented: $showingNamePrompt) {
            TextField("Location name", text: $newPinName)
            Button("Cancel", role: .cancel) {
                newPinName = ""
                newPinCoordinate = nil
            }
            Button("Add") {
                if let coordinate = newPinCoordinate {
                    let newLocation = SimpleLocation(
                        id: UUID().uuidString,
                        name: newPinName.isEmpty ? "New Location" : newPinName,
                        latitude: coordinate.latitude,
                        longitude: coordinate.longitude
                    )
                    locations.append(newLocation)
                    selectedLocation = newLocation
                    showingForecast = true
                    
                    // Reset
                    newPinName = ""
                    newPinCoordinate = nil
                }
            }
        } message: {
            Text("Enter a name for this fishing location")
        }
    }
    
    // MARK: - Map View
    private var mapView: some View {
        ZStack {
            // Main map view
            Map(coordinateRegion: $region, annotationItems: locations) { location in
                MapAnnotation(coordinate: CLLocationCoordinate2D(
                    latitude: location.latitude, 
                    longitude: location.longitude
                )) {
                    LocationPin(location: location) {
                        selectedLocation = location
                        showingForecast = true
                    }
                }
            }
            .ignoresSafeArea(edges: [.top, .bottom])
            
            // Transparent overlay for long press only
            // This allows the map to handle its own panning gestures
            Color.clear
                .contentShape(Rectangle())
                .onLongPressGesture(minimumDuration: 0.5) { location in
                    // When long pressed, use the current center of the map for the new pin
                    newPinCoordinate = CLLocationCoordinate2D(
                        latitude: region.center.latitude,
                        longitude: region.center.longitude
                    )
                    showingNamePrompt = true
                }
        }
        .overlay(
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
                    
                    VStack(spacing: 12) {
                        // Zoom in button
                        Button(action: {
                            // Zoom in
                            let newSpan = MKCoordinateSpan(
                                latitudeDelta: max(region.span.latitudeDelta * 0.5, 0.005),
                                longitudeDelta: max(region.span.longitudeDelta * 0.5, 0.005)
                            )
                            region = MKCoordinateRegion(center: region.center, span: newSpan)
                        }) {
                            Image(systemName: "plus")
                                .font(.system(size: 18, weight: .bold))
                                .padding(12)
                                .background(Color.white)
                                .clipShape(Circle())
                                .shadow(radius: 2)
                        }
                        
                        // Zoom out button
                        Button(action: {
                            // Zoom out
                            let newSpan = MKCoordinateSpan(
                                latitudeDelta: region.span.latitudeDelta * 2,
                                longitudeDelta: region.span.longitudeDelta * 2
                            )
                            region = MKCoordinateRegion(center: region.center, span: newSpan)
                        }) {
                            Image(systemName: "minus")
                                .font(.system(size: 18, weight: .bold))
                                .padding(12)
                                .background(Color.white)
                                .clipShape(Circle())
                                .shadow(radius: 2)
                        }
                        
                        // Current location button
                        Button(action: {
                            // Get current location (for demo, we'll use a fixed location)
                            region = MKCoordinateRegion(
                                center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
                                span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                            )
                        }) {
                            Image(systemName: "location")
                                .font(.system(size: 18, weight: .bold))
                                .padding(12)
                                .background(Color.white)
                                .clipShape(Circle())
                                .shadow(radius: 2)
                        }
                    }
                    .padding()
                }
            }
        )
    }
    
    // MARK: - List View
    private var listView: some View {
        VStack {
            // Search bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                
                TextField("Search locations", text: $searchText)
                    .autocapitalization(.none)
                
                if !searchText.isEmpty {
                    Button(action: {
                        searchText = ""
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(8)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(10)
            .padding(.horizontal)
            
            List {
                ForEach(filteredLocations) { location in
                    LocationRow(location: location)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            selectedLocation = location
                            showingForecast = true
                        }
                }
                .onDelete { indexSet in
                    // Filter to only delete from filtered results
                    let locationsToDelete = indexSet.map { filteredLocations[$0] }
                    locations.removeAll { location in
                        locationsToDelete.contains { $0.id == location.id }
                    }
                }
            }
            .listStyle(InsetGroupedListStyle())
        }
    }
}

// Custom forecast view with weather, tide, and wind information
struct ForecastViewWrapper: View {
    let location: SimpleLocation
    @Environment(\.presentationMode) var presentationMode
    @State private var selectedDayIndex = 0
    
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
    
    // Helper functions for weather icons
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
    
    // Helper functions for tide icons
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
}