import Foundation
import Combine

class CatchLoggingViewModel: ObservableObject {
    private let catchLoggingService: CatchLoggingService
    private var cancellables = Set<AnyCancellable>()
    
    @Published var catchHistory: [CatchData] = []
    @Published var selectedCatch: CatchData?
    @Published var catchLoggingState: CatchLoggingState = .initial
    
    init(catchLoggingService: CatchLoggingService) {
        self.catchLoggingService = catchLoggingService
        
        // Subscribe to catch history changes
        catchLoggingService.catchHistory
            .map { catches in
                catches.sorted { $0.timestamp > $1.timestamp }
            }
            .assign(to: \.catchHistory, on: self)
            .store(in: &cancellables)
    }
    
    func logCatch(
        locationId: String,
        species: FishSpecies,
        size: Double?,
        weight: Double?,
        equipmentUsed: [String],
        weatherConditionsId: String?,
        tideConditionsId: String?,
        notes: String?,
        photoUrls: [String] = []
    ) {
        catchLoggingState = .loading
        
        let catchData = CatchData(
            id: UUID().uuidString,
            timestamp: Date(),
            locationId: locationId,
            species: species,
            size: size,
            weight: weight,
            equipmentUsed: equipmentUsed,
            weatherConditionsId: weatherConditionsId,
            tideConditionsId: tideConditionsId,
            notes: notes,
            photoUrls: photoUrls
        )
        
        Task {
            let result = await catchLoggingService.logCatch(catchData)
            
            await MainActor.run {
                switch result {
                case .success:
                    catchLoggingState = .success
                case .failure(let error):
                    catchLoggingState = .error(error.localizedDescription)
                }
            }
        }
    }
    
    func updateCatch(_ catchData: CatchData) {
        catchLoggingState = .loading
        
        Task {
            let result = await catchLoggingService.updateCatch(catchData)
            
            await MainActor.run {
                switch result {
                case .success:
                    catchLoggingState = .success
                case .failure(let error):
                    catchLoggingState = .error(error.localizedDescription)
                }
            }
        }
    }
    
    func deleteCatch(catchId: String) {
        catchLoggingState = .loading
        
        Task {
            let result = await catchLoggingService.deleteCatch(catchId: catchId)
            
            await MainActor.run {
                switch result {
                case .success:
                    catchLoggingState = .success
                case .failure(let error):
                    catchLoggingState = .error(error.localizedDescription)
                }
            }
        }
    }
    
    func addPhotoCatch(catchId: String, photoUrl: String) {
        catchLoggingState = .loading
        
        Task {
            let result = await catchLoggingService.addPhotoCatch(catchId: catchId, photoUrl: photoUrl)
            
            await MainActor.run {
                switch result {
                case .success:
                    catchLoggingState = .success
                case .failure(let error):
                    catchLoggingState = .error(error.localizedDescription)
                }
            }
        }
    }
    
    func removePhotoCatch(catchId: String, photoUrl: String) {
        catchLoggingState = .loading
        
        Task {
            let result = await catchLoggingService.removePhotoCatch(catchId: catchId, photoUrl: photoUrl)
            
            await MainActor.run {
                switch result {
                case .success:
                    catchLoggingState = .success
                case .failure(let error):
                    catchLoggingState = .error(error.localizedDescription)
                }
            }
        }
    }
    
    func selectCatch(catchId: String) {
        Task {
            let result = await catchLoggingService.getCatchById(catchId: catchId)
            
            await MainActor.run {
                switch result {
                case .success(let catchData):
                    selectedCatch = catchData
                case .failure:
                    selectedCatch = nil
                }
            }
        }
    }
    
    func clearSelectedCatch() {
        selectedCatch = nil
    }
    
    func resetCatchLoggingState() {
        catchLoggingState = .initial
    }
    
    enum CatchLoggingState: Equatable {
        case initial
        case loading
        case success
        case error(String)
        
        static func == (lhs: CatchLoggingState, rhs: CatchLoggingState) -> Bool {
            switch (lhs, rhs) {
            case (.initial, .initial):
                return true
            case (.loading, .loading):
                return true
            case (.success, .success):
                return true
            case (.error(let lhsMessage), .error(let rhsMessage)):
                return lhsMessage == rhsMessage
            default:
                return false
            }
        }
    }
}