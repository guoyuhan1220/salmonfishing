package com.example.salmontrollingassistant.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.salmontrollingassistant.domain.model.CatchData
import com.example.salmontrollingassistant.domain.service.AuthenticationService
import com.example.salmontrollingassistant.domain.service.CatchLoggingService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.catchDataStore: DataStore<Preferences> by preferencesDataStore(name = "catch_data")

@Singleton
class CatchLoggingServiceImpl @Inject constructor(
    private val context: Context,
    private val authService: AuthenticationService,
    private val moshi: Moshi
) : CatchLoggingService {
    
    private val catchListAdapter = moshi.adapter<List<CatchData>>(
        Types.newParameterizedType(List::class.java, CatchData::class.java)
    )
    
    override suspend fun logCatch(catchData: CatchData): Result<Boolean> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not logged in"))
        val key = stringPreferencesKey("${userId}_catches")
        
        return try {
            context.catchDataStore.edit { preferences ->
                val currentCatches = preferences[key]?.let {
                    catchListAdapter.fromJson(it) ?: emptyList()
                } ?: emptyList()
                
                val updatedCatches = currentCatches + catchData
                preferences[key] = catchListAdapter.toJson(updatedCatches)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getCatchHistory(): Flow<List<CatchData>> {
        return context.catchDataStore.data.map { preferences ->
            val userId = getCurrentUserId() ?: return@map emptyList<CatchData>()
            val key = stringPreferencesKey("${userId}_catches")
            val json = preferences[key] ?: return@map emptyList<CatchData>()
            catchListAdapter.fromJson(json) ?: emptyList()
        }
    }
    
    override fun getCatchHistoryByLocation(locationId: String): Flow<List<CatchData>> {
        return getCatchHistory().map { catches ->
            catches.filter { it.locationId == locationId }
        }
    }
    
    override fun getCatchHistoryBySpecies(species: String): Flow<List<CatchData>> {
        return getCatchHistory().map { catches ->
            catches.filter { it.species.name == species }
        }
    }
    
    override suspend fun getCatchById(catchId: String): Result<CatchData> {
        val catches = getCatchHistory().first()
        val catch = catches.find { it.id == catchId }
        
        return if (catch != null) {
            Result.success(catch)
        } else {
            Result.failure(NoSuchElementException("Catch not found"))
        }
    }
    
    override suspend fun updateCatch(catchData: CatchData): Result<Boolean> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not logged in"))
        val key = stringPreferencesKey("${userId}_catches")
        
        return try {
            context.catchDataStore.edit { preferences ->
                val currentCatches = preferences[key]?.let {
                    catchListAdapter.fromJson(it) ?: emptyList()
                } ?: emptyList()
                
                val updatedCatches = currentCatches.map {
                    if (it.id == catchData.id) catchData else it
                }
                
                preferences[key] = catchListAdapter.toJson(updatedCatches)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCatch(catchId: String): Result<Boolean> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not logged in"))
        val key = stringPreferencesKey("${userId}_catches")
        
        return try {
            context.catchDataStore.edit { preferences ->
                val currentCatches = preferences[key]?.let {
                    catchListAdapter.fromJson(it) ?: emptyList()
                } ?: emptyList()
                
                val updatedCatches = currentCatches.filter { it.id != catchId }
                preferences[key] = catchListAdapter.toJson(updatedCatches)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addPhotoCatch(catchId: String, photoUri: String): Result<Boolean> {
        val catch = getCatchById(catchId).getOrNull() ?: return Result.failure(NoSuchElementException("Catch not found"))
        
        val updatedCatch = catch.copy(
            photoUrls = catch.photoUrls + photoUri
        )
        
        return updateCatch(updatedCatch)
    }
    
    override suspend fun removePhotoCatch(catchId: String, photoUri: String): Result<Boolean> {
        val catch = getCatchById(catchId).getOrNull() ?: return Result.failure(NoSuchElementException("Catch not found"))
        
        val updatedCatch = catch.copy(
            photoUrls = catch.photoUrls.filter { it != photoUri }
        )
        
        return updateCatch(updatedCatch)
    }
    
    private suspend fun getCurrentUserId(): String? {
        return authService.getCurrentUser().first()?.id
    }
}