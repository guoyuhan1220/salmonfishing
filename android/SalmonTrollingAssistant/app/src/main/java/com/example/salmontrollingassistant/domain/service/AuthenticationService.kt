package com.example.salmontrollingassistant.domain.service

import com.example.salmontrollingassistant.domain.model.AuthCredentials
import com.example.salmontrollingassistant.domain.model.AuthResult
import com.example.salmontrollingassistant.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthenticationService {
    /**
     * Create a new user account with email and password
     */
    suspend fun createAccount(credentials: AuthCredentials, name: String?): AuthResult
    
    /**
     * Sign in with existing credentials
     */
    suspend fun signIn(credentials: AuthCredentials): AuthResult
    
    /**
     * Sign out the current user
     */
    suspend fun signOut(): Boolean
    
    /**
     * Create or get an anonymous user account
     */
    suspend fun signInAnonymously(): AuthResult
    
    /**
     * Convert an anonymous account to a permanent account
     */
    suspend fun convertAnonymousAccount(credentials: AuthCredentials, name: String?): AuthResult
    
    /**
     * Get the current user profile
     */
    fun getCurrentUser(): Flow<UserProfile?>
    
    /**
     * Check if the user is signed in
     */
    fun isSignedIn(): Flow<Boolean>
    
    /**
     * Check if the current user is anonymous
     */
    fun isAnonymousUser(): Flow<Boolean>
}