import Foundation
import CoreLocation
import Combine

class LocationServiceImpl: NSObject, LocationService, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let locationSubject = CurrentValueSubject<CLLocation?, Never>(nil)
    private let permissionStatusSubject = CurrentValueSubject<LocationPermissionStatus, Never>(.notDetermined)
    
    // Local storage for saved locations
    private let savedLocationsSubject = CurrentValueSubject<[Location], Never>([])
    private let userDefaults = UserDefaults.standard
    private let savedLocationsKey = "savedLocations"
    private let cachedLocationKey = "cachedLocation"
    private let locationCacheTimestampKey = "locationCacheTimestamp"
    
    // Battery optimization settings
    private var isMoving = false
    private var lastSignificantMovement = Date()
    private let significantMovementThreshold: TimeInterval = 300 // 5 minutes
    
    // Location accuracy settings
    private var currentAccuracyMode: AccuracyMode = .balanced
    
    enum AccuracyMode {
        case highAccuracy
        case balanced
        case lowPower
        
        var desiredAccuracy: CLLocationAccuracy {
            switch self {
            case .highAccuracy:
                return kCLLocationAccuracyBest
            case .balanced:
                return kCLLocationAccuracyNearestTenMeters
            case .lowPower:
                return kCLLocationAccuracyHundredMeters
            }
        }
        
        var distanceFilter: CLLocationDistance {
            switch self {
            case .highAccuracy:
                return 5 // 5 meters
            case .balanced:
                return 20 // 20 meters
            case .lowPower:
                return 100 // 100 meters
            }
        }
        
        var updateInterval: TimeInterval {
            switch self {
            case .highAccuracy:
                return 10 // 10 seconds
            case .balanced:
                return 30 // 30 seconds
            case .lowPower:
                return 120 // 2 minutes
            }
        }
    }
    
    override init() {
        super.init()
        setupLocationManager()
        loadSavedLocations()
        loadCachedLocation()
    }
    
    // MARK: - LocationService Protocol
    
    var currentLocation: AnyPublisher<CLLocation?, Never> {
        return locationSubject.eraseToAnyPublisher()
    }
    
    var permissionStatus: AnyPublisher<LocationPermissionStatus, Never> {
        return permissionStatusSubject.eraseToAnyPublisher()
    }
    
    func requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    func getCurrentLocation() -> AnyPublisher<CLLocation?, Error> {
        return Future<CLLocation?, Error> { [weak self] promise in
            guard let self = self else {
                promise(.failure(NSError(domain: "LocationService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Service not available"])))
                return
            }
            
            let status = self.permissionStatusSubject.value
            
            if status.isAuthorized {
                // Check if we have a recent location
                if let location = self.locationSubject.value {
                    promise(.success(location))
                    return
                }
                
                // Check if we have a cached location that's still valid
                if let cachedLocation = self.loadCachedLocation() {
                    promise(.success(cachedLocation))
                    
                    // Still request a fresh location in the background
                    self.requestFreshLocation()
                    return
                }
                
                // Request a fresh location
                self.setAccuracyMode(.highAccuracy)
                self.locationManager.requestLocation()
                
                // Set a timeout for the location request
                DispatchQueue.main.asyncAfter(deadline: .now() + 5) { [weak self] in
                    if let location = self?.locationSubject.value {
                        promise(.success(location))
                    } else {
                        // If we still don't have a location, return nil
                        promise(.success(nil))
                        
                        // Reset accuracy mode
                        self?.setAccuracyMode(.balanced)
                    }
                }
            } else {
                promise(.failure(NSError(domain: "LocationService", code: -2, userInfo: [NSLocalizedDescriptionKey: "Location permission denied"])))
            }
        }.eraseToAnyPublisher()
    }
    
    func searchLocations(query: String) -> AnyPublisher<[Location], Error> {
        return Future<[Location], Error> { promise in
            let geocoder = CLGeocoder()
            geocoder.geocodeAddressString(query) { placemarks, error in
                if let error = error {
                    promise(.failure(error))
                    return
                }
                
                let locations = placemarks?.compactMap { placemark -> Location? in
                    guard let name = placemark.name,
                          let location = placemark.location else {
                        return nil
                    }
                    
                    return Location(
                        id: UUID().uuidString,
                        name: name,
                        latitude: location.coordinate.latitude,
                        longitude: location.coordinate.longitude,
                        isSaved: false,
                        notes: nil
                    )
                } ?? []
                
                promise(.success(locations))
            }
        }.eraseToAnyPublisher()
    }
    
    func getSavedLocations() -> AnyPublisher<[Location], Never> {
        return savedLocationsSubject.eraseToAnyPublisher()
    }
    
    func saveLocation(_ location: Location) -> AnyPublisher<Bool, Error> {
        return Future<Bool, Error> { [weak self] promise in
            guard let self = self else {
                promise(.failure(NSError(domain: "LocationService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Service not available"])))
                return
            }
            
            var locations = self.savedLocationsSubject.value
            
            // Check if location already exists
            if let index = locations.firstIndex(where: { $0.id == location.id }) {
                locations[index] = location
            } else {
                // Create a new location with isSaved = true
                var newLocation = location
                newLocation.isSaved = true
                locations.append(newLocation)
            }
            
            self.savedLocationsSubject.send(locations)
            self.saveToDisk(locations)
            promise(.success(true))
        }.eraseToAnyPublisher()
    }
    
    func deleteLocation(withId id: String) -> AnyPublisher<Bool, Error> {
        return Future<Bool, Error> { [weak self] promise in
            guard let self = self else {
                promise(.failure(NSError(domain: "LocationService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Service not available"])))
                return
            }
            
            var locations = self.savedLocationsSubject.value
            locations.removeAll { $0.id == id }
            self.savedLocationsSubject.send(locations)
            self.saveToDisk(locations)
            promise(.success(true))
        }.eraseToAnyPublisher()
    }
    
    // MARK: - CLLocationManagerDelegate
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let newLocation = locations.last else { return }
        
        // Check if this is a significant movement
        if let previousLocation = locationSubject.value {
            let distance = newLocation.distance(from: previousLocation)
            let timeInterval = newLocation.timestamp.timeIntervalSince(previousLocation.timestamp)
            
            // If moved more than 100 meters in less than 5 minutes, consider as moving
            if distance > 100 && timeInterval < 300 {
                isMoving = true
                lastSignificantMovement = Date()
                
                // If moving, increase accuracy
                if currentAccuracyMode != .highAccuracy {
                    setAccuracyMode(.highAccuracy)
                }
            } else {
                // Check if we haven't moved significantly for a while
                if Date().timeIntervalSince(lastSignificantMovement) > significantMovementThreshold {
                    isMoving = false
                    
                    // If not moving, decrease accuracy to save battery
                    if currentAccuracyMode == .highAccuracy {
                        setAccuracyMode(.balanced)
                    }
                }
            }
        } else {
            // First location update
            lastSignificantMovement = Date()
        }
        
        // Update current location
        locationSubject.send(newLocation)
        
        // Cache the location
        cacheLocation(newLocation)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location manager failed with error: \(error.localizedDescription)")
        
        // If we get frequent errors, reduce accuracy to save battery
        if currentAccuracyMode == .highAccuracy {
            setAccuracyMode(.balanced)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        updatePermissionStatus(status)
    }
    
    // MARK: - Private Methods
    
    private func setupLocationManager() {
        locationManager.delegate = self
        setAccuracyMode(.balanced) // Start with balanced accuracy
        
        // Enable significant location changes to wake up the app when the device moves significantly
        locationManager.startMonitoringSignificantLocationChanges()
        
        // Check initial permission status
        let status = CLLocationManager.authorizationStatus()
        updatePermissionStatus(status)
    }
    
    private func setAccuracyMode(_ mode: AccuracyMode) {
        currentAccuracyMode = mode
        locationManager.desiredAccuracy = mode.desiredAccuracy
        locationManager.distanceFilter = mode.distanceFilter
        
        // Adjust update interval based on mode
        if mode == .lowPower {
            locationManager.stopUpdatingLocation()
            locationManager.startUpdatingLocation()
            
            // Schedule to stop updates after a while
            DispatchQueue.main.asyncAfter(deadline: .now() + 30) { [weak self] in
                self?.locationManager.stopUpdatingLocation()
            }
        } else {
            locationManager.stopUpdatingLocation()
            locationManager.startUpdatingLocation()
        }
    }
    
    private func requestFreshLocation() {
        // Temporarily increase accuracy for a fresh location
        let previousMode = currentAccuracyMode
        setAccuracyMode(.highAccuracy)
        
        locationManager.requestLocation()
        
        // Reset to previous mode after a short time
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) { [weak self] in
            self?.setAccuracyMode(previousMode)
        }
    }
    
    private func updatePermissionStatus(_ status: CLAuthorizationStatus) {
        let mappedStatus: LocationPermissionStatus
        
        switch status {
        case .notDetermined:
            mappedStatus = .notDetermined
        case .restricted:
            mappedStatus = .restricted
        case .denied:
            mappedStatus = .denied
        case .authorizedAlways:
            mappedStatus = .authorizedAlways
            locationManager.startUpdatingLocation()
        case .authorizedWhenInUse:
            mappedStatus = .authorizedWhenInUse
            locationManager.startUpdatingLocation()
        @unknown default:
            mappedStatus = .notDetermined
        }
        
        permissionStatusSubject.send(mappedStatus)
    }
    
    private func loadSavedLocations() {
        if let data = userDefaults.data(forKey: savedLocationsKey),
           let locations = try? JSONDecoder().decode([Location].self, from: data) {
            savedLocationsSubject.send(locations)
        }
    }
    
    private func saveToDisk(_ locations: [Location]) {
        if let data = try? JSONEncoder().encode(locations) {
            userDefaults.set(data, forKey: savedLocationsKey)
        }
    }
    
    // MARK: - Location Caching
    
    private func cacheLocation(_ location: CLLocation) {
        // Archive the location
        if let archivedLocation = try? NSKeyedArchiver.archivedData(withRootObject: location, requiringSecureCoding: false) {
            userDefaults.set(archivedLocation, forKey: cachedLocationKey)
            userDefaults.set(Date().timeIntervalSince1970, forKey: locationCacheTimestampKey)
        }
    }
    
    private func loadCachedLocation() -> CLLocation? {
        // Check if we have a cached location
        guard let cachedData = userDefaults.data(forKey: cachedLocationKey),
              let timestamp = userDefaults.object(forKey: locationCacheTimestampKey) as? TimeInterval else {
            return nil
        }
        
        // Check if the cached location is still valid (less than 1 hour old)
        let cacheAge = Date().timeIntervalSince1970 - timestamp
        if cacheAge > 3600 { // 1 hour
            return nil
        }
        
        // Unarchive the location
        if let location = try? NSKeyedUnarchiver.unarchiveTopLevelObjectWithData(cachedData) as? CLLocation {
            return location
        }
        
        return nil
    }
}