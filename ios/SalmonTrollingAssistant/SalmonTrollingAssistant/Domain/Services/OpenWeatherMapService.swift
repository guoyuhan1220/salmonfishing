import Foundation
import Combine

class OpenWeatherMapService: WeatherService {
    private let apiKey: String
    private let baseUrl = "https://api.openweathermap.org/data/2.5"
    private let session: URLSession
    
    init(apiKey: String, session: URLSession = .shared) {
        self.apiKey = apiKey
        self.session = session
    }
    
    func getCurrentWeather(for location: Location) -> AnyPublisher<WeatherData, WeatherError> {
        let endpoint = "\(baseUrl)/weather?lat=\(location.latitude)&lon=\(location.longitude)&units=imperial&appid=\(apiKey)"
        
        guard let url = URL(string: endpoint) else {
            return Fail(error: WeatherError.invalidLocation).eraseToAnyPublisher()
        }
        
        return fetchData(from: url)
            .map { (data: CurrentWeatherResponse) -> WeatherData in
                self.mapCurrentResponseToWeatherData(data)
            }
            .eraseToAnyPublisher()
    }
    
    func getForecast(for location: Location, days: Int) -> AnyPublisher<[WeatherData], WeatherError> {
        // OpenWeatherMap's free tier only provides 5 day / 3 hour forecast
        // We'll use the one-call API for daily forecasts
        let endpoint = "\(baseUrl)/onecall?lat=\(location.latitude)&lon=\(location.longitude)&exclude=minutely,hourly&units=imperial&appid=\(apiKey)"
        
        guard let url = URL(string: endpoint) else {
            return Fail(error: WeatherError.invalidLocation).eraseToAnyPublisher()
        }
        
        return fetchData(from: url)
            .map { (response: OneCallResponse) -> [WeatherData] in
                // Limit to requested number of days (max 7)
                let limitedDays = min(days, 7)
                let dailyForecasts = Array(response.daily.prefix(limitedDays))
                return dailyForecasts.map { self.mapDailyForecastToWeatherData($0) }
            }
            .eraseToAnyPublisher()
    }
    
    func getWeather(for location: Location, at dateTime: Date) -> AnyPublisher<WeatherData, WeatherError> {
        // For historical data (past dates), we would need to use a different API endpoint
        // For future dates, we'll use the forecast data
        
        let now = Date()
        if dateTime <= now {
            // For current or past dates, return current weather
            return getCurrentWeather(for: location)
        } else {
            // For future dates, get forecast and find the closest match
            return getForecast(for: location, days: 7)
                .map { forecasts -> WeatherData in
                    // Find the forecast closest to the requested date/time
                    let closest = forecasts.min(by: { abs($0.timestamp.timeIntervalSince(dateTime)) < abs($1.timestamp.timeIntervalSince(dateTime)) })
                    return closest ?? forecasts.first!
                }
                .eraseToAnyPublisher()
        }
    }
    
    // MARK: - Private Helper Methods
    
    private func fetchData<T: Decodable>(from url: URL) -> AnyPublisher<T, WeatherError> {
        return session.dataTaskPublisher(for: url)
            .mapError { error -> WeatherError in
                return .networkError(error)
            }
            .flatMap { data, response -> AnyPublisher<T, WeatherError> in
                guard let httpResponse = response as? HTTPURLResponse else {
                    return Fail(error: WeatherError.invalidResponse).eraseToAnyPublisher()
                }
                
                guard 200..<300 ~= httpResponse.statusCode else {
                    return Fail(error: WeatherError.serverError(httpResponse.statusCode)).eraseToAnyPublisher()
                }
                
                return Just(data)
                    .decode(type: T.self, decoder: JSONDecoder())
                    .mapError { error -> WeatherError in
                        return .decodingError(error)
                    }
                    .eraseToAnyPublisher()
            }
            .eraseToAnyPublisher()
    }
    
    private func mapCurrentResponseToWeatherData(_ response: CurrentWeatherResponse) -> WeatherData {
        return WeatherData(
            timestamp: Date(timeIntervalSince1970: TimeInterval(response.dt)),
            temperature: response.main.temp,
            windSpeed: response.wind.speed,
            windDirection: mapWindDegreesToDirection(response.wind.deg),
            precipitation: response.rain?.lastHour ?? 0.0,
            cloudCover: response.clouds.all,
            visibility: Double(response.visibility) / 1000.0, // Convert from meters to kilometers
            pressure: response.main.pressure,
            humidity: response.main.humidity,
            uvIndex: 0, // UV Index not available in this endpoint
            waterTemperature: nil // Water temperature not available
        )
    }
    
    private func mapDailyForecastToWeatherData(_ daily: DailyForecast) -> WeatherData {
        return WeatherData(
            timestamp: Date(timeIntervalSince1970: TimeInterval(daily.dt)),
            temperature: daily.temp.day,
            windSpeed: daily.windSpeed,
            windDirection: mapWindDegreesToDirection(daily.windDeg),
            precipitation: daily.rain ?? 0.0,
            cloudCover: daily.clouds,
            visibility: 10.0, // Visibility not provided in daily forecast
            pressure: daily.pressure,
            humidity: daily.humidity,
            uvIndex: Int(daily.uvi),
            waterTemperature: nil // Water temperature not available
        )
    }
    
    private func mapWindDegreesToDirection(_ degrees: Int) -> String {
        let directions = ["N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"]
        let index = Int((Double(degrees) / 22.5) + 0.5) % 16
        return directions[index]
    }
}

// MARK: - API Response Models

struct CurrentWeatherResponse: Decodable {
    let dt: Int
    let main: MainWeather
    let wind: Wind
    let clouds: Clouds
    let rain: Rain?
    let visibility: Int
    
    struct MainWeather: Decodable {
        let temp: Double
        let pressure: Double
        let humidity: Int
    }
    
    struct Wind: Decodable {
        let speed: Double
        let deg: Int
    }
    
    struct Clouds: Decodable {
        let all: Int
    }
    
    struct Rain: Decodable {
        let lastHour: Double
        
        enum CodingKeys: String, CodingKey {
            case lastHour = "1h"
        }
    }
}

struct OneCallResponse: Decodable {
    let daily: [DailyForecast]
}

struct DailyForecast: Decodable {
    let dt: Int
    let temp: Temperature
    let pressure: Double
    let humidity: Int
    let windSpeed: Double
    let windDeg: Int
    let clouds: Int
    let rain: Double?
    let uvi: Double
    
    struct Temperature: Decodable {
        let day: Double
        let min: Double
        let max: Double
        let night: Double
        let eve: Double
        let morn: Double
    }
}