package com.jd.fitness.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.CoachPersonalityState
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.JDLogoBadge
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.navigation.Screen
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.NeonAmber
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonMagenta
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Always ensure fresh data on arrival
    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // --- TOP BRANDING HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "FITNESS 1.2",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "JD EDITION — PERSONAL COMPANION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = state.currentDateFormatted,
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NeonElectricBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    JDLogoBadge(
                        size = 46.dp,
                        initials = state.preferences.userName.take(2).uppercase().ifBlank { "JD" }
                    )
                }
            }
        }

        // --- 1. FITNESS AI COACH INSIGHT HERO CARD ---
        item {
            val coach = state.coachInsight
            val coachColor = when (coach?.personalityState) {
                CoachPersonalityState.HIGH_PERFORMANCE.name -> NeonGreen
                CoachPersonalityState.RECOVERY_NEEDED.name -> NeonAmber
                CoachPersonalityState.STRUGGLING.name -> NeonMagenta
                else -> NeonCyan
            }

            GlassCard(
                borderColor = coachColor.copy(alpha = 0.6f),
                onClick = { onNavigate(Screen.Coach.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(coachColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Coach",
                                tint = coachColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI COACH INSIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = coachColor,
                            letterSpacing = 1.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = coach?.guidanceText ?: "Tap to calibrate today's adaptive discipline coaching.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhite,
                    lineHeight = 18.sp
                )
            }
        }

        // --- 2. 90-DAY CHALLENGE PROGRESS CARD ---
        item {
            val challenge = state.challengeProgress
            GlassCard(
                borderColor = NeonElectricBlue.copy(alpha = 0.6f),
                onClick = { onNavigate(Screen.Challenge.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "90-DAY CHALLENGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Day ${challenge?.currentDay ?: 1} of ${challenge?.totalDays ?: 90}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }
                    HolographicStatBadge(
                        label = "MILESTONE",
                        value = challenge?.currentMilestone ?: "FOUNDATION",
                        accentColor = NeonPurple
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressBar(progress = (challenge?.overallPercentage ?: 0f) / 100f, height = 8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", challenge?.overallPercentage ?: 0f)}% Complete",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonElectricBlue
                    )
                    Text(
                        text = "${challenge?.daysRemaining ?: 90} Days Remaining",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // --- 3. TODAY'S HABITS CHECKLIST CARD ---
        item {
            val daySum = state.daySummary
            val streak = state.streakInfo
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S HABITS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${daySum?.completedHabits ?: 0} of ${daySum?.totalHabits ?: 13} Completed",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HolographicStatBadge(
                            label = "STREAK",
                            value = "${streak.currentStreak}d",
                            accentColor = NeonAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        HolographicStatBadge(
                            label = "SCORE",
                            value = "${(daySum?.percentage ?: 0f).toInt()}%",
                            accentColor = if ((daySum?.percentage ?: 0f) >= 100f) NeonGreen else NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressBar(progress = (daySum?.percentage ?: 0f) / 100f, height = 6.dp)

                Spacer(modifier = Modifier.height(12.dp))
                // Quick toggle checklist items
                state.habits.take(5).forEach { (habit, isChecked) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleHabit(habit.id, !isChecked) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleHabit(habit.id, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonGreen,
                                uncheckedColor = TextSubtle,
                                checkmarkColor = DeepBlackNavy
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = habit.title,
                            fontSize = 13.sp,
                            color = if (isChecked) TextMuted else TextWhite,
                            fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.Habits.route) }
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All 13 Habits",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonElectricBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = NeonElectricBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // --- 4. TODAY'S DAILY PLAN & RECOVERY ROW ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Planner Card
                val completedTasks = state.todayPlannerTasks.count { it.isCompleted }
                val totalTasks = state.todayPlannerTasks.size

                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Planner.route) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = "Plan",
                            tint = NeonElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (totalTasks > 0) "${(completedTasks * 100 / totalTasks)}%" else "0%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonElectricBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedTasks / $totalTasks Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = "My Daily Plan",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Recovery Card
                val recovery = state.todayRecovery
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Recovery.route) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Recovery",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (recovery?.isRestDay == true) "REST" else "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (recovery?.isRestDay == true) NeonGreen else NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (recovery != null) "${recovery.sleepHours}h Sleep" else "Check in",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = recovery?.perceivedEnergy?.replace("_", " ") ?: "Recovery Centre",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // --- 5. STEPS & WEIGHT SNAPSHOT ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Steps Snapshot
                val stepGoal = state.preferences.stepGoal
                val currentSteps = state.todaySteps
                val stepProgress = (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f)

                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Steps.route) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = "Steps",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${(stepProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentSteps >= stepGoal) NeonGreen else NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$currentSteps",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = "of ${stepGoal / 1000}k steps",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Weight Snapshot
                val weight = state.latestWeight
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Weight.route) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = "Weight",
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Latest",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (weight != null) "${String.format(Locale.US, "%.1f", weight.weightKg)} kg" else "-- kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = weight?.date ?: "Tap to log",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // --- 6. FASTING SNAPSHOT ---
        item {
            val activeFasting = state.activeFastingSession
            GlassCard(
                borderColor = if (activeFasting != null) NeonCyan.copy(alpha = 0.5f) else Color(0x33446699),
                onClick = { onNavigate(Screen.Fasting.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (activeFasting != null) Color(0x3300F5D4) else Color(0x2216203B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Fasting",
                                tint = if (activeFasting != null) NeonCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (activeFasting?.isOMAD == true) "OMAD SESSION" else "FASTING TRACKER 2.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeFasting != null) NeonCyan else TextMuted
                            )
                            Text(
                                text = if (activeFasting != null) "Fast in progress (${activeFasting.targetHours}h Target)" else "No active fast • Tap to start",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- 7. COMMAND MODULE SHORTCUTS GRID ---
        item {
            Text(
                text = "COMMAND MODULES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandItem("Milestones", Icons.Default.MilitaryTech, NeonMagenta, Modifier.weight(1f)) { onNavigate(Screen.Achievements.route) }
                QuickCommandItem("Reports", Icons.Default.Assessment, NeonCyan, Modifier.weight(1f)) { onNavigate(Screen.WeeklyReports.route) }
                QuickCommandItem("Journal", Icons.AutoMirrored.Filled.MenuBook, NeonPurple, Modifier.weight(1f)) { onNavigate(Screen.Journal.route) }
                QuickCommandItem("Motivation", Icons.Default.FormatQuote, NeonAmber, Modifier.weight(1f)) { onNavigate(Screen.Motivation.route) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandItem("Skincare", Icons.Default.Face, NeonPurple, Modifier.weight(1f)) { onNavigate(Screen.Skincare.route) }
                QuickCommandItem("Calendar", Icons.Default.WorkspacePremium, NeonAmber, Modifier.weight(1f)) { onNavigate(Screen.Calendar.route) }
                QuickCommandItem("Analytics", Icons.Default.Insights, NeonGreen, Modifier.weight(1f)) { onNavigate(Screen.Analytics.route) }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun QuickCommandItem(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
        }
    }
}
