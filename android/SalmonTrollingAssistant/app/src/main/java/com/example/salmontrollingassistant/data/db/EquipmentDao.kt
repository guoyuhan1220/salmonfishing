package com.example.salmontrollingassistant.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.salmontrollingassistant.domain.model.EquipmentType
import com.example.salmontrollingassistant.domain.model.FishSpecies
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment_items")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>
    
    @Query("SELECT * FROM equipment_items WHERE id = :id")
    suspend fun getEquipmentById(id: String): EquipmentEntity?
    
    @Query("SELECT * FROM equipment_items WHERE type = :type")
    fun getEquipmentByType(type: String): Flow<List<EquipmentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEquipment(equipment: List<EquipmentEntity>)
    
    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)
    
    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)
    
    @Query("DELETE FROM equipment_items WHERE id = :id")
    suspend fun deleteEquipmentById(id: String)
    
    @Query("DELETE FROM equipment_items")
    suspend fun deleteAllEquipment()
}