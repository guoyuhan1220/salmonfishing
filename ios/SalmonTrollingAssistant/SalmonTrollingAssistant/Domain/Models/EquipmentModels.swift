import Foundation

struct EquipmentItem: Identifiable {
    let id: String
    let name: String
    let description: String
    let imageUrl: String?
    let specifications: [String: String]
}