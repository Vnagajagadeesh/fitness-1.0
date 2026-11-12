package com.jd.fitness.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.sync.SyncResult
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.JDLogoBadge
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

import com.jd.fitness.ui.components.SubScreenHeader

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var userNameInput by remember(state.preferences.userName) { mutableStateOf(state.preferences.userName) }
    var startDateInput by remember(state.challengeConfig.startDate) { mutableStateOf(state.challengeConfig.startDate) }
    var endDateInput by remember(state.challengeConfig.endDate) { mutableStateOf(state.challengeConfig.endDate) }
    var stepGoalInput by remember(state.preferences.stepGoal) { mutableStateOf(state.preferences.stepGoal.toString()) }

    var supabaseUrlInput by remember(state.preferences.supabaseUrl) { mutableStateOf(state.preferences.supabaseUrl) }
    var supabaseAnonKeyInput by remember(state.preferences.supabaseAnonKey) { mutableStateOf(state.preferences.supabaseAnonKey) }

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
                title = "SETTINGS & DATA",
                subtitle = "FITNESS 1.2 — JD EDITION  •  Configuration & Cloud Sync",
                onBack = onBack,
                onHome = onHome
            )
        }

        // Status Banner
        if (state.statusMessage != null) {
            item {
                GlassCard(
                    borderColor = NeonCyan,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = state.statusMessage!!, fontSize = 12.sp, color = NeonCyan)
                        TextButton(onClick = { viewModel.clearStatusMessage() }) {
                            Text("OK", color = TextWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- BRANDING & PROFILE ---
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JDLogoBadge(
                        size = 50.dp,
                        initials = userNameInput.take(2).uppercase().ifBlank { "JD" }
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FITNESS 1.0 IDENTITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        OutlinedTextField(
                            value = userNameInput,
                            onValueChange = {
                                userNameInput = it
                                viewModel.updateUserName(it.trim())
                            },
                            label = { Text("Display Name / Initials") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonElectricBlue,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- 90-DAY CHALLENGE CONFIGURATION ---
        item {
            GlassCard {
                Text(
                    text = "90-DAY CHALLENGE DATES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Adjust start and target dates without destroying historical daily logs.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startDateInput,
                        onValueChange = { startDateInput = it },
                        label = { Text("Start Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDateInput,
                        onValueChange = { endDateInput = it },
                        label = { Text("End Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HolographicButton(
                    text = "Update Challenge Dates",
                    onClick = { viewModel.updateChallengeDates(startDateInput.trim(), endDateInput.trim()) }
                )
            }
        }

        // --- STEP GOAL ---
        item {
            GlassCard {
                Text(
                    text = "DAILY STEP GOAL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Health Connect sensor tracking target",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stepGoalInput,
                    onValueChange = {
                        stepGoalInput = it
                        val g = it.toIntOrNull()
                        if (g != null && g > 0) viewModel.updateStepGoal(g)
                    },
                    label = { Text("Target Steps (Default: 10000)") },
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
        }

        // --- NOTIFICATIONS & ALARMS ---
        item {
            val isNotifEnabled = state.preferences.isNotificationsEnabled
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "DAILY DISCIPLINE REMINDERS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(text = "Scheduled local reminders for habits", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Switch(
                        checked = isNotifEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it, context) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                if (isNotifEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ReminderRow("Morning Skincare", state.preferences.morningSkincareReminder) {
                        viewModel.updateReminderTime("morning_skincare", it, context)
                    }
                    ReminderRow("Night Skincare", state.preferences.nightSkincareReminder) {
                        viewModel.updateReminderTime("night_skincare", it, context)
                    }
                    ReminderRow("Study (1 Hour)", state.preferences.studyReminder) {
                        viewModel.updateReminderTime("study", it, context)
                    }
                    ReminderRow("Home Workout", state.preferences.workoutReminder) {
                        viewModel.updateReminderTime("workout", it, context)
                    }
                    ReminderRow("Daily Habit Review", state.preferences.dailyReviewReminder) {
                        viewModel.updateReminderTime("daily_review", it, context)
                    }
                    ReminderRow("Morning Weigh-in", state.preferences.weightReminder) {
                        viewModel.updateReminderTime("weight", it, context)
                    }
                    ReminderRow("Step Goal Check", state.preferences.stepGoalReminder) {
                        viewModel.updateReminderTime("step_goal", it, context)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "NOTIFICATION INTELLIGENCE (FITNESS 1.2)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(modifier = Modifier.height(4.dp))

                    ReminderRow("Morning Motivation (${if (state.preferences.isMorningMotivationEnabled) "On" else "Off"})", state.preferences.morningMotivationTime) {
                        viewModel.updateReminderTime("morning_motivation", it, context)
                    }
                    ReminderRow("Evening Motivation (${if (state.preferences.isEveningMotivationEnabled) "On" else "Off"})", state.preferences.eveningMotivationTime) {
                        viewModel.updateReminderTime("evening_motivation", it, context)
                    }
                    ReminderRow("Journal Reflection", state.preferences.journalReminderTime) {
                        viewModel.updateReminderTime("journal_reminder", it, context)
                    }
                    ReminderRow("Recovery Check-in", state.preferences.recoveryReminderTime) {
                        viewModel.updateReminderTime("recovery_reminder", it, context)
                    }
                    ReminderRow("Quiet Hours Start", state.preferences.quietHoursStart) {
                        viewModel.updateReminderTime("quiet_start", it, context)
                    }
                    ReminderRow("Quiet Hours End", state.preferences.quietHoursEnd) {
                        viewModel.updateReminderTime("quiet_end", it, context)
                    }
                }
            }
        }

        // --- SUPABASE CLOUD SYNC ---
        item {
            GlassCard {
                Text(
                    text = "SUPABASE CLOUD SYNC",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Project: Fitness-season-1 • Table: season10_progress",
                    fontSize = 11.sp,
                    color = NeonCyan
                )
                Text(
                    text = "Zero service-role keys exposed. Connects securely via Supabase Anon REST API.",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = supabaseUrlInput,
                    onValueChange = { supabaseUrlInput = it },
                    label = { Text("Supabase URL (https://xxxx.supabase.co)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonElectricBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = supabaseAnonKeyInput,
                    onValueChange = { supabaseAnonKeyInput = it },
                    label = { Text("Supabase Public Anon Key") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonElectricBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                HolographicButton(
                    text = "Save Cloud Credentials",
                    onClick = { viewModel.saveSupabaseConfig(supabaseUrlInput.trim(), supabaseAnonKeyInput.trim()) }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HolographicButton(
                        text = "Cloud Backup",
                        icon = Icons.Default.CloudUpload,
                        onClick = { viewModel.syncBackupToCloud() },
                        modifier = Modifier.weight(1f)
                    )
                    HolographicButton(
                        text = "Cloud Restore",
                        icon = Icons.Default.CloudDownload,
                        onClick = { viewModel.syncRestoreFromCloud() },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.preferences.lastSyncTimestamp > 0) {
                    val dateStr = SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(state.preferences.lastSyncTimestamp))
                    Text(
                        text = "Last synced: $dateStr",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // --- BACKUP & LEGACY RESTORE ---
        item {
            GlassCard {
                Text(
                    text = "DATA BACKUP & LEGACY IMPORT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Export complete JSON backup or import legacy season10-data-v1 from your phone.",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HolographicButton(
                        text = "Export Backup",
                        icon = Icons.Default.FileDownload,
                        onClick = { viewModel.exportBackup() },
                        modifier = Modifier.weight(1f)
                    )
                    HolographicButton(
                        text = "Import Backup",
                        icon = Icons.Default.FileUpload,
                        onClick = { viewModel.openImportDialog() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- DANGER ZONE: DATA RESET ---
        item {
            GlassCard(borderColor = NeonMagenta.copy(alpha = 0.5f)) {
                Text(
                    text = "DANGER ZONE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonMagenta
                )
                Text(
                    text = "Reset all stored habit check-ins, weight entries, and fasting sessions. Default habit definitions and challenge settings are preserved.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { viewModel.promptResetData() },
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonMagenta)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset All Historical Data")
                }
            }
        }

        // --- ABOUT CARD ---
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "FITNESS 1.0 — THE BEGINNING", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(text = "Version 1.0.0 (Release-Ready Native Architecture)", fontSize = 11.sp, color = TextMuted)
                        Text(text = "Target Device: POCO X7 Pro (HyperOS / Android 14+)", fontSize = 11.sp, color = NeonCyan)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Export Dialog
    if (state.showExportDialog && state.exportedJson != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeExportDialog() },
            containerColor = DarkNavyCard,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Export Complete Backup", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your complete FITNESS 1.0 backup JSON is generated. You can copy it to your clipboard.", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.exportedJson!!,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            },
            confirmButton = {
                HolographicButton(
                    text = "Copy to Clipboard",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("FITNESS 1.0 Backup", state.exportedJson))
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        viewModel.closeExportDialog()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeExportDialog() }) { Text("Close", color = TextMuted) }
            }
        )
    }

    // Import Dialog
    if (state.showImportDialog) {
        BackupRestoreDialog(
            migrationSummary = state.migrationSummary,
            rawJsonToImport = state.rawJsonToImport,
            onDismiss = { viewModel.closeImportDialog() },
            onAnalyze = { viewModel.analyzeImportJson(it) },
            onConfirmImport = { json, splitSkincare -> viewModel.executeImport(json, splitSkincare) }
        )
    }

    // Reset Confirm Dialog
    if (state.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelResetData() },
            containerColor = DarkNavyCard,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Are you absolutely sure?", color = NeonMagenta, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will permanently delete all daily habit check-ins, weight logs, skincare logs, fasting sessions, and step records from your phone's database. This action cannot be undone.",
                    color = TextWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmResetData() },
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonMagenta)
                ) {
                    Text("YES, DELETE EVERYTHING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelResetData() }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ReminderRow(
    title: String,
    time: String,
    onTimeChanged: (String) -> Unit
) {
    var timeInput by remember(time) { mutableStateOf(time) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 13.sp, color = TextWhite)
        OutlinedTextField(
            value = timeInput,
            onValueChange = {
                timeInput = it
                if (it.length == 5 && it.contains(":")) {
                    onTimeChanged(it)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonElectricBlue,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            modifier = Modifier.width(90.dp)
        )
    }
}

