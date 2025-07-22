package com.example.salmontrollingassistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.salmontrollingassistant.domain.model.TideData
import com.example.salmontrollingassistant.domain.model.TideEvent
import com.example.salmontrollingassistant.domain.model.TideType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.util.Date
import java.util.UUID

@Entity(tableName = "tides")
@TypeConverters(TideConverters::class)
data class TideEntity(
    @PrimaryKey
    val id: String,
    val locationId: String,
    val timestamp: Long,
    val height: Double,
    val type: TideType,
    val nextHighTide: TideEvent?,
    val nextLowTide: TideEvent?,
    val isForecast: Boolean,
    val cacheTimestamp: Long = System.currentTimeMillis()
) {
    fun toTideData(): TideData {
        return TideData(
            id = id,
            timestamp = Date(timestamp),
            height = height,
            type = type,
            nextHighTide = nextHighTide,
            nextLowTide = nextLowTide
        )
    }
    
    companion object {
        fun fromTideData(tideData: TideData, locationId: String, isForecast: Boolean): TideEntity {
            return TideEntity(
                id = tideData.id,
                locationId = locationId,
                timestamp = tideData.timestamp.time,
                height = tideData.height,
                type = tideData.type,
                nextHighTide = tideData.nextHighTide,
                nextLowTide = tideData.nextLowTide,
                isForecast = isForecast
            )
        }
    }
}

class TideConverters {
    private val moshi = Moshi.Builder().build()
    private val tideEventAdapter: JsonAdapter<TideEvent> = moshi.adapter(TideEvent::class.java)
    
    @TypeConverter
    fun fromTideType(tideType: TideType): String {
        return tideType.name
    }
    
    @TypeConverter
    fun toTideType(value: String): TideType {
        return TideType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTideEvent(tideEvent: TideEvent?): String? {
        return tideEvent?.let { tideEventAdapter.toJson(it) }
    }
    
    @TypeConverter
    fun toTideEvent(value: String?): TideEvent? {
        return value?.let { tideEventAdapter.fromJson(it) }
    }
}