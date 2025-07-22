package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.Location
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.model.TideEvent
import com.example.salmontrollingassistant.domain.model.TideType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Mock implementation of TideService for testing and development
 */
class MockTideService : TideService {
    
    override suspend fun getCurrentTide(location: Location): Result<TideData> {
        delay(500) // Simulate network delay
        return Result.success(generateTideData(Date()))
    }
    
    override suspend fun getTidePredictions(location: Location, days: Int): Result<List<TideData>> {
        delay(800) // Simulate network delay
        val limitedDays = minOf(days, 7) // Enforce 7-day limit
        val calendar = Calendar.getInstance()
        val predictions = mutableListOf<TideData>()
        
        // Generate 4 tide events per day (roughly every 6 hours)
        for (day in 0 until limitedDays) {
            for (i in 0 until 4) {
                calendar.add(Calendar.HOUR, 6)
                predictions.add(generateTideData(calendar.time))
            }
        }
        
        return Result.success(predictions)
    }
    
    override suspend fun getTideForDateTime(location: Location, dateTime: Date): Result<TideData> {
        delay(500) // Simulate network delay
        return Result.success(generateTideData(dateTime))
    }
    
    override fun observeTideData(location: Location): Flow<Result<TideData>> = flow {
        while (true) {
            emit(Result.success(generateTideData(Date())))
            delay(TimeUnit.MINUTES.toMillis(15)) // Update every 15 minutes
        }
    }
    
    /**
     * Generate realistic mock tide data for a given date
     */
    private fun generateTideData(date: Date): TideData {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // Determine tide type based on hour of day (simplified model)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val tideType = when {
            hour in 0..5 -> TideType.FALLING
            hour in 6..11 -> TideType.LOW
            hour in 12..17 -> TideType.RISING
            else -> TideType.HIGH
        }
        
        // Generate tide height based on type
        val height = when (tideType) {
            TideType.HIGH -> Random.nextDouble(2.0, 3.5)
            TideType.LOW -> Random.nextDouble(0.1, 0.8)
            TideType.RISING -> Random.nextDouble(0.8, 2.0)
            TideType.FALLING -> Random.nextDouble(0.8, 2.0)
        }
        
        // Generate next high and low tide events
        val nextHighTideCalendar = Calendar.getInstance()
        nextHighTideCalendar.time = date
        
        val nextLowTideCalendar = Calendar.getInstance()
        nextLowTideCalendar.time = date
        
        when (tideType) {
            TideType.HIGH -> {
                nextHighTideCalendar.add(Calendar.HOUR, 12)
                nextLowTideCalendar.add(Calendar.HOUR, 6)
            }
            TideType.LOW -> {
                nextHighTideCalendar.add(Calendar.HOUR, 6)
                nextLowTideCalendar.add(Calendar.HOUR, 12)
            }
            TideType.RISING -> {
                nextHighTideCalendar.add(Calendar.HOUR, 3)
                nextLowTideCalendar.add(Calendar.HOUR, 9)
            }
            TideType.FALLING -> {
                nextHighTideCalendar.add(Calendar.HOUR, 9)
                nextLowTideCalendar.add(Calendar.HOUR, 3)
            }
        }
        
        val nextHighTide = TideEvent(
            timestamp = nextHighTideCalendar.time,
            height = Random.nextDouble(2.0, 3.5)
        )
        
        val nextLowTide = TideEvent(
            timestamp = nextLowTideCalendar.time,
            height = Random.nextDouble(0.1, 0.8)
        )
        
        return TideData(
            id = UUID.randomUUID().toString(),
            timestamp = date,
            height = height,
            type = tideType,
            nextHighTide = nextHighTide,
            nextLowTide = nextLowTide
        )
    }
}