package com.example.salmontrollingassistant.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.salmontrollingassistant.presentation.screens.HomeScreen
import com.example.salmontrollingassistant.presentation.screens.LocationsScreen
import com.example.salmontrollingassistant.presentation.screens.UserPreferencesScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Recommendations : Screen("recommendations", "Recommendations", Icons.Filled.List)
    object WeatherTide : Screen("weather_tide", "Weather & Tide", Icons.Filled.WbSunny)
    object Locations : Screen("locations", "Locations", Icons.Filled.LocationOn)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

@Composable
fun AppNavHost(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Recommendations,
        Screen.WeatherTide,
        Screen.Locations,
        Screen.Profile
    )
    
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Recommendations.route) {
                RecommendationsScreen()
            }
            composable(Screen.WeatherTide.route) {
                WeatherTideScreen()
            }
            composable(Screen.Locations.route) {
                LocationsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}