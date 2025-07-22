import Foundation
import Combine

protocol CatchLoggingService {
    /**
     * Log a new catch
     */
    func logCatch(_ catchData: CatchData) async -> Result<Bool, Error>
    
    /**
     * Get all catch history
     */
    var catchHistory: CurrentValueSubject<[CatchData], Never> { get }
    
    /**
     * Get catch history for a specific location
     */
    func getCatchHistoryByLocation(locationId: String) -> AnyPublisher<[CatchData], Never>
    
    /**
     * Get catch history for a specific species
     */
    func getCatchHistoryBySpecies(species: FishSpecies) -> AnyPublisher<[CatchData], Never>
    
    /**
     * Get a specific catch by ID
     */
    func getCatchById(catchId: String) async -> Result<CatchData, Error>
    
    /**
     * Update an existing catch
     */
    func updateCatch(_ catchData: CatchData) async -> Result<Bool, Error>
    
    /**
     * Delete a catch
     */
    func deleteCatch(catchId: String) async -> Result<Bool, Error>
    
    /**
     * Add a photo to a catch
     */
    func addPhotoCatch(catchId: String, photoUrl: String) async -> Result<Bool, Error>
    
    /**
     * Remove a photo from a catch
     */
    func removePhotoCatch(catchId: String, photoUrl: String) async -> Result<Bool, Error>
}