package com.jd.fitness.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.RocketLaunch)
    object Habits : Screen("habits", "Habits", Icons.Default.CheckCircleOutline)
    object Fasting : Screen("fasting", "Fasting", Icons.Default.Timer)
    object Steps : Screen("steps", "Steps", Icons.AutoMirrored.Filled.DirectionsWalk)
    object Weight : Screen("weight", "Weight", Icons.Default.MonitorWeight)
    object Skincare : Screen("skincare", "Skincare", Icons.Default.Face)
    object Challenge : Screen("challenge", "90-Day", Icons.Default.WorkspacePremium)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Insights)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Habits,
    Screen.Steps,
    Screen.Weight,
    Screen.Settings
)

