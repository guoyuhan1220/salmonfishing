import Foundation

struct TideData: Identifiable, Codable {
    enum TideType: String, Codable {
        case high
        case low
        case rising
        case falling
    }
    
    let id: String
    let timestamp: Date
    let height: Double
    let type: TideType
    
    init(id: String = UUID().uuidString, timestamp: Date = Date(), height: Double, type: TideType) {
        self.id = id
        self.timestamp = timestamp
        self.height = height
        self.type = type
    }
    
    static func mockData() -> TideData {
        TideData(
            timestamp: Date(),
            height: 3.2,
            type: .rising
        )
    }
}