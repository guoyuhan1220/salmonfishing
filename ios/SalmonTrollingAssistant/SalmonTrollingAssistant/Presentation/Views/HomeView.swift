import SwiftUI

struct HomeView: View {
    @StateObject private var weatherViewModel = WeatherForecastViewModel()
    @State private var selectedDate = Date()
    @State private var showingDatePicker = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Offline indicator at the top
                    OfflineIndicatorView()
                    
                    // Current conditions section
                    currentConditionsSection
                    
                    // Quick recommendations section
                    quickRecommendationsSection
                    
                    // Saved locations carousel
                    savedLocationsSection
                    
                    // Date/time selector
                    dateTimeSelectorSection
                }
                .padding()
            }
            .navigationTitle("Salmon Trolling")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        // Action for settings or refresh
                    }) {
                        Image(systemName: "gear")
                    }
                }
            }
            .sheet(isPresented: $showingDatePicker) {
                DateTimeSelectorView(selectedDate: $selectedDate, isPresented: $showingDatePicker)
            }
        }
        .onAppear {
            // Load data when view appears
            weatherViewModel.loadCurrentWeather()
        }
    }
    
    // MARK: - UI Components
    
    private var currentConditionsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Current Conditions")
                .font(.headline)
                .padding(.bottom, 5)
            
            HStack(spacing: 20) {
                // Weather condition
                VStack {
                    Image(systemName: weatherViewModel.currentWeather?.cloudCover ?? 0 > 50 ? "cloud" : "sun.max")
                        .font(.system(size: 40))
                    Text("\(weatherViewModel.currentWeather?.temperature.rounded() ?? 0, specifier: "%.0f")°")
                        .font(.title2)
                    Text("Feels like \(weatherViewModel.currentWeather?.temperature.rounded() ?? 0, specifier: "%.0f")°")
                        .font(.caption)
                }
                .frame(width: 100)
                
                Divider()
                    .frame(height: 70)
                
                // Tide information
                VStack(alignment: .leading) {
                    Text("Tide: \(weatherViewModel.currentTide?.type.rawValue ?? "Unknown")")
                    Text("Height: \(weatherViewModel.currentTide?.height ?? 0, specifier: "%.1f") ft")
                    if let nextTide = weatherViewModel.currentTide?.nextHighTide {
                        Text("Next High: \(formatTime(from: nextTide.timestamp))")
                            .font(.caption)
                    }
                }
            }
            .padding()
            .background(Color(.systemBackground))
            .cornerRadius(10)
            .shadow(radius: 2)
        }
    }
    
    private var quickRecommendationsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Quick Recommendations")
                .font(.headline)
                .padding(.bottom, 5)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 15) {
                    ForEach(0..<3) { _ in
                        recommendationCard
                    }
                }
            }
        }
    }
    
    private var recommendationCard: some View {
        VStack(alignment: .leading) {
            Image(systemName: "lasso")
                .font(.system(size: 30))
                .padding(.bottom, 5)
            
            Text("Flasher: Green Glow")
                .font(.subheadline)
                .bold()
            
            Text("Perfect for current low light conditions")
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(2)
        }
        .padding()
        .frame(width: 160, height: 140)
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(radius: 2)
    }
    
    private var savedLocationsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Saved Locations")
                .font(.headline)
                .padding(.bottom, 5)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 15) {
                    ForEach(0..<4) { index in
                        locationCard(name: "Location \(index + 1)")
                    }
                }
            }
        }
    }
    
    private func locationCard(name: String) -> some View {
        Button(action: {
            // Select this location
        }) {
            VStack {
                Image(systemName: "mappin.circle.fill")
                    .font(.system(size: 30))
                    .foregroundColor(.blue)
                
                Text(name)
                    .font(.subheadline)
            }
            .padding()
            .frame(width: 120, height: 100)
            .background(Color(.systemBackground))
            .cornerRadius(10)
            .shadow(radius: 2)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var dateTimeSelectorSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Plan Your Trip")
                .font(.headline)
                .padding(.bottom, 5)
            
            Button(action: {
                showingDatePicker = true
            }) {
                HStack {
                    Image(systemName: "calendar")
                        .font(.system(size: 20))
                    
                    Text(formatDate(selectedDate))
                        .font(.subheadline)
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(10)
                .shadow(radius: 2)
            }
            .buttonStyle(PlainButtonStyle())
        }
    }
    
    // MARK: - Helper Functions
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    private func formatTime(from timestamp: TimeInterval) -> String {
        let date = Date(timeIntervalSince1970: timestamp)
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        HomeView()
    }
}
