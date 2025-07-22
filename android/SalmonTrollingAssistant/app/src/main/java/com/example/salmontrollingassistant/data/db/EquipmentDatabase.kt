package com.example.salmontrollingassistant.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.salmontrollingassistant.domain.model.EquipmentType
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.TideType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [EquipmentEntity::class], version = 1, exportSchema = false)
@TypeConverters(EquipmentConverters::class)
abstract class EquipmentDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    
    companion object {
        @Volatile
        private var INSTANCE: EquipmentDatabase? = null
        
        fun getDatabase(context: Context): EquipmentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EquipmentDatabase::class.java,
                    "equipment_database"
                )
                .addCallback(EquipmentDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class EquipmentDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.equipmentDao())
                    }
                }
            }
        }
        
        private suspend fun populateDatabase(equipmentDao: EquipmentDao) {
            // Clear all data
            equipmentDao.deleteAllEquipment()
            
            // Add sample data
            val sampleEquipment = createSampleEquipment()
            equipmentDao.insertAllEquipment(sampleEquipment)
        }
        
        private fun createSampleEquipment(): List<EquipmentEntity> {
            val equipmentList = mutableListOf<EquipmentEntity>()
            
            // Flashers
            equipmentList.add(
                EquipmentEntity(
                    name = "Hot Spot Flasher - Green",
                    description = "11\" UV Green Flasher",
                    type = EquipmentType.FLASHER.name,
                    imageUrl = "flasher_green",
                    specifications = mapOf(
                        "size" to "11 inch",
                        "color" to "UV Green",
                        "material" to "Plastic"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
                    waterClarityConditions = listOf("clear", "medium"),
                    lightConditions = listOf("bright", "overcast"),
                    weatherConditions = listOf("calm", "windy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Hot Spot Flasher - Red",
                    description = "11\" UV Red Flasher",
                    type = EquipmentType.FLASHER.name,
                    imageUrl = "flasher_red",
                    specifications = mapOf(
                        "size" to "11 inch",
                        "color" to "UV Red",
                        "material" to "Plastic"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("overcast", "low_light"),
                    weatherConditions = listOf("calm", "rainy"),
                    tideConditions = listOf(TideType.LOW.name, TideType.FALLING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Gibbs Delta Guide Series Flasher",
                    description = "8\" Chrome Flasher",
                    type = EquipmentType.FLASHER.name,
                    imageUrl = "flasher_chrome",
                    specifications = mapOf(
                        "size" to "8 inch",
                        "color" to "Chrome",
                        "material" to "Metal"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name, FishSpecies.SOCKEYE.name),
                    waterClarityConditions = listOf("clear"),
                    lightConditions = listOf("bright"),
                    weatherConditions = listOf("calm"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name, TideType.FALLING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "O'Ki Tackle Titan Flasher",
                    description = "10\" UV Purple/Silver Flasher",
                    type = EquipmentType.FLASHER.name,
                    imageUrl = "flasher_titan",
                    specifications = mapOf(
                        "size" to "10 inch",
                        "color" to "UV Purple/Silver",
                        "material" to "Plastic"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("overcast", "low_light"),
                    weatherConditions = listOf("calm", "windy", "rainy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name, TideType.FALLING.name, TideType.LOW.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Silver Horde Kingfisher Lite",
                    description = "11\" Glow Green Flasher",
                    type = EquipmentType.FLASHER.name,
                    imageUrl = "flasher_kingfisher",
                    specifications = mapOf(
                        "size" to "11 inch",
                        "color" to "Glow Green",
                        "material" to "Plastic"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("low_light"),
                    weatherConditions = listOf("calm", "rainy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name)
                )
            )
            
            // Lures
            equipmentList.add(
                EquipmentEntity(
                    name = "Coho Killer - Green",
                    description = "Green spoon with glow stripe",
                    type = EquipmentType.LURE.name,
                    imageUrl = "lure_coho_killer_green",
                    specifications = mapOf(
                        "size" to "3.5 inch",
                        "color" to "Green/Glow",
                        "material" to "Metal",
                        "weight" to "1 oz"
                    ),
                    targetSpecies = listOf(FishSpecies.COHO.name, FishSpecies.CHINOOK.name),
                    waterClarityConditions = listOf("clear", "medium"),
                    lightConditions = listOf("bright", "overcast"),
                    weatherConditions = listOf("calm", "windy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Coho Killer - Blue",
                    description = "Blue spoon with silver stripe",
                    type = EquipmentType.LURE.name,
                    imageUrl = "lure_coho_killer_blue",
                    specifications = mapOf(
                        "size" to "3.5 inch",
                        "color" to "Blue/Silver",
                        "material" to "Metal",
                        "weight" to "1 oz"
                    ),
                    targetSpecies = listOf(FishSpecies.COHO.name, FishSpecies.CHINOOK.name),
                    waterClarityConditions = listOf("clear", "medium"),
                    lightConditions = listOf("bright", "overcast"),
                    weatherConditions = listOf("calm", "windy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name, TideType.FALLING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Hoochie - Purple",
                    description = "Purple squid with glow spots",
                    type = EquipmentType.LURE.name,
                    imageUrl = "lure_hoochie_purple",
                    specifications = mapOf(
                        "size" to "4 inch",
                        "color" to "Purple/Glow",
                        "material" to "Plastic"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("overcast", "low_light"),
                    weatherConditions = listOf("calm", "rainy"),
                    tideConditions = listOf(TideType.LOW.name, TideType.FALLING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Apex Lure - UV Glow",
                    description = "UV enhanced trolling lure",
                    type = EquipmentType.LURE.name,
                    imageUrl = "lure_apex_uv",
                    specifications = mapOf(
                        "size" to "5 inch",
                        "color" to "UV Glow",
                        "material" to "Plastic"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name, FishSpecies.SOCKEYE.name),
                    waterClarityConditions = listOf("clear", "medium", "murky"),
                    lightConditions = listOf("bright", "overcast", "low_light"),
                    weatherConditions = listOf("calm", "windy", "rainy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name, TideType.FALLING.name, TideType.LOW.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Coyote Spoon - Herring Scale",
                    description = "Herring pattern trolling spoon",
                    type = EquipmentType.LURE.name,
                    imageUrl = "lure_coyote_herring",
                    specifications = mapOf(
                        "size" to "4 inch",
                        "color" to "Herring Scale",
                        "material" to "Metal",
                        "weight" to "1.5 oz"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
                    waterClarityConditions = listOf("clear", "medium"),
                    lightConditions = listOf("bright", "overcast"),
                    weatherConditions = listOf("calm", "windy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Silver Horde Kingfisher - Army Truck",
                    description = "Green/black pattern spoon",
                    type = EquipmentType.LURE.name,
                    imageUrl = "lure_kingfisher_army",
                    specifications = mapOf(
                        "size" to "3.5 inch",
                        "color" to "Green/Black",
                        "material" to "Metal",
                        "weight" to "1 oz"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("overcast", "low_light"),
                    weatherConditions = listOf("calm", "rainy"),
                    tideConditions = listOf(TideType.LOW.name, TideType.FALLING.name)
                )
            )
            
            // Leaders
            equipmentList.add(
                EquipmentEntity(
                    name = "Fluorocarbon Leader - 30lb",
                    description = "Clear fluorocarbon leader",
                    type = EquipmentType.LEADER.name,
                    imageUrl = "leader_fluorocarbon",
                    specifications = mapOf(
                        "length" to "36 inch",
                        "material" to "Fluorocarbon",
                        "weight" to "30 lb"
                    ),
                    targetSpecies = listOf(
                        FishSpecies.CHINOOK.name, 
                        FishSpecies.COHO.name, 
                        FishSpecies.SOCKEYE.name, 
                        FishSpecies.PINK.name, 
                        FishSpecies.CHUM.name
                    ),
                    waterClarityConditions = listOf("clear"),
                    lightConditions = listOf("bright"),
                    weatherConditions = listOf("calm", "windy"),
                    tideConditions = listOf(
                        TideType.HIGH.name, 
                        TideType.LOW.name, 
                        TideType.RISING.name, 
                        TideType.FALLING.name
                    )
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Monofilament Leader - 40lb",
                    description = "Clear monofilament leader",
                    type = EquipmentType.LEADER.name,
                    imageUrl = "leader_mono",
                    specifications = mapOf(
                        "length" to "42 inch",
                        "material" to "Monofilament",
                        "weight" to "40 lb"
                    ),
                    targetSpecies = listOf(
                        FishSpecies.CHINOOK.name, 
                        FishSpecies.COHO.name, 
                        FishSpecies.SOCKEYE.name, 
                        FishSpecies.PINK.name, 
                        FishSpecies.CHUM.name
                    ),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("overcast", "low_light"),
                    weatherConditions = listOf("calm", "rainy", "windy"),
                    tideConditions = listOf(
                        TideType.HIGH.name, 
                        TideType.LOW.name, 
                        TideType.RISING.name, 
                        TideType.FALLING.name
                    )
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Wire Leader - 60lb",
                    description = "Black wire leader",
                    type = EquipmentType.LEADER.name,
                    imageUrl = "leader_wire",
                    specifications = mapOf(
                        "length" to "24 inch",
                        "material" to "Wire",
                        "weight" to "60 lb"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name),
                    waterClarityConditions = listOf("murky"),
                    lightConditions = listOf("low_light"),
                    weatherConditions = listOf("rainy"),
                    tideConditions = listOf(TideType.LOW.name, TideType.FALLING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Short Fluorocarbon Leader - 25lb",
                    description = "Short clear fluorocarbon leader",
                    type = EquipmentType.LEADER.name,
                    imageUrl = "leader_short_fluoro",
                    specifications = mapOf(
                        "length" to "24 inch",
                        "material" to "Fluorocarbon",
                        "weight" to "25 lb"
                    ),
                    targetSpecies = listOf(FishSpecies.COHO.name, FishSpecies.SOCKEYE.name, FishSpecies.PINK.name),
                    waterClarityConditions = listOf("clear", "medium"),
                    lightConditions = listOf("bright", "overcast"),
                    weatherConditions = listOf("calm"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name)
                )
            )
            
            equipmentList.add(
                EquipmentEntity(
                    name = "Long Monofilament Leader - 50lb",
                    description = "Extra long monofilament leader",
                    type = EquipmentType.LEADER.name,
                    imageUrl = "leader_long_mono",
                    specifications = mapOf(
                        "length" to "60 inch",
                        "material" to "Monofilament",
                        "weight" to "50 lb"
                    ),
                    targetSpecies = listOf(FishSpecies.CHINOOK.name),
                    waterClarityConditions = listOf("medium", "murky"),
                    lightConditions = listOf("overcast", "low_light"),
                    weatherConditions = listOf("windy", "rainy"),
                    tideConditions = listOf(TideType.HIGH.name, TideType.RISING.name)
                )
            )
            
            return equipmentList
        }
    }
}