import Foundation
import Combine

class CatchLoggingServiceImpl: CatchLoggingService {
    private let authService: AuthenticationService
    private let userDefaults = UserDefaults.standard
    private var cancellables = Set<AnyCancellable>()
    
    let catchHistory = CurrentValueSubject<[CatchData], Never>([])
    
    init(authService: AuthenticationService) {
        self.authService = authService
        
        // Load initial data
        loadCatchHistory()
        
        // Subscribe to user changes to reload catch history when user changes
        authService.currentUser
            .sink { [weak self] _ in
                self?.loadCatchHistory()
            }
            .store(in: &cancellables)
    }
    
    func logCatch(_ catchData: CatchData) async -> Result<Bool, Error> {
        guard let userId = authService.currentUser.value?.id else {
            return .failure(NSError(domain: "CatchLoggingService", code: 401, userInfo: [NSLocalizedDescriptionKey: "User not logged in"]))
        }
        
        do {
            var currentCatches = catchHistory.value
            currentCatches.append(catchData)
            
            let data = try JSONEncoder().encode(currentCatches)
            userDefaults.set(data, forKey: "\(userId)_catches")
            
            await MainActor.run {
                catchHistory.send(currentCatches)
            }
            
            return .success(true)
        } catch {
            return .failure(error)
        }
    }
    
    func getCatchHistoryByLocation(locationId: String) -> AnyPublisher<[CatchData], Never> {
        return catchHistory
            .map { catches in
                catches.filter { $0.locationId == locationId }
            }
            .eraseToAnyPublisher()
    }
    
    func getCatchHistoryBySpecies(species: FishSpecies) -> AnyPublisher<[CatchData], Never> {
        return catchHistory
            .map { catches in
                catches.filter { $0.species == species }
            }
            .eraseToAnyPublisher()
    }
    
    func getCatchById(catchId: String) async -> Result<CatchData, Error> {
        if let catch = catchHistory.value.first(where: { $0.id == catchId }) {
            return .success(catch)
        } else {
            return .failure(NSError(domain: "CatchLoggingService", code: 404, userInfo: [NSLocalizedDescriptionKey: "Catch not found"]))
        }
    }
    
    func updateCatch(_ catchData: CatchData) async -> Result<Bool, Error> {
        guard let userId = authService.currentUser.value?.id else {
            return .failure(NSError(domain: "CatchLoggingService", code: 401, userInfo: [NSLocalizedDescriptionKey: "User not logged in"]))
        }
        
        do {
            var currentCatches = catchHistory.value
            if let index = currentCatches.firstIndex(where: { $0.id == catchData.id }) {
                currentCatches[index] = catchData
            }
            
            let data = try JSONEncoder().encode(currentCatches)
            userDefaults.set(data, forKey: "\(userId)_catches")
            
            await MainActor.run {
                catchHistory.send(currentCatches)
            }
            
            return .success(true)
        } catch {
            return .failure(error)
        }
    }
    
    func deleteCatch(catchId: String) async -> Result<Bool, Error> {
        guard let userId = authService.currentUser.value?.id else {
            return .failure(NSError(domain: "CatchLoggingService", code: 401, userInfo: [NSLocalizedDescriptionKey: "User not logged in"]))
        }
        
        do {
            var currentCatches = catchHistory.value
            currentCatches.removeAll { $0.id == catchId }
            
            let data = try JSONEncoder().encode(currentCatches)
            userDefaults.set(data, forKey: "\(userId)_catches")
            
            await MainActor.run {
                catchHistory.send(currentCatches)
            }
            
            return .success(true)
        } catch {
            return .failure(error)
        }
    }
    
    func addPhotoCatch(catchId: String, photoUrl: String) async -> Result<Bool, Error> {
        let catchResult = await getCatchById(catchId)
        
        switch catchResult {
        case .success(var catchData):
            var photoUrls = catchData.photoUrls
            photoUrls.append(photoUrl)
            catchData = CatchData(
                id: catchData.id,
                timestamp: catchData.timestamp,
                locationId: catchData.locationId,
                species: catchData.species,
                size: catchData.size,
                weight: catchData.weight,
                equipmentUsed: catchData.equipmentUsed,
                weatherConditionsId: catchData.weatherConditionsId,
                tideConditionsId: catchData.tideConditionsId,
                notes: catchData.notes,
                photoUrls: photoUrls
            )
            return await updateCatch(catchData)
            
        case .failure(let error):
            return .failure(error)
        }
    }
    
    func removePhotoCatch(catchId: String, photoUrl: String) async -> Result<Bool, Error> {
        let catchResult = await getCatchById(catchId)
        
        switch catchResult {
        case .success(var catchData):
            var photoUrls = catchData.photoUrls
            photoUrls.removeAll { $0 == photoUrl }
            catchData = CatchData(
                id: catchData.id,
                timestamp: catchData.timestamp,
                locationId: catchData.locationId,
                species: catchData.species,
                size: catchData.size,
                weight: catchData.weight,
                equipmentUsed: catchData.equipmentUsed,
                weatherConditionsId: catchData.weatherConditionsId,
                tideConditionsId: catchData.tideConditionsId,
                notes: catchData.notes,
                photoUrls: photoUrls
            )
            return await updateCatch(catchData)
            
        case .failure(let error):
            return .failure(error)
        }
    }
    
    // MARK: - Private Methods
    
    private func loadCatchHistory() {
        guard let userId = authService.currentUser.value?.id else {
            catchHistory.send([])
            return
        }
        
        if let data = userDefaults.data(forKey: "\(userId)_catches"),
           let decoded = try? JSONDecoder().decode([CatchData].self, from: data) {
            catchHistory.send(decoded)
        } else {
            catchHistory.send([])
        }
    }
}