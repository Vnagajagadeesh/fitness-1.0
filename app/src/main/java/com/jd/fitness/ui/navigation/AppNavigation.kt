package com.jd.fitness.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jd.fitness.data.backup.BackupManager
import com.jd.fitness.data.backup.LegacyMigrationManager
import com.jd.fitness.data.health.HealthConnectManager
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.sync.SupabaseManager
import com.jd.fitness.ui.screens.analytics.AnalyticsScreen
import com.jd.fitness.ui.screens.analytics.AnalyticsViewModel
import com.jd.fitness.ui.screens.calendar.CalendarScreen
import com.jd.fitness.ui.screens.calendar.CalendarViewModel
import com.jd.fitness.ui.screens.challenge.ChallengeScreen
import com.jd.fitness.ui.screens.challenge.ChallengeViewModel
import com.jd.fitness.ui.screens.dashboard.DashboardScreen
import com.jd.fitness.ui.screens.dashboard.DashboardViewModel
import com.jd.fitness.ui.screens.fasting.FastingScreen
import com.jd.fitness.ui.screens.fasting.FastingViewModel
import com.jd.fitness.ui.screens.habits.HabitListScreen
import com.jd.fitness.ui.screens.habits.HabitViewModel
import com.jd.fitness.ui.screens.settings.SettingsScreen
import com.jd.fitness.ui.screens.settings.SettingsViewModel
import com.jd.fitness.ui.screens.skincare.SkincareScreen
import com.jd.fitness.ui.screens.skincare.SkincareViewModel
import com.jd.fitness.ui.screens.steps.StepTrackingScreen
import com.jd.fitness.ui.screens.steps.StepViewModel
import com.jd.fitness.ui.screens.weight.WeightScreen
import com.jd.fitness.ui.screens.weight.WeightViewModel
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextWhite

@Composable
fun AppNavigation(
    repository: FitnessRepository,
    preferencesRepository: UserPreferencesRepository,
    healthConnectManager: HealthConnectManager,
    supabaseManager: SupabaseManager,
    backupManager: BackupManager,
    legacyMigrationManager: LegacyMigrationManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel instances
    val dashboardViewModel = rememberViewModel { DashboardViewModel(repository, preferencesRepository, healthConnectManager) }
    val challengeViewModel = rememberViewModel { ChallengeViewModel(repository) }
    val habitViewModel = rememberViewModel { HabitViewModel(repository) }
    val weightViewModel = rememberViewModel { WeightViewModel(repository) }
    val skincareViewModel = rememberViewModel { SkincareViewModel(repository) }
    val fastingViewModel = rememberViewModel { FastingViewModel(repository, preferencesRepository) }
    val stepViewModel = rememberViewModel { StepViewModel(repository, healthConnectManager, preferencesRepository) }
    val calendarViewModel = rememberViewModel { CalendarViewModel(repository) }
    val analyticsViewModel = rememberViewModel { AnalyticsViewModel(repository) }
    val settingsViewModel = rememberViewModel { SettingsViewModel(repository, preferencesRepository, supabaseManager, backupManager, legacyMigrationManager) }

    Scaffold(
        bottomBar = {
            // Futuristic Holographic Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(DarkNavyCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    BottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) NeonElectricBlue else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    color = if (isSelected) NeonElectricBlue else TextMuted
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x3300E5FF)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = DeepBlackNavy
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Habits.route) {
                HabitListScreen(
                    viewModel = habitViewModel,
                    onNavigateToSkincare = { navController.navigate(Screen.Skincare.route) }
                )
            }
            composable(Screen.Steps.route) {
                StepTrackingScreen(
                    viewModel = stepViewModel,
                    healthConnectManager = healthConnectManager
                )
            }
            composable(Screen.Weight.route) {
                WeightScreen(viewModel = weightViewModel)
            }
            composable(Screen.Skincare.route) {
                SkincareScreen(viewModel = skincareViewModel)
            }
            composable(Screen.Fasting.route) {
                FastingScreen(viewModel = fastingViewModel)
            }
            composable(Screen.Challenge.route) {
                ChallengeScreen(
                    viewModel = challengeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel = calendarViewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = analyticsViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

@Composable
inline fun <reified T : androidx.lifecycle.ViewModel> rememberViewModel(
    crossinline creator: () -> T
): T {
    return androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <M : androidx.lifecycle.ViewModel> create(modelClass: Class<M>): M {
                @Suppress("UNCHECKED_CAST")
                return creator() as M
            }
        }
    )
}

