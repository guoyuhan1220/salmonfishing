package com.example.salmontrollingassistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.salmontrollingassistant.domain.model.Location

@Entity(tableName = "saved_locations")
data class LocationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String?
) {
    fun toDomainModel(): Location {
        return Location(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            isSaved = true,
            notes = notes
        )
    }
    
    companion object {
        fun fromDomainModel(location: Location): LocationEntity {
            return LocationEntity(
                id = location.id,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                notes = location.notes
            )
        }
    }
}