package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.salmontrollingassistant.domain.model.ExperienceLevel
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.NotificationSettings
import com.example.salmontrollingassistant.domain.model.UserEquipment
import com.example.salmontrollingassistant.domain.model.UserPreferences
import com.example.salmontrollingassistant.domain.service.UserPreferencesService
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserPreferencesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    // Mock dependencies
    private val mockPreferencesService: UserPreferencesService = mockk()
    
    // Test data
    private lateinit var testPreferences: UserPreferences
    private lateinit var testEquipment: List<UserEquipment>
    
    // System under test
    private lateinit var viewModel: UserPreferencesViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup test data
        testPreferences = UserPreferences(
            preferredSpecies = listOf(FishSpecies.CHINOOK, FishSpecies.COHO),
            preferredEquipment = listOf("equipment1", "equipment2"),
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            notificationSettings = NotificationSettings(
                enableWeatherAlerts = true,
                enableTideAlerts = true,
                enableOptimalConditionAlerts = false
            )
        )
        
        testEquipment = listOf(
            UserEquipment(
                id = "userEquipment1",
                equipmentId = "equipment1",
                name = "My Flasher",
                isFavorite = true
            ),
            UserEquipment(
                id = "userEquipment2",
                equipmentId = "equipment2",
                name = "My Lure",
                isFavorite = false
            )
        )
        
        // Setup mocks
        every { mockPreferencesService.getUserPreferences() } returns flowOf(testPreferences)
        every { mockPreferencesService.getUserEquipment() } returns flowOf(testEquipment)
        
        // Create the view model
        viewModel = UserPreferencesViewModel(mockPreferencesService)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
    
    @Test
    fun `init should load user preferences and equipment`() = testDispatcher.runBlockingTest {
        // Then
        assertThat(viewModel.userPreferences.value).isEqualTo(testPreferences)
        assertThat(viewModel.preferredSpecies.value).isEqualTo(testPreferences.preferredSpecies)
        assertThat(viewModel.preferredEquipment.value).isEqualTo(testPreferences.preferredEquipment)
        assertThat(viewModel.experienceLevel.value).isEqualTo(testPreferences.experienceLevel)
        assertThat(viewModel.notificationSettings.value).isEqualTo(testPreferences.notificationSettings)
        assertThat(viewModel.userEquipment.value).isEqualTo(testEquipment)
    }
    
    @Test
    fun `updatePreferredSpecies should update species and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        val newSpecies = listOf(FishSpecies.SOCKEYE, FishSpecies.PINK)
        coEvery { mockPreferencesService.updatePreferredSpecies(newSpecies) } returns true
        
        // When
        viewModel.updatePreferredSpecies(newSpecies)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.updatePreferredSpecies(newSpecies) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `updatePreferredSpecies should set error state when unsuccessful`() = testDispatcher.runBlockingTest {
        // Given
        val newSpecies = listOf(FishSpecies.SOCKEYE, FishSpecies.PINK)
        coEvery { mockPreferencesService.updatePreferredSpecies(newSpecies) } returns false
        
        // When
        viewModel.updatePreferredSpecies(newSpecies)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.updatePreferredSpecies(newSpecies) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Error::class.java)
    }
    
    @Test
    fun `updatePreferredEquipment should update equipment and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        val newEquipment = listOf("equipment3", "equipment4")
        coEvery { mockPreferencesService.updatePreferredEquipment(newEquipment) } returns true
        
        // When
        viewModel.updatePreferredEquipment(newEquipment)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.updatePreferredEquipment(newEquipment) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `updateExperienceLevel should update level and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        val newLevel = ExperienceLevel.EXPERT
        coEvery { mockPreferencesService.updateExperienceLevel(newLevel.name) } returns true
        
        // When
        viewModel.updateExperienceLevel(newLevel)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.updateExperienceLevel(newLevel.name) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `updateNotificationSettings should update settings and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        coEvery { 
            mockPreferencesService.updateNotificationSettings(
                enableWeatherAlerts = false,
                enableTideAlerts = true,
                enableOptimalConditionAlerts = true
            ) 
        } returns true
        
        // When
        viewModel.updateNotificationSettings(
            enableWeatherAlerts = false,
            enableTideAlerts = true,
            enableOptimalConditionAlerts = true
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockPreferencesService.updateNotificationSettings(
                enableWeatherAlerts = false,
                enableTideAlerts = true,
                enableOptimalConditionAlerts = true
            )
        }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `addUserEquipment should add equipment and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        val newEquipment = UserEquipment(
            id = "userEquipment3",
            equipmentId = "equipment3",
            name = "New Equipment"
        )
        coEvery { mockPreferencesService.addUserEquipment(newEquipment) } returns true
        
        // When
        viewModel.addUserEquipment(newEquipment)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.addUserEquipment(newEquipment) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `removeUserEquipment should remove equipment and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        val equipmentId = "userEquipment1"
        coEvery { mockPreferencesService.removeUserEquipment(equipmentId) } returns true
        
        // When
        viewModel.removeUserEquipment(equipmentId)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.removeUserEquipment(equipmentId) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `updateUserEquipment should update equipment and set success state when successful`() = testDispatcher.runBlockingTest {
        // Given
        val updatedEquipment = testEquipment[0].copy(name = "Updated Name", isFavorite = false)
        coEvery { mockPreferencesService.updateUserEquipment(updatedEquipment) } returns true
        
        // When
        viewModel.updateUserEquipment(updatedEquipment)
        
        // Then
        coVerify(exactly = 1) { mockPreferencesService.updateUserEquipment(updatedEquipment) }
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Success::class.java)
    }
    
    @Test
    fun `resetUpdateState should set state to Initial`() = testDispatcher.runBlockingTest {
        // Given
        viewModel.updatePreferredSpecies(listOf(FishSpecies.CHINOOK))
        assertThat(viewModel.updateState.value).isNotInstanceOf(UserPreferencesViewModel.UpdateState.Initial::class.java)
        
        // When
        viewModel.resetUpdateState()
        
        // Then
        assertThat(viewModel.updateState.value).isInstanceOf(UserPreferencesViewModel.UpdateState.Initial::class.java)
    }
}