import Foundation
import Combine

class MockWeatherService: WeatherService {
    private var mockWeatherData: WeatherData
    private var mockForecastData: [WeatherData]
    
    init() {
        // Create mock data for testing
        let now = Date()
        
        mockWeatherData = WeatherData(
            timestamp: now,
            temperature: 68.5,
            windSpeed: 8.3,
            windDirection: "NW",
            precipitation: 0.0,
            cloudCover: 25,
            visibility: 10.0,
            pressure: 1012.5,
            humidity: 65,
            uvIndex: 6,
            waterTemperature: 58.2
        )
        
        // Create mock forecast data for 7 days
        var forecastData: [WeatherData] = []
        for i in 0..<7 {
            let forecastDate = Calendar.current.date(byAdding: .day, value: i, to: now) ?? now
            forecastData.append(
                WeatherData(
                    timestamp: forecastDate,
                    temperature: 65.0 + Double.random(in: -5...10),
                    windSpeed: 5.0 + Double.random(in: 0...10),
                    windDirection: ["N", "NE", "E", "SE", "S", "SW", "W", "NW"].randomElement() ?? "N",
                    precipitation: Double.random(in: 0...0.5),
                    cloudCover: Int.random(in: 0...100),
                    visibility: Double.random(in: 5...15),
                    pressure: 1010.0 + Double.random(in: -5...5),
                    humidity: Int.random(in: 50...90),
                    uvIndex: Int.random(in: 1...10),
                    waterTemperature: 55.0 + Double.random(in: -2...5)
                )
            )
        }
        mockForecastData = forecastData
    }
    
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, WeatherError> {
        // Simulate network delay
        return Just(mockWeatherData)
            .delay(for: .seconds(0.5), scheduler: RunLoop.main)
            .setFailureType(to: WeatherError.self)
            .eraseToAnyPublisher()
    }
    
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], WeatherError> {
        let daysToReturn = min(days, 7) // Limit to 7 days
        let forecast = Array(mockForecastData.prefix(daysToReturn))
        
        // Simulate network delay
        return Just(forecast)
            .delay(for: .seconds(0.5), scheduler: RunLoop.main)
            .setFailureType(to: WeatherError.self)
            .eraseToAnyPublisher()
    }
    
    func getWeather(for location: Location, at dateTime: Date) -> AnyPublisher<WeatherData, WeatherError> {
        // Find the closest forecast to the requested date/time
        let calendar = Calendar.current
        let requestedDay = calendar.startOfDay(for: dateTime)
        
        if let matchingForecast = mockForecastData.first(where: { calendar.isDate($0.timestamp, inSameDayAs: dateTime) }) {
            return Just(matchingForecast)
                .delay(for: .seconds(0.5), scheduler: RunLoop.main)
                .setFailureType(to: WeatherError.self)
                .eraseToAnyPublisher()
        } else {
            // If no matching forecast, return current weather
            return Just(mockWeatherData)
                .delay(for: .seconds(0.5), scheduler: RunLoop.main)
                .setFailureType(to: WeatherError.self)
                .eraseToAnyPublisher()
        }
    }
}