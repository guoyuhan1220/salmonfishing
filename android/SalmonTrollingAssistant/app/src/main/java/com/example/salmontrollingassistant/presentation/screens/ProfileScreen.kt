package com.example.salmontrollingassistant.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.salmontrollingassistant.domain.model.CatchData
import com.example.salmontrollingassistant.domain.model.ExperienceLevel
import com.example.salmontrollingassistant.domain.model.FishSpecies
import com.example.salmontrollingassistant.presentation.components.OfflineIndicator
import com.example.salmontrollingassistant.presentation.viewmodels.AuthViewModel
import com.example.salmontrollingassistant.presentation.viewmodels.CatchAnalyticsViewModel
import com.example.salmontrollingassistant.presentation.viewmodels.UserPreferencesViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    preferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
    catchViewModel: CatchAnalyticsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userPreferences by preferencesViewModel.userPreferences.collectAsState()
    val catchHistory by catchViewModel.catchHistory.collectAsState(initial = emptyList())
    
    LaunchedEffect(Unit) {
        if (!isAuthenticated) {
            authViewModel.checkAuthStatus()
        }
        preferencesViewModel.loadUserPreferences()
        catchViewModel.loadCatchHistory()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            // Offline indicator
            OfflineIndicator()
            
            // Profile header
            ProfileHeader(
                isAuthenticated = isAuthenticated,
                userName = currentUser?.name,
                userEmail = currentUser?.email,
                experienceLevel = userPreferences?.experienceLevel,
                onSignInClick = { navController.navigate("auth") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preferences section
            PreferencesSection(
                userPreferences = userPreferences,
                onEditPreferencesClick = { navController.navigate("user_preferences") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Catch history section
            CatchHistorySection(
                catchHistory = catchHistory,
                onViewAllClick = { navController.navigate("catch_analytics") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings section
            SettingsSection(
                onDataManagementClick = { navController.navigate("data_management") },
                onAppearanceClick = { navController.navigate("app_settings") },
                onNotificationsClick = { navController.navigate("app_settings") },
                onAboutClick = { navController.navigate("app_settings") }
            )
            
            // Sign out button
            if (isAuthenticated) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { authViewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Sign Out",
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
    
    // Edit profile dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            userName = currentUser?.name ?: "",
            experienceLevel = userPreferences?.experienceLevel ?: ExperienceLevel.BEGINNER,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, level ->
                preferencesViewModel.updateUserName(name)
                preferencesViewModel.updateExperienceLevel(level)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun ProfileHeader(
    isAuthenticated: Boolean,
    userName: String?,
    userEmail: String?,
    experienceLevel: ExperienceLevel?,
    onSignInClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        if (isAuthenticated) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User name
                Text(
                    text = userName ?: "Angler",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
                
                // User email
                Text(
                    text = userEmail ?: "",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Experience level
                Surface(
                    color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Experience: ${experienceLevel?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Beginner"}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Sign In",
                    tint = Color.Gray,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sign in to access your profile",
                    style = MaterialTheme.typography.h6
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In")
                }
            }
        }
    }
}

@Composable
fun PreferencesSection(
    userPreferences: com.example.salmontrollingassistant.domain.model.UserPreferences?,
    onEditPreferencesClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Fishing Preferences",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column {
                // Preferred species
                PreferenceRow(
                    title = "Preferred Species",
                    value = formatSpeciesList(userPreferences?.preferredSpecies ?: emptyList())
                )
                
                Divider()
                
                // Preferred equipment
                PreferenceRow(
                    title = "Preferred Equipment",
                    value = "${userPreferences?.preferredEquipment?.size ?: 0} items"
                )
                
                Divider()
                
                // Notification settings
                PreferenceRow(
                    title = "Notifications",
                    value = if (userPreferences?.notificationSettings?.enableOptimalConditionAlerts == true) "Enabled" else "Disabled"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onEditPreferencesClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Edit Preferences",
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun PreferenceRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body1
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

@Composable
fun CatchHistorySection(
    catchHistory: List<CatchData>,
    onViewAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Catch History",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (catchHistory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Text(
                    text = "No catch history recorded yet",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(catchHistory.take(5)) { catchData ->
                    CatchHistoryCard(catchData = catchData)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onViewAllClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "View All Catches",
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CatchHistoryCard(catchData: CatchData) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(150.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Species and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = catchData.species.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatDate(catchData.timestamp),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Size and weight
            catchData.size?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Size:",
                        style = MaterialTheme.typography.caption
                    )
                    
                    Text(
                        text = "${String.format("%.1f", it)} in",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            
            catchData.weight?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Weight:",
                        style = MaterialTheme.typography.caption
                    )
                    
                    Text(
                        text = "${String.format("%.1f", it)} lbs",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Location
            Text(
                text = catchData.location.name,
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SettingsSection(
    onDataManagementClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column {
                SettingsRow(
                    title = "Data Management",
                    icon = Icons.Default.Storage,
                    onClick = onDataManagementClick
                )
                
                Divider()
                
                SettingsRow(
                    title = "Appearance",
                    icon = Icons.Default.Palette,
                    onClick = onAppearanceClick
                )
                
                Divider()
                
                SettingsRow(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    onClick = onNotificationsClick
                )
                
                Divider()
                
                SettingsRow(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = onAboutClick
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun EditProfileDialog(
    userName: String,
    experienceLevel: ExperienceLevel,
    onDismiss: () -> Unit,
    onSave: (name: String, level: ExperienceLevel) -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var selectedLevel by remember { mutableStateOf(experienceLevel) }
    
    val experienceLevels = remember { ExperienceLevel.values() }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.h6
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Experience Level",
                    style = MaterialTheme.typography.subtitle1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                experienceLevels.forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLevel = level }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = level.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(onClick = { onSave(name, selectedLevel) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun formatSpeciesList(species: List<FishSpecies>): String {
    return if (species.isEmpty()) {
        "None"
    } else {
        species.joinToString(", ") { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp * 1000) // Convert seconds to milliseconds
    val formatter = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
    return formatter.format(date)
}