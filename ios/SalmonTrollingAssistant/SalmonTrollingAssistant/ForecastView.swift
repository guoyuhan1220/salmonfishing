import SwiftUI

struct ForecastView: View {
    let location: SimpleLocation
    @State private var selectedDayIndex = 0
    @State private var selectedTab = 0 // 0: Overview, 1: Hourly
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var appState = AppState.shared
    
    // Mock forecast data
    private let days = ["Today", "Tomorrow", "Wednesday", "Thursday", "Friday"]
    private let temperatures = [72, 68, 70, 75, 73]
    private let conditions = ["Sunny", "Partly Cloudy", "Cloudy", "Sunny", "Partly Cloudy"]
    private let windSpeeds = [8, 10, 12, 7, 9]
    private let windDirections = ["NW", "N", "NE", "E", "SE"]
    private let tideHeights = [3.2, 2.8, 3.5, 3.0, 2.5]
    private let tideTypes = ["Rising", "High", "Falling", "Low", "Rising"]
    private let tideIcons = ["arrow.up.right.circle.fill", "arrow.up.circle.fill", "arrow.down.right.circle.fill", "arrow.down.circle.fill", "arrow.up.right.circle.fill"]
    
    // Function to handle Smart Recommendations button tap
    private func showSmartRecommendations() {
        print("Smart Recommendations button tapped, navigating to tab 2")
        
        // Use the global AppState to switch tabs
        DispatchQueue.main.async {
            self.appState.switchToTab(2)
            self.presentationMode.wrappedValue.dismiss()
        }
    }
    
    // Hourly data
    private let hours = ["6 AM", "9 AM", "12 PM", "3 PM", "6 PM", "9 PM"]
    private let hourlyTemps = [
        [64, 68, 72, 71, 69, 65], // Today
        [60, 64, 68, 67, 65, 62], // Tomorrow
        [62, 66, 70, 69, 67, 64], // Wednesday
        [66, 70, 75, 74, 71, 68], // Thursday
        [65, 69, 73, 72, 70, 67]  // Friday
    ]
    private let hourlyConditions = [
        ["Sunny", "Sunny", "Sunny", "Sunny", "Partly Cloudy", "Clear"],
        ["Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Partly Cloudy", "Cloudy", "Cloudy"],
        ["Cloudy", "Cloudy", "Cloudy", "Partly Cloudy", "Partly Cloudy", "Clear"],
        ["Sunny", "Sunny", "Sunny", "Sunny", "Clear", "Clear"],
        ["Partly Cloudy", "Partly Cloudy", "Sunny", "Sunny", "Partly Cloudy", "Clear"]
    ]
    private let hourlyWindSpeeds = [
        [5, 7, 8, 8, 6, 4], // Today
        [7, 9, 10, 10, 8, 6], // Tomorrow
        [9, 11, 12, 12, 10, 8], // Wednesday
        [4, 6, 7, 7, 5, 3], // Thursday
        [6, 8, 9, 9, 7, 5]  // Friday
    ]
    private let hourlyWindDirections = [
        ["NW", "NW", "NW", "NW", "NW", "NW"],
        ["N", "N", "N", "N", "N", "N"],
        ["NE", "NE", "NE", "NE", "NE", "NE"],
        ["E", "E", "E", "E", "E", "E"],
        ["SE", "SE", "SE", "SE", "SE", "SE"]
    ]
    private let hourlyTideHeights = [
        [2.5, 3.0, 3.2, 3.1, 2.8, 2.4], // Today
        [2.0, 2.5, 2.8, 2.7, 2.4, 2.0], // Tomorrow
        [2.8, 3.2, 3.5, 3.4, 3.1, 2.7], // Wednesday
        [2.3, 2.7, 3.0, 2.9, 2.6, 2.2], // Thursday
        [1.8, 2.2, 2.5, 2.4, 2.1, 1.7]  // Friday
    ]
    private let hourlyTideTypes = [
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"],
        ["Rising", "Rising", "High", "Falling", "Falling", "Low"]
    ]
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Location header
                HStack {
                    VStack(alignment: .leading) {
                        Text(location.name)
                            .font(.title)
                            .fontWeight(.bold)
                        
                        Text("5-Day Forecast")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }
                .padding()
                
                // Day selector
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 15) {
                        ForEach(0..<5) { index in
                            Button(action: {
                                selectedDayIndex = index
                            }) {
                                VStack {
                                    Text(days[index])
                                        .font(.caption)
                                    
                                    Text("\(Calendar.current.component(.day, from: Date().addingTimeInterval(TimeInterval(86400 * index))))")
                                        .font(.title3)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 8)
                                .padding(.horizontal, 12)
                                .background(selectedDayIndex == index ? Color.blue : Color.clear)
                                .foregroundColor(selectedDayIndex == index ? .white : .primary)
                                .cornerRadius(10)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                
                // Tab selector
                Picker("View", selection: $selectedTab) {
                    Text("Overview").tag(0)
                    Text("Hourly").tag(1)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                ScrollView {
                    if selectedTab == 0 {
                        // Overview tab
                        VStack(alignment: .leading, spacing: 20) {
                            // Weather card
                            VStack(alignment: .leading, spacing: 15) {
                                HStack {
                                    Text("Weather")
                                        .font(.headline)
                                    Spacer()
                                }
                                
                                HStack(spacing: 20) {
                                    // Weather icon
                                    Image(systemName: weatherIcon(for: conditions[selectedDayIndex]))
                                        .font(.system(size: 50))
                                        .foregroundColor(.blue)
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text("\(temperatures[selectedDayIndex])°F")
                                            .font(.system(size: 36, weight: .bold))
                                        
                                        Text(conditions[selectedDayIndex])
                                            .font(.headline)
                                    }
                                    
                                    Spacer()
                                }
                                
                                Divider()
                                
                                // Weather details
                                HStack {
                                    VStack(alignment: .leading, spacing: 5) {
                                        HStack {
                                            Image(systemName: "wind")
                                            Text("Wind")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                        Text("\(windSpeeds[selectedDayIndex]) mph \(windDirections[selectedDayIndex])")
                                            .font(.headline)
                                    }
                                    
                                    Spacer()
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        HStack {
                                            Image(systemName: "humidity")
                                            Text("Humidity")
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                        Text("\(60 + selectedDayIndex * 5)%")
                                            .font(.headline)
                                    }
                                }
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                            
                            // Tide card
                            VStack(alignment: .leading, spacing: 15) {
                                HStack {
                                    Text("Tide")
                                        .font(.headline)
                                    Spacer()
                                }
                                
                                HStack(spacing: 20) {
                                    // Tide icon
                                    Image(systemName: tideIcons[selectedDayIndex])
                                        .font(.system(size: 50))
                                        .foregroundColor(.blue)
                                    
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text("\(String(format: "%.1f", tideHeights[selectedDayIndex])) ft")
                                            .font(.system(size: 36, weight: .bold))
                                        
                                        Text(tideTypes[selectedDayIndex])
                                            .font(.headline)
                                    }
                                    
                                    Spacer()
                                }
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                            
                            // Fishing conditions summary
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Fishing Conditions")
                                    .font(.headline)
                                
                                HStack {
                                    Image(systemName: "fish.fill")
                                        .foregroundColor(.green)
                                    Text("Good conditions for Coho and Chinook")
                                        .font(.subheadline)
                                }
                                
                                Text("Rising tide with moderate winds creates favorable conditions for salmon trolling.")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                            
                            // Smart Recommendations button
                            Button(action: showSmartRecommendations) {
                                HStack {
                                    Image(systemName: "wand.and.stars")
                                        .font(.title2)
                                    Text("Get Smart Recommendations")
                                        .fontWeight(.semibold)
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }
                            .padding(.horizontal)
                            .padding(.top, 10)
                        }
                    } else {
                        // Hourly tab
                        VStack(alignment: .leading, spacing: 20) {
                            // Hourly weather forecast
                            VStack(alignment: .leading, spacing: 15) {
                                Text("Hourly Weather")
                                    .font(.headline)
                                    .padding(.horizontal)
                                
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 20) {
                                        ForEach(0..<hours.count, id: \.self) { hourIndex in
                                            VStack(spacing: 10) {
                                                Text(hours[hourIndex])
                                                    .font(.caption)
                                                    .foregroundColor(.secondary)
                                                
                                                Image(systemName: weatherIcon(for: hourlyConditions[selectedDayIndex][hourIndex]))
                                                    .font(.system(size: 24))
                                                    .foregroundColor(.blue)
                                                
                                                Text("\(hourlyTemps[selectedDayIndex][hourIndex])°F")
                                                    .font(.headline)
                                                
                                                HStack(spacing: 2) {
                                                    Image(systemName: "wind")
                                                        .font(.caption)
                                                    Text("\(hourlyWindSpeeds[selectedDayIndex][hourIndex])")
                                                        .font(.caption)
                                                }
                                            }
                                            .frame(width: 70)
                                            .padding(.vertical)
                                            .background(Color(.secondarySystemBackground))
                                            .cornerRadius(10)
                                        }
                                    }
                                    .padding(.horizontal)
                                }
                            }
                            
                            // Hourly wind details
                            VStack(alignment: .leading, spacing: 15) {
                                Text("Wind Details")
                                    .font(.headline)
                                    .padding(.horizontal)
                                
                                VStack(spacing: 15) {
                                    ForEach(0..<hours.count, id: \.self) { hourIndex in
                                        HStack {
                                            Text(hours[hourIndex])
                                                .font(.subheadline)
                                                .frame(width: 60, alignment: .leading)
                                            
                                            Image(systemName: "wind")
                                                .foregroundColor(.blue)
                                            
                                            Text("\(hourlyWindSpeeds[selectedDayIndex][hourIndex]) mph")
                                                .font(.subheadline)
                                            
                                            Text(hourlyWindDirections[selectedDayIndex][hourIndex])
                                                .font(.subheadline)
                                                .foregroundColor(.secondary)
                                            
                                            Spacer()
                                            
                                            // Wind direction arrow
                                            Image(systemName: "arrow.up")
                                                .rotationEffect(windDirectionAngle(for: hourlyWindDirections[selectedDayIndex][hourIndex]))
                                                .foregroundColor(.blue)
                                        }
                                        .padding(.horizontal)
                                        
                                        if hourIndex < hours.count - 1 {
                                            Divider()
                                                .padding(.horizontal)
                                        }
                                    }
                                }
                                .padding(.vertical)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(10)
                                .padding(.horizontal)
                            }
                            
                            // Hourly tide details
                            VStack(alignment: .leading, spacing: 15) {
                                Text("Tide Details")
                                    .font(.headline)
                                    .padding(.horizontal)
                                
                                VStack(spacing: 15) {
                                    ForEach(0..<hours.count, id: \.self) { hourIndex in
                                        HStack {
                                            Text(hours[hourIndex])
                                                .font(.subheadline)
                                                .frame(width: 60, alignment: .leading)
                                            
                                            Image(systemName: tideIcon(for: hourlyTideTypes[selectedDayIndex][hourIndex]))
                                                .foregroundColor(.blue)
                                            
                                            Text("\(String(format: "%.1f", hourlyTideHeights[selectedDayIndex][hourIndex])) ft")
                                                .font(.subheadline)
                                            
                                            Spacer()
                                            
                                            Text(hourlyTideTypes[selectedDayIndex][hourIndex])
                                                .font(.subheadline)
                                                .foregroundColor(.secondary)
                                        }
                                        .padding(.horizontal)
                                        
                                        if hourIndex < hours.count - 1 {
                                            Divider()
                                                .padding(.horizontal)
                                        }
                                    }
                                }
                                .padding(.vertical)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(10)
                                .padding(.horizontal)
                            }
                            
                            // Smart Recommendations button
                            Button(action: showSmartRecommendations) {
                                HStack {
                                    Image(systemName: "wand.and.stars")
                                        .font(.title2)
                                    Text("Get Smart Recommendations")
                                        .fontWeight(.semibold)
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }
                            .padding(.horizontal)
                            .padding(.top, 10)
                        }
                        .padding(.bottom)
                    }
                }
            }
            .navigationBarTitle("Forecast", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    private func weatherIcon(for condition: String) -> String {
        let condition = condition.lowercased()
        if condition.contains("clear") || condition.contains("sunny") {
            return "sun.max.fill"
        } else if condition.contains("partly cloudy") {
            return "cloud.sun.fill"
        } else if condition.contains("cloudy") {
            return "cloud.fill"
        } else if condition.contains("rain") {
            return "cloud.rain.fill"
        } else if condition.contains("thunder") {
            return "cloud.bolt.fill"
        } else if condition.contains("snow") {
            return "cloud.snow.fill"
        } else {
            return "cloud.fill"
        }
    }
    
    private func tideIcon(for tideType: String) -> String {
        let type = tideType.lowercased()
        if type.contains("rising") {
            return "arrow.up.right.circle.fill"
        } else if type.contains("high") {
            return "arrow.up.circle.fill"
        } else if type.contains("falling") {
            return "arrow.down.right.circle.fill"
        } else if type.contains("low") {
            return "arrow.down.circle.fill"
        } else {
            return "circle.fill"
        }
    }
    
    private func windDirectionAngle(for direction: String) -> Angle {
        switch direction {
        case "N": return .degrees(0)
        case "NE": return .degrees(45)
        case "E": return .degrees(90)
        case "SE": return .degrees(135)
        case "S": return .degrees(180)
        case "SW": return .degrees(225)
        case "W": return .degrees(270)
        case "NW": return .degrees(315)
        default: return .degrees(0)
        }
    }
}