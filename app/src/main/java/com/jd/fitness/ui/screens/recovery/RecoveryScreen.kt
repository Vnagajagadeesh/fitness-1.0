package com.jd.fitness.ui.screens.recovery

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.jd.fitness.data.local.entity.PerceivedEnergy
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
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.util.Locale

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

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
                title = "RECOVERY CENTRE",
                subtitle = "Sustainable fitness & energy balance tracking",
                onBack = onBack,
                onHome = onHome
            )
        }

        // --- INTELLIGENT RECOMMENDATION HERO CARD ---
        item {
            GlassCard(
                borderColor = NeonCyan.copy(alpha = 0.6f)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Recovery",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SMART RECOVERY INSIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.intelligentRecommendation.ifBlank {
                                "Loading smart recovery balance for today..."
                            },
                            fontSize = 13.sp,
                            color = TextWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // --- WEEKLY SUMMARY STATS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Avg Sleep", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", state.averageSleepHours)}h",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Rest Days", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.weeklyRestDaysCount} days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen
                    )
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Consec. Workouts", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.consecutiveWorkoutDays} days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (state.consecutiveWorkoutDays >= 3) NeonAmber else NeonElectricBlue
                    )
                }
            }
        }

        // --- PERCEIVED ENERGY LEVEL SELECTOR ---
        item {
            Text(
                text = "PERCEIVED ENERGY LEVEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val energyOptions = listOf(
                Pair(PerceivedEnergy.WELL_RESTED, "Well Rested"),
                Pair(PerceivedEnergy.NORMAL, "Normal Energy"),
                Pair(PerceivedEnergy.TIRED, "Tired"),
                Pair(PerceivedEnergy.NEED_REST, "Need Rest")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                energyOptions.forEach { (energy, label) ->
                    val isSelected = state.perceivedEnergy == energy.name
                    GlassCard(
                        borderColor = if (isSelected) {
                            when (energy) {
                                PerceivedEnergy.WELL_RESTED -> NeonGreen
                                PerceivedEnergy.NORMAL -> NeonCyan
                                PerceivedEnergy.TIRED -> NeonAmber
                                PerceivedEnergy.NEED_REST -> Color(0xFFFF5555)
                            }
                        } else GlassBorder,
                        borderWidth = if (isSelected) 1.5.dp else 1.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        onClick = { viewModel.setPerceivedEnergy(energy) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonCyan else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextWhite else TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SLEEP DURATION LOGGING ---
        item {
            Text(
                text = "SLEEP DURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, contentDescription = "Sleep", tint = NeonPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recorded Sleep", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", state.sleepHours)} Hours",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonPurple
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = state.sleepHours,
                    onValueChange = { viewModel.setSleepHours(it) },
                    valueRange = 4.0f..12.0f,
                    steps = 15, // 0.5h increments
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPurple,
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = Color(0x2216203B)
                    )
                )

                Text(
                    text = "Note: Sleep duration is logged for recovery context. Sleep quality is not inferred from duration alone.",
                    fontSize = 10.sp,
                    color = TextSubtle
                )
            }
        }

        // --- REST DAY VS WORKOUT SWITCHES ---
        item {
            Text(
                text = "TRAINING & REST STATUS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Official Rest Day", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Rest days do not negatively impact habit streak", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = state.isRestDay,
                        onCheckedChange = { viewModel.setRestDay(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBlackNavy,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkNavyCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Workout Performed Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(
                            text = if (state.workoutDone) "Type: ${state.workoutType}" else "No workout logged today",
                            fontSize = 11.sp,
                            color = if (state.workoutDone) NeonCyan else TextMuted
                        )
                    }
                    Switch(
                        checked = state.workoutDone,
                        onCheckedChange = { viewModel.setWorkoutDone(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepBlackNavy,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkNavyCard
                        )
                    )
                }

                if (state.workoutDone) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Workout Discipline:", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Strength", "Cardio", "Calisthenics", "Mobility").forEach { type ->
                            val isChosen = state.workoutType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) NeonCyan else Color(0x2216203B))
                                    .clickable { viewModel.setWorkoutDone(true, type) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) DeepBlackNavy else TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- RECOVERY NOTES ---
        item {
            GlassCard {
                Text("Recovery Notes / Physical Sensations", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    placeholder = { Text("e.g., Slight shoulder tightness, good morning energy...", color = TextSubtle) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonElectricBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                HolographicButton(
                    text = "Save Recovery Log",
                    icon = Icons.Default.Check,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.saveCurrentEntry()
                }
            }
        }

        // --- PAST RECOVERY LOGS ---
        if (state.recentEntries.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT RECOVERY LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
            }

            items(state.recentEntries.take(7)) { entry ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = entry.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(
                                text = "Sleep: ${entry.sleepHours}h  •  Energy: ${entry.perceivedEnergy.replace("_", " ")}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        HolographicStatBadge(
                            label = if (entry.isRestDay) "REST" else "ACTIVE",
                            value = if (entry.workoutDone) entry.workoutType else "Rest",
                            accentColor = if (entry.isRestDay) NeonGreen else NeonCyan
                        )
                    }
                    if (entry.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = entry.notes, fontSize = 11.sp, color = TextSubtle)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

