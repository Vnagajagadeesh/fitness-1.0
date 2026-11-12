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
import com.jd.fitness.ui.screens.coach.CoachScreen
import com.jd.fitness.ui.screens.coach.CoachViewModel
import com.jd.fitness.ui.screens.dashboard.DashboardScreen
import com.jd.fitness.ui.screens.dashboard.DashboardViewModel
import com.jd.fitness.ui.screens.fasting.FastingScreen
import com.jd.fitness.ui.screens.fasting.FastingViewModel
import com.jd.fitness.ui.screens.habits.HabitListScreen
import com.jd.fitness.ui.screens.habits.HabitViewModel
import com.jd.fitness.ui.screens.journal.JournalScreen
import com.jd.fitness.ui.screens.journal.JournalViewModel
import com.jd.fitness.ui.screens.milestones.MilestonesScreen
import com.jd.fitness.ui.screens.milestones.MilestonesViewModel
import com.jd.fitness.ui.screens.motivation.MotivationScreen
import com.jd.fitness.ui.screens.motivation.MotivationViewModel
import com.jd.fitness.ui.screens.planner.PlannerScreen
import com.jd.fitness.ui.screens.planner.PlannerViewModel
import com.jd.fitness.ui.screens.recovery.RecoveryScreen
import com.jd.fitness.ui.screens.recovery.RecoveryViewModel
import com.jd.fitness.ui.screens.reports.WeeklyReportScreen
import com.jd.fitness.ui.screens.reports.WeeklyReportViewModel
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
import com.jd.fitness.ui.theme.TextMuted

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

    // FITNESS 1.2 ViewModels
    val recoveryViewModel = rememberViewModel { RecoveryViewModel(repository) }
    val plannerViewModel = rememberViewModel { PlannerViewModel(repository, preferencesRepository) }
    val milestonesViewModel = rememberViewModel { MilestonesViewModel(repository) }
    val weeklyReportViewModel = rememberViewModel { WeeklyReportViewModel(repository) }
    val journalViewModel = rememberViewModel { JournalViewModel(repository) }
    val motivationViewModel = rememberViewModel { MotivationViewModel(repository) }
    val coachViewModel = rememberViewModel { CoachViewModel(repository) }

    val goHome: () -> Unit = {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Dashboard.route) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    val goBack: () -> Unit = {
        if (!navController.popBackStack()) {
            goHome()
        }
    }

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
                                if (screen.route == Screen.Dashboard.route) {
                                    goHome()
                                } else {
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
                    onNavigateToSkincare = { navController.navigate(Screen.Skincare.route) },
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Steps.route) {
                StepTrackingScreen(
                    viewModel = stepViewModel,
                    healthConnectManager = healthConnectManager,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Weight.route) {
                WeightScreen(
                    viewModel = weightViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Skincare.route) {
                SkincareScreen(
                    viewModel = skincareViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Fasting.route) {
                FastingScreen(
                    viewModel = fastingViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Challenge.route) {
                ChallengeScreen(
                    viewModel = challengeViewModel,
                    onBack = goBack
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }

            // --- NEW FITNESS 1.2 MODULE DESTINATIONS ---
            composable(Screen.Recovery.route) {
                RecoveryScreen(
                    viewModel = recoveryViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Planner.route) {
                PlannerScreen(
                    viewModel = plannerViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Achievements.route) {
                MilestonesScreen(
                    viewModel = milestonesViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.WeeklyReports.route) {
                WeeklyReportScreen(
                    viewModel = weeklyReportViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Journal.route) {
                JournalScreen(
                    viewModel = journalViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Motivation.route) {
                MotivationScreen(
                    viewModel = motivationViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
            }
            composable(Screen.Coach.route) {
                CoachScreen(
                    viewModel = coachViewModel,
                    onBack = goBack,
                    onHome = goHome
                )
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
