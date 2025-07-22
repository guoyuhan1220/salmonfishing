import SwiftUI

struct CatchAnalyticsView: View {
    @ObservedObject var viewModel: CatchAnalyticsViewModel
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Personalized Recommendations
                    AnalyticsCard(title: "Personalized Recommendations") {
                        if viewModel.personalizedRecommendations.isEmpty {
                            Text("Start logging your catches to get personalized recommendations!")
                                .font(.body)
                                .padding(.vertical, 4)
                        } else {
                            ForEach(viewModel.personalizedRecommendations, id: \.self) { recommendation in
                                HStack(alignment: .top) {
                                    Text("•")
                                    Text(recommendation)
                                        .font(.body)
                                }
                                .padding(.vertical, 2)
                            }
                        }
                    }
                    
                    // Catch Count by Species
                    AnalyticsCard(title: "Catch Count by Species") {
                        if viewModel.catchCountBySpecies.isEmpty {
                            Text("No catch data available")
                                .font(.body)
                        } else {
                            ForEach(viewModel.catchCountBySpecies.sorted(by: { $0.value > $1.value }), id: \.key) { species, count in
                                HStack {
                                    Text(species.rawValue.capitalized)
                                        .font(.body)
                                    Spacer()
                                    Text("\(count)")
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Average Size by Species
                    AnalyticsCard(title: "Average Size by Species (inches)") {
                        if viewModel.averageSizeBySpecies.isEmpty {
                            Text("No size data available")
                                .font(.body)
                        } else {
                            ForEach(viewModel.averageSizeBySpecies.sorted(by: { $0.value > $1.value }), id: \.key) { species, size in
                                HStack {
                                    Text(species.rawValue.capitalized)
                                        .font(.body)
                                    Spacer()
                                    Text(viewModel.formatDouble(size))
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Average Weight by Species
                    AnalyticsCard(title: "Average Weight by Species (lbs)") {
                        if viewModel.averageWeightBySpecies.isEmpty {
                            Text("No weight data available")
                                .font(.body)
                        } else {
                            ForEach(viewModel.averageWeightBySpecies.sorted(by: { $0.value > $1.value }), id: \.key) { species, weight in
                                HStack {
                                    Text(species.rawValue.capitalized)
                                        .font(.body)
                                    Spacer()
                                    Text(viewModel.formatDouble(weight))
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Most Successful Equipment
                    AnalyticsCard(title: "Most Successful Equipment") {
                        if viewModel.mostSuccessfulEquipment.isEmpty {
                            Text("No equipment data available")
                                .font(.body)
                        } else {
                            ForEach(viewModel.mostSuccessfulEquipment.prefix(5), id: \.0) { equipment, count in
                                HStack {
                                    Text(equipment)
                                        .font(.body)
                                    Spacer()
                                    Text("\(count)")
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Most Successful Locations
                    AnalyticsCard(title: "Most Successful Locations") {
                        if viewModel.mostSuccessfulLocations.isEmpty {
                            Text("No location data available")
                                .font(.body)
                        } else {
                            ForEach(viewModel.mostSuccessfulLocations.prefix(5), id: \.0) { location, count in
                                HStack {
                                    Text(location)
                                        .font(.body)
                                    Spacer()
                                    Text("\(count)")
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Catch Count by Month
                    AnalyticsCard(title: "Catch Count by Month") {
                        if viewModel.catchCountByMonth.isEmpty {
                            Text("No monthly data available")
                                .font(.body)
                        } else {
                            ForEach(0..<12, id: \.self) { month in
                                HStack {
                                    Text(viewModel.getMonthName(for: month))
                                        .font(.body)
                                    Spacer()
                                    Text("\(viewModel.catchCountByMonth[month] ?? 0)")
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    
                    // Catch Trend Over Time
                    AnalyticsCard(title: "Catch Trend Over Time") {
                        if viewModel.catchTrendOverTime.isEmpty {
                            Text("No trend data available")
                                .font(.body)
                        } else {
                            ForEach(viewModel.catchTrendOverTime, id: \.0) { date, count in
                                HStack {
                                    Text(viewModel.formatDate(date))
                                        .font(.body)
                                    Spacer()
                                    Text("\(count)")
                                        .font(.body)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Catch Analytics")
        }
    }
}

struct AnalyticsCard<Content: View>: View {
    let title: String
    let content: Content
    
    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
                .fontWeight(.bold)
            
            content
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
    }
}

struct CatchAnalyticsView_Previews: PreviewProvider {
    static var previews: some View {
        let authService = AuthenticationServiceImpl()
        let catchLoggingService = CatchLoggingServiceImpl(authService: authService)
        let catchAnalyticsService = CatchAnalyticsServiceImpl(catchLoggingService: catchLoggingService)
        let viewModel = CatchAnalyticsViewModel(catchAnalyticsService: catchAnalyticsService)
        
        return CatchAnalyticsView(viewModel: viewModel)
    }
}