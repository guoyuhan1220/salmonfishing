import SwiftUI

struct CatchLoggingView: View {
    @ObservedObject var viewModel: CatchLoggingViewModel
    @State private var showingAddCatchSheet = false
    @State private var showingEditCatchSheet = false
    @State private var showingAlert = false
    @State private var alertMessage = ""
    
    var body: some View {
        NavigationView {
            Group {
                if viewModel.catchHistory.isEmpty {
                    VStack {
                        Text("No catches logged yet.")
                            .font(.headline)
                        Text("Tap the + button to add your first catch!")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(viewModel.catchHistory) { catchData in
                            CatchItemView(catchData: catchData)
                                .swipeActions(edge: .trailing) {
                                    Button(role: .destructive) {
                                        viewModel.deleteCatch(catchId: catchData.id)
                                    } label: {
                                        Label("Delete", systemImage: "trash")
                                    }
                                    
                                    Button {
                                        viewModel.selectCatch(catchId: catchData.id)
                                        showingEditCatchSheet = true
                                    } label: {
                                        Label("Edit", systemImage: "pencil")
                                    }
                                    .tint(.blue)
                                }
                        }
                    }
                }
            }
            .navigationTitle("Catch History")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showingAddCatchSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddCatchSheet) {
                AddEditCatchView(
                    catchData: nil,
                    onSave: { locationId, species, size, weight, equipment, weatherId, tideId, notes in
                        viewModel.logCatch(
                            locationId: locationId,
                            species: species,
                            size: size,
                            weight: weight,
                            equipmentUsed: equipment,
                            weatherConditionsId: weatherId,
                            tideConditionsId: tideId,
                            notes: notes
                        )
                    },
                    onCancel: {
                        showingAddCatchSheet = false
                    }
                )
            }
            .sheet(isPresented: $showingEditCatchSheet) {
                if let selectedCatch = viewModel.selectedCatch {
                    AddEditCatchView(
                        catchData: selectedCatch,
                        onSave: { locationId, species, size, weight, equipment, weatherId, tideId, notes in
                            var updatedCatch = selectedCatch
                            updatedCatch.locationId = locationId
                            updatedCatch.species = species
                            updatedCatch.size = size
                            updatedCatch.weight = weight
                            updatedCatch.equipmentUsed = equipment
                            updatedCatch.weatherConditionsId = weatherId
                            updatedCatch.tideConditionsId = tideId
                            updatedCatch.notes = notes
                            
                            viewModel.updateCatch(updatedCatch)
                            viewModel.clearSelectedCatch()
                        },
                        onCancel: {
                            showingEditCatchSheet = false
                            viewModel.clearSelectedCatch()
                        }
                    )
                }
            }
            .onChange(of: viewModel.catchLoggingState) { state in
                handleCatchLoggingState(state)
            }
            .alert(isPresented: $showingAlert) {
                Alert(
                    title: Text("Catch Logging"),
                    message: Text(alertMessage),
                    dismissButton: .default(Text("OK"))
                )
            }
        }
    }
    
    private func handleCatchLoggingState(_ state: CatchLoggingViewModel.CatchLoggingState) {
        switch state {
        case .success:
            alertMessage = "Operation completed successfully"
            showingAlert = true
            showingAddCatchSheet = false
            showingEditCatchSheet = false
            viewModel.resetCatchLoggingState()
        case .error(let message):
            alertMessage = message
            showingAlert = true
            viewModel.resetCatchLoggingState()
        default:
            break
        }
    }
}

struct CatchItemView: View {
    let catchData: CatchData
    
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(catchData.species.rawValue.capitalized)
                    .font(.headline)
                Spacer()
                Text(dateFormatter.string(from: catchData.timestamp))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            if let size = catchData.size {
                Text("Size: \(String(format: "%.1f", size)) inches")
                    .font(.subheadline)
            }
            
            if let weight = catchData.weight {
                Text("Weight: \(String(format: "%.1f", weight)) lbs")
                    .font(.subheadline)
            }
            
            if let notes = catchData.notes, !notes.isEmpty {
                Text("Notes: \(notes)")
                    .font(.subheadline)
                    .lineLimit(2)
            }
        }
        .padding(.vertical, 4)
    }
}

struct AddEditCatchView: View {
    let catchData: CatchData?
    let onSave: (String, FishSpecies, Double?, Double?, [String], String?, String?, String?) -> Void
    let onCancel: () -> Void
    
    @State private var locationId: String
    @State private var selectedSpecies: FishSpecies
    @State private var sizeText: String
    @State private var weightText: String
    @State private var notes: String
    @State private var equipment: [String]
    @State private var weatherId: String
    @State private var tideId: String
    
    @Environment(\.presentationMode) var presentationMode
    
    init(catchData: CatchData?, onSave: @escaping (String, FishSpecies, Double?, Double?, [String], String?, String?, String?) -> Void, onCancel: @escaping () -> Void) {
        self.catchData = catchData
        self.onSave = onSave
        self.onCancel = onCancel
        
        _locationId = State(initialValue: catchData?.locationId ?? "")
        _selectedSpecies = State(initialValue: catchData?.species ?? .chinook)
        _sizeText = State(initialValue: catchData?.size != nil ? String(format: "%.1f", catchData!.size!) : "")
        _weightText = State(initialValue: catchData?.weight != nil ? String(format: "%.1f", catchData!.weight!) : "")
        _notes = State(initialValue: catchData?.notes ?? "")
        _equipment = State(initialValue: catchData?.equipmentUsed ?? [])
        _weatherId = State(initialValue: catchData?.weatherConditionsId ?? "")
        _tideId = State(initialValue: catchData?.tideConditionsId ?? "")
    }
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Location")) {
                    TextField("Location ID", text: $locationId)
                }
                
                Section(header: Text("Species")) {
                    Picker("Species", selection: $selectedSpecies) {
                        ForEach(FishSpecies.allCases, id: \.self) { species in
                            Text(species.rawValue.capitalized)
                                .tag(species)
                        }
                    }
                    .pickerStyle(MenuPickerStyle())
                }
                
                Section(header: Text("Size & Weight")) {
                    TextField("Size (inches)", text: $sizeText)
                        .keyboardType(.decimalPad)
                    
                    TextField("Weight (lbs)", text: $weightText)
                        .keyboardType(.decimalPad)
                }
                
                Section(header: Text("Notes")) {
                    TextEditor(text: $notes)
                        .frame(height: 100)
                }
            }
            .navigationTitle(catchData == nil ? "Log New Catch" : "Edit Catch")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onCancel()
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        onSave(
                            locationId,
                            selectedSpecies,
                            Double(sizeText),
                            Double(weightText),
                            equipment,
                            weatherId.isEmpty ? nil : weatherId,
                            tideId.isEmpty ? nil : tideId,
                            notes.isEmpty ? nil : notes
                        )
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
}

struct CatchLoggingView_Previews: PreviewProvider {
    static var previews: some View {
        let authService = AuthenticationServiceImpl()
        let catchLoggingService = CatchLoggingServiceImpl(authService: authService)
        let viewModel = CatchLoggingViewModel(catchLoggingService: catchLoggingService)
        
        return CatchLoggingView(viewModel: viewModel)
    }
}