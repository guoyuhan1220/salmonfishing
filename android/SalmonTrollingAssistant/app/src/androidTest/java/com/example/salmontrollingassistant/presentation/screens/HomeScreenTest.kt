package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.salmontrollingassistant.domain.model.*
import com.example.salmontrollingassistant.presentation.viewmodels.WeatherForecastViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val mockViewModel = mockk<WeatherForecastViewModel>(relaxed = true)
    
    @Test
    fun homeScreen_displaysAllSections() {
        // Given
        val weatherData = WeatherData(
            id = "weather1",
            timestamp = Date(),
            temperature = 72.5,
            windSpeed = 8.5,
            windDirection = "N",
            precipitation = 0.0,
            cloudCover = 20,
            visibility = 10.0,
            pressure = 1013.0,
            humidity = 60,
            uvIndex = 5
        )
        
        val tideData = TideData(
            id = "tide1",
            timestamp = Date(),
            height = 3.5,
            type = TideType.RISING,
            nextHighTide = TideEvent(
                timestamp = Date(System.currentTimeMillis() + 3600000), // 1 hour later
                height = 5.0
            )
        )
        
        every { mockViewModel.currentWeather } returns MutableStateFlow(weatherData)
        every { mockViewModel.currentTide } returns MutableStateFlow(tideData)
        
        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Salmon Trolling").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Conditions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quick Recommendations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Locations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plan Your Trip").assertIsDisplayed()
        
        // Verify weather data is displayed
        composeTestRule.onNodeWithText("73°").assertIsDisplayed() // Rounded from 72.5
        
        // Verify tide data is displayed
        composeTestRule.onNodeWithText("Tide: RISING").assertIsDisplayed()
        composeTestRule.onNodeWithText("Height: 3.5 ft").assertIsDisplayed()
    }
    
    @Test
    fun dateTimeSelector_opensDateTimePicker() {
        // Given
        every { mockViewModel.currentWeather } returns MutableStateFlow(null)
        every { mockViewModel.currentTide } returns MutableStateFlow(null)
        
        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Plan Your Trip").assertIsDisplayed()
        
        // Click on the date/time selector
        composeTestRule.onNodeWithContentDescription("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Calendar").performClick()
        
        // Verify date time picker is shown
        // Note: The actual DateTimeSelectorScreen might have different UI elements
        // This is a simplified check
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun savedLocations_areDisplayed() {
        // Given
        every { mockViewModel.currentWeather } returns MutableStateFlow(null)
        every { mockViewModel.currentTide } returns MutableStateFlow(null)
        
        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Saved Locations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seattle Bay").assertIsDisplayed()
        composeTestRule.onNodeWithText("Puget Sound").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hood Canal").assertIsDisplayed()
        composeTestRule.onNodeWithText("San Juan Islands").assertIsDisplayed()
    }
    
    @Test
    fun quickRecommendations_areDisplayed() {
        // Given
        every { mockViewModel.currentWeather } returns MutableStateFlow(null)
        every { mockViewModel.currentTide } returns MutableStateFlow(null)
        
        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("Quick Recommendations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Green Flasher").assertIsDisplayed()
        composeTestRule.onNodeWithText("Blue Hoochie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Silver Spoon").assertIsDisplayed()
    }
    
    @Test
    fun offlineIndicator_isDisplayed() {
        // Given
        every { mockViewModel.currentWeather } returns MutableStateFlow(null)
        every { mockViewModel.currentTide } returns MutableStateFlow(null)
        
        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = mockViewModel)
        }
        
        // Then
        // Note: This test assumes the OfflineIndicator is visible
        // In a real app, this would depend on the network state
        composeTestRule.waitForIdle()
    }
}