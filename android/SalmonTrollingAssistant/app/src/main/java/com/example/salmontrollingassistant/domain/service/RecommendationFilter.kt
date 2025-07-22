package com.example.salmontrollingassistant.domain.service

import android.content.Context
import com.example.salmontrollingassistant.domain.model.*
import kotlin.math.max
import kotlin.math.min

// Interface for recommendation filtering
interface RecommendationFilter {
    fun filterBySpecies(recommendations: List<EquipmentRecommendation>, species: FishSpecies): List<EquipmentRecommendation>
    fun filterByWaterClarity(recommendations: List<EquipmentRecommendation>, clarity: WaterClarity): List<EquipmentRecommendation>
    fun prioritizeUserPreferences(recommendations: List<EquipmentRecommendation>, userEquipment: List<UserEquipment>): List<EquipmentRecommendation>
}

class RecommendationFilterImpl(context: Context) : RecommendationFilter {
    private val recommendationService: RecommendationService = RuleBasedRecommendationService(context)
    
    override fun filterBySpecies(recommendations: List<EquipmentRecommendation>, species: FishSpecies): List<EquipmentRecommendation> {
        return recommendations.map { recommendation ->
            // Filter items in each recommendation by species
            val filteredItems = recommendation.items.filter { item ->
                item.targetSpecies?.contains(species) ?: true
            }
            
            // If no items match, keep original items
            val items = if (filteredItems.isEmpty()) recommendation.items else filteredItems
            
            // Create new recommendation with filtered items
            EquipmentRecommendation(
                id = recommendation.id,
                type = recommendation.type,
                items = items,
                reasonForRecommendation = recommendation.reasonForRecommendation + " Filtered for ${species.name} salmon.",
                confidenceScore = calculateConfidenceScore(filteredItems = items, originalItems = recommendation.items, recommendation = recommendation)
            )
        }
    }
    
    override fun filterByWaterClarity(recommendations: List<EquipmentRecommendation>, clarity: WaterClarity): List<EquipmentRecommendation> {
        return recommendations.map { recommendation ->
            // Filter items in each recommendation by water clarity
            val filteredItems = recommendation.items.filter { item ->
                item.waterClarityConditions?.contains(clarity.name.lowercase()) ?: true
            }
            
            // If no items match, keep original items
            val items = if (filteredItems.isEmpty()) recommendation.items else filteredItems
            
            // Create new recommendation with filtered items
            EquipmentRecommendation(
                id = recommendation.id,
                type = recommendation.type,
                items = items,
                reasonForRecommendation = recommendation.reasonForRecommendation + " Optimized for ${clarity.name.lowercase()} water clarity.",
                confidenceScore = calculateConfidenceScore(filteredItems = items, originalItems = recommendation.items, recommendation = recommendation)
            )
        }
    }
    
    override fun prioritizeUserPreferences(recommendations: List<EquipmentRecommendation>, userEquipment: List<UserEquipment>): List<EquipmentRecommendation> {
        return recommendations.map { recommendation ->
            // Find user equipment of the same type
            val userEquipmentIds = userEquipment.map { it.equipmentId }
            
            // Filter items that are in user's equipment
            val userItems = recommendation.items.filter { item ->
                userEquipmentIds.contains(item.id)
            }
            
            // If user has equipment of this type, prioritize it
            val items = if (userItems.isEmpty()) {
                recommendation.items
            } else {
                userItems + recommendation.items.filter { !userItems.contains(it) }
            }
            
            // Create new recommendation with prioritized items
            EquipmentRecommendation(
                id = recommendation.id,
                type = recommendation.type,
                items = items,
                reasonForRecommendation = if (userItems.isEmpty()) {
                    recommendation.reasonForRecommendation
                } else {
                    recommendation.reasonForRecommendation + " Prioritized based on your equipment preferences."
                },
                confidenceScore = if (userItems.isEmpty()) {
                    recommendation.confidenceScore
                } else {
                    min(0.95f, recommendation.confidenceScore + 0.1f)
                }
            )
        }
    }
    
    // Helper method to calculate confidence score after filtering
    private fun calculateConfidenceScore(filteredItems: List<EquipmentItem>, originalItems: List<EquipmentItem>, recommendation: EquipmentRecommendation): Float {
        if (filteredItems.size == originalItems.size) {
            // No filtering occurred
            return recommendation.confidenceScore
        }
        
        if (filteredItems.isEmpty()) {
            // All items were filtered out, which means low confidence
            return max(0.3f, recommendation.confidenceScore - 0.2f)
        }
        
        // More specific recommendations (fewer items) have higher confidence
        val specificityIncrease = (originalItems.size - filteredItems.size).toFloat() / originalItems.size.toFloat() * 0.2f
        
        // Increase confidence due to more specific filtering, but cap at 0.95
        return min(0.95f, recommendation.confidenceScore + specificityIncrease)
    }
}