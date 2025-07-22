package com.example.salmontrollingassistant.domain.service

import android.content.Context
import com.example.salmontrollingassistant.data.db.EquipmentDao
import com.example.salmontrollingassistant.data.db.EquipmentDatabase
import com.example.salmontrollingassistant.data.db.EquipmentEntity
import com.example.salmontrollingassistant.domain.model.*
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class RecommendationServiceTest {

    // Mock dependencies
    private val mockContext: Context = mockk()
    private val mockEquipmentDao: EquipmentDao = mockk()
    private val mockEquipmentDatabase: EquipmentDatabase = mockk()
    
    // Test data
    private lateinit var testWeatherData: WeatherData
    private lateinit var testTideData: TideData
    private lateinit var testEquipmentItems: List<EquipmentEntity>
    
    // System under test
    private lateinit var recommendationService: RecommendationService
    
    @Before
    fun setup() {
        // Setup test data
        testWeatherData = WeatherData(
            timestamp = Date(),
            temperature = 22.0,
            windSpeed = 5.0,
            windDirection = "N",
            precipitation = 0.0,
            cloudCover = 20,
            visibility = 10.0,
            pressure = 1013.0,
            humidity = 60,
            uvIndex = 5
        )
        
        testTideData = TideData(
            timestamp = Date(),
            height = 2.5,
            type = TideType.RISING
        )
        
        // Create test equipment items
        testEquipmentItems = listOf(
            EquipmentEntity(
                id = "flasher1",
                name = "Test Flasher 1",
                description = "A test flasher for clear water",
                type = "FLASHER",
                specifications = mapOf("size" to "Large", "color" to "Green"),
                waterClarityConditions = listOf("clear"),
                lightConditions = listOf("bright"),
                weatherConditions = listOf("calm"),
                tideConditions = listOf("RISING")
            ),
            EquipmentEntity(
                id = "flasher2",
                name = "Test Flasher 2",
                description = "A test flasher for murky water",
                type = "FLASHER",
                specifications = mapOf("size" to "Large", "color" to "Red"),
                waterClarityConditions = listOf("murky"),
                lightConditions = listOf("overcast"),
                weatherConditions = listOf("rainy"),
                tideConditions = listOf("FALLING")
            ),
            EquipmentEntity(
                id = "lure1",
                name = "Test Lure 1",
                description = "A test lure for clear water",
                type = "LURE",
                specifications = mapOf("size" to "Medium", "color" to "Blue"),
                waterClarityConditions = listOf("clear"),
                lightConditions = listOf("bright"),
                weatherConditions = listOf("calm"),
                tideConditions = listOf("RISING"),
                targetSpecies = listOf("CHINOOK")
            ),
            EquipmentEntity(
                id = "leader1",
                name = "Test Leader 1",
                description = "A test leader for clear water",
                type = "LEADER",
                specifications = mapOf("length" to "36 inches", "material" to "Fluorocarbon"),
                waterClarityConditions = listOf("clear"),
                lightConditions = listOf("bright"),
                weatherConditions = listOf("calm"),
                tideConditions = listOf("RISING")
            )
        )
        
        // Setup mocks
        every { mockEquipmentDatabase.equipmentDao() } returns mockEquipmentDao
        every { EquipmentDatabase.getDatabase(mockContext) } returns mockEquipmentDatabase
        
        // Mock the database query to return our test data
        coEvery { mockEquipmentDao.getAllEquipment() } returns flowOf(testEquipmentItems)
        
        // Create the service with mocked dependencies
        mockkStatic(EquipmentDatabase::class)
        recommendationService = RuleBasedRecommendationService(mockContext)
    }
    
    @Test
    fun `getRecommendations should return recommendations for all equipment types`() {
        // When
        val recommendations = recommendationService.getRecommendations(
            weatherData = testWeatherData,
            tideData = testTideData
        )
        
        // Then
        assertThat(recommendations).isNotEmpty()
        assertThat(recommendations.size).isEqualTo(3) // One for each equipment type
        
        // Verify we have recommendations for each equipment type
        val equipmentTypes = recommendations.map { it.type }
        assertThat(equipmentTypes).contains(EquipmentType.FLASHER)
        assertThat(equipmentTypes).contains(EquipmentType.LURE)
        assertThat(equipmentTypes).contains(EquipmentType.LEADER)
    }
    
    @Test
    fun `getRecommendations should filter by fish species when specified`() {
        // When
        val recommendations = recommendationService.getRecommendations(
            weatherData = testWeatherData,
            tideData = testTideData,
            fishSpecies = FishSpecies.CHINOOK
        )
        
        // Then
        assertThat(recommendations).isNotEmpty()
        
        // Find the lure recommendation
        val lureRecommendation = recommendations.find { it.type == EquipmentType.LURE }
        assertThat(lureRecommendation).isNotNull()
        
        // Verify that the lure recommendation contains items for Chinook
        val lureItems = lureRecommendation!!.items
        assertThat(lureItems).isNotEmpty()
        
        // Check if the recommendation reason mentions the species
        assertThat(lureRecommendation.reasonForRecommendation).contains("CHINOOK", ignoreCase = true)
    }
    
    @Test
    fun `getRecommendations should prioritize user equipment when provided`() {
        // Given
        val userEquipment = listOf(
            UserEquipment(
                equipmentId = "flasher1",
                equipmentType = EquipmentType.FLASHER,
                name = "My Favorite Flasher",
                isFavorite = true
            )
        )
        
        // When
        val recommendations = recommendationService.getRecommendations(
            weatherData = testWeatherData,
            tideData = testTideData,
            userEquipment = userEquipment
        )
        
        // Then
        assertThat(recommendations).isNotEmpty()
        
        // Find the flasher recommendation
        val flasherRecommendation = recommendations.find { it.type == EquipmentType.FLASHER }
        assertThat(flasherRecommendation).isNotNull()
        
        // Verify that the user's equipment is prioritized (should be first in the list)
        val flasherItems = flasherRecommendation!!.items
        assertThat(flasherItems).isNotEmpty()
        assertThat(flasherItems[0].id).isEqualTo("flasher1")
        
        // Check if the recommendation reason mentions user preferences
        assertThat(flasherRecommendation.reasonForRecommendation).contains("preferences", ignoreCase = true)
    }
    
    @Test
    fun `getEquipmentById should return correct equipment item`() {
        // When
        val equipment = recommendationService.getEquipmentById("flasher1")
        
        // Then
        assertThat(equipment).isNotNull()
        assertThat(equipment?.id).isEqualTo("flasher1")
        assertThat(equipment?.name).isEqualTo("Test Flasher 1")
        assertThat(equipment?.type).isEqualTo(EquipmentType.FLASHER)
    }
    
    @Test
    fun `getEquipmentById should return null for non-existent ID`() {
        // When
        val equipment = recommendationService.getEquipmentById("non-existent-id")
        
        // Then
        assertThat(equipment).isNull()
    }
    
    @Test
    fun `getEquipmentByType should return all equipment of specified type`() {
        // When
        val flashers = recommendationService.getEquipmentByType(EquipmentType.FLASHER)
        
        // Then
        assertThat(flashers).isNotEmpty()
        assertThat(flashers.size).isEqualTo(2) // We have 2 flashers in our test data
        assertThat(flashers.all { it.type == EquipmentType.FLASHER }).isTrue()
    }
}