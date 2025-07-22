import Foundation

struct WeatherData: Identifiable, Codable {
    let id: String
    let timestamp: Date
    let temperature: Double
    let condition: String
    let windSpeed: Double
    let windDirection: String
    let humidity: Int
    
    init(id: String = UUID().uuidString,
         timestamp: Date = Date(),
         temperature: Double,
         condition: String,
         windSpeed: Double,
         windDirection: String,
         humidity: Int) {
        self.id = id
        self.timestamp = timestamp
        self.temperature = temperature
        self.condition = condition
        self.windSpeed = windSpeed
        self.windDirection = windDirection
        self.humidity = humidity
    }
    
    static func mockData() -> WeatherData {
        WeatherData(
            timestamp: Date(),
            temperature: 68.5,
            condition: "Partly Cloudy",
            windSpeed: 8.5,
            windDirection: "NW",
            humidity: 65
        )
    }
}