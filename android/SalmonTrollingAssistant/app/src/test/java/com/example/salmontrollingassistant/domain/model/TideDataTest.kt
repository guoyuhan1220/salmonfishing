package com.example.salmontrollingassistant.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

class TideDataTest {

    @Test
    fun `TideData should be created with correct values`() {
        // Given
        val timestamp = Date()
        val height = 2.5
        val type = TideType.RISING
        
        // When
        val tideData = TideData(
            timestamp = timestamp,
            height = height,
            type = type
        )
        
        // Then
        assertThat(tideData.timestamp).isEqualTo(timestamp)
        assertThat(tideData.height).isEqualTo(height)
        assertThat(tideData.type).isEqualTo(type)
        assertThat(tideData.nextHighTide).isNull()
        assertThat(tideData.nextLowTide).isNull()
    }
    
    @Test
    fun `TideData should handle next tide events correctly`() {
        // Given
        val timestamp = Date()
        val height = 2.5
        val type = TideType.RISING
        
        val nextHighTideTime = Date(timestamp.time + 3600000) // 1 hour later
        val nextHighTide = TideEvent(
            timestamp = nextHighTideTime,
            height = 4.0
        )
        
        val nextLowTideTime = Date(timestamp.time + 7200000) // 2 hours later
        val nextLowTide = TideEvent(
            timestamp = nextLowTideTime,
            height = 1.0
        )
        
        // When
        val tideData = TideData(
            timestamp = timestamp,
            height = height,
            type = type,
            nextHighTide = nextHighTide,
            nextLowTide = nextLowTide
        )
        
        // Then
        assertThat(tideData.nextHighTide).isEqualTo(nextHighTide)
        assertThat(tideData.nextLowTide).isEqualTo(nextLowTide)
        assertThat(tideData.nextHighTide?.timestamp).isEqualTo(nextHighTideTime)
        assertThat(tideData.nextHighTide?.height).isEqualTo(4.0)
        assertThat(tideData.nextLowTide?.timestamp).isEqualTo(nextLowTideTime)
        assertThat(tideData.nextLowTide?.height).isEqualTo(1.0)
    }
    
    @Test
    fun `TideEvent should be created with correct values`() {
        // Given
        val timestamp = Date()
        val height = 3.5
        
        // When
        val tideEvent = TideEvent(
            timestamp = timestamp,
            height = height
        )
        
        // Then
        assertThat(tideEvent.timestamp).isEqualTo(timestamp)
        assertThat(tideEvent.height).isEqualTo(height)
    }
    
    @Test
    fun `TideData instances should be comparable`() {
        // Given
        val timestamp = Date()
        val tideData1 = TideData(
            timestamp = timestamp,
            height = 2.5,
            type = TideType.RISING
        )
        
        val tideData2 = TideData(
            timestamp = timestamp,
            height = 2.5,
            type = TideType.RISING
        )
        
        val tideData3 = TideData(
            timestamp = timestamp,
            height = 3.0, // Different height
            type = TideType.RISING
        )
        
        // Then
        assertThat(tideData1).isEqualTo(tideData1) // Same instance
        assertThat(tideData1).isNotEqualTo(tideData3) // Different values
        
        // Different instances with same values should be equal due to data class equality
        assertThat(tideData1).isEqualTo(tideData2)
    }
}