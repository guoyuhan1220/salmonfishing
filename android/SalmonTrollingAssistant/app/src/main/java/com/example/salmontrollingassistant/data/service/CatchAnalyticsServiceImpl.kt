package com.example.salmontrollingassistant.data.service

import com.example.salmontrollingassistant.domain.model.CatchData
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.service.CatchAnalyticsService
import com.example.salmontrollingassistant.domain.service.CatchLoggingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatchAnalyticsServiceImpl @Inject constructor(
    private val catchLoggingService: CatchLoggingService
) : CatchAnalyticsService {
    
    override fun getCatchCountBySpecies(): Flow<Map<FishSpecies, Int>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            catches.groupingBy { it.species }.eachCount()
        }
    }
    
    override fun getCatchCountByLocation(): Flow<Map<String, Int>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            catches.groupingBy { it.locationId }.eachCount()
        }
    }
    
    override fun getCatchCountByMonth(): Flow<Map<Int, Int>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            catches.groupBy { catch ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = catch.timestamp
                calendar.get(Calendar.MONTH)
            }.mapValues { it.value.size }
        }
    }
    
    override fun getAverageSizeBySpecies(): Flow<Map<FishSpecies, Double>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            catches
                .filter { it.size != null }
                .groupBy { it.species }
                .mapValues { entry ->
                    entry.value.mapNotNull { it.size }.average()
                }
        }
    }
    
    override fun getAverageWeightBySpecies(): Flow<Map<FishSpecies, Double>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            catches
                .filter { it.weight != null }
                .groupBy { it.species }
                .mapValues { entry ->
                    entry.value.mapNotNull { it.weight }.average()
                }
        }
    }
    
    override fun getMostSuccessfulEquipment(): Flow<List<Pair<String, Int>>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            // Flatten the list of equipment used in each catch
            val allEquipment = catches.flatMap { it.equipmentUsed }
            
            // Count occurrences of each equipment
            val equipmentCounts = allEquipment.groupingBy { it }.eachCount()
            
            // Sort by count in descending order
            equipmentCounts.entries
                .sortedByDescending { it.value }
                .map { Pair(it.key, it.value) }
        }
    }
    
    override fun getMostSuccessfulLocations(): Flow<List<Pair<String, Int>>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            catches
                .groupingBy { it.locationId }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { Pair(it.key, it.value) }
        }
    }
    
    override fun getCatchTrendOverTime(): Flow<List<Pair<Date, Int>>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            // Group catches by day
            val calendar = Calendar.getInstance()
            
            catches
                .groupBy { catch ->
                    calendar.timeInMillis = catch.timestamp
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    Date(calendar.timeInMillis)
                }
                .entries
                .sortedBy { it.key }
                .map { Pair(it.key, it.value.size) }
        }
    }
    
    override fun getPersonalizedRecommendations(): Flow<List<String>> {
        return catchLoggingService.getCatchHistory().map { catches ->
            if (catches.isEmpty()) {
                return@map listOf("Start logging your catches to get personalized recommendations!")
            }
            
            val recommendations = mutableListOf<String>()
            
            // Most successful species
            val speciesCounts = catches.groupingBy { it.species }.eachCount()
            val mostCaughtSpecies = speciesCounts.maxByOrNull { it.value }?.key
            if (mostCaughtSpecies != null) {
                recommendations.add("You've had the most success catching ${mostCaughtSpecies.name}.")
            }
            
            // Most successful location
            val locationCounts = catches.groupingBy { it.locationId }.eachCount()
            val mostSuccessfulLocation = locationCounts.maxByOrNull { it.value }?.key
            if (mostSuccessfulLocation != null) {
                recommendations.add("Your most productive fishing spot is location $mostSuccessfulLocation.")
            }
            
            // Most successful equipment
            val allEquipment = catches.flatMap { it.equipmentUsed }
            val equipmentCounts = allEquipment.groupingBy { it }.eachCount()
            val mostSuccessfulEquipment = equipmentCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }
            
            if (mostSuccessfulEquipment.isNotEmpty()) {
                recommendations.add("Your most effective equipment includes: ${mostSuccessfulEquipment.joinToString(", ")}.")
            }
            
            // Best time of year
            val monthCounts = catches.groupBy { catch ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = catch.timestamp
                calendar.get(Calendar.MONTH)
            }.mapValues { it.value.size }
            
            val bestMonth = monthCounts.maxByOrNull { it.value }?.key
            if (bestMonth != null) {
                val monthName = when (bestMonth) {
                    Calendar.JANUARY -> "January"
                    Calendar.FEBRUARY -> "February"
                    Calendar.MARCH -> "March"
                    Calendar.APRIL -> "April"
                    Calendar.MAY -> "May"
                    Calendar.JUNE -> "June"
                    Calendar.JULY -> "July"
                    Calendar.AUGUST -> "August"
                    Calendar.SEPTEMBER -> "September"
                    Calendar.OCTOBER -> "October"
                    Calendar.NOVEMBER -> "November"
                    Calendar.DECEMBER -> "December"
                    else -> "Unknown"
                }
                recommendations.add("Your best fishing month appears to be $monthName.")
            }
            
            recommendations
        }
    }
}