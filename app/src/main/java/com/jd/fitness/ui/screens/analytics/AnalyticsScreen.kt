package com.jd.fitness.ui.screens.analytics

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
import androidx.compose.foundation.lazy.items
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
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.components.StepBarChart
import com.jd.fitness.ui.components.WeeklyConsistencyHeatmap
import com.jd.fitness.ui.components.WeightLineChart
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

import com.jd.fitness.ui.components.SubScreenHeader

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val streaks = state.streakInfo

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SubScreenHeader(
                title = "ANALYTICS & METRICS",
                subtitle = "Stored database calculations and discipline statistics",
                onBack = onBack,
                onHome = onHome
            )
        }

        // Streak & Perfect Days Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HolographicStatBadge(
                    label = "CURRENT STREAK",
                    value = "${streaks.currentStreak}d",
                    accentColor = NeonAmber,
                    modifier = Modifier.weight(1f)
                )
                HolographicStatBadge(
                    label = "LONGEST STREAK",
                    value = "${streaks.longestStreak}d",
                    accentColor = NeonElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                HolographicStatBadge(
                    label = "PERFECT DAYS",
                    value = "${streaks.perfectDaysCount}",
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                HolographicStatBadge(
                    label = "TOTAL LOGS",
                    value = "${streaks.totalCheckIns}",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7-Day Consistency Heatmap
        if (state.weeklyHeatmap.isNotEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = "WEEKLY CONSISTENCY OVERVIEW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WeeklyConsistencyHeatmap(weeklyScores = state.weeklyHeatmap)
                }
            }
        }

        // Weight Trend
        item {
            GlassCard {
                Text(
                    text = "BODY WEIGHT HISTORY TREND",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                WeightLineChart(dataPoints = state.weightTrend)
            }
        }

        // Step Trend
        item {
            GlassCard {
                Text(
                    text = "STEP PERFORMANCE (HEALTH CONNECT)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                StepBarChart(stepDays = state.stepTrend)
            }
        }

        // Habit Performance Breakdown
        item {
            Text(
                text = "HABIT DISCIPLINE BREAKDOWN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
        }

        items(state.habitPerformanceList, key = { it.habit.id }) { item ->
            GlassCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.habit.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "${item.completedDays} days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                NeonProgressBar(progress = item.consistencyPercentage / 100f, height = 4.dp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

