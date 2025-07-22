import SwiftUI
import MapKit
import CoreLocation
import Combine

struct FullScreenMapView: View {
    @StateObject private var viewModel: FullScreenMapViewModel
    @State private var mapRegion: MKCoordinateRegion
    @State private var showingAddressConfirmation = false
    @State private var showingForecast = false
    @State private var pinLocation: CLLocationCoordinate2D?
    @State private var addressDetails: String = ""
    @State private var pinTitle: String = ""
    @State private var showingTextFieldAlert = false
    
    init(locationService: LocationService, weatherService: WeatherService, tideService: TideService) {
        let vm = FullScreenMapViewModel(locationService: locationService, weatherService: weatherService, tideService: tideService)
        _viewModel = StateObject(wrappedValue: vm)
        
        // Default to Seattle if no location is available
        _mapRegion = State(initialValue: MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
            span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
        ))
    }
    
    var body: some View {
        ZStack {
            // Full screen map
            Map(coordinateRegion: $mapRegion, showsUserLocation: true, annotationItems: viewModel.pins) { pin in
                MapAnnotation(coordinate: pin.coordinate) {
                    VStack {
                        Image(systemName: "mappin.circle.fill")
                            .font(.title)
                            .foregroundColor(.red)
                        
                        if !pin.title.isEmpty {
                            Text(pin.title)
                                .font(.caption)
                                .padding(5)
                                .background(Color.white.opacity(0.8))
                                .cornerRadius(5)
                        }
                    }
                    .onTapGesture {
                        viewModel.selectPin(pin)
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
                                let coordinate = convertToCoordinate(location)
                                pinLocation = coordinate
                                viewModel.lookupAddress(for: coordinate) { address in
                                    addressDetails = address
                                    showingAddressConfirmation = true
                                }
                            }
                        default:
                            break
                        }
                    }
            )
            
            // Controls overlay
            VStack {
                HStack {
                    Button(action: {
                        // Go back
                    }) {
                        Image(systemName: "arrow.left")
                            .font(.title2)
                            .padding(12)
                            .background(Color.white)
                            .clipShape(Circle())
                            .shadow(radius: 3)
                    }
                    
                    Spacer()
                    
                    Button(action: {
                        viewModel.getCurrentLocation { location in
                            if let location = location {
                                mapRegion = MKCoordinateRegion(
                                    center: location.coordinate,
                                    span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                                )
                            }
                        }
                    }) {
                        Image(systemName: "location")
                            .font(.title2)
                            .padding(12)
                            .background(Color.white)
                            .clipShape(Circle())
                            .shadow(radius: 3)
                    }
                }
                .padding()
                
                Spacer()
                
                // Instructions
                Text("Long press to drop a pin")
                    .font(.caption)
                    .padding(8)
                    .background(Color.white.opacity(0.8))
                    .cornerRadius(8)
                    .padding(.bottom)
            }
        }
        .alert(isPresented: $showingAddressConfirmation) {
            Alert(
                title: Text("Confirm Location"),
                message: Text(addressDetails),
                primaryButton: .default(Text("Use This Location")) {
                    showingTextFieldAlert = true
                },
                secondaryButton: .cancel()
            )
        }
        .sheet(isPresented: $showingForecast) {
            if let selectedPin = viewModel.selectedPin {
                ForecastView(
                    location: Location(
                        id: selectedPin.id,
                        name: selectedPin.title,
                        latitude: selectedPin.coordinate.latitude,
                        longitude: selectedPin.coordinate.longitude
                    ),
                    weatherService: viewModel.weatherService,
                    tideService: viewModel.tideService
                )
            }
        }
        .overlay(
            Group {
                if showingTextFieldAlert {
                    TextFieldAlert(
                        title: "Name this location",
                        message: "Enter a name for this fishing spot",
                        text: $pinTitle,
                        isShowing: $showingTextFieldAlert,
                        onSave: {
                            if let coordinate = pinLocation {
                                viewModel.addPin(
                                    title: pinTitle,
                                    coordinate: coordinate,
                                    address: addressDetails
                                )
                                pinTitle = ""
                            }
                        }
                    )
                }
            }
        )
    }
    
    private func convertToCoordinate(_ point: CGPoint) -> CLLocationCoordinate2D {
        let mapSize = UIScreen.main.bounds.size
        
        let normalizedPoint = CGPoint(
            x: point.x / mapSize.width,
            y: point.y / mapSize.height
        )
        
        let span = mapRegion.span
        let center = mapRegion.center
        
        let xOffset = span.longitudeDelta * (normalizedPoint.x - 0.5)
        let yOffset = span.latitudeDelta * (0.5 - normalizedPoint.y)
        
        return CLLocationCoordinate2D(
            latitude: center.latitude + yOffset,
            longitude: center.longitude + xOffset
        )
    }
}

// Custom TextFieldAlert since SwiftUI doesn't natively support text fields in alerts
struct TextFieldAlert: View {
    let title: String
    let message: String
    @Binding var text: String
    @Binding var isShowing: Bool
    let onSave: () -> Void
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
                .onTapGesture {
                    isShowing = false
                }
            
            VStack(spacing: 20) {
                Text(title)
                    .font(.headline)
                
                Text(message)
                    .font(.subheadline)
                
                TextField("Location name", text: $text)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding(.horizontal)
                
                HStack {
                    Button("Cancel") {
                        isShowing = false
                    }
                    .foregroundColor(.red)
                    
                    Spacer()
                    
                    Button("Save") {
                        onSave()
                        isShowing = false
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

struct ForecastView: View {
    let location: Location
    @StateObject private var viewModel: WeatherForecastViewModel
    @State private var selectedDayIndex = 0
    @Environment(\.presentationMode) var presentationMode
    
    init(location: Location, weatherService: WeatherService, tideService: TideService) {
        self.location = location
        _viewModel = StateObject(wrappedValue: WeatherForecastViewModel(weatherService: weatherService, tideService: tideService))
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
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
                                let date = Calendar.current.date(byAdding: .day, value: index, to: Date()) ?? Date()
                                
                                Button(action: {
                                    selectedDayIndex = index
                                    viewModel.selectedDate = date
                                    viewModel.loadWeatherForSelectedDate(location: location)
                                }) {
                                    VStack {
                                        Text(dayOfWeek(date))
                                            .font(.caption)
                                        
                                        Text(dayNumber(date))
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
                    
                    if viewModel.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, alignment: .center)
                            .padding()
                    } else if let weather = viewModel.selectedWeather, let tide = viewModel.selectedTide {
                        // Weather card
                        VStack(alignment: .leading, spacing: 15) {
                            HStack {
                                Text("Weather")
                                    .font(.headline)
                                Spacer()
                                Text(formatDate(weather.timestamp))
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            HStack(spacing: 20) {
                                // Weather icon
                                Image(systemName: weatherIcon(for: weather.condition))
                                    .font(.system(size: 50))
                                    .foregroundColor(.blue)
                                
                                VStack(alignment: .leading, spacing: 5) {
                                    Text("\(Int(weather.temperature))°F")
                                        .font(.system(size: 36, weight: .bold))
                                    
                                    Text(weather.condition)
                                        .font(.headline)
                                }
                                
                                Spacer()
                            }
                            
                            Divider()
                            
                            // Weather details
                            HStack {
                                WeatherDetailItem(
                                    icon: "wind",
                                    title: "Wind",
                                    value: "\(weather.windSpeed) mph \(weather.windDirection)"
                                )
                                
                                Spacer()
                                
                                WeatherDetailItem(
                                    icon: "humidity",
                                    title: "Humidity",
                                    value: "\(weather.humidity)%"
                                )
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
                                Text(formatDate(tide.timestamp))
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            HStack(spacing: 20) {
                                // Tide icon
                                Image(systemName: tideIcon(for: tide.type))
                                    .font(.system(size: 50))
                                    .foregroundColor(.blue)
                                
                                VStack(alignment: .leading, spacing: 5) {
                                    Text("\(String(format: "%.1f", tide.height)) ft")
                                        .font(.system(size: 36, weight: .bold))
                                    
                                    Text(tideDescription(for: tide.type))
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
                                Image(systemName: "fish")
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
                    } else if let error = viewModel.errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .padding()
                    } else {
                        Text("Select a date to view forecast")
                            .foregroundColor(.secondary)
                            .padding()
                    }
                }
                .padding(.bottom)
            }
            .navigationBarTitle("Forecast", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
            .onAppear {
                // Load initial forecast for today
                viewModel.selectedDate = Date()
                viewModel.loadWeatherForSelectedDate(location: location)
                viewModel.loadForecast(for: location)
            }
        }
    }
    
    private func dayOfWeek(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE"
        return formatter.string(from: date)
    }
    
    private func dayNumber(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "d"
        return formatter.string(from: date)
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, h:mm a"
        return formatter.string(from: date)
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
    
    private func tideIcon(for type: TideData.TideType) -> String {
        switch type {
        case .high:
            return "arrow.up.circle.fill"
        case .low:
            return "arrow.down.circle.fill"
        case .rising:
            return "arrow.up.right.circle.fill"
        case .falling:
            return "arrow.down.right.circle.fill"
        }
    }
    
    private func tideDescription(for type: TideData.TideType) -> String {
        switch type {
        case .high:
            return "High Tide"
        case .low:
            return "Low Tide"
        case .rising:
            return "Rising Tide"
        case .falling:
            return "Falling Tide"
        }
    }
}
str
uct FullScreenMapView_Previews: PreviewProvider {
    static var previews: some View {
        let dependencies = AppDependencies.preview
        return FullScreenMapView(
            locationService: dependencies.locationService,
            weatherService: dependencies.weatherService,
            tideService: dependencies.tideService
        )
    }
}