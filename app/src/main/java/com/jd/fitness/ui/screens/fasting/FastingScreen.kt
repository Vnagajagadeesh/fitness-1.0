package com.jd.fitness.ui.screens.fasting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DarkNavySurface
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
fun FastingScreen(
    viewModel: FastingViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val activeSession = state.activeSession

    val elapsedHours = state.elapsedSeconds / 3600
    val elapsedMinutes = (state.elapsedSeconds % 3600) / 60
    val elapsedSec = state.elapsedSeconds % 60
    val timerString = String.format(Locale.US, "%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSec)

    val targetSeconds = state.targetHours * 3600L
    val progress = (state.elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "INTERMITTENT FASTING & OMAD",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                letterSpacing = 1.sp
            )
            Text(
                text = "Metabolic regulation and disciplined eating-window tracking",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        // --- HEALTH & MEDICAL SAFETY NOTICE ---
        item {
            GlassCard(borderColor = NeonAmber.copy(alpha = 0.4f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Health notice",
                        tint = NeonAmber,
                        modifier = Modifier.padding(top = 2.dp, end = 10.dp).size(20.dp)
                    )
                    Column {
                        Text(
                            text = "HEALTH & SAFETY NOTICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                        Text(
                            text = "Fasting is entirely optional. It is not mandatory for fitness or health. If you are under 18 or have any medical condition, avoid extreme fasting and consult a certified medical practitioner. You may stop or skip any session at any time with zero penalty.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- ACTIVE FASTING TIMER HERO CARD ---
        item {
            GlassCard(
                borderColor = if (activeSession != null) NeonCyan.copy(alpha = 0.6f) else GlassBorder
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (activeSession != null) {
                            if (activeSession.isOMAD) "OMAD FAST IN PROGRESS" else "FASTING IN PROGRESS"
                        } else "FASTING READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeSession != null) NeonCyan else TextMuted,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeSession != null) timerString else "00:00:00",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Target: ${state.targetHours} Hours ${if (state.isOMAD) "(OMAD - One Meal A Day)" else ""}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    NeonProgressBar(progress = progress, height = 8.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (activeSession == null) {
                        // Duration Selector Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            FastingChip("16:8", state.targetHours == 16 && !state.isOMAD) {
                                viewModel.setOMAD(false)
                                viewModel.setTargetHours(16)
                            }
                            FastingChip("18:6", state.targetHours == 18 && !state.isOMAD) {
                                viewModel.setOMAD(false)
                                viewModel.setTargetHours(18)
                            }
                            FastingChip("20:4", state.targetHours == 20 && !state.isOMAD) {
                                viewModel.setOMAD(false)
                                viewModel.setTargetHours(20)
                            }
                            FastingChip("OMAD 23h", state.isOMAD) {
                                viewModel.setOMAD(true)
                            }
                        }

                        HolographicButton(
                            text = "Start Fasting Session",
                            onClick = { viewModel.startFasting() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HolographicButton(
                                text = "End Fast",
                                onClick = { viewModel.endFasting() },
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { viewModel.cancelFasting() },
                                colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                            ) {
                                Text("Stop (No Penalty)")
                            }
                        }
                    }
                }
            }
        }

        // --- MEAL & EATING WINDOW LOGGING ---
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MEAL LOGGING",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Record meal times within your eating window",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    HolographicButton(
                        text = "+ Log Meal",
                        onClick = { viewModel.openLogMeal() }
                    )
                }

                if (state.sessionMeals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    state.sessionMeals.forEach { meal ->
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(meal.mealTime))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = meal.mealTitle, fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                                if (meal.isOMADMeal) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "• OMAD", fontSize = 10.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(text = timeStr, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // --- PAST FASTING SESSIONS ---
        item {
            Text(
                text = "PAST FASTING SESSIONS (${state.pastSessions.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
        }

        items(state.pastSessions, key = { it.id }) { session ->
            val durationHours = if (session.endTime != null) (session.endTime - session.startTime) / 3600000.0 else 0.0
            val startDateStr = SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(session.startTime))

            GlassCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", durationHours)} hrs Fasted",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            if (session.isOMAD) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OMAD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPurple
                                )
                            }
                        }
                        Text(text = startDateStr, fontSize = 11.sp, color = TextMuted)
                    }

                    Text(
                        text = if (durationHours >= session.targetHours) "TARGET REACHED" else "COMPLETED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (durationHours >= session.targetHours) NeonGreen else NeonCyan
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (state.showMealDialog) {
        MealLogDialog(
            onDismiss = { viewModel.closeMealDialog() },
            onSave = { title, isOMAD, notes -> viewModel.logMeal(title, isOMAD, notes) }
        )
    }
}

@Composable
private fun FastingChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkNavySurface)
            .border(1.dp, if (isSelected) NeonCyan else GlassBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) NeonCyan else TextMuted
        )
    }
}

@Composable
private fun MealLogDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, isOMAD: Boolean, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isOMAD by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Log Meal / Break Fast", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Meal Title (e.g. Eggs & Avocado)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OMAD Meal (Single meal of the day)", fontSize = 12.sp, color = TextWhite)
                    Switch(
                        checked = isOMAD,
                        onCheckedChange = { isOMAD = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        },
        confirmButton = {
            HolographicButton(
                text = "Save Meal",
                enabled = title.isNotBlank(),
                onClick = { onSave(title.trim(), isOMAD, notes.trim()) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

