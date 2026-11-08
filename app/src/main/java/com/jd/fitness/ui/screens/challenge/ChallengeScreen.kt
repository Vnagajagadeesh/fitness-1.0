package com.jd.fitness.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.MilestoneCard
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.NeonAmber
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.util.Locale

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val progress = state.progress
    val currentDay = progress?.currentDay ?: 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Column {
                    Text(
                        text = "THE 90-DAY CHALLENGE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "FITNESS 1.0 — THE BEGINNING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // Hero Card
        item {
            GlassCard(borderColor = NeonElectricBlue.copy(alpha = 0.5f)) {
                Text(
                    text = "COMMAND PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Day $currentDay of ${progress?.totalDays ?: 90}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Text(
                    text = "${progress?.daysRemaining ?: 90} days remaining until total transformation",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))
                NeonProgressBar(progress = (progress?.overallPercentage ?: 0f) / 100f, height = 10.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", progress?.overallPercentage ?: 0f)}% Complete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonElectricBlue
                    )
                    Text(
                        text = "Target: 31 Jan 2027",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Challenge Schedule Dates Card
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CHALLENGE TIMEFRAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle
                        )
                        Text(
                            text = "${state.config.startDate}  →  ${state.config.endDate}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Text(
                            text = "90 consecutive days of strict discipline",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Stats Badges
        item {
            val streaks = state.streakInfo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HolographicStatBadge(
                    label = "CURRENT",
                    value = "${streaks.currentStreak}d",
                    accentColor = NeonAmber,
                    modifier = Modifier.weight(1f)
                )
                HolographicStatBadge(
                    label = "LONGEST",
                    value = "${streaks.longestStreak}d",
                    accentColor = NeonElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                HolographicStatBadge(
                    label = "PERFECT",
                    value = "${streaks.perfectDaysCount}d",
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                HolographicStatBadge(
                    label = "LOGS",
                    value = "${streaks.totalCheckIns}",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Milestones Section
        item {
            Text(
                text = "CHALLENGE MILESTONES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            MilestoneCard(
                dayNumber = 30,
                title = "DAY 30 — FOUNDATION",
                subtitle = "Establish immutable hygiene, study, step count and routine baselines.",
                currentDay = currentDay
            )
        }

        item {
            MilestoneCard(
                dayNumber = 60,
                title = "DAY 60 — MOMENTUM",
                subtitle = "Consistency becomes identity. Workout intensity and dietary discipline locked in.",
                currentDay = currentDay
            )
        }

        item {
            MilestoneCard(
                dayNumber = 90,
                title = "DAY 90 — TRANSFORMATION",
                subtitle = "Ultimate self-mastery. Complete physical conditioning, mental clarity and habit automation.",
                currentDay = currentDay
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

