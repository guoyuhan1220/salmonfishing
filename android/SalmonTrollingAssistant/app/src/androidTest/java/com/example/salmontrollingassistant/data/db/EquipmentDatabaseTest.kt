package com.example.salmontrollingassistant.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.salmontrollingassistant.domain.model.EquipmentType
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.TideType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class EquipmentDatabaseTest {
    private lateinit var equipmentDao: EquipmentDao
    private lateinit var db: EquipmentDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, EquipmentDatabase::class.java
        ).build()
        equipmentDao = db.equipmentDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
    
    @Test
    @Throws(Exception::class)
    fun insertAndGetEquipment() = runBlocking {
        // Create test equipment
        val equipment = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name, FishSpecies.COHO.name),
            waterClarityConditions = listOf("clear", "medium"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(equipment)
        
        // Get equipment by ID
        val retrievedEquipment = equipmentDao.getEquipmentById(equipment.id)
        
        // Verify
        assertNotNull(retrievedEquipment)
        assertEquals(equipment.id, retrievedEquipment?.id)
        assertEquals(equipment.name, retrievedEquipment?.name)
        assertEquals(equipment.type, retrievedEquipment?.type)
        assertEquals(equipment.specifications, retrievedEquipment?.specifications)
        assertEquals(equipment.targetSpecies, retrievedEquipment?.targetSpecies)
    }
    
    @Test
    @Throws(Exception::class)
    fun getAllEquipment() = runBlocking {
        // Create test equipment
        val equipment1 = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher 1",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name),
            waterClarityConditions = listOf("clear"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        val equipment2 = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Lure 1",
            description = "A test lure",
            type = EquipmentType.LURE.name,
            imageUrl = null,
            specifications = mapOf("size" to "Medium", "color" to "Blue"),
            targetSpecies = listOf(FishSpecies.COHO.name),
            waterClarityConditions = listOf("medium"),
            lightConditions = listOf("overcast"),
            weatherConditions = listOf("windy"),
            tideConditions = listOf(TideType.FALLING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(equipment1)
        equipmentDao.insertEquipment(equipment2)
        
        // Get all equipment
        val allEquipment = equipmentDao.getAllEquipment().first()
        
        // Verify
        assertEquals(2, allEquipment.size)
        assertEquals(equipment1.id, allEquipment.find { it.id == equipment1.id }?.id)
        assertEquals(equipment2.id, allEquipment.find { it.id == equipment2.id }?.id)
    }
    
    @Test
    @Throws(Exception::class)
    fun getEquipmentByType() = runBlocking {
        // Create test equipment
        val flasher = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name),
            waterClarityConditions = listOf("clear"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        val lure = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Lure",
            description = "A test lure",
            type = EquipmentType.LURE.name,
            imageUrl = null,
            specifications = mapOf("size" to "Medium", "color" to "Blue"),
            targetSpecies = listOf(FishSpecies.COHO.name),
            waterClarityConditions = listOf("medium"),
            lightConditions = listOf("overcast"),
            weatherConditions = listOf("windy"),
            tideConditions = listOf(TideType.FALLING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(flasher)
        equipmentDao.insertEquipment(lure)
        
        // Get equipment by type
        val flashers = equipmentDao.getEquipmentByType(EquipmentType.FLASHER.name).first()
        val lures = equipmentDao.getEquipmentByType(EquipmentType.LURE.name).first()
        
        // Verify
        assertEquals(1, flashers.size)
        assertEquals(1, lures.size)
        assertEquals(flasher.id, flashers[0].id)
        assertEquals(lure.id, lures[0].id)
    }
    
    @Test
    @Throws(Exception::class)
    fun updateEquipment() = runBlocking {
        // Create test equipment
        val equipment = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name),
            waterClarityConditions = listOf("clear"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(equipment)
        
        // Update equipment
        val updatedEquipment = equipment.copy(
            name = "Updated Flasher",
            specifications = mapOf("size" to "Medium", "color" to "Red")
        )
        equipmentDao.updateEquipment(updatedEquipment)
        
        // Get updated equipment
        val retrievedEquipment = equipmentDao.getEquipmentById(equipment.id)
        
        // Verify
        assertNotNull(retrievedEquipment)
        assertEquals(updatedEquipment.name, retrievedEquipment?.name)
        assertEquals(updatedEquipment.specifications, retrievedEquipment?.specifications)
    }
    
    @Test
    @Throws(Exception::class)
    fun deleteEquipment() = runBlocking {
        // Create test equipment
        val equipment = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name),
            waterClarityConditions = listOf("clear"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(equipment)
        
        // Delete equipment
        equipmentDao.deleteEquipment(equipment)
        
        // Try to get deleted equipment
        val retrievedEquipment = equipmentDao.getEquipmentById(equipment.id)
        
        // Verify
        assertNull(retrievedEquipment)
    }
    
    @Test
    @Throws(Exception::class)
    fun deleteEquipmentById() = runBlocking {
        // Create test equipment
        val equipment = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name),
            waterClarityConditions = listOf("clear"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(equipment)
        
        // Delete equipment by ID
        equipmentDao.deleteEquipmentById(equipment.id)
        
        // Try to get deleted equipment
        val retrievedEquipment = equipmentDao.getEquipmentById(equipment.id)
        
        // Verify
        assertNull(retrievedEquipment)
    }
    
    @Test
    @Throws(Exception::class)
    fun deleteAllEquipment() = runBlocking {
        // Create test equipment
        val equipment1 = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Flasher",
            description = "A test flasher",
            type = EquipmentType.FLASHER.name,
            imageUrl = null,
            specifications = mapOf("size" to "Large", "color" to "Green"),
            targetSpecies = listOf(FishSpecies.CHINOOK.name),
            waterClarityConditions = listOf("clear"),
            lightConditions = listOf("bright"),
            weatherConditions = listOf("calm"),
            tideConditions = listOf(TideType.RISING.name)
        )
        
        val equipment2 = EquipmentEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Lure",
            description = "A test lure",
            type = EquipmentType.LURE.name,
            imageUrl = null,
            specifications = mapOf("size" to "Medium", "color" to "Blue"),
            targetSpecies = listOf(FishSpecies.COHO.name),
            waterClarityConditions = listOf("medium"),
            lightConditions = listOf("overcast"),
            weatherConditions = listOf("windy"),
            tideConditions = listOf(TideType.FALLING.name)
        )
        
        // Insert equipment
        equipmentDao.insertEquipment(equipment1)
        equipmentDao.insertEquipment(equipment2)
        
        // Delete all equipment
        equipmentDao.deleteAllEquipment()
        
        // Get all equipment
        val allEquipment = equipmentDao.getAllEquipment().first()
        
        // Verify
        assertEquals(0, allEquipment.size)
    }
}