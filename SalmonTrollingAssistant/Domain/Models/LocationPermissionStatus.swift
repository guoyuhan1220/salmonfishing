import Foundation

enum LocationPermissionStatus: String, Codable {
    case notDetermined
    case restricted
    case denied
    case authorizedAlways
    case authorizedWhenInUse
}