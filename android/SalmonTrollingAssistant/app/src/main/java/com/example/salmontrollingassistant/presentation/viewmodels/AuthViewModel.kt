package com.example.salmontrollingassistant.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salmontrollingassistant.domain.model.AuthCredentials
import com.example.salmontrollingassistant.domain.model.AuthResult
import com.example.salmontrollingassistant.domain.model.UserProfile
import com.example.salmontrollingassistant.domain.service.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthenticationService
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()
    
    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
    
    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()
    
    init {
        viewModelScope.launch {
            authService.getCurrentUser().collectLatest {
                _currentUser.value = it
            }
        }
        
        viewModelScope.launch {
            authService.isSignedIn().collectLatest {
                _isSignedIn.value = it
            }
        }
        
        viewModelScope.launch {
            authService.isAnonymousUser().collectLatest {
                _isAnonymous.value = it
            }
        }
    }
    
    fun createAccount(email: String, password: String, name: String?) {
        if (!validateCredentials(email, password)) {
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authService.createAccount(
                AuthCredentials(email, password),
                name
            )
            _authState.value = if (result.success) {
                AuthState.Success(result)
            } else {
                AuthState.Error(result.error ?: "Unknown error")
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        if (!validateCredentials(email, password)) {
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authService.signIn(AuthCredentials(email, password))
            _authState.value = if (result.success) {
                AuthState.Success(result)
            } else {
                AuthState.Error(result.error ?: "Unknown error")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authService.signOut()
            _authState.value = if (success) {
                AuthState.Initial
            } else {
                AuthState.Error("Failed to sign out")
            }
        }
    }
    
    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authService.signInAnonymously()
            _authState.value = if (result.success) {
                AuthState.Success(result)
            } else {
                AuthState.Error(result.error ?: "Unknown error")
            }
        }
    }
    
    fun convertAnonymousAccount(email: String, password: String, name: String?) {
        if (!validateCredentials(email, password)) {
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authService.convertAnonymousAccount(
                AuthCredentials(email, password),
                name
            )
            _authState.value = if (result.success) {
                AuthState.Success(result)
            } else {
                AuthState.Error(result.error ?: "Unknown error")
            }
        }
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }
    
    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Invalid email address")
            return false
        }
        
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return false
        }
        
        return true
    }
    
    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        data class Success(val result: AuthResult) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}