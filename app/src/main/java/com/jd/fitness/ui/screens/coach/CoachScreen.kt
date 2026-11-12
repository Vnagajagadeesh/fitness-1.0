package com.jd.fitness.ui.screens.coach

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.CoachPersonalityState
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.SubScreenHeader
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
import com.jd.fitness.ui.theme.NeonAmber
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonMagenta
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite

@Composable
fun CoachScreen(
    viewModel: CoachViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val stateColor = when (state.personalityState) {
        CoachPersonalityState.HIGH_PERFORMANCE -> NeonGreen
        CoachPersonalityState.RECOVERY_NEEDED -> NeonAmber
        CoachPersonalityState.STRUGGLING -> NeonMagenta
        CoachPersonalityState.INCONSISTENT -> NeonCyan
    }

    val stateLabel = when (state.personalityState) {
        CoachPersonalityState.HIGH_PERFORMANCE -> "HIGH PERFORMANCE"
        CoachPersonalityState.RECOVERY_NEEDED -> "RECOVERY NEEDED"
        CoachPersonalityState.STRUGGLING -> "REBUILDING MOMENTUM"
        CoachPersonalityState.INCONSISTENT -> "CONSISTENCY FOCUS"
    }

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
                title = "FITNESS AI COACH",
                subtitle = "Adaptive personal intelligence & accountability",
                onBack = onBack,
                onHome = onHome,
                actionIcon = Icons.Default.Refresh,
                actionDescription = "Refresh Insight",
                onAction = { viewModel.refreshInsight() }
            )
        }

        // --- HERO COACH INSIGHT CARD ---
        item {
            GlassCard(
                borderColor = stateColor.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(stateColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Coach",
                                tint = stateColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "COACH STATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = stateLabel,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = stateColor
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2216203B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (state.isAiGenerated) "CLOUD AI" else "LOCAL RULE ENGINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = state.guidanceText.ifBlank {
                        "Analyzing habits, step logs, recovery state and consistency trends to formulate your guidance..."
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Engine details
                Text(
                    text = "Engine mode: Deterministic on-device rule engine. Zero external data transmission unless enabled.",
                    fontSize = 10.sp,
                    color = TextSubtle
                )
            }
        }

        // --- SUPPORTING DATA EVIDENCE CARD ---
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Data",
                        tint = NeonElectricBlue,
                        modifier = Modifier.padding(top = 2.dp, end = 10.dp).size(20.dp)
                    )
                    Column {
                        Text(
                            text = "DATA-DRIVEN EVIDENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonElectricBlue,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.supportingDataSummary.ifBlank {
                                "Calculated directly from your genuine database logs (habits, steps, weights, recovery)."
                            },
                            fontSize = 12.sp,
                            color = TextWhite,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // --- ACTIONABLE GOALS FOR TODAY ---
        if (state.actionableGoals.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIONABLE TARGETS FOR TODAY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.actionableGoals.forEach { goal ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = stateColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = goal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- PRIVACY & JOURNAL INTEGRATION TOGGLE ---
        item {
            GlassCard(borderColor = NeonPurple.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Journal-Based AI Insights",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Allow AI coach to read journal reflections to contextualize guidance. Off by default for privacy.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(
                        checked = state.allowJournalConsent,
                        onCheckedChange = { viewModel.setJournalConsent(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBlackNavy,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkNavyCard
                        )
                    )
                }
            }
        }

        // --- COACH HISTORY LOG ---
        if (state.historyInsights.isNotEmpty()) {
            item {
                Text(
                    text = "PREVIOUS COACH INSIGHTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
            }

            items(state.historyInsights.take(7)) { history ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = history.date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x2216203B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = history.personalityState.replace("_", " "),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = history.guidanceText,
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

