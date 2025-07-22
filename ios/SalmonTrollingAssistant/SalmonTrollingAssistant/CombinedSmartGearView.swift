import SwiftUI
import PhotosUI
import UIKit

struct CombinedSmartGearView: View {
    @Binding var userGear: [FishingGear]
    @State private var selectedTab = 0
    @State private var selectedLocation: SimpleLocation = sampleLocations[0]
    @State private var selectedDate = Date()
    @State private var weatherConditions = "Partly Cloudy"
    @State private var windSpeed = 8
    @State private var tideType = "Rising"
    @State private var showingLocationPicker = false
    @State private var showingDatePicker = false
    @State private var showingPhotosPicker = false
    @State private var showingCameraView = false
    @State private var selectedImage: UIImage?
    @State private var showingAIProcessing = false
    @State private var processingProgress: CGFloat = 0.0
    @State private var showingNewGearForm = false
    @State private var newGearName = ""
    @State private var newGearType: GearType = .lure
    @State private var newGearColor = ""
    @State private var newGearSize = ""
    @State private var newGearBrand = ""
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Tab selector
                Picker("View", selection: $selectedTab) {
                    Text("Recommendations").tag(0)
                    Text("My Gear").tag(1)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                if selectedTab == 0 {
                    // Recommendations Tab
                    VStack(spacing: 0) {
                        // Location and date selector
                        HStack {
                            Button(action: {
                                showingLocationPicker = true
                            }) {
                                HStack {
                                    Image(systemName: "mappin.circle.fill")
                                        .foregroundColor(.red)
                                    Text(selectedLocation.name)
                                    Image(systemName: "chevron.down")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                }
                                .padding(8)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(8)
                            }
                            
                            Spacer()
                            
                            Button(action: {
                                showingDatePicker = true
                            }) {
                                HStack {
                                    Image(systemName: "calendar")
                                        .foregroundColor(.blue)
                                    Text(formattedDate(selectedDate))
                                    Image(systemName: "chevron.down")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                }
                                .padding(8)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(8)
                            }
                        }
                        .padding(.horizontal)
                        
                        // Conditions summary
                        HStack(spacing: 15) {
                            conditionItem(icon: weatherIcon(for: weatherConditions), 
                                         label: weatherConditions)
                            
                            conditionItem(icon: "wind", 
                                         label: "\(windSpeed) mph")
                            
                            conditionItem(icon: tideIcon(for: tideType), 
                                         label: tideType)
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        
                        // Recommendations list
                        List {
                            Section(header: Text("Recommended Flashers")) {
                                recommendationRow(
                                    name: "Hot Spot Flasher - UV Green",
                                    description: "Perfect for today's light conditions",
                                    matchScore: 95,
                                    icon: "circle",
                                    isInUserGear: userGear.contains(where: { $0.name.contains("Hot Spot") })
                                )
                                
                                recommendationRow(
                                    name: "Pro-Troll Flasher - UV Purple",
                                    description: "Good alternative in current conditions",
                                    matchScore: 87,
                                    icon: "circle",
                                    isInUserGear: userGear.contains(where: { $0.name.contains("Pro-Troll") })
                                )
                            }
                            
                            Section(header: Text("Recommended Lures")) {
                                recommendationRow(
                                    name: "Coho Killer - Green Pirate",
                                    description: "Top choice for today",
                                    matchScore: 92,
                                    icon: "oval",
                                    isInUserGear: userGear.contains(where: { $0.name.contains("Coho Killer") })
                                )
                                
                                recommendationRow(
                                    name: "Ace Hi Fly - Blue/Silver",
                                    description: "Good choice with current conditions",
                                    matchScore: 85,
                                    icon: "oval",
                                    isInUserGear: userGear.contains(where: { $0.name.contains("Ace Hi") })
                                )
                            }
                            
                            Section(header: Text("Recommended Leaders")) {
                                recommendationRow(
                                    name: "Fluorocarbon Leader - 40lb test",
                                    description: "Optimal leader for today's conditions",
                                    matchScore: 90,
                                    icon: "line.diagonal",
                                    isInUserGear: userGear.contains(where: { $0.name.contains("Fluorocarbon") })
                                )
                            }
                            
                            Section(header: Text("Recommended Setup")) {
                                VStack(alignment: .leading, spacing: 15) {
                                    Text("Optimal Trolling Setup")
                                        .font(.headline)
                                    
                                    // Using a placeholder since we don't have the actual image
                                    ZStack {
                                        Rectangle()
                                            .fill(Color.blue.opacity(0.1))
                                            .frame(height: 150)
                                            .cornerRadius(8)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                            )
                                        
                                        VStack {
                                            Image(systemName: "water.waves")
                                                .font(.system(size: 40))
                                                .foregroundColor(.blue)
                                            
                                            Text("Trolling Setup Diagram")
                                                .padding(.top, 8)
                                                .foregroundColor(.secondary)
                                        }
                                    }
                                    
                                    Text("Recommended Configuration:")
                                        .font(.subheadline)
                                        .fontWeight(.medium)
                                    
                                    bulletPoint(text: "Downrigger depth: 40-60 feet")
                                    bulletPoint(text: "Leader length: 42-48 inches")
                                    bulletPoint(text: "Trolling speed: 2.2-2.8 knots")
                                    bulletPoint(text: "Use Hot Spot Flasher with Coho Killer")
                                }
                                .padding(.vertical, 8)
                            }
                        }
                        .listStyle(InsetGroupedListStyle())
                    }
                } else {
                    // My Gear Tab
                    VStack {
                        if userGear.isEmpty {
                            // Empty state
                            VStack(spacing: 20) {
                                Spacer()
                                
                                Image(systemName: "camera")
                                    .font(.system(size: 60))
                                    .foregroundColor(.blue)
                                
                                Text("No Gear Added Yet")
                                    .font(.title2)
                                    .fontWeight(.bold)
                                
                                Text("Add your fishing gear to get personalized recommendations")
                                    .multilineTextAlignment(.center)
                                    .foregroundColor(.secondary)
                                    .padding(.horizontal, 40)
                                
                                HStack(spacing: 20) {
                                    Button(action: {
                                        showingCameraView = true
                                    }) {
                                        VStack {
                                            Image(systemName: "camera.fill")
                                                .font(.system(size: 24))
                                            Text("Take Photo")
                                                .font(.caption)
                                        }
                                        .frame(width: 100)
                                        .padding()
                                        .background(Color.blue)
                                        .foregroundColor(.white)
                                        .cornerRadius(10)
                                    }
                                    
                                    Button(action: {
                                        showingPhotosPicker = true
                                    }) {
                                        VStack {
                                            Image(systemName: "photo.on.rectangle")
                                                .font(.system(size: 24))
                                            Text("Choose Photo")
                                                .font(.caption)
                                        }
                                        .frame(width: 100)
                                        .padding()
                                        .background(Color.blue)
                                        .foregroundColor(.white)
                                        .cornerRadius(10)
                                    }
                                }
                                .padding(.top)
                                
                                Button(action: {
                                    // Add sample gear for demo
                                    userGear = FishingGear.mockGear()
                                }) {
                                    Text("Add Sample Gear")
                                        .padding()
                                        .background(Color.gray.opacity(0.2))
                                        .foregroundColor(.primary)
                                        .cornerRadius(10)
                                }
                                .padding(.top, 30)
                                
                                Spacer()
                            }
                        } else {
                            // Gear list
                            List {
                                ForEach(GearType.allCases, id: \.self) { gearType in
                                    let gearOfType = userGear.filter { $0.type == gearType }
                                    
                                    if !gearOfType.isEmpty {
                                        Section(header: Text(gearType.rawValue + "s")) {
                                            ForEach(gearOfType) { gear in
                                                HStack {
                                                    Image(systemName: gearType.icon)
                                                        .font(.title2)
                                                        .foregroundColor(.blue)
                                                        .frame(width: 40, height: 40)
                                                        .background(Color.blue.opacity(0.1))
                                                        .clipShape(Circle())
                                                    
                                                    VStack(alignment: .leading, spacing: 4) {
                                                        Text(gear.name)
                                                            .font(.headline)
                                                        
                                                        Text("\(gear.brand) • \(gear.color) • \(gear.size)")
                                                            .font(.subheadline)
                                                            .foregroundColor(.secondary)
                                                    }
                                                    
                                                    Spacer()
                                                    
                                                    // AI confidence indicator
                                                    Text("\(gear.confidence)%")
                                                        .font(.caption)
                                                        .padding(4)
                                                        .background(
                                                            gear.confidence > 90 ? Color.green.opacity(0.2) :
                                                            gear.confidence > 80 ? Color.blue.opacity(0.2) :
                                                            Color.orange.opacity(0.2)
                                                        )
                                                        .foregroundColor(
                                                            gear.confidence > 90 ? .green :
                                                            gear.confidence > 80 ? .blue :
                                                            .orange
                                                        )
                                                        .cornerRadius(4)
                                                }
                                            }
                                            .onDelete { indexSet in
                                                let gearToDelete = indexSet.map { gearOfType[$0] }
                                                userGear.removeAll { gear in
                                                    gearToDelete.contains { $0.id == gear.id }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .listStyle(InsetGroupedListStyle())
                        }
                    }
                }
            }
            .navigationTitle("Smart Fishing")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    if selectedTab == 0 {
                        Button(action: {
                            refreshRecommendations()
                        }) {
                            Image(systemName: "arrow.clockwise")
                        }
                    } else {
                        Menu {
                            Button(action: {
                                showingCameraView = true
                            }) {
                                Label("Take Photo", systemImage: "camera")
                            }
                            
                            Button(action: {
                                showingPhotosPicker = true
                            }) {
                                Label("Choose Photo", systemImage: "photo")
                            }
                            
                            Button(action: {
                                showingNewGearForm = true
                            }) {
                                Label("Add Manually", systemImage: "pencil")
                            }
                        } label: {
                            Image(systemName: "plus")
                        }
                    }
                }
            }
            .sheet(isPresented: $showingLocationPicker) {
                LocationPickerView(selectedLocation: $selectedLocation)
            }
            .sheet(isPresented: $showingDatePicker) {
                DatePickerView(selectedDate: $selectedDate)
            }
            .sheet(isPresented: $showingPhotosPicker) {
                PhotoPickerView(selectedImage: $selectedImage, showingAIProcessing: $showingAIProcessing)
            }
            .sheet(isPresented: $showingCameraView) {
                CameraView(selectedImage: $selectedImage, showingAIProcessing: $showingAIProcessing)
            }
            .sheet(isPresented: $showingNewGearForm) {
                NewGearFormView(
                    userGear: $userGear,
                    name: $newGearName,
                    type: $newGearType,
                    color: $newGearColor,
                    size: $newGearSize,
                    brand: $newGearBrand
                )
            }
            .onChange(of: selectedImage) { _ in
                if selectedImage != nil {
                    processGearImage()
                }
            }
            .overlay(
                ZStack {
                    if showingAIProcessing {
                        Color.black.opacity(0.7)
                            .edgesIgnoringSafeArea(.all)
                        
                        VStack(spacing: 20) {
                            Text("AI Analyzing Gear...")
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
                            
                            Text("Our AI is analyzing your fishing gear to identify and categorize it...")
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
    
    private func bulletPoint(text: String) -> some View {
        HStack(alignment: .top, spacing: 8) {
            Text("•")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Text(text)
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Spacer()
        }
    }
    
    private func conditionItem(icon: String, label: String) -> some View {
        VStack {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(.blue)
            
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
    
    private func recommendationRow(name: String, description: String, matchScore: Int, icon: String, isInUserGear: Bool) -> some View {
        HStack(spacing: 15) {
            // Icon with match score
            ZStack {
                Circle()
                    .fill(matchScoreColor(for: matchScore).opacity(0.2))
                    .frame(width: 50, height: 50)
                
                Image(systemName: icon)
                    .font(.system(size: 24))
                    .foregroundColor(matchScoreColor(for: matchScore))
                
                // Match score indicator
                Text("\(matchScore)%")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
                    .padding(4)
                    .background(matchScoreColor(for: matchScore))
                    .clipShape(Circle())
                    .offset(x: 15, y: -15)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(name)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            // Indicator if user has this gear
            if isInUserGear {
                Image(systemName: "checkmark.circle")
                    .foregroundColor(.green)
                    .font(.title3)
            }
        }
        .padding(.vertical, 8)
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
                
                // Update conditions based on selected date and location
                // This would normally come from a weather API
                let daysDifference = Calendar.current.dateComponents([.day], from: Date(), to: selectedDate).day ?? 0
                
                if daysDifference == 0 {
                    weatherConditions = "Partly Cloudy"
                    windSpeed = 8
                    tideType = "Rising"
                } else if daysDifference == 1 {
                    weatherConditions = "Sunny"
                    windSpeed = 6
                    tideType = "High"
                } else if daysDifference == 2 {
                    weatherConditions = "Cloudy"
                    windSpeed = 12
                    tideType = "Falling"
                } else {
                    weatherConditions = ["Sunny", "Partly Cloudy", "Cloudy"].randomElement()!
                    windSpeed = Int.random(in: 5...15)
                    tideType = ["Rising", "High", "Falling", "Low"].randomElement()!
                }
            }
        }
        
        // Add the timer to the run loop
        RunLoop.current.add(timer, forMode: .common)
    }
    
    private func processGearImage() {
        showingAIProcessing = true
        
        // Simulate AI processing with a timer
        var progress: CGFloat = 0.0
        let timer = Timer.scheduledTimer(withTimeInterval: 0.05, repeats: true) { timer in
            progress += 0.01
            processingProgress = progress
            
            if progress >= 1.0 {
                timer.invalidate()
                showingAIProcessing = false
                
                // Add a new gear item based on "AI analysis"
                let gearTypes: [GearType] = [.flasher, .lure, .leader, .hook]
                let gearNames = [
                    "Hot Spot Flasher", "Pro-Troll Flasher", "Coho Killer Spoon", 
                    "Ace Hi Fly", "Herring Aid Dodger", "Silver Horde Spoon"
                ]
                let gearColors = ["Green", "Blue/Silver", "Purple", "Red/Gold", "UV Green", "UV Blue"]
                let gearSizes = ["Standard", "Large", "Small", "3.5\"", "4\"", "5\""]
                let gearBrands = ["Hot Spot", "Pro-Troll", "Silver Horde", "Ace", "Luhr Jensen"]
                
                let randomType = gearTypes.randomElement()!
                let randomName = gearNames.randomElement()!
                let randomColor = gearColors.randomElement()!
                let randomSize = gearSizes.randomElement()!
                let randomBrand = gearBrands.randomElement()!
                let confidence = Int.random(in: 85...98)
                
                let newGear = FishingGear(
                    name: randomName,
                    type: randomType,
                    image: selectedImage ?? UIImage(systemName: randomType.icon)!,
                    color: randomColor,
                    size: randomSize,
                    brand: randomBrand,
                    confidence: confidence
                )
                
                userGear.append(newGear)
                selectedImage = nil
            }
        }
        
        // Add the timer to the run loop
        RunLoop.current.add(timer, forMode: .common)
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
            return "sun.max"
        } else if condition.contains("partly cloudy") {
            return "cloud.sun"
        } else if condition.contains("cloudy") {
            return "cloud"
        } else if condition.contains("rain") {
            return "cloud.rain"
        } else if condition.contains("thunder") {
            return "cloud.bolt"
        } else if condition.contains("snow") {
            return "cloud.snow"
        } else {
            return "cloud"
        }
    }
    
    private func tideIcon(for tideType: String) -> String {
        let type = tideType.lowercased()
        if type.contains("rising") {
            return "arrow.up.right"
        } else if type.contains("high") {
            return "arrow.up"
        } else if type.contains("falling") {
            return "arrow.down.right"
        } else if type.contains("low") {
            return "arrow.down"
        } else {
            return "circle"
        }
    }
    
    private func formattedDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }
}

// Helper views
struct LocationPickerView: View {
    @Binding var selectedLocation: SimpleLocation
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            List(sampleLocations) { location in
                Button(action: {
                    selectedLocation = location
                    presentationMode.wrappedValue.dismiss()
                }) {
                    HStack {
                        Text(location.name)
                        
                        Spacer()
                        
                        if location.id == selectedLocation.id {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                }
            }
            .navigationTitle("Select Location")
            .navigationBarItems(trailing: Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}

struct DatePickerView: View {
    @Binding var selectedDate: Date
    @Environment(\.presentationMode) var presentationMode
    @State private var tempDate = Date()
    
    var body: some View {
        NavigationView {
            VStack {
                DatePicker(
                    "Select Date",
                    selection: $tempDate,
                    displayedComponents: [.date]
                )
                .datePickerStyle(GraphicalDatePickerStyle())
                .padding()
                
                Spacer()
            }
            .navigationTitle("Select Date")
            .navigationBarItems(
                leading: Button("Cancel") {
                    presentationMode.wrappedValue.dismiss()
                },
                trailing: Button("Done") {
                    selectedDate = tempDate
                    presentationMode.wrappedValue.dismiss()
                }
            )
            .onAppear {
                tempDate = selectedDate
            }
        }
    }
}

struct PhotoPickerView: View {
    @Binding var selectedImage: UIImage?
    @Binding var showingAIProcessing: Bool
    @Environment(\.presentationMode) var presentationMode
    @State private var showingImagePicker = false
    
    var body: some View {
        NavigationView {
            VStack {
                Button(action: {
                    showingImagePicker = true
                }) {
                    VStack {
                        Image(systemName: "photo.on.rectangle")
                            .font(.system(size: 60))
                            .foregroundColor(.blue)
                        
                        Text("Select a Photo")
                            .font(.headline)
                            .padding(.top)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .navigationTitle("Choose Photo")
            .navigationBarItems(trailing: Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            })
            .sheet(isPresented: $showingImagePicker) {
                ImagePicker(selectedImage: $selectedImage, sourceType: .photoLibrary)
                    .onDisappear {
                        if selectedImage != nil {
                            presentationMode.wrappedValue.dismiss()
                        }
                    }
            }
        }
    }
}

struct CameraView: View {
    @Binding var selectedImage: UIImage?
    @Binding var showingAIProcessing: Bool
    @Environment(\.presentationMode) var presentationMode
    @State private var showingImagePicker = false
    
    var body: some View {
        NavigationView {
            VStack {
                Button(action: {
                    showingImagePicker = true
                }) {
                    VStack {
                        Image(systemName: "camera.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.blue)
                        
                        Text("Take a Photo")
                            .font(.headline)
                            .padding(.top)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .navigationTitle("Camera")
            .navigationBarItems(trailing: Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            })
            .sheet(isPresented: $showingImagePicker) {
                ImagePicker(selectedImage: $selectedImage, sourceType: .camera)
                    .onDisappear {
                        if selectedImage != nil {
                            presentationMode.wrappedValue.dismiss()
                        }
                    }
            }
        }
    }
}

struct NewGearFormView: View {
    @Binding var userGear: [FishingGear]
    @Binding var name: String
    @Binding var type: GearType
    @Binding var color: String
    @Binding var size: String
    @Binding var brand: String
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Gear Details")) {
                    TextField("Name", text: $name)
                    
                    Picker("Type", selection: $type) {
                        ForEach(GearType.allCases, id: \.self) { type in
                            Text(type.rawValue).tag(type)
                        }
                    }
                    
                    TextField("Color", text: $color)
                    TextField("Size", text: $size)
                    TextField("Brand", text: $brand)
                }
            }
            .navigationTitle("Add Gear")
            .navigationBarItems(
                leading: Button("Cancel") {
                    presentationMode.wrappedValue.dismiss()
                },
                trailing: Button("Save") {
                    if !name.isEmpty {
                        let newGear = FishingGear(
                            name: name,
                            type: type,
                            image: UIImage(systemName: type.icon)!,
                            color: color.isEmpty ? "Unknown" : color,
                            size: size.isEmpty ? "Standard" : size,
                            brand: brand.isEmpty ? "Unknown" : brand,
                            confidence: 100 // 100% confidence since user entered it
                        )
                        
                        userGear.append(newGear)
                        
                        // Reset form
                        name = ""
                        type = .lure
                        color = ""
                        size = ""
                        brand = ""
                        
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                .disabled(name.isEmpty)
            )
        }
    }
}

// Image picker from UIKit
struct ImagePicker: UIViewControllerRepresentable {
    @Binding var selectedImage: UIImage?
    var sourceType: UIImagePickerController.SourceType
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.selectedImage = image
            }
            
            picker.dismiss(animated: true)
        }
    }
}

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