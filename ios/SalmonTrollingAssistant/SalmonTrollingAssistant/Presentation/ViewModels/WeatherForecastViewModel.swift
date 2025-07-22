import Foundation
import Combine

class WeatherForecastViewModel: ObservableObject {
    @Published var currentWeather: WeatherData?
    @Published var forecast: [WeatherData] = []
    @Published var selectedDate: Date = Date()
    @Published var selectedWeather: WeatherData?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    
    // Tide data
    @Published var currentTide: TideData?
    @Published var tideForecast: [TideData] = []
    @Published var selectedTide: TideData?
    
    private let weatherService: WeatherService
    private let tideService: TideService
    private var cancellables = Set<AnyCancellable>()
    
    // Date range for the date picker (today to 7 days in the future)
    let minimumDate: Date = Date()
    var maximumDate: Date {
        Calendar.current.date(byAdding: .day, value: 7, to: Date()) ?? Date()
    }
    
    init(weatherService: WeatherService, tideService: TideService) {
        self.weatherService = weatherService
        self.tideService = tideService
    }
    
    func loadCurrentWeather(for location: Location) {
        isLoading = true
        errorMessage = nil
        
        weatherService.getCurrentWeather(for: location)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    if case .failure(let error) = completion {
                        self?.errorMessage = "Failed to load current weather: \(error.localizedDescription)"
                        self?.isLoading = false
                    }
                },
                receiveValue: { [weak self] weatherData in
                    self?.currentWeather = weatherData
                    // Also load current tide data
                    self?.loadCurrentTide(for: location)
                }
            )
            .store(in: &cancellables)
    }
    
    private func loadCurrentTide(for location: Location) {
        tideService.getCurrentTide(for: location)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    self?.isLoading = false
                    if case .failure(let error) = completion {
                        self?.errorMessage = "Failed to load tide data: \(error.localizedDescription)"
                    }
                },
                receiveValue: { [weak self] tideData in
                    self?.currentTide = tideData
                    self?.isLoading = false
                }
            )
            .store(in: &cancellables)
    }
    
    func loadForecast(for location: Location) {
        isLoading = true
        errorMessage = nil
        
        weatherService.getForecast(for: location, days: 7)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    if case .failure(let error) = completion {
                        self?.errorMessage = "Failed to load forecast: \(error.localizedDescription)"
                        self?.isLoading = false
                    }
                },
                receiveValue: { [weak self] forecast in
                    self?.forecast = forecast
                    // Also load tide forecast
                    self?.loadTideForecast(for: location)
                }
            )
            .store(in: &cancellables)
    }
    
    private func loadTideForecast(for location: Location) {
        tideService.getTidePredictions(for: location, days: 7)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    self?.isLoading = false
                    if case .failure(let error) = completion {
                        self?.errorMessage = "Failed to load tide forecast: \(error.localizedDescription)"
                    }
                },
                receiveValue: { [weak self] forecast in
                    self?.tideForecast = forecast
                    self?.isLoading = false
                }
            )
            .store(in: &cancellables)
    }
    
    func loadWeatherForSelectedDate(location: Location) {
        isLoading = true
        errorMessage = nil
        
        // Load both weather and tide data for the selected date
        let weatherPublisher = weatherService.getWeather(for: location, at: selectedDate)
        let tidePublisher = tideService.getTideForDateTime(for: location, dateTime: selectedDate)
        
        // Combine both publishers
        Publishers.Zip(weatherPublisher, tidePublisher)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    self?.isLoading = false
                    if case .failure(let error) = completion {
                        self?.errorMessage = "Failed to load data for selected date: \(error.localizedDescription)"
                    }
                },
                receiveValue: { [weak self] weatherData, tideData in
                    self?.selectedWeather = weatherData
                    self?.selectedTide = tideData
                }
            )
            .store(in: &cancellables)
    }
    
    func dateString(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }
    
    func timeString(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}