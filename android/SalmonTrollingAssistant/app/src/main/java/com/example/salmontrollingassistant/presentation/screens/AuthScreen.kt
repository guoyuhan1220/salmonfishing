package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.salmontrollingassistant.presentation.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            onAuthSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUp) "Create Account" else "Sign In",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        when (val state = authState) {
            is AuthViewModel.AuthState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            is AuthViewModel.AuthState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }
            else -> {}
        }
        
        Button(
            onClick = {
                if (isSignUp) {
                    viewModel.createAccount(email, password, name.takeIf { it.isNotBlank() })
                } else {
                    viewModel.signIn(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthViewModel.AuthState.Loading
        ) {
            Text(text = if (isSignUp) "Create Account" else "Sign In")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = { isSignUp = !isSignUp },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up"
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = { viewModel.signInAnonymously() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = authState !is AuthViewModel.AuthState.Loading
        ) {
            Text(text = "Continue as Guest")
        }
    }
}