package com.example.salmontrollingassistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(entity: WeatherEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(entities: List<WeatherEntity>)
    
    @Query("SELECT * FROM weather WHERE locationId = :locationId AND isForecast = 0 LIMIT 1")
    suspend fun getCurrentWeather(locationId: String): WeatherEntity?
    
    @Query("SELECT * FROM weather WHERE locationId = :locationId AND isForecast = 1 ORDER BY timestamp ASC")
    suspend fun getForecast(locationId: String): List<WeatherEntity>
    
    @Query("SELECT timestamp FROM weather WHERE locationId = :locationId AND isForecast = 0 LIMIT 1")
    suspend fun getCurrentWeatherTimestamp(locationId: String): Long?
    
    @Query("SELECT MAX(timestamp) FROM weather WHERE locationId = :locationId AND isForecast = 1")
    suspend fun getForecastTimestamp(locationId: String): Long?
    
    @Query("DELETE FROM weather WHERE locationId = :locationId AND isForecast = 0")
    suspend fun deleteCurrentWeather(locationId: String)
    
    @Query("DELETE FROM weather WHERE locationId = :locationId AND isForecast = 1")
    suspend fun deleteForecast(locationId: String)
    
    @Query("DELETE FROM weather")
    suspend fun clearAllWeatherData()
}