package com.jd.fitness.ui.screens.fasting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingScheduleEntity
import com.jd.fitness.data.local.entity.FastingState
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.components.SubScreenHeader
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
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FastingScreen(
    viewModel: FastingViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val activeSession = state.activeSession
    val context = LocalContext.current

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val dateFormatter = remember { SimpleDateFormat("EEE, d MMM yyyy", Locale.US) }

    var showStartNotesDialog by remember { mutableStateOf(false) }
    var showEndNotesDialog by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<FastingEntity?>(null) }
    var showSaveScheduleDialog by remember { mutableStateOf(false) }

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
                title = "FASTING TRACKER 2.0",
                subtitle = "Metabolic regulation & disciplined schedule tracking",
                onBack = onBack,
                onHome = onHome
            )
        }

        // --- HEALTH & SAFETY NOTICE ---
        item {
            GlassCard(borderColor = NeonAmber.copy(alpha = 0.4f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Health notice",
                        tint = NeonAmber,
                        modifier = Modifier
                            .padding(top = 2.dp, end = 10.dp)
                            .size(20.dp)
                    )
                    Column {
                        Text(
                            text = "HEALTH & SAFETY NOTICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                        Text(
                            text = "Fasting is entirely optional and not mandatory for fitness or health. Stages shown are approximate educational descriptions, not exact biological milestones. Stop or break your fast at any time if you feel unwell.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- HERO ACTIVE FASTING TIMER ---
        item {
            GlassCard(
                borderColor = if (activeSession != null) NeonCyan.copy(alpha = 0.6f) else GlassBorder
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeSession != null) {
                                if (activeSession.isOMAD) "ACTIVE OMAD FAST" else "ACTIVE FAST (${activeSession.targetHours}h)"
                            } else {
                                "FASTING READY"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSession != null) NeonCyan else TextMuted,
                            letterSpacing = 1.sp
                        )
                        HolographicStatBadge(
                            label = "TARGET",
                            value = "${state.targetHours}h",
                            accentColor = if (state.isOMAD) NeonMagenta else NeonElectricBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Isolated Live Timer (Recomposes only inside this composable)
                    FastingLiveTimerWidget(
                        elapsedSecondsFlow = viewModel.elapsedSeconds,
                        targetHours = state.targetHours
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start / Planned End Info
                    if (activeSession != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Started", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = timeFormatter.format(Date(activeSession.startTime)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Planned End", fontSize = 11.sp, color = TextMuted)
                                val endMillis = activeSession.plannedEndTime ?: (activeSession.startTime + activeSession.targetHours * 3600L * 1000L)
                                Text(
                                    text = timeFormatter.format(Date(endMillis)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyan
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action Controls
                    if (activeSession == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HolographicButton(
                                text = "Start Fasting",
                                icon = Icons.Default.PlayArrow,
                                modifier = Modifier.weight(1f)
                            ) {
                                notesInput = ""
                                showStartNotesDialog = true
                            }
                            HolographicButton(
                                text = "Custom Times",
                                icon = Icons.Default.Schedule,
                                gradient = listOf(NeonPurple, NeonElectricBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.setShowCustomTimeDialog(true)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HolographicButton(
                                text = "Complete Fast",
                                icon = Icons.Default.Check,
                                gradient = listOf(NeonGreen, NeonCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                notesInput = activeSession.notes
                                showEndNotesDialog = true
                            }
                            HolographicButton(
                                text = "Cancel",
                                icon = Icons.Default.Close,
                                gradient = listOf(Color(0xFF882233), Color(0xFF441122)),
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.cancelFast()
                            }
                        }
                    }
                }
            }
        }

        // --- FASTING SCHEDULE SELECTOR & SAVED SCHEDULES ---
        item {
            Text(
                text = "FASTING SCHEDULES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset options
                val presets = listOf(
                    Triple("12:12 Circadian", 12, false),
                    Triple("14:10 Gentle", 14, false),
                    Triple("16:8 Standard", 16, false),
                    Triple("18:6 Deep", 18, false),
                    Triple("23:1 OMAD", 23, true)
                )

                items(presets) { (title, hours, omad) ->
                    val isSelected = state.targetHours == hours && state.isOMAD == omad
                    GlassCard(
                        modifier = Modifier.width(135.dp),
                        borderColor = if (isSelected) NeonCyan else GlassBorder,
                        borderWidth = if (isSelected) 1.5.dp else 1.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        onClick = { viewModel.setSchedule(hours, omad) }
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyan else TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${hours}h Fast / ${24 - hours}h Eat",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Custom saved schedules
                items(state.savedSchedules) { schedule ->
                    val isSelected = state.targetHours == schedule.fastingHours
                    GlassCard(
                        modifier = Modifier.width(140.dp),
                        borderColor = if (isSelected) NeonPurple else GlassBorder,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        onClick = { viewModel.setSchedule(schedule.fastingHours, schedule.fastingHours >= 23) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = schedule.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonPurple else TextWhite
                            )
                            IconButton(
                                onClick = { viewModel.deleteSchedule(schedule) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "${schedule.fastingHours}:${schedule.eatingHours}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Add custom schedule card
                item {
                    GlassCard(
                        modifier = Modifier.width(120.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        onClick = { showSaveScheduleDialog = true }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, "Add schedule", tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Save Schedule", fontSize = 11.sp, color = TextWhite)
                        }
                    }
                }
            }
        }

        // --- EDUCATIONAL FASTING TIMELINE ---
        item {
            Text(
                text = "APPROXIMATE METABOLIC STAGES (EDUCATIONAL)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            EducationalStagesList(elapsedSecondsFlow = viewModel.elapsedSeconds)
        }

        // --- HISTORY & SUMMARIES ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FASTING HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
                Row {
                    HolographicStatBadge(label = "7-DAY", value = "${String.format(Locale.US, "%.1f", state.weeklyTotalHours)}h", accentColor = NeonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    HolographicStatBadge(label = "30-DAY", value = "${String.format(Locale.US, "%.1f", state.monthlyTotalHours)}h", accentColor = NeonPurple)
                }
            }
        }

        if (state.pastSessions.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = "No completed fasting sessions recorded yet.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(state.pastSessions.take(15)) { session ->
                val durationHrs = if (session.endTime != null && session.endTime > session.startTime) {
                    (session.endTime - session.startTime) / 3600000.0
                } else {
                    0.0
                }

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dateFormatter.format(Date(session.startTime)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${timeFormatter.format(Date(session.startTime))} — ${if (session.endTime != null) timeFormatter.format(Date(session.endTime)) else "Stopped"}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HolographicStatBadge(
                                label = if (session.state == FastingState.COMPLETED) "COMPLETED" else "CANCELLED",
                                value = "${String.format(Locale.US, "%.1f", durationHrs)}h / ${session.targetHours}h",
                                accentColor = if (session.state == FastingState.COMPLETED) NeonGreen else NeonAmber
                            )
                            IconButton(onClick = { viewModel.setEditingSession(session) }) {
                                Icon(Icons.Default.Edit, "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { sessionToDelete = session }) {
                                Icon(Icons.Default.Delete, "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    if (session.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: ${session.notes}",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- START FAST NOTES DIALOG ---
    if (showStartNotesDialog) {
        AlertDialog(
            onDismissRequest = { showStartNotesDialog = false },
            containerColor = DarkNavyCard,
            title = { Text("Start Fasting Session", color = TextWhite) },
            text = {
                Column {
                    Text("Target: ${state.targetHours} Hours", color = NeonCyan, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Optional Notes") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showStartNotesDialog = false
                    viewModel.startFast(notesInput)
                }) {
                    Text("Start", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartNotesDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- COMPLETE FAST NOTES DIALOG ---
    if (showEndNotesDialog) {
        AlertDialog(
            onDismissRequest = { showEndNotesDialog = false },
            containerColor = DarkNavyCard,
            title = { Text("Complete Fasting Session", color = TextWhite) },
            text = {
                Column {
                    Text("Great job on your discipline!", color = NeonGreen, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Session Notes / Reflections") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEndNotesDialog = false
                    viewModel.endFast(notesInput)
                }) {
                    Text("Save & Complete", color = NeonGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndNotesDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            containerColor = DarkNavyCard,
            title = { Text("Delete Fasting Entry", color = TextWhite) },
            text = { Text("Are you sure you want to delete this recorded fasting session? This cannot be undone.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePastSession(session)
                    sessionToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF5555))
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- SAVE CUSTOM SCHEDULE DIALOG ---
    if (showSaveScheduleDialog) {
        var schedName by remember { mutableStateOf("") }
        var schedFastHours by remember { mutableStateOf("16") }
        var schedEatHours by remember { mutableStateOf("8") }

        AlertDialog(
            onDismissRequest = { showSaveScheduleDialog = false },
            containerColor = DarkNavyCard,
            title = { Text("Save Custom Schedule", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = schedName,
                        onValueChange = { schedName = it },
                        label = { Text("Schedule Name (e.g., Weekend Fast)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    OutlinedTextField(
                        value = schedFastHours,
                        onValueChange = { schedFastHours = it },
                        label = { Text("Fasting Hours") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    OutlinedTextField(
                        value = schedEatHours,
                        onValueChange = { schedEatHours = it },
                        label = { Text("Eating Window Hours") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val f = schedFastHours.toIntOrNull() ?: 16
                    val e = schedEatHours.toIntOrNull() ?: (24 - f).coerceAtLeast(0)
                    if (schedName.isNotBlank()) {
                        viewModel.saveSchedule(schedName, f, e, "20:00")
                    }
                    showSaveScheduleDialog = false
                }) {
                    Text("Save", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveScheduleDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- EDIT PAST SESSION DIALOG ---
    state.editingSession?.let { session ->
        var editNotes by remember { mutableStateOf(session.notes) }
        var editTargetHours by remember { mutableStateOf(session.targetHours.toString()) }

        AlertDialog(
            onDismissRequest = { viewModel.setEditingSession(null) },
            containerColor = DarkNavyCard,
            title = { Text("Edit Fasting Session", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Session: ${dateFormatter.format(Date(session.startTime))}", color = NeonCyan, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editTargetHours,
                        onValueChange = { editTargetHours = it },
                        label = { Text("Planned Target Hours") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Session Notes") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updatedHours = editTargetHours.toIntOrNull() ?: session.targetHours
                    viewModel.updatePastSession(
                        session.copy(targetHours = updatedHours, notes = editNotes)
                    )
                    viewModel.setEditingSession(null)
                }) {
                    Text("Save Changes", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEditingSession(null) }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- CUSTOM DATE/TIME PICKER DIALOG ---
    if (state.showCustomTimeDialog) {
        var startMillis by remember { mutableStateOf(System.currentTimeMillis()) }
        var endMillis by remember { mutableStateOf(System.currentTimeMillis() + state.targetHours * 3600000L) }

        AlertDialog(
            onDismissRequest = { viewModel.setShowCustomTimeDialog(false) },
            containerColor = DarkNavyCard,
            title = { Text("Configure Custom Times", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select independent starting and ending timestamps for your fast.", color = TextMuted, fontSize = 12.sp)

                    // Start time picker trigger
                    GlassCard(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = startMillis }
                            DatePickerDialog(context, { _, year, month, day ->
                                TimePickerDialog(context, { _, hour, min ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, day, hour, min, 0)
                                    }
                                    startMillis = picked.timeInMillis
                                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    ) {
                        Text("Start Date & Time:", fontSize = 11.sp, color = TextMuted)
                        Text(SimpleDateFormat("MMM d, yyyy  hh:mm a", Locale.US).format(Date(startMillis)), fontSize = 13.sp, color = NeonCyan)
                    }

                    // End time picker trigger
                    GlassCard(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = endMillis }
                            DatePickerDialog(context, { _, year, month, day ->
                                TimePickerDialog(context, { _, hour, min ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, day, hour, min, 0)
                                    }
                                    endMillis = picked.timeInMillis
                                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                        }
                    ) {
                        Text("Planned End Date & Time:", fontSize = 11.sp, color = TextMuted)
                        Text(SimpleDateFormat("MMM d, yyyy  hh:mm a", Locale.US).format(Date(endMillis)), fontSize = 13.sp, color = NeonElectricBlue)
                    }

                    val dur = ((endMillis - startMillis) / 3600000.0).toInt().coerceAtLeast(1)
                    Text("Calculated Duration: $dur Hours", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCustomTimes(startMillis, endMillis)
                    viewModel.setShowCustomTimeDialog(false)
                }) {
                    Text("Apply Times", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowCustomTimeDialog(false) }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

/**
 * Isolated Live Timer Widget: Recomposes on every second tick without
 * triggering recomposition of the outer LazyColumn or parent screen.
 */
@Composable
fun FastingLiveTimerWidget(
    elapsedSecondsFlow: StateFlow<Long>,
    targetHours: Int
) {
    val elapsedSeconds by elapsedSecondsFlow.collectAsState()

    val elapsedHours = elapsedSeconds / 3600
    val elapsedMinutes = (elapsedSeconds % 3600) / 60
    val elapsedSec = elapsedSeconds % 60
    val timerString = String.format(Locale.US, "%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSec)

    val targetSeconds = targetHours * 3600L
    val remainingSeconds = (targetSeconds - elapsedSeconds).coerceAtLeast(0)
    val remainingHours = remainingSeconds / 3600
    val remainingMinutes = (remainingSeconds % 3600) / 60

    val progress = (elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(500), label = "progress")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(190.dp),
                color = Color(0x2216203B),
                strokeWidth = 10.dp,
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(190.dp),
                color = if (progress >= 1f) NeonGreen else NeonCyan,
                strokeWidth = 10.dp,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timerString,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${(progress * 100).toInt()}% COMPLETED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (progress >= 1f) NeonGreen else NeonCyan
                )
                Text(
                    text = "${remainingHours}h ${remainingMinutes}m remaining",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Educational fasting stages timeline.
 */
@Composable
fun EducationalStagesList(
    elapsedSecondsFlow: StateFlow<Long>
) {
    val elapsedSeconds by elapsedSecondsFlow.collectAsState()
    val elapsedHours = elapsedSeconds / 3600.0

    val stages = listOf(
        FastingStageInfo(1, "0–4h", "Early Post-Meal Period", "Digestion and nutrient absorption occurring; blood glucose and insulin levels gradually stabilize.", elapsedHours in 0.0..<4.0, elapsedHours >= 4.0),
        FastingStageInfo(2, "4–8h", "Post-Absorptive Period", "Digestive activity completes; glycogen stored in liver is drawn upon for basal energy.", elapsedHours in 4.0..<8.0, elapsedHours >= 8.0),
        FastingStageInfo(3, "8–12h", "Nutrient Transition", "Glycogen depletion continues; baseline insulin levels allow gradual shift in energy pathways.", elapsedHours in 8.0..<12.0, elapsedHours >= 12.0),
        FastingStageInfo(4, "12–16h", "Increasing Stored Energy Use", "Gradual shift towards fatty acid mobilization as an ongoing energy substrate.", elapsedHours in 12.0..<16.0, elapsedHours >= 16.0),
        FastingStageInfo(5, "16–24h", "Further Metabolic Adaptation", "Extended transition with continued baseline insulin and cellular maintenance signaling.", elapsedHours >= 16.0, elapsedHours >= 24.0)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stages.forEach { stage ->
            val borderColor = when {
                stage.isCurrent -> NeonCyan
                stage.isCompleted -> NeonGreen.copy(alpha = 0.5f)
                else -> GlassBorder
            }

            GlassCard(
                borderColor = borderColor,
                borderWidth = if (stage.isCurrent) 1.5.dp else 1.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    stage.isCompleted -> Color(0x3300F59B)
                                    stage.isCurrent -> Color(0x3300E5FF)
                                    else -> Color(0x2216203B)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stage.stageNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                stage.isCompleted -> NeonGreen
                                stage.isCurrent -> NeonCyan
                                else -> TextMuted
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stage.rangeText}: ${stage.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stage.isCurrent) NeonCyan else TextWhite
                            )
                            if (stage.isCurrent) {
                                Text(
                                    text = "ACTIVE STAGE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                            }
                        }
                        Text(
                            text = stage.description,
                            fontSize = 10.sp,
                            color = TextMuted,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
