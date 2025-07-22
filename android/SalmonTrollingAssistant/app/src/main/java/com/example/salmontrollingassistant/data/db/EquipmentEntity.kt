package com.example.salmontrollingassistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.salmontrollingassistant.domain.model.EquipmentItem
import com.example.salmontrollingassistant.domain.model.EquipmentType
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.domain.model.TideType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import java.util.UUID

@Entity(tableName = "equipment_items")
@TypeConverters(EquipmentConverters::class)
data class EquipmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val type: String, // EquipmentType as string
    val imageUrl: String?,
    val specifications: Map<String, String>,
    val targetSpecies: List<String>?, // FishSpecies as strings
    val waterClarityConditions: List<String>?,
    val lightConditions: List<String>?,
    val weatherConditions: List<String>?,
    val tideConditions: List<String>? // TideType as strings
) {
    // Convert to domain model
    fun toDomainModel(): EquipmentItem {
        return EquipmentItem(
            id = id,
            name = name,
            description = description,
            type = EquipmentType.valueOf(type),
            imageUrl = imageUrl,
            specifications = specifications,
            targetSpecies = targetSpecies?.map { FishSpecies.valueOf(it) },
            waterClarityConditions = waterClarityConditions,
            lightConditions = lightConditions,
            weatherConditions = weatherConditions,
            tideConditions = tideConditions?.map { TideType.valueOf(it) }
        )
    }
    
    companion object {
        // Convert from domain model
        fun fromDomainModel(equipmentItem: EquipmentItem): EquipmentEntity {
            return EquipmentEntity(
                id = equipmentItem.id,
                name = equipmentItem.name,
                description = equipmentItem.description,
                type = equipmentItem.type.name,
                imageUrl = equipmentItem.imageUrl,
                specifications = equipmentItem.specifications,
                targetSpecies = equipmentItem.targetSpecies?.map { it.name },
                waterClarityConditions = equipmentItem.waterClarityConditions,
                lightConditions = equipmentItem.lightConditions,
                weatherConditions = equipmentItem.weatherConditions,
                tideConditions = equipmentItem.tideConditions?.map { it.name }
            )
        }
    }
}

class EquipmentConverters {
    private val moshi = Moshi.Builder().build()
    
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val mapType: Type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        val adapter: JsonAdapter<Map<String, String>> = moshi.adapter(mapType)
        return adapter.fromJson(value)
    }
    
    @TypeConverter
    fun toStringMap(map: Map<String, String>?): String? {
        if (map == null) return null
        val mapType: Type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        val adapter: JsonAdapter<Map<String, String>> = moshi.adapter(mapType)
        return adapter.toJson(map)
    }
    
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType: Type = Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
        val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)
        return adapter.fromJson(value)
    }
    
    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list == null) return null
        val listType: Type = Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
        val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)
        return adapter.toJson(list)
    }
}