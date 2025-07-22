package com.example.salmontrollingassistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date

@Dao
interface TideDao {
    @Query("SELECT * FROM tides WHERE locationId = :locationId AND isForecast = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getCurrentTide(locationId: String): TideEntity?
    
    @Query("SELECT * FROM tides WHERE locationId = :locationId AND isForecast = 1 ORDER BY timestamp ASC")
    suspend fun getTideForecast(locationId: String): List<TideEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentTide(tideEntity: TideEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTideForecast(tideEntities: List<TideEntity>)
    
    @Query("DELETE FROM tides WHERE locationId = :locationId AND isForecast = 0")
    suspend fun deleteCurrentTide(locationId: String)
    
    @Query("DELETE FROM tides WHERE locationId = :locationId AND isForecast = 1")
    suspend fun deleteTideForecast(locationId: String)
    
    @Query("DELETE FROM tides")
    suspend fun clearAllTideData()
}