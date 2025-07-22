import Foundation
import CoreLocation
import MapKit
import Combine

class FullScreenMapViewModel: ObservableObject {
    @Published var pins: [MapPin] = []
    @Published var selectedPin: MapPin?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    let locationService: LocationService
    let weatherService: WeatherService
    let tideService: TideService
    private var cancellables = Set<AnyCancellable>()
    
    init(locationService: LocationService, weatherService: WeatherService, tideService: TideService) {
        self.locationService = locationService
        self.weatherService = weatherService
        self.tideService = tideService
        
        // Load saved locations as pins
        loadSavedLocations()
    }
    
    func loadSavedLocations() {
        locationService.getSavedLocations()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] locations in
                self?.pins = locations.map { location in
                    MapPin(
                        id: location.id,
                        title: location.name,
                        coordinate: CLLocationCoordinate2D(
                            latitude: location.latitude,
                            longitude: location.longitude
                        ),
                        address: ""
                    )
                }
            }
            .store(in: &cancellables)
    }
    
    func getCurrentLocation(completion: @escaping (CLLocation?) -> Void) {
        locationService.getCurrentLocation()
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in },
                receiveValue: { location in
                    completion(location)
                }
            )
            .store(in: &cancellables)
    }
    
    func lookupAddress(for coordinate: CLLocationCoordinate2D, completion: @escaping (String) -> Void) {
        let geocoder = CLGeocoder()
        let location = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
        
        geocoder.reverseGeocodeLocation(location) { placemarks, error in
            if let error = error {
                print("Reverse geocoding error: \(error.localizedDescription)")
                completion("Unknown location")
                return
            }
            
            guard let placemark = placemarks?.first else {
                completion("Unknown location")
                return
            }
            
            var addressComponents: [String] = []
            
            if let name = placemark.name {
                addressComponents.append(name)
            }
            
            if let thoroughfare = placemark.thoroughfare {
                addressComponents.append(thoroughfare)
            }
            
            if let locality = placemark.locality {
                addressComponents.append(locality)
            }
            
            if let administrativeArea = placemark.administrativeArea {
                addressComponents.append(administrativeArea)
            }
            
            if let postalCode = placemark.postalCode {
                addressComponents.append(postalCode)
            }
            
            let addressString = addressComponents.joined(separator: ", ")
            completion(addressString.isEmpty ? "Unknown location" : addressString)
        }
    }
    
    func addPin(title: String, coordinate: CLLocationCoordinate2D, address: String) {
        let newPin = MapPin(
            id: UUID().uuidString,
            title: title,
            coordinate: coordinate,
            address: address
        )
        
        pins.append(newPin)
        
        // Save as a location
        let location = Location(
            id: newPin.id,
            name: title,
            latitude: coordinate.latitude,
            longitude: coordinate.longitude,
            isSaved: true,
            notes: address
        )
        
        locationService.saveLocation(location)
            .sink(
                receiveCompletion: { completion in
                    if case .failure(let error) = completion {
                        print("Error saving location: \(error.localizedDescription)")
                    }
                },
                receiveValue: { _ in }
            )
            .store(in: &cancellables)
    }
    
    func selectPin(_ pin: MapPin) {
        selectedPin = pin
    }
    
    func removePin(_ pin: MapPin) {
        pins.removeAll { $0.id == pin.id }
        
        // Delete from saved locations
        locationService.deleteLocation(withId: pin.id)
            .sink(
                receiveCompletion: { completion in
                    if case .failure(let error) = completion {
                        print("Error deleting location: \(error.localizedDescription)")
                    }
                },
                receiveValue: { _ in }
            )
            .store(in: &cancellables)
    }
}

struct MapPin: Identifiable {
    let id: String
    let title: String
    let coordinate: CLLocationCoordinate2D
    let address: String
}