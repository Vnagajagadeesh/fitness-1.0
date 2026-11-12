package com.jd.fitness.ui.screens.reports

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.components.SubScreenHeader
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
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
fun WeeklyReportScreen(
    viewModel: WeeklyReportViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val report = state.selectedReport
    val prevReport = state.previousReport

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
                title = "WEEKLY REPORT",
                subtitle = "Automated Sunday intelligence & performance review",
                onBack = onBack,
                onHome = onHome,
                actionIcon = Icons.Default.Share,
                actionDescription = "Share",
                onAction = { viewModel.shareReport(context) }
            )
        }

        // --- REPORT PERIOD SELECTOR ---
        if (state.allReports.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.allReports) { r ->
                        val isSelected = r.id == report?.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonElectricBlue else DarkNavyCard)
                                .clickable { viewModel.selectReport(r) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${r.weekStartDate} to ${r.weekEndDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DeepBlackNavy else TextWhite
                            )
                        }
                    }
                }
            }
        }

        if (report == null) {
            item {
                GlassCard {
                    Text(
                        text = "Calculating and gathering weekly intelligence from your records...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            // --- HERO OVERVIEW CARD ---
            item {
                val delta = if (prevReport != null) report.overallHabitPercentage - prevReport.overallHabitPercentage else 0f

                GlassCard(borderColor = NeonElectricBlue.copy(alpha = 0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEKLY DISCIPLINE SCORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${report.overallHabitPercentage.toInt()}% Compliance",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                        }

                        if (prevReport != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (delta >= 0f) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (delta >= 0f) NeonGreen else NeonAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (delta >= 0f) "+" else ""}${delta.toInt()}% vs prev",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (delta >= 0f) NeonGreen else NeonAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    NeonProgressBar(progress = report.overallHabitPercentage / 100f, height = 8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Best Habit", fontSize = 10.sp, color = TextMuted)
                            Text(report.bestHabitTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Needs Attention", fontSize = 10.sp, color = TextMuted)
                            Text(report.worstHabitTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                        }
                    }
                }
            }

            // --- METRICS BREAKDOWN GRID ---
            item {
                Text(
                    text = "PERFORMANCE METRICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Avg Steps", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${report.averageDailySteps}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Workouts", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${report.totalWorkouts}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Face, null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Skincare", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${report.skincareRate.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SelfImprovement, null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rest Days", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${report.restDaysCount}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
                    }
                }
            }

            // --- THREE PRACTICAL SUGGESTIONS ---
            if (state.suggestions.isNotEmpty()) {
                item {
                    Text(
                        text = "ACTIONABLE SUGGESTIONS FOR NEXT WEEK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.suggestions.forEachIndexed { idx, sug ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.padding(top = 2.dp, end = 8.dp).size(16.dp)
                                    )
                                    Text(
                                        text = "${idx + 1}. $sug",
                                        fontSize = 12.sp,
                                        color = TextWhite,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- PDF EXPORT & SHARE BUTTONS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HolographicButton(
                        text = "Export PDF",
                        icon = Icons.Default.PictureAsPdf,
                        gradient = listOf(NeonPurple, NeonElectricBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        val file = viewModel.exportToPdf(context)
                        if (file != null) {
                            Toast.makeText(context, "PDF Exported: ${file.name}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                        }
                    }

                    HolographicButton(
                        text = "Share Report",
                        icon = Icons.Default.Share,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.shareReport(context)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

