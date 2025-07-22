import Foundation
import Combine
import CoreLocation
import SwiftUI

class LocationManager: ObservableObject {
    private let locationService: LocationService
    private var cancellables = Set<AnyCancellable>()
    
    @Published var currentLocation: CLLocation?
    @Published var savedLocations: [Location] = []
    @Published var searchResults: [Location] = []
    @Published var permissionStatus: LocationPermissionStatus = .notDetermined
    @Published var isSearching = false
    @Published var searchQuery = ""
    @Published var selectedLocation: Location?
    
    init(locationService: LocationService) {
        self.locationService = locationService
        
        // Subscribe to location updates
        locationService.currentLocation
            .receive(on: DispatchQueue.main)
            .sink { [weak self] location in
                self?.currentLocation = location
            }
            .store(in: &cancellables)
        
        // Subscribe to permission status updates
        locationService.permissionStatus
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                self?.permissionStatus = status
            }
            .store(in: &cancellables)
        
        // Subscribe to saved locations updates
        locationService.getSavedLocations()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] locations in
                self?.savedLocations = locations
            }
            .store(in: &cancellables)
        
        // Request location permission if not determined
        if permissionStatus == .notDetermined {
            locationService.requestLocationPermission()
        }
    }
    
    func requestLocationPermission() {
        locationService.requestLocationPermission()
    }
    
    func getCurrentLocation() {
        locationService.getCurrentLocation()
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { completion in
                    if case .failure(let error) = completion {
                        print("Error getting current location: \(error.localizedDescription)")
                    }
                },
                receiveValue: { [weak self] location in
                    if let location = location {
                        self?.currentLocation = location
                    }
                }
            )
            .store(in: &cancellables)
    }
    
    func getCurrentLocation(completion: @escaping (CLLocation?) -> Void) {
        if let currentLocation = self.currentLocation {
            completion(currentLocation)
            return
        }
        
        locationService.getCurrentLocation()
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in
                    completion(nil)
                },
                receiveValue: { location in
                    self.currentLocation = location
                    completion(location)
                }
            )
            .store(in: &cancellables)
    }
    
    func searchLocations(query: String) {
        guard !query.isEmpty else {
            searchResults = []
            return
        }
        
        isSearching = true
        
        locationService.searchLocations(query: query)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    self?.isSearching = false
                    if case .failure(let error) = completion {
                        print("Error searching locations: \(error.localizedDescription)")
                    }
                },
                receiveValue: { [weak self] locations in
                    self?.searchResults = locations
                    self?.isSearching = false
                }
            )
            .store(in: &cancellables)
    }
    
    func saveLocation(_ location: Location) {
        locationService.saveLocation(location)
            .receive(on: DispatchQueue.main)
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
    
    func deleteLocation(withId id: String) {
        locationService.deleteLocation(withId: id)
            .receive(on: DispatchQueue.main)
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
    
    func saveCurrentLocation(name: String, notes: String? = nil) {
        guard let currentCLLocation = currentLocation else { return }
        
        let location = Location(
            from: currentCLLocation,
            name: name,
            isSaved: true,
            notes: notes
        )
        
        saveLocation(location)
    }
    
    func selectLocation(_ location: Location) {
        selectedLocation = location
    }
    
    func loadSavedLocations() {
        locationService.getSavedLocations()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] locations in
                self?.savedLocations = locations
            }
            .store(in: &cancellables)
    }
}