import Foundation
import CoreLocation

struct Location: Identifiable, Codable, Equatable {
    let id: String
    let name: String
    let latitude: Double
    let longitude: Double
    let isSaved: Bool
    let notes: String?
    
    init(id: String = UUID().uuidString, name: String, latitude: Double, longitude: Double, isSaved: Bool = false, notes: String? = nil) {
        self.id = id
        self.name = name
        self.latitude = latitude
        self.longitude = longitude
        self.isSaved = isSaved
        self.notes = notes
    }
    
    init(from location: CLLocation, name: String, isSaved: Bool = false, notes: String? = nil) {
        self.id = UUID().uuidString
        self.name = name
        self.latitude = location.coordinate.latitude
        self.longitude = location.coordinate.longitude
        self.isSaved = isSaved
        self.notes = notes
    }
    
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
    
    static func == (lhs: Location, rhs: Location) -> Bool {
        lhs.id == rhs.id
    }
    
    static func mockData() -> [Location] {
        [
            Location(id: "1", name: "Puget Sound", latitude: 47.6062, longitude: -122.3321),
            Location(id: "2", name: "Hood Canal", latitude: 47.6792, longitude: -122.9646),
            Location(id: "3", name: "Strait of Juan de Fuca", latitude: 48.2285, longitude: -124.1009)
        ]
    }
}