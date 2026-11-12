package com.jd.fitness.ui.screens.steps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.jd.fitness.data.health.HealthConnectManager
import com.jd.fitness.data.health.HealthConnectStatus
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.components.StepBarChart
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

import com.jd.fitness.ui.components.SubScreenHeader

@Composable
fun StepTrackingScreen(
    viewModel: StepViewModel,
    healthConnectManager: HealthConnectManager,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val progress = (state.todaySteps.toFloat() / state.stepGoal.toFloat()).coerceIn(0f, 1f)

    // Health Connect Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        viewModel.syncStepsFromHealthConnect()
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
                title = "AUTOMATIC STEP TRACKING",
                subtitle = "Android Health Connect & POCO X7 Pro Sensor Sync",
                onBack = onBack,
                onHome = onHome,
                actionIcon = Icons.Default.Refresh,
                actionDescription = "Sync",
                onAction = { viewModel.syncStepsFromHealthConnect() }
            )
        }

        // Hero Card
        item {
            GlassCard(
                borderColor = if (state.isGoalReached) NeonGreen.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S STEPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${state.todaySteps}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }

                    if (state.isGoalReached) {
                        HolographicStatBadge(
                            label = "GOAL",
                            value = "REACHED",
                            accentColor = NeonGreen
                        )
                    } else {
                        HolographicStatBadge(
                            label = "REMAINING",
                            value = "${state.remainingSteps}",
                            accentColor = NeonAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressBar(progress = progress, height = 9.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% of ${state.stepGoal / 1000}k goal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isGoalReached) NeonGreen else NeonCyan
                    )
                    Text(
                        text = "Auto-completes Daily Habit #9",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Health Connect Status Card
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (state.hasPermission) NeonGreen else NeonAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "POCO X7 Pro • Health Connect",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (state.hasPermission) {
                                "Permission active. Automatic step sync running."
                            } else {
                                "Health Connect permission needed to read steps."
                            },
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!state.hasPermission) {
                        HolographicButton(
                            text = "Grant Permission",
                            onClick = { permissionLauncher.launch(healthConnectManager.permissions) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        HolographicButton(
                            text = "Refresh Steps",
                            onClick = { viewModel.syncStepsFromHealthConnect() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TextButton(onClick = { viewModel.openManualEntry() }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Manual Entry", color = NeonCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // 7-Day Step Bar Chart
        item {
            GlassCard {
                Text(
                    text = "7-DAY STEP PERFORMANCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                StepBarChart(stepDays = state.weeklyHistory, goal = state.stepGoal)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (state.showManualEntryDialog) {
        ManualStepEntryDialog(
            currentSteps = state.todaySteps,
            onDismiss = { viewModel.closeManualEntry() },
            onSave = { viewModel.saveManualSteps(it) }
        )
    }
}

@Composable
private fun ManualStepEntryDialog(
    currentSteps: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var stepText by remember { mutableStateOf(if (currentSteps > 0) currentSteps.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Manual Step Fallback", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "If Health Connect is temporarily unavailable or permission is disabled, you can manually log today's total steps.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = stepText,
                    onValueChange = { stepText = it },
                    label = { Text("Today's Steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            HolographicButton(
                text = "Save Steps",
                enabled = (stepText.toIntOrNull() ?: 0) >= 0,
                onClick = { onSave(stepText.toIntOrNull() ?: 0) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

