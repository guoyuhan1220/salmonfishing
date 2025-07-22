package com.example.salmontrollingassistant.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocationEntity::class, WeatherEntity::class, TideEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun weatherDao(): WeatherDao
    abstract fun tideDao(): TideDao
}