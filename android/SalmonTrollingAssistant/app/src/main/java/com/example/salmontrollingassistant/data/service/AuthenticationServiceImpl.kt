package com.example.salmontrollingassistant.data.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.salmontrollingassistant.domain.model.AuthCredentials
import com.example.salmontrollingassistant.domain.model.AuthResult
import com.example.salmontrollingassistant.domain.model.UserProfile
import com.example.salmontrollingassistant.domain.service.AuthenticationService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationServiceImpl @Inject constructor(
    private val context: Context,
    private val moshi: Moshi
) : AuthenticationService {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val userProfileAdapter = moshi.adapter(UserProfile::class.java)
    
    private val currentUserFlow = MutableStateFlow<UserProfile?>(loadCurrentUser())
    
    private fun loadCurrentUser(): UserProfile? {
        val userJson = sharedPreferences.getString(KEY_USER_PROFILE, null)
        return userJson?.let { userProfileAdapter.fromJson(it) }
    }
    
    private fun saveCurrentUser(userProfile: UserProfile?) {
        if (userProfile == null) {
            sharedPreferences.edit().remove(KEY_USER_PROFILE).apply()
        } else {
            val userJson = userProfileAdapter.toJson(userProfile)
            sharedPreferences.edit().putString(KEY_USER_PROFILE, userJson).apply()
        }
        currentUserFlow.value = userProfile
    }
    
    override suspend fun createAccount(credentials: AuthCredentials, name: String?): AuthResult {
        // In a real app, this would make an API call to create an account
        // For now, we'll simulate account creation locally
        
        // Check if email already exists
        if (emailExists(credentials.email)) {
            return AuthResult(
                success = false,
                error = "Email already in use"
            )
        }
        
        val userId = UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString()
        
        // Create and save the user profile
        val userProfile = UserProfile(
            id = userId,
            name = name,
            email = credentials.email,
            isAnonymous = false
        )
        
        saveCurrentUser(userProfile)
        saveCredentials(credentials.email, credentials.password)
        saveAuthToken(token)
        
        return AuthResult(
            success = true,
            userId = userId,
            token = token
        )
    }
    
    override suspend fun signIn(credentials: AuthCredentials): AuthResult {
        // In a real app, this would make an API call to authenticate
        // For now, we'll simulate authentication locally
        
        val savedPassword = getSavedPassword(credentials.email)
        if (savedPassword == null || savedPassword != credentials.password) {
            return AuthResult(
                success = false,
                error = "Invalid email or password"
            )
        }
        
        // Load or create user profile
        val currentUser = loadCurrentUser()
        val userId = currentUser?.id ?: UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString()
        
        val userProfile = currentUser?.copy(
            email = credentials.email,
            isAnonymous = false
        ) ?: UserProfile(
            id = userId,
            email = credentials.email,
            isAnonymous = false
        )
        
        saveCurrentUser(userProfile)
        saveAuthToken(token)
        
        return AuthResult(
            success = true,
            userId = userId,
            token = token
        )
    }
    
    override suspend fun signOut(): Boolean {
        saveAuthToken(null)
        saveCurrentUser(null)
        return true
    }
    
    override suspend fun signInAnonymously(): AuthResult {
        val userId = UUID.randomUUID().toString()
        val token = UUID.randomUUID().toString()
        
        val userProfile = UserProfile(
            id = userId,
            isAnonymous = true
        )
        
        saveCurrentUser(userProfile)
        saveAuthToken(token)
        
        return AuthResult(
            success = true,
            userId = userId,
            token = token
        )
    }
    
    override suspend fun convertAnonymousAccount(credentials: AuthCredentials, name: String?): AuthResult {
        val currentUser = loadCurrentUser() ?: return AuthResult(
            success = false,
            error = "No anonymous user to convert"
        )
        
        if (!currentUser.isAnonymous) {
            return AuthResult(
                success = false,
                error = "Current user is not anonymous"
            )
        }
        
        // Check if email already exists
        if (emailExists(credentials.email)) {
            return AuthResult(
                success = false,
                error = "Email already in use"
            )
        }
        
        val token = UUID.randomUUID().toString()
        
        val updatedUser = currentUser.copy(
            name = name,
            email = credentials.email,
            isAnonymous = false
        )
        
        saveCurrentUser(updatedUser)
        saveCredentials(credentials.email, credentials.password)
        saveAuthToken(token)
        
        return AuthResult(
            success = true,
            userId = currentUser.id,
            token = token
        )
    }
    
    override fun getCurrentUser(): Flow<UserProfile?> {
        return currentUserFlow
    }
    
    override fun isSignedIn(): Flow<Boolean> {
        return currentUserFlow.map { it != null }
    }
    
    override fun isAnonymousUser(): Flow<Boolean> {
        return currentUserFlow.map { it?.isAnonymous ?: false }
    }
    
    // Helper methods for credential management
    private fun emailExists(email: String): Boolean {
        return sharedPreferences.contains("email_$email")
    }
    
    private fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit()
            .putString("email_$email", password)
            .apply()
    }
    
    private fun getSavedPassword(email: String): String? {
        return sharedPreferences.getString("email_$email", null)
    }
    
    private fun saveAuthToken(token: String?) {
        if (token == null) {
            sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
        } else {
            sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
        }
    }
    
    private fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    companion object {
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}