package com.jd.fitness.ui.screens.planner

import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import com.jd.fitness.data.local.entity.TaskCategory
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
import com.jd.fitness.ui.theme.NeonMagenta
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    val today = remember { LocalDate.now().format(dateFormatter) }
    val tomorrow = remember { LocalDate.now().plusDays(1).format(dateFormatter) }

    var taskToDelete by remember { mutableStateOf<PlannerTaskEntity?>(null) }

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
                title = "MY DAILY PLAN",
                subtitle = "Custom schedule, agenda & discipline timeline",
                onBack = onBack,
                onHome = onHome,
                actionIcon = Icons.Default.Add,
                actionDescription = "Add Task",
                onAction = { viewModel.setShowAddDialog(true) }
            )
        }

        // --- DATE TABS SELECTOR ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Pair("Today", today),
                    Pair("Tomorrow", tomorrow)
                ).forEach { (label, date) ->
                    val isSelected = state.selectedDate == date
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        borderColor = if (isSelected) NeonCyan else GlassBorder,
                        borderWidth = if (isSelected) 1.5.dp else 1.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
                        onClick = { viewModel.setSelectedDate(date) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyan else TextWhite
                        )
                        Text(
                            text = date,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // --- PROGRESS OVERVIEW & QUICK ACTIONS ---
        item {
            GlassCard(borderColor = NeonElectricBlue.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AGENDA PROGRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${state.completedCount} of ${state.totalCount} Tasks Done",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }
                    HolographicStatBadge(
                        label = "COMPLETION",
                        value = "${state.completionPercentage.toInt()}%",
                        accentColor = if (state.completionPercentage >= 100f) NeonGreen else NeonElectricBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeonProgressBar(progress = state.completionPercentage / 100f, height = 7.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HolographicButton(
                        text = "Smart Suggest",
                        icon = Icons.Default.AutoAwesome,
                        gradient = listOf(NeonPurple, NeonElectricBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.generateIntelligentSchedule()
                    }

                    HolographicButton(
                        text = "Add Task",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.setShowAddDialog(true)
                    }
                }
            }
        }

        // --- TASKS TIMELINE LIST ---
        item {
            Text(
                text = "TODAY'S SCHEDULED TIMELINE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
        }

        if (state.tasks.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = "No tasks planned for this date yet. Tap 'Smart Suggest' to populate an optimized schedule based on your habits.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(state.tasks) { task ->
                val categoryColor = when (task.category) {
                    TaskCategory.MORNING_ROUTINE.name -> NeonCyan
                    TaskCategory.STUDY.name -> NeonPurple
                    TaskCategory.FITNESS.name -> NeonGreen
                    TaskCategory.SKINCARE.name -> NeonMagenta
                    TaskCategory.MEALS.name -> NeonAmber
                    TaskCategory.EVENING_ROUTINE.name -> NeonElectricBlue
                    TaskCategory.SLEEP.name -> Color(0xFF6C5CE7)
                    else -> NeonElectricBlue
                }

                GlassCard(
                    borderColor = if (task.isCompleted) NeonGreen.copy(alpha = 0.5f) else GlassBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTask(task.id, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonGreen,
                                uncheckedColor = TextSubtle,
                                checkmarkColor = DeepBlackNavy
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(categoryColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.category.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${task.startTime} – ${task.endTime}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                color = if (task.isCompleted) TextMuted else TextWhite
                            )
                            if (task.notes.isNotBlank()) {
                                Text(
                                    text = task.notes,
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setEditingTask(task) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { taskToDelete = task },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- ADD / EDIT TASK DIALOG ---
    if (state.showAddDialog) {
        val editing = state.editingTask
        var title by remember { mutableStateOf(editing?.title ?: "") }
        var category by remember { mutableStateOf(editing?.category?.let { runCatching { TaskCategory.valueOf(it) }.getOrNull() } ?: TaskCategory.STUDY) }
        var startTime by remember { mutableStateOf(editing?.startTime ?: "09:00") }
        var endTime by remember { mutableStateOf(editing?.endTime ?: "10:00") }
        var priority by remember { mutableStateOf(editing?.priority ?: 2) }
        var notes by remember { mutableStateOf(editing?.notes ?: "") }

        AlertDialog(
            onDismissRequest = { viewModel.setShowAddDialog(false) },
            containerColor = DarkNavyCard,
            title = { Text(if (editing == null) "Add Daily Task" else "Edit Task", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Text("Category:", fontSize = 11.sp, color = TextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TaskCategory.values()) { cat ->
                            val isSel = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonCyan else Color(0x2216203B))
                                    .clickable { category = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat.name.replace("_", " "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) DeepBlackNavy else TextWhite
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonElectricBlue,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonElectricBlue,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
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
                    if (title.isNotBlank()) {
                        viewModel.saveTask(title, category, startTime, endTime, priority, notes)
                    }
                }) {
                    Text("Save Task", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAddDialog(false) }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = DarkNavyCard,
            title = { Text("Delete Task", color = TextWhite) },
            text = { Text("Delete \"${task.title}\"?", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    taskToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF5555))
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

