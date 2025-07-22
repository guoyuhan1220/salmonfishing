package com.example.salmontrollingassistant.domain.service

import android.graphics.Color
import com.example.salmontrollingassistant.domain.model.*
import java.text.DecimalFormat

// Interface for recommendation explanation service
interface RecommendationExplanationService {
    fun generateDetailedExplanation(recommendation: EquipmentRecommendation, weatherData: WeatherData, tideData: TideData): String
    fun generateConfidenceDescription(confidenceScore: Float): String
    fun getConfidenceColor(confidenceScore: Float): Int // Returns color as an integer for Android UI
}

class RecommendationExplanationServiceImpl : RecommendationExplanationService {
    
    private val decimalFormat = DecimalFormat("#.#")
    
    override fun generateDetailedExplanation(recommendation: EquipmentRecommendation, weatherData: WeatherData, tideData: TideData): String {
        var explanation = recommendation.reasonForRecommendation
        
        // Add more detailed explanation based on equipment type
        when (recommendation.type) {
            EquipmentType.FLASHER -> explanation += generateFlasherDetailedExplanation(weatherData, tideData)
            EquipmentType.LURE -> explanation += generateLureDetailedExplanation(weatherData, tideData)
            EquipmentType.LEADER -> explanation += generateLeaderDetailedExplanation(weatherData, tideData)
        }
        
        // Add specific equipment details if there are recommended items
        if (recommendation.items.isNotEmpty()) {
            explanation += "\n\nRecommended ${recommendation.type.name.lowercase()}s include:"
            
            // Limit to top 3 recommendations
            recommendation.items.take(3).forEach { item ->
                explanation += "\n• ${item.name}: ${item.description}"
                
                // Add key specifications based on equipment type
                when (recommendation.type) {
                    EquipmentType.FLASHER -> {
                        val size = item.specifications["size"]
                        val color = item.specifications["color"]
                        if (size != null && color != null) {
                            explanation += " ($size, $color)"
                        }
                    }
                    EquipmentType.LURE -> {
                        val size = item.specifications["size"]
                        val color = item.specifications["color"]
                        if (size != null && color != null) {
                            explanation += " ($size, $color)"
                        }
                    }
                    EquipmentType.LEADER -> {
                        val length = item.specifications["length"]
                        val material = item.specifications["material"]
                        val weight = item.specifications["weight"]
                        if (length != null && material != null && weight != null) {
                            explanation += " ($length, $material, $weight)"
                        }
                    }
                }
            }
            
            // Indicate if there are more recommendations
            if (recommendation.items.size > 3) {
                explanation += "\n• ...and ${recommendation.items.size - 3} more options"
            }
        }
        
        return explanation
    }
    
    override fun generateConfidenceDescription(confidenceScore: Float): String {
        return when {
            confidenceScore >= 0.8 -> "High confidence recommendation based on current conditions"
            confidenceScore >= 0.6 -> "Medium-high confidence recommendation"
            confidenceScore >= 0.4 -> "Medium confidence recommendation"
            confidenceScore >= 0.2 -> "Low-medium confidence recommendation"
            else -> "Low confidence recommendation - consider trying different options"
        }
    }
    
    override fun getConfidenceColor(confidenceScore: Float): Int {
        return when {
            confidenceScore >= 0.8 -> Color.parseColor("#4CAF50") // Green - High confidence
            confidenceScore >= 0.6 -> Color.parseColor("#8BC34A") // Light Green - Medium-high confidence
            confidenceScore >= 0.4 -> Color.parseColor("#FFEB3B") // Yellow - Medium confidence
            confidenceScore >= 0.2 -> Color.parseColor("#FF9800") // Orange - Low-medium confidence
            else -> Color.parseColor("#F44336") // Red - Low confidence
        }
    }
    
    // Helper methods for generating detailed explanations
    private fun generateFlasherDetailedExplanation(weatherData: WeatherData, tideData: TideData): String {
        var explanation = "\n\nFlashers create visual attraction through reflection and movement in the water. "
        
        // Water clarity explanation
        val waterClarity = WaterClarity.fromVisibility(weatherData.visibility)
        when (waterClarity) {
            WaterClarity.CLEAR -> 
                explanation += "In clear water conditions (visibility: ${decimalFormat.format(weatherData.visibility)} meters), fish can see further, so more subtle flashers with natural colors work well. "
            WaterClarity.MEDIUM -> 
                explanation += "In medium water clarity (visibility: ${decimalFormat.format(weatherData.visibility)} meters), moderately bright flashers provide good visibility without being too aggressive. "
            WaterClarity.MURKY -> 
                explanation += "In murky water conditions (visibility: ${decimalFormat.format(weatherData.visibility)} meters), bright, high-contrast flashers help fish locate your lure from further away. "
        }
        
        // Light conditions explanation
        val lightCondition = LightCondition.fromWeatherData(weatherData)
        when (lightCondition) {
            LightCondition.BRIGHT -> 
                explanation += "With bright light conditions (cloud cover: ${weatherData.cloudCover}%), UV enhanced flashers will reflect more light and create more flash. "
            LightCondition.OVERCAST -> 
                explanation += "Under overcast conditions (cloud cover: ${weatherData.cloudCover}%), glow or chrome flashers provide better visibility. "
            LightCondition.LOW_LIGHT -> 
                explanation += "In low light conditions, glow flashers will be most visible to fish. "
        }
        
        // Tide explanation
        when (tideData.type) {
            TideType.HIGH, TideType.RISING -> 
                explanation += "During ${tideData.type.name.lowercase()} tide (height: ${decimalFormat.format(tideData.height)} meters), larger flashers create more attraction in deeper water. "
            TideType.LOW, TideType.FALLING -> 
                explanation += "During ${tideData.type.name.lowercase()} tide (height: ${decimalFormat.format(tideData.height)} meters), smaller flashers with less drag work better in shallower water. "
        }
        
        return explanation
    }
    
    private fun generateLureDetailedExplanation(weatherData: WeatherData, tideData: TideData): String {
        var explanation = "\n\nLures mimic prey fish and attract salmon through their appearance and action. "
        
        // Water clarity explanation
        val waterClarity = WaterClarity.fromVisibility(weatherData.visibility)
        when (waterClarity) {
            WaterClarity.CLEAR -> 
                explanation += "In clear water conditions (visibility: ${decimalFormat.format(weatherData.visibility)} meters), smaller, more natural colored lures that closely resemble actual prey fish are most effective. "
            WaterClarity.MEDIUM -> 
                explanation += "In medium water clarity (visibility: ${decimalFormat.format(weatherData.visibility)} meters), medium-sized lures with some flash will attract fish without spooking them. "
            WaterClarity.MURKY -> 
                explanation += "In murky water conditions (visibility: ${decimalFormat.format(weatherData.visibility)} meters), larger, brighter lures create more vibration and visibility to help salmon find them. "
        }
        
        // Light conditions explanation
        val lightCondition = LightCondition.fromWeatherData(weatherData)
        when (lightCondition) {
            LightCondition.BRIGHT -> 
                explanation += "With bright light conditions (cloud cover: ${weatherData.cloudCover}%), silver and blue colors reflect more light and create attractive flashes. "
            LightCondition.OVERCAST -> 
                explanation += "Under overcast conditions (cloud cover: ${weatherData.cloudCover}%), green and chartreuse colors provide good visibility and contrast. "
            LightCondition.LOW_LIGHT -> 
                explanation += "In low light conditions, glow or UV enhanced lures will be most visible to fish. "
        }
        
        // Weather condition explanation
        val weatherCondition = WeatherCondition.fromWeatherData(weatherData)
        when (weatherCondition) {
            WeatherCondition.CALM -> 
                explanation += "In calm conditions (wind speed: ${decimalFormat.format(weatherData.windSpeed)} mph), subtle action lures work well as fish can detect minor movements. "
            WeatherCondition.WINDY -> 
                explanation += "In windy conditions (wind speed: ${decimalFormat.format(weatherData.windSpeed)} mph), lures with more action help attract attention in choppy water. "
            WeatherCondition.RAINY -> 
                explanation += "During rainy conditions (precipitation: ${decimalFormat.format(weatherData.precipitation)} mm), darker lures create better silhouettes against the surface. "
        }
        
        return explanation
    }
    
    private fun generateLeaderDetailedExplanation(weatherData: WeatherData, tideData: TideData): String {
        var explanation = "\n\nLeaders connect your flasher to your lure and affect how your lure moves in the water. "
        
        // Water clarity explanation
        val waterClarity = WaterClarity.fromVisibility(weatherData.visibility)
        when (waterClarity) {
            WaterClarity.CLEAR -> 
                explanation += "In clear water conditions (visibility: ${decimalFormat.format(weatherData.visibility)} meters), fluorocarbon leaders are nearly invisible to fish, making them less likely to spook. "
            WaterClarity.MEDIUM -> 
                explanation += "In medium water clarity (visibility: ${decimalFormat.format(weatherData.visibility)} meters), standard monofilament leaders provide a good balance of invisibility and strength. "
            WaterClarity.MURKY -> 
                explanation += "In murky water conditions (visibility: ${decimalFormat.format(weatherData.visibility)} meters), leader visibility is less important than strength and durability. "
        }
        
        // Tide and weather explanation
        if (tideData.type == TideType.HIGH || tideData.type == TideType.RISING) {
            explanation += "During ${tideData.type.name.lowercase()} tide (height: ${decimalFormat.format(tideData.height)} meters), longer leaders (36-42 inches) allow for more natural presentation and movement of your lure. "
        } else {
            explanation += "During ${tideData.type.name.lowercase()} tide (height: ${decimalFormat.format(tideData.height)} meters), shorter leaders (24-30 inches) help maintain control in shallower water. "
        }
        
        val weatherCondition = WeatherCondition.fromWeatherData(weatherData)
        if (weatherCondition == WeatherCondition.WINDY || weatherCondition == WeatherCondition.RAINY) {
            explanation += "In rough conditions (wind speed: ${decimalFormat.format(weatherData.windSpeed)} mph, precipitation: ${decimalFormat.format(weatherData.precipitation)} mm), heavier leaders (40-60 lb) provide better durability and control. "
        } else {
            explanation += "In calm conditions (wind speed: ${decimalFormat.format(weatherData.windSpeed)} mph), lighter leaders (20-30 lb) allow for more natural lure action. "
        }
        
        return explanation
    }
}