import Foundation

// Protocol for equipment database operations
protocol EquipmentDatabase {
    func getAllEquipment() -> [EquipmentItem]
    func getEquipmentById(id: String) -> EquipmentItem?
    func getEquipmentByType(type: EquipmentType) -> [EquipmentItem]
    func getEquipmentBySpecies(species: FishSpecies) -> [EquipmentItem]
    func getEquipmentByWaterClarity(clarity: WaterClarity) -> [EquipmentItem]
    func addEquipment(item: EquipmentItem) -> Bool
    func updateEquipment(item: EquipmentItem) -> Bool
    func deleteEquipment(id: String) -> Bool
}

// In-memory implementation of equipment database
class InMemoryEquipmentDatabase: EquipmentDatabase {
    private var equipmentItems: [EquipmentItem] = []
    
    init() {
        loadInitialData()
    }
    
    func getAllEquipment() -> [EquipmentItem] {
        return equipmentItems
    }
    
    func getEquipmentById(id: String) -> EquipmentItem? {
        return equipmentItems.first { $0.id == id }
    }
    
    func getEquipmentByType(type: EquipmentType) -> [EquipmentItem] {
        return equipmentItems.filter { $0.type == type }
    }
    
    func getEquipmentBySpecies(species: FishSpecies) -> [EquipmentItem] {
        return equipmentItems.filter { item in
            guard let targetSpecies = item.targetSpecies else { return false }
            return targetSpecies.contains(species)
        }
    }
    
    func getEquipmentByWaterClarity(clarity: WaterClarity) -> [EquipmentItem] {
        return equipmentItems.filter { item in
            guard let waterClarityConditions = item.waterClarityConditions else { return false }
            return waterClarityConditions.contains(clarity.rawValue.lowercased())
        }
    }
    
    func addEquipment(item: EquipmentItem) -> Bool {
        // Check if item with same ID already exists
        if equipmentItems.contains(where: { $0.id == item.id }) {
            return false
        }
        
        equipmentItems.append(item)
        return true
    }
    
    func updateEquipment(item: EquipmentItem) -> Bool {
        if let index = equipmentItems.firstIndex(where: { $0.id == item.id }) {
            equipmentItems[index] = item
            return true
        }
        return false
    }
    
    func deleteEquipment(id: String) -> Bool {
        if let index = equipmentItems.firstIndex(where: { $0.id == id }) {
            equipmentItems.remove(at: index)
            return true
        }
        return false
    }
    
    // Load initial sample data
    private func loadInitialData() {
        // Flashers
        let flashers: [EquipmentItem] = [
            EquipmentItem(
                name: "Hot Spot Flasher - Green",
                description: "11\" UV Green Flasher",
                type: .flasher,
                imageUrl: "flasher_green",
                specifications: [
                    "size": "11 inch",
                    "color": "UV Green",
                    "material": "Plastic"
                ],
                targetSpecies: [.chinook, .coho],
                waterClarityConditions: ["clear", "medium"],
                lightConditions: ["bright", "overcast"],
                weatherConditions: ["calm", "windy"],
                tideConditions: [.high, .rising]
            ),
            EquipmentItem(
                name: "Hot Spot Flasher - Red",
                description: "11\" UV Red Flasher",
                type: .flasher,
                imageUrl: "flasher_red",
                specifications: [
                    "size": "11 inch",
                    "color": "UV Red",
                    "material": "Plastic"
                ],
                targetSpecies: [.chinook, .coho],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["overcast", "low_light"],
                weatherConditions: ["calm", "rainy"],
                tideConditions: [.low, .falling]
            ),
            EquipmentItem(
                name: "Gibbs Delta Guide Series Flasher",
                description: "8\" Chrome Flasher",
                type: .flasher,
                imageUrl: "flasher_chrome",
                specifications: [
                    "size": "8 inch",
                    "color": "Chrome",
                    "material": "Metal"
                ],
                targetSpecies: [.chinook, .coho, .sockeye],
                waterClarityConditions: ["clear"],
                lightConditions: ["bright"],
                weatherConditions: ["calm"],
                tideConditions: [.high, .rising, .falling]
            ),
            EquipmentItem(
                name: "O'Ki Tackle Titan Flasher",
                description: "10\" UV Purple/Silver Flasher",
                type: .flasher,
                imageUrl: "flasher_titan",
                specifications: [
                    "size": "10 inch",
                    "color": "UV Purple/Silver",
                    "material": "Plastic"
                ],
                targetSpecies: [.chinook, .coho],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["overcast", "low_light"],
                weatherConditions: ["calm", "windy", "rainy"],
                tideConditions: [.high, .rising, .falling, .low]
            ),
            EquipmentItem(
                name: "Silver Horde Kingfisher Lite",
                description: "11\" Glow Green Flasher",
                type: .flasher,
                imageUrl: "flasher_kingfisher",
                specifications: [
                    "size": "11 inch",
                    "color": "Glow Green",
                    "material": "Plastic"
                ],
                targetSpecies: [.chinook, .coho],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["low_light"],
                weatherConditions: ["calm", "rainy"],
                tideConditions: [.high, .rising]
            )
        ]
        
        // Lures
        let lures: [EquipmentItem] = [
            EquipmentItem(
                name: "Coho Killer - Green",
                description: "Green spoon with glow stripe",
                type: .lure,
                imageUrl: "lure_coho_killer_green",
                specifications: [
                    "size": "3.5 inch",
                    "color": "Green/Glow",
                    "material": "Metal",
                    "weight": "1 oz"
                ],
                targetSpecies: [.coho, .chinook],
                waterClarityConditions: ["clear", "medium"],
                lightConditions: ["bright", "overcast"],
                weatherConditions: ["calm", "windy"],
                tideConditions: [.high, .rising]
            ),
            EquipmentItem(
                name: "Coho Killer - Blue",
                description: "Blue spoon with silver stripe",
                type: .lure,
                imageUrl: "lure_coho_killer_blue",
                specifications: [
                    "size": "3.5 inch",
                    "color": "Blue/Silver",
                    "material": "Metal",
                    "weight": "1 oz"
                ],
                targetSpecies: [.coho, .chinook],
                waterClarityConditions: ["clear", "medium"],
                lightConditions: ["bright", "overcast"],
                weatherConditions: ["calm", "windy"],
                tideConditions: [.high, .rising, .falling]
            ),
            EquipmentItem(
                name: "Hoochie - Purple",
                description: "Purple squid with glow spots",
                type: .lure,
                imageUrl: "lure_hoochie_purple",
                specifications: [
                    "size": "4 inch",
                    "color": "Purple/Glow",
                    "material": "Plastic"
                ],
                targetSpecies: [.chinook, .coho],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["overcast", "low_light"],
                weatherConditions: ["calm", "rainy"],
                tideConditions: [.low, .falling]
            ),
            EquipmentItem(
                name: "Apex Lure - UV Glow",
                description: "UV enhanced trolling lure",
                type: .lure,
                imageUrl: "lure_apex_uv",
                specifications: [
                    "size": "5 inch",
                    "color": "UV Glow",
                    "material": "Plastic"
                ],
                targetSpecies: [.chinook, .coho, .sockeye],
                waterClarityConditions: ["clear", "medium", "murky"],
                lightConditions: ["bright", "overcast", "low_light"],
                weatherConditions: ["calm", "windy", "rainy"],
                tideConditions: [.high, .rising, .falling, .low]
            ),
            EquipmentItem(
                name: "Coyote Spoon - Herring Scale",
                description: "Herring pattern trolling spoon",
                type: .lure,
                imageUrl: "lure_coyote_herring",
                specifications: [
                    "size": "4 inch",
                    "color": "Herring Scale",
                    "material": "Metal",
                    "weight": "1.5 oz"
                ],
                targetSpecies: [.chinook, .coho],
                waterClarityConditions: ["clear", "medium"],
                lightConditions: ["bright", "overcast"],
                weatherConditions: ["calm", "windy"],
                tideConditions: [.high, .rising]
            ),
            EquipmentItem(
                name: "Silver Horde Kingfisher - Army Truck",
                description: "Green/black pattern spoon",
                type: .lure,
                imageUrl: "lure_kingfisher_army",
                specifications: [
                    "size": "3.5 inch",
                    "color": "Green/Black",
                    "material": "Metal",
                    "weight": "1 oz"
                ],
                targetSpecies: [.chinook],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["overcast", "low_light"],
                weatherConditions: ["calm", "rainy"],
                tideConditions: [.low, .falling]
            )
        ]
        
        // Leaders
        let leaders: [EquipmentItem] = [
            EquipmentItem(
                name: "Fluorocarbon Leader - 30lb",
                description: "Clear fluorocarbon leader",
                type: .leader,
                imageUrl: "leader_fluorocarbon",
                specifications: [
                    "length": "36 inch",
                    "material": "Fluorocarbon",
                    "weight": "30 lb"
                ],
                targetSpecies: [.chinook, .coho, .sockeye, .pink, .chum],
                waterClarityConditions: ["clear"],
                lightConditions: ["bright"],
                weatherConditions: ["calm", "windy"],
                tideConditions: [.high, .low, .rising, .falling]
            ),
            EquipmentItem(
                name: "Monofilament Leader - 40lb",
                description: "Clear monofilament leader",
                type: .leader,
                imageUrl: "leader_mono",
                specifications: [
                    "length": "42 inch",
                    "material": "Monofilament",
                    "weight": "40 lb"
                ],
                targetSpecies: [.chinook, .coho, .sockeye, .pink, .chum],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["overcast", "low_light"],
                weatherConditions: ["calm", "rainy", "windy"],
                tideConditions: [.high, .low, .rising, .falling]
            ),
            EquipmentItem(
                name: "Wire Leader - 60lb",
                description: "Black wire leader",
                type: .leader,
                imageUrl: "leader_wire",
                specifications: [
                    "length": "24 inch",
                    "material": "Wire",
                    "weight": "60 lb"
                ],
                targetSpecies: [.chinook],
                waterClarityConditions: ["murky"],
                lightConditions: ["low_light"],
                weatherConditions: ["rainy"],
                tideConditions: [.low, .falling]
            ),
            EquipmentItem(
                name: "Short Fluorocarbon Leader - 25lb",
                description: "Short clear fluorocarbon leader",
                type: .leader,
                imageUrl: "leader_short_fluoro",
                specifications: [
                    "length": "24 inch",
                    "material": "Fluorocarbon",
                    "weight": "25 lb"
                ],
                targetSpecies: [.coho, .sockeye, .pink],
                waterClarityConditions: ["clear", "medium"],
                lightConditions: ["bright", "overcast"],
                weatherConditions: ["calm"],
                tideConditions: [.high, .rising]
            ),
            EquipmentItem(
                name: "Long Monofilament Leader - 50lb",
                description: "Extra long monofilament leader",
                type: .leader,
                imageUrl: "leader_long_mono",
                specifications: [
                    "length": "60 inch",
                    "material": "Monofilament",
                    "weight": "50 lb"
                ],
                targetSpecies: [.chinook],
                waterClarityConditions: ["medium", "murky"],
                lightConditions: ["overcast", "low_light"],
                weatherConditions: ["windy", "rainy"],
                tideConditions: [.high, .rising]
            )
        ]
        
        // Add all equipment to the database
        equipmentItems.append(contentsOf: flashers)
        equipmentItems.append(contentsOf: lures)
        equipmentItems.append(contentsOf: leaders)
    }
}

// Factory class to provide the appropriate database implementation
class EquipmentDatabaseFactory {
    static func getDatabase() -> EquipmentDatabase {
        // For now, we only have the in-memory implementation
        // In the future, this could return different implementations based on configuration
        return InMemoryEquipmentDatabase()
    }
}