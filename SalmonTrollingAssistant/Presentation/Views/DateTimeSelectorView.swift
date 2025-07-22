import SwiftUI

struct DateTimeSelectorView: View {
    @ObservedObject var viewModel: WeatherForecastViewModel
    let location: Location
    
    var body: some View {
        VStack(spacing: 16) {
            Text("Select Date and Time")
                .font(.headline)
            
            DatePicker(
                "Select Date",
                selection: $viewModel.selectedDate,
                in: viewModel.minimumDate...viewModel.maximumDate,
                displayedComponents: [.date, .hourAndMinute]
            )
            .datePickerStyle(GraphicalDatePickerStyle())
            .padding(.horizontal)
            
            Button(action: {
                viewModel.loadWeatherForSelectedDate(location: location)
            }) {
                Text("Get Forecast")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .padding(.horizontal)
            
            if viewModel.isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else {
                ScrollView {
                    VStack(spacing: 16) {
                        if let selectedWeather = viewModel.selectedWeather {
                            WeatherDetailView(weather: selectedWeather)
                        }
                        
                        if let selectedTide = viewModel.selectedTide {
                            TideDetailView(tide: selectedTide)
                        }
                        
                        if let errorMessage = viewModel.errorMessage {
                            Text(errorMessage)
                                .foregroundColor(.red)
                                .padding()
                        }
                    }
                }
            }
            
            Spacer()
        }
        .padding()
        .navigationTitle("Weather Forecast")
    }
}

struct WeatherDetailView: View {
    let weather: WeatherData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Weather Details")
                .font(.headline)
                .padding(.bottom, 8)
            
            HStack {
                VStack(alignment: .leading) {
                    DetailRow(label: "Temperature", value: "\(String(format: "%.1f", weather.temperature))°F")
                    DetailRow(label: "Wind", value: "\(String(format: "%.1f", weather.windSpeed)) mph \(weather.windDirection)")
                    DetailRow(label: "Precipitation", value: "\(String(format: "%.2f", weather.precipitation)) in")
                    DetailRow(label: "Cloud Cover", value: "\(weather.cloudCover)%")
                }
                
                Spacer()
                
                VStack(alignment: .leading) {
                    DetailRow(label: "Visibility", value: "\(String(format: "%.1f", weather.visibility)) mi")
                    DetailRow(label: "Pressure", value: "\(String(format: "%.1f", weather.pressure)) hPa")
                    DetailRow(label: "Humidity", value: "\(weather.humidity)%")
                    DetailRow(label: "UV Index", value: "\(weather.uvIndex)")
                }
            }
            
            if let waterTemp = weather.waterTemperature {
                DetailRow(label: "Water Temperature", value: "\(String(format: "%.1f", waterTemp))°F")
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
        .padding(.horizontal)
    }
}

struct DetailRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label + ":")
                .fontWeight(.medium)
            Text(value)
                .fontWeight(.regular)
        }
        .padding(.vertical, 2)
    }
}

struct TideDetailView: View {
    let tide: TideData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Tide Details")
                .font(.headline)
                .padding(.bottom, 8)
            
            HStack {
                VStack(alignment: .leading) {
                    DetailRow(label: "Current Height", value: "\(String(format: "%.2f", tide.height)) ft")
                    DetailRow(label: "Tide Type", value: getTideTypeDisplay(tide.type))
                }
                
                Spacer()
                
                VStack(alignment: .leading) {
                    if let nextHigh = tide.nextHighTide {
                        DetailRow(
                            label: "Next High Tide",
                            value: "\(formatTime(nextHigh.timestamp)) (\(String(format: "%.2f", nextHigh.height)) ft)"
                        )
                    }
                    
                    if let nextLow = tide.nextLowTide {
                        DetailRow(
                            label: "Next Low Tide",
                            value: "\(formatTime(nextLow.timestamp)) (\(String(format: "%.2f", nextLow.height)) ft)"
                        )
                    }
                }
            }
            
            // Simple tide chart visualization
            TideChartView(tide: tide)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
        .padding(.horizontal)
    }
    
    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    private func getTideTypeDisplay(_ tideType: TideType) -> String {
        switch tideType {
        case .high:
            return "High Tide"
        case .low:
            return "Low Tide"
        case .rising:
            return "Rising Tide"
        case .falling:
            return "Falling Tide"
        }
    }
}

struct TideChartView: View {
    let tide: TideData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Tide Chart")
                .fontWeight(.medium)
                .padding(.bottom, 4)
            
            ZStack(alignment: .bottom) {
                // Background
                Rectangle()
                    .fill(Color(.systemGray6))
                    .frame(height: 60)
                
                // Water level
                Rectangle()
                    .fill(Color.blue.opacity(0.6))
                    .frame(height: CGFloat(min(tide.height / 4.0, 1.0)) * 60)
                
                // Current tide marker
                Rectangle()
                    .fill(Color.red)
                    .frame(height: 2)
                    .offset(y: -CGFloat(min(tide.height / 4.0, 1.0)) * 60 / 2)
                
                // Tide type indicator
                Text(getTideTypeDisplay(tide.type))
                    .font(.caption)
                    .padding(4)
                    .background(Color.white.opacity(0.7))
                    .cornerRadius(4)
            }
            
            // Tide scale
            HStack {
                Text("0 ft")
                    .font(.caption)
                Spacer()
                Text("4.0 ft")
                    .font(.caption)
            }
        }
    }
    
    private func getTideTypeDisplay(_ tideType: TideType) -> String {
        switch tideType {
        case .high:
            return "High Tide"
        case .low:
            return "Low Tide"
        case .rising:
            return "Rising Tide"
        case .falling:
            return "Falling Tide"
        }
    }
}

struct DateTimeSelectorView_Previews: PreviewProvider {
    static var previews: some View {
        let mockWeatherService = MockWeatherService()
        let mockTideService = MockTideService()
        let viewModel = WeatherForecastViewModel(weatherService: mockWeatherService, tideService: mockTideService)
        let location = Location(id: "1", name: "Seattle", latitude: 47.6062, longitude: -122.3321)
        
        return NavigationView {
            DateTimeSelectorView(viewModel: viewModel, location: location)
        }
    }
}