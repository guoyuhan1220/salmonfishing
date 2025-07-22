import SwiftUI

struct SmartRecommendationsView: View {
    let userGear: [FishingGear]
    let selectedLocation: SimpleLocation
    let weatherConditions: String
    let windSpeed: Int
    let tideType: String
    
    @State private var recommendationMode: RecommendationMode = .optimal
    @State private var showingAIProcessing = false
    @State private var processingProgress: CGFloat = 0.0
    @State private var showingGearDetail = false
    @State private var selectedGear: FishingGear?
    
    // Recommendation data
    private var optimalRecommendations: [RecommendationCategory] {
        return [
            RecommendationCategory(
                name: "Flashers",
                items: [
                    RecommendationItem(
                        name: "Hot Spot Flasher - UV Green",
                        description: "Perfect for today's light conditions at \(selectedLocation.name)",
                        matchScore: 95,
                        isInUserGear: userGear.contains(where: { $0.name.contains("Hot Spot") && $0.color.contains("Green") }),
                        gearType: .flasher,
                        icon: "circle.hexagongrid.fill",
                        reasons: [
                            "Current \(weatherConditions.lowercased()) conditions",
                            "\(tideType) tide at \(selectedLocation.name)",
                            "Wind speed of \(windSpeed) mph"
                        ]
                    ),
                    RecommendationItem(
                        name: "Pro-Troll Flasher - UV Purple",
                        description: "Good alternative for \(selectedLocation.name) in current conditions",
                        matchScore: 87,
                        isInUserGear: userGear.contains(where: { $0.name.contains("Pro-Troll") }),
                        gearType: .flasher,
                        icon: "circle.hexagongrid.fill",
                        reasons: [
                            "Works well in \(weatherConditions.lowercased()) conditions",
                            "Effective during \(tideType.lowercased()) tide"
                        ]
                    )
                ]
            ),
            RecommendationCategory(
                name: "Lures",
                items: [
                    RecommendationItem(
                        name: "Coho Killer - Green Pirate",
                        description: "Top choice for \(selectedLocation.name) today",
                        matchScore: 92,
                        isInUserGear: userGear.contains(where: { $0.name.contains("Coho Killer") }),
                        gearType: .lure,
                        icon: "fish.fill",
                        reasons: [
                            "Matches local baitfish patterns",
                            "Effective in \(weatherConditions.lowercased()) conditions",
                            "Works well with \(windSpeed) mph winds"
                        ]
                    ),
                    RecommendationItem(
                        name: "Ace Hi Fly - Blue/Silver",
                        description: "Good choice for \(selectedLocation.name) with current conditions",
                        matchScore: 85,
                        isInUserGear: userGear.contains(where: { $0.name.contains("Ace Hi") }),
                        gearType: .lure,
                        icon: "fish.fill",
                        reasons: [
                            "Works well during \(tideType.lowercased()) tide",
                            "Good visibility in current water conditions"
                        ]
                    )
                ]
            ),
            RecommendationCategory(
                name: "Leaders",
                items: [
                    RecommendationItem(
                        name: "Fluorocarbon Leader - 40lb test",
                        description: "Optimal leader for today's conditions",
                        matchScore: 90,
                        isInUserGear: userGear.contains(where: { $0.name.contains("Fluorocarbon") }),
                        gearType: .leader,
                        icon: "line.diagonal",
                        reasons: [
                            "Nearly invisible in clear water",
                            "Strong enough for expected fish size",
                            "Optimal for \(tideType.lowercased()) tide conditions"
                        ]
                    )
                ]
            )
        ]
    }
    
    private var fromYourGearRecommendations: [RecommendationCategory] {
        var categories: [RecommendationCategory] = []
        
        // Group user gear by type
        let gearByType = Dictionary(grouping: userGear) { $0.type }
        
        for (type, gear) in gearByType {
            let items = gear.map { gearItem in
                return RecommendationItem(
                    name: gearItem.name,
                    description: "From your gear collection",
                    matchScore: calculateMatchScore(for: gearItem),
                    isInUserGear: true,
                    gearType: gearItem.type,
                    icon: gearItem.type.icon,
                    reasons: generateReasons(for: gearItem)
                )
            }.sorted { $0.matchScore > $1.matchScore }
            
            if !items.isEmpty {
                categories.append(RecommendationCategory(
                    name: "\(type.rawValue)s",
                    items: items
                ))
            }
        }
        
        return categories
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Location and conditions summary
                VStack(spacing: 5) {
                    Text(selectedLocation.name)
                        .font(.headline)
                    
                    HStack(spacing: 15) {
                        Label(weatherConditions, systemImage: weatherIcon(for: weatherConditions))
                            .font(.caption)
                        
                        Label("\(windSpeed) mph", systemImage: "wind")
                            .font(.caption)
                        
                        Label(tideType, systemImage: tideIcon(for: tideType))
                            .font(.caption)
                    }
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color(.secondarySystemBackground))
                
                // Recommendation mode selector
                Picker("Mode", selection: $recommendationMode) {
                    Text("Optimal Gear").tag(RecommendationMode.optimal)
                    Text("From Your Gear").tag(RecommendationMode.fromYourGear)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                if userGear.isEmpty && recommendationMode == .fromYourGear {
                    // Empty state for "From Your Gear" when no gear is added
                    VStack(spacing: 20) {
                        Spacer()
                        
                        Image(systemName: "camera.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.blue)
                        
                        Text("No Gear Found")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text("Add your fishing gear in the My Gear tab to get personalized recommendations based on your collection.")
                            .multilineTextAlignment(.center)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 40)
                        
                        NavigationLink(destination: MyGearView(userGear: .constant([]))) {
                            HStack {
                                Image(systemName: "plus")
                                Text("Add Your Gear")
                            }
                            .frame(minWidth: 200)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }
                        .padding(.top)
                        
                        Spacer()
                    }
                } else {
                    // Recommendations list
                    List {
                        ForEach(recommendationMode == .optimal ? optimalRecommendations : fromYourGearRecommendations) { category in
                            Section(header: Text(category.name)) {
                                ForEach(category.items) { item in
                                    Button(action: {
                                        selectedGear = userGear.first(where: { $0.name == item.name }) ?? 
                                            FishingGear(name: item.name, 
                                                       type: item.gearType, 
                                                       image: UIImage(systemName: item.icon)!, 
                                                       color: "Multi", 
                                                       size: "Standard", 
                                                       brand: "Various", 
                                                       confidence: 100)
                                        showingGearDetail = true
                                    }) {
                                        HStack(spacing: 15) {
                                            // Recommendation icon with match score
                                            ZStack {
                                                Circle()
                                                    .fill(matchScoreColor(for: item.matchScore).opacity(0.2))
                                                    .frame(width: 60, height: 60)
                                                
                                                Image(systemName: item.icon)
                                                    .font(.system(size: 30))
                                                    .foregroundColor(matchScoreColor(for: item.matchScore))
                                                
                                                // Match score indicator
                                                Text("\(item.matchScore)%")
                                                    .font(.system(size: 10, weight: .bold))
                                                    .foregroundColor(.white)
                                                    .padding(4)
                                                    .background(matchScoreColor(for: item.matchScore))
                                                    .clipShape(Circle())
                                                    .offset(x: 20, y: -20)
                                            }
                                            
                                            VStack(alignment: .leading, spacing: 4) {
                                                Text(item.name)
                                                    .font(.headline)
                                                    .foregroundColor(.primary)
                                                
                                                Text(item.description)
                                                    .font(.subheadline)
                                                    .foregroundColor(.secondary)
                                                    .lineLimit(1)
                                                
                                                // Match reasons
                                                if !item.reasons.isEmpty {
                                                    HStack {
                                                        Image(systemName: "checkmark.circle.fill")
                                                            .foregroundColor(.green)
                                                            .font(.caption)
                                                        
                                                        Text(item.reasons[0])
                                                            .font(.caption)
                                                            .foregroundColor(.secondary)
                                                            .lineLimit(1)
                                                    }
                                                }
                                            }
                                            
                                            Spacer()
                                            
                                            // Indicator if user has this gear
                                            if item.isInUserGear {
                                                Image(systemName: "checkmark.circle.fill")
                                                    .foregroundColor(.green)
                                                    .font(.title3)
                                            }
                                        }
                                        .padding(.vertical, 8)
                                    }
                                }
                            }
                        }
                    }
                    .listStyle(InsetGroupedListStyle())
                }
            }
            .navigationTitle("Smart Recommendations")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        refreshRecommendations()
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .sheet(isPresented: $showingGearDetail) {
                if let gear = selectedGear {
                    GearDetailView(
                        gear: gear,
                        location: selectedLocation,
                        weatherConditions: weatherConditions,
                        windSpeed: windSpeed,
                        tideType: tideType
                    )
                }
            }
            .overlay(
                ZStack {
                    if showingAIProcessing {
                        Color.black.opacity(0.7)
                            .edgesIgnoringSafeArea(.all)
                        
                        VStack(spacing: 20) {
                            Text("Analyzing Conditions...")
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                            
                            ZStack {
                                Circle()
                                    .stroke(lineWidth: 8)
                                    .opacity(0.3)
                                    .foregroundColor(.white)
                                
                                Circle()
                                    .trim(from: 0, to: processingProgress)
                                    .stroke(style: StrokeStyle(lineWidth: 8, lineCap: .round, lineJoin: .round))
                                    .foregroundColor(.blue)
                                    .rotationEffect(Angle(degrees: 270))
                                    .animation(.linear(duration: 0.5), value: processingProgress)
                                
                                Image(systemName: "wand.and.stars")
                                    .font(.system(size: 40))
                                    .foregroundColor(.white)
                            }
                            .frame(width: 150, height: 150)
                            
                            Text("Our AI is analyzing weather, tide, and location data to provide the best recommendations...")
                                .foregroundColor(.white)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal)
                        }
                        .padding()
                        .background(Color.black.opacity(0.5))
                        .cornerRadius(20)
                    }
                }
                .animation(.easeInOut, value: showingAIProcessing)
            )
        }
    }
    
    private func refreshRecommendations() {
        showingAIProcessing = true
        
        // Simulate AI processing with a timer
        var progress: CGFloat = 0.0
        let timer = Timer.scheduledTimer(withTimeInterval: 0.05, repeats: true) { timer in
            progress += 0.01
            processingProgress = progress
            
            if progress >= 1.0 {
                timer.invalidate()
                showingAIProcessing = false
            }
        }
        
        // Add the timer to the run loop
        RunLoop.current.add(timer, forMode: .common)
    }
    
    private func calculateMatchScore(for gear: FishingGear) -> Int {
        // In a real app, this would use AI to calculate how well this gear matches current conditions
        // For demo purposes, we'll use a simple algorithm
        
        var score = 70 // Base score
        
        // Adjust based on weather conditions
        if weatherConditions == "Sunny" && gear.color.contains("Green") {
            score += 10
        } else if weatherConditions == "Cloudy" && gear.color.contains("Blue") {
            score += 10
        }
        
        // Adjust based on tide
        if tideType == "Rising" && gear.type == .flasher {
            score += 5
        } else if tideType == "Falling" && gear.type == .lure {
            score += 5
        }
        
        // Adjust based on wind speed
        if windSpeed < 10 && gear.type == .lure {
            score += 5
        } else if windSpeed >= 10 && gear.type == .flasher {
            score += 5
        }
        
        // Add some randomness for demo purposes
        score += Int.random(in: -5...5)
        
        // Ensure score is within bounds
        return min(max(score, 60), 98)
    }
    
    private func generateReasons(for gear: FishingGear) -> [String] {
        var reasons: [String] = []
        
        // Generate reasons based on gear type and conditions
        switch gear.type {
        case .flasher:
            if weatherConditions == "Sunny" {
                reasons.append("Good visibility in sunny conditions")
            } else if weatherConditions == "Cloudy" {
                reasons.append("Creates flash in low light conditions")
            }
            
            if tideType == "Rising" {
                reasons.append("Effective during rising tide")
            }
            
        case .lure:
            if gear.color.contains("Green") && weatherConditions == "Sunny" {
                reasons.append("Green color works well in bright conditions")
            } else if gear.color.contains("Blue") && weatherConditions == "Cloudy" {
                reasons.append("Blue color stands out in low light")
            }
            
            if windSpeed < 10 {
                reasons.append("Good action in light wind conditions")
            } else {
                reasons.append("Stable in stronger winds")
            }
            
        case .leader:
            if gear.name.contains("Fluorocarbon") {
                reasons.append("Nearly invisible in clear water")
            } else if gear.name.contains("Monofilament") {
                reasons.append("Good stretch for fighting fish")
            }
            
        default:
            reasons.append("Suitable for current conditions")
        }
        
        // Add location-specific reason
        reasons.append("Recommended for \(selectedLocation.name)")
        
        return reasons
    }
    
    private func matchScoreColor(for score: Int) -> Color {
        if score >= 90 {
            return .green
        } else if score >= 80 {
            return .blue
        } else if score >= 70 {
            return .orange
        } else {
            return .red
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
}

struct GearDetailView: View {
    let gear: FishingGear
    let location: SimpleLocation
    let weatherConditions: String
    let windSpeed: Int
    let tideType: String
    
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Hero image
                    ZStack {
                        Rectangle()
                            .fill(Color.blue.opacity(0.2))
                            .frame(height: 200)
                        
                        Image(systemName: gear.type.icon)
                            .font(.system(size: 80))
                            .foregroundColor(.blue)
                    }
                    
                    VStack(alignment: .leading, spacing: 15) {
                        // Title and match score
                        HStack {
                            Text(gear.name)
                                .font(.title2)
                                .fontWeight(.bold)
                            
                            Spacer()
                            
                            Text("95% Match")
                                .font(.subheadline)
                                .padding(8)
                                .background(Color.green.opacity(0.2))
                                .foregroundColor(.green)
                                .cornerRadius(8)
                        }
                        .padding(.horizontal)
                        
                        // Current conditions
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Current Conditions at \(location.name)")
                                .font(.headline)
                                .padding(.horizontal)
                            
                            HStack(spacing: 20) {
                                conditionItem(icon: weatherIcon(for: weatherConditions), 
                                             label: weatherConditions)
                                
                                conditionItem(icon: "wind", 
                                             label: "\(windSpeed) mph")
                                
                                conditionItem(icon: tideIcon(for: tideType), 
                                             label: tideType)
                            }
                            .padding(.horizontal)
                        }
                        
                        // Why this works well
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Why This Works Well")
                                .font(.headline)
                                .padding(.horizontal)
                                .padding(.top)
                            
                            VStack(alignment: .leading, spacing: 12) {
                                reasonRow(icon: "sun.max.fill", reason: "Color is optimal for current light conditions")
                                reasonRow(icon: "water.waves", reason: "Works well during \(tideType.lowercased()) tide")
                                reasonRow(icon: "wind", reason: "Designed for \(windSpeed < 10 ? "light" : "moderate") wind conditions")
                                reasonRow(icon: "mappin.circle.fill", reason: "Proven effective at \(location.name)")
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                        }
                        
                        // Specifications
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Specifications")
                                .font(.headline)
                                .padding(.horizontal)
                                .padding(.top)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                specificationRow(label: "Type", value: gear.type.rawValue)
                                specificationRow(label: "Color", value: gear.color)
                                specificationRow(label: "Size", value: gear.size)
                                specificationRow(label: "Brand", value: gear.brand)
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                        }
                        
                        // Usage tips
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Usage Tips")
                                .font(.headline)
                                .padding(.horizontal)
                                .padding(.top)
                            
                            VStack(alignment: .leading, spacing: 12) {
                                tipRow(icon: "speedometer", tip: "Trolling speed: 2.2-2.8 knots")
                                tipRow(icon: "arrow.down.to.line", tip: "Depth: 30-60 feet")
                                tipRow(icon: "water.waves", tip: "Best in moderate chop conditions")
                                tipRow(icon: "sun.max.fill", tip: "Performs well in \(weatherConditions.lowercased()) conditions")
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(10)
                            .padding(.horizontal)
                        }
                    }
                    .padding(.bottom, 30)
                }
            }
            .navigationBarTitle("Gear Details", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    private func conditionItem(icon: String, label: String) -> some View {
        VStack {
            Image(systemName: icon)
                .font(.system(size: 30))
                .foregroundColor(.blue)
            
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
    
    private func reasonRow(icon: String, reason: String) -> some View {
        HStack(alignment: .top, spacing: 15) {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 20)
            
            Text(reason)
                .font(.subheadline)
            
            Spacer()
        }
    }
    
    private func specificationRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(width: 80, alignment: .leading)
            
            Text(value)
                .font(.subheadline)
            
            Spacer()
        }
    }
    
    private func tipRow(icon: String, tip: String) -> some View {
        HStack(spacing: 15) {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 20)
            
            Text(tip)
                .font(.subheadline)
            
            Spacer()
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
}

// Supporting models
enum RecommendationMode {
    case optimal
    case fromYourGear
}

struct RecommendationCategory: Identifiable {
    let id = UUID()
    let name: String
    let items: [RecommendationItem]
}

struct RecommendationItem: Identifiable {
    let id = UUID()
    let name: String
    let description: String
    let matchScore: Int
    let isInUserGear: Bool
    let gearType: GearType
    let icon: String
    let reasons: [String]
}