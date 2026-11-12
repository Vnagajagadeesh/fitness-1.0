package com.jd.fitness.ui.screens.milestones

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.AchievementCategory
import com.jd.fitness.data.local.entity.AchievementEntity
import com.jd.fitness.ui.components.GlassCard
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
import com.jd.fitness.ui.theme.NeonMagenta
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MilestonesScreen(
    viewModel: MilestonesViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }

    val filteredAchievements = state.achievements.filter {
        state.selectedCategory == "ALL" || it.category == state.selectedCategory
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
                title = "DISCIPLINE MILESTONES",
                subtitle = "Genuine progress rewards & consistency trophies",
                onBack = onBack,
                onHome = onHome
            )
        }

        // --- NEWLY UNLOCKED CELEBRATION CARD ---
        if (state.newlyUnlocked.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    GlassCard(
                        borderColor = NeonGreen.copy(alpha = 0.8f),
                        onClick = { viewModel.dismissNewUnlockNotice() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, "Celebration", tint = NeonGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NEW MILESTONE UNLOCKED!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = state.newlyUnlocked.joinToString(", ") { it.title },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- HERO OVERVIEW CARD ---
        item {
            GlassCard(borderColor = NeonPurple.copy(alpha = 0.6f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0x33B5179E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Trophies",
                                tint = NeonMagenta,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DISCIPLINE VAULT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonMagenta,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${state.unlockedCount} of ${state.totalCount} Unlocked",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                        }
                    }

                    HolographicStatBadge(
                        label = "TIER",
                        value = "${state.unlockPercentage.toInt()}%",
                        accentColor = if (state.unlockPercentage >= 50f) NeonGreen else NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                NeonProgressBar(progress = state.unlockPercentage / 100f, height = 8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Calculated exclusively from genuine habit streaks, workout logs, and step records.",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // --- CATEGORY FILTERS ---
        item {
            val categories = listOf(
                Pair("ALL", "All Badges"),
                Pair(AchievementCategory.HABIT.name, "Habit Streaks"),
                Pair(AchievementCategory.FITNESS.name, "Fitness & Movement"),
                Pair(AchievementCategory.PRODUCTIVITY.name, "Productivity")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (catKey, label) ->
                    val isSelected = state.selectedCategory == catKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonElectricBlue else DarkNavyCard)
                            .clickable { viewModel.setSelectedCategory(catKey) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DeepBlackNavy else TextWhite
                        )
                    }
                }
            }
        }

        // --- ACHIEVEMENTS LIST ---
        items(filteredAchievements) { ach ->
            val progressFrac = (ach.currentProgress.toFloat() / ach.maxProgress.toFloat()).coerceIn(0f, 1f)

            GlassCard(
                borderColor = if (ach.isUnlocked) NeonGreen.copy(alpha = 0.5f) else GlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (ach.isUnlocked) Color(0x3300F59B) else Color(0x2216203B)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (ach.isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Unlocked",
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ach.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) TextWhite else TextMuted
                            )
                            if (ach.isUnlocked && ach.unlockedAt != null) {
                                Text(
                                    text = dateFormatter.format(Date(ach.unlockedAt)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = ach.description,
                            fontSize = 11.sp,
                            color = TextSubtle,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                NeonProgressBar(
                                    progress = progressFrac,
                                    height = 5.dp,
                                    activeColor = if (ach.isUnlocked) NeonGreen else NeonCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (ach.isUnlocked) "UNLOCKED" else "${ach.currentProgress}/${ach.maxProgress}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) NeonGreen else NeonCyan
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

