package com.jd.fitness.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Habits : Screen("habits", "Habits", Icons.Default.CheckCircleOutline)
    object Fasting : Screen("fasting", "Fasting", Icons.Default.Timer)
    object Steps : Screen("steps", "Steps", Icons.AutoMirrored.Filled.DirectionsWalk)
    object Weight : Screen("weight", "Weight", Icons.Default.MonitorWeight)
    object Skincare : Screen("skincare", "Skincare", Icons.Default.Face)
    object Challenge : Screen("challenge", "90-Day", Icons.Default.WorkspacePremium)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Insights)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // New FITNESS 1.2 Modules
    object Recovery : Screen("recovery", "Recovery", Icons.Default.SelfImprovement)
    object Planner : Screen("planner", "Plan", Icons.AutoMirrored.Filled.EventNote)
    object Achievements : Screen("achievements", "Milestones", Icons.Default.MilitaryTech)
    object WeeklyReports : Screen("weekly_reports", "Reports", Icons.Default.Assessment)
    object Journal : Screen("journal", "Journal", Icons.AutoMirrored.Filled.MenuBook)
    object Motivation : Screen("motivation", "Motivation", Icons.Default.FormatQuote)
    object Coach : Screen("coach", "AI Coach", Icons.Default.Psychology)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Habits,
    Screen.Planner,
    Screen.Coach,
    Screen.Settings
)
