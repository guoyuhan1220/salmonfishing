import Foundation
import Combine
import CoreLocation

enum LocationPermissionStatus {
    case notDetermined
    case denied
    case authorized
    case restricted
}

protocol LocationService {
    func requestLocationPermission()
    func getCurrentLocation() -> AnyPublisher<CLLocation?, Error>
    func searchLocations(query: String) -> AnyPublisher<[Location], Error>
    func getSavedLocations() -> AnyPublisher<[Location], Never>
    func saveLocation(_ location: Location) -> AnyPublisher<Bool, Error>
    func deleteLocation(withId id: String) -> AnyPublisher<Bool, Error>
    
    var currentLocation: AnyPublisher<CLLocation?, Never> { get }
    var permissionStatus: AnyPublisher<LocationPermissionStatus, Never> { get }
}

class LocationServiceImpl: NSObject, LocationService, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let currentLocationSubject = CurrentValueSubject<CLLocation?, Never>(nil)
    private let permissionStatusSubject = CurrentValueSubject<LocationPermissionStatus, Never>(.notDetermined)
    private let savedLocationsSubject = CurrentValueSubject<[Location], Never>([])
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        
        // Load saved locations
        loadSavedLocations()
        
        // Update initial permission status
        updatePermissionStatus()
    }
    
    // MARK: - LocationService Protocol
    
    func requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    func getCurrentLocation() -> AnyPublisher<CLLocation?, Error> {
        let subject = PassthroughSubject<CLLocation?, Error>()
        
        if let location = locationManager.location {
            subject.send(location)
            subject.send(completion: .finished)
        } else {
            locationManager.requestLocation()
            // In a real app, we would need to handle the async nature of requestLocation
            // For now, we'll just send nil if no location is available
            subject.send(nil)
            subject.send(completion: .finished)
        }
        
        return subject.eraseToAnyPublisher()
    }
    
    func searchLocations(query: String) -> AnyPublisher<[Location], Error> {
        let subject = PassthroughSubject<[Location], Error>()
        
        let geocoder = CLGeocoder()
        geocoder.geocodeAddressString(query) { placemarks, error in
            if let error = error {
                subject.send(completion: .failure(error))
                return
            }
            
            let locations = placemarks?.compactMap { placemark -> Location? in
                guard let name = placemark.name ?? placemark.locality,
                      let location = placemark.location else {
                    return nil
                }
                
                return Location(
                    id: UUID().uuidString,
                    name: name,
                    latitude: location.coordinate.latitude,
                    longitude: location.coordinate.longitude,
                    isSaved: false
                )
            } ?? []
            
            subject.send(locations)
            subject.send(completion: .finished)
        }
        
        return subject.eraseToAnyPublisher()
    }
    
    func getSavedLocations() -> AnyPublisher<[Location], Never> {
        return savedLocationsSubject.eraseToAnyPublisher()
    }
    
    func saveLocation(_ location: Location) -> AnyPublisher<Bool, Error> {
        let subject = PassthroughSubject<Bool, Error>()
        
        // Check if location already exists
        var locations = savedLocationsSubject.value
        if let index = locations.firstIndex(where: { $0.id == location.id }) {
            // Update existing location
            locations[index] = location
        } else {
            // Add new location
            locations.append(location)
        }
        
        // Save to UserDefaults
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(locations)
            UserDefaults.standard.set(data, forKey: "savedLocations")
            savedLocationsSubject.send(locations)
            subject.send(true)
            subject.send(completion: .finished)
        } catch {
            subject.send(completion: .failure(error))
        }
        
        return subject.eraseToAnyPublisher()
    }
    
    func deleteLocation(withId id: String) -> AnyPublisher<Bool, Error> {
        let subject = PassthroughSubject<Bool, Error>()
        
        var locations = savedLocationsSubject.value
        locations.removeAll { $0.id == id }
        
        // Save to UserDefaults
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(locations)
            UserDefaults.standard.set(data, forKey: "savedLocations")
            savedLocationsSubject.send(locations)
            subject.send(true)
            subject.send(completion: .finished)
        } catch {
            subject.send(completion: .failure(error))
        }
        
        return subject.eraseToAnyPublisher()
    }
    
    var currentLocation: AnyPublisher<CLLocation?, Never> {
        return currentLocationSubject.eraseToAnyPublisher()
    }
    
    var permissionStatus: AnyPublisher<LocationPermissionStatus, Never> {
        return permissionStatusSubject.eraseToAnyPublisher()
    }
    
    // MARK: - Private Methods
    
    private func loadSavedLocations() {
        if let data = UserDefaults.standard.data(forKey: "savedLocations") {
            do {
                let decoder = JSONDecoder()
                let locations = try decoder.decode([Location].self, from: data)
                savedLocationsSubject.send(locations)
            } catch {
                print("Error loading saved locations: \(error.localizedDescription)")
            }
        }
    }
    
    private func updatePermissionStatus() {
        let status: LocationPermissionStatus
        
        switch locationManager.authorizationStatus {
        case .notDetermined:
            status = .notDetermined
        case .restricted:
            status = .restricted
        case .denied:
            status = .denied
        case .authorizedAlways, .authorizedWhenInUse:
            status = .authorized
        @unknown default:
            status = .notDetermined
        }
        
        permissionStatusSubject.send(status)
    }
    
    // MARK: - CLLocationManagerDelegate
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            currentLocationSubject.send(location)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location manager failed with error: \(error.localizedDescription)")
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        updatePermissionStatus()
        
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            locationManager.startUpdatingLocation()
        }
    }
}