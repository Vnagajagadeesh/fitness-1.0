package com.jd.fitness.ui.screens.weight

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.jd.fitness.data.local.entity.WeightEntity
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.WeightLineChart
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonMagenta
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

import com.jd.fitness.ui.components.SubScreenHeader

@Composable
fun WeightScreen(
    viewModel: WeightViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = NeonPurple,
                contentColor = TextWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Weight")
            }
        },
        containerColor = DeepBlackNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SubScreenHeader(
                    title = "BODY WEIGHT TRACKER",
                    subtitle = "Precision kilogram tracking and body composition trend",
                    onBack = onBack,
                    onHome = onHome
                )
            }

            // Latest Weight Hero Card
            item {
                val latest = state.latestWeight
                GlassCard(borderColor = NeonPurple.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT WEIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (latest != null) "${String.format(Locale.US, "%.1f", latest.weightKg)} kg" else "-- kg",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = if (latest != null) "Logged on ${latest.date} at ${timeFormat.format(Date(latest.timestamp))}" else "No entries recorded yet",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        HolographicButton(
                            text = "+ Log",
                            onClick = { viewModel.openAddDialog() }
                        )
                    }
                }
            }

            // Averages Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HolographicStatBadge(
                        label = "WEEKLY AVG",
                        value = state.weeklyAverage?.let { "${String.format(Locale.US, "%.1f", it)} kg" } ?: "—",
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    HolographicStatBadge(
                        label = "MONTHLY AVG",
                        value = state.monthlyAverage?.let { "${String.format(Locale.US, "%.1f", it)} kg" } ?: "—",
                        accentColor = NeonElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    HolographicStatBadge(
                        label = "ALL-TIME AVG",
                        value = state.allTimeAverage?.let { "${String.format(Locale.US, "%.1f", it)} kg" } ?: "—",
                        accentColor = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Line Chart
            item {
                GlassCard {
                    Text(
                        text = "WEIGHT PROGRESS TREND",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    WeightLineChart(dataPoints = state.chartDataPoints)
                }
            }

            // Log Entries Header
            item {
                Text(
                    text = "ENTRY HISTORY (${state.allLogs.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 1.sp
                )
            }

            // List of Weight entries
            items(state.allLogs, key = { it.id }) { entry ->
                GlassCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", entry.weightKg)} kg",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = timeFormat.format(Date(entry.timestamp)),
                                    fontSize = 11.sp,
                                    color = NeonCyan
                                )
                            }
                            Text(
                                text = entry.date,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            if (entry.notes.isNotBlank()) {
                                Text(
                                    text = entry.notes,
                                    fontSize = 12.sp,
                                    color = TextSubtle
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { viewModel.openEditDialog(entry) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TextSubtle,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (state.showAddDialog) {
        AddEditWeightDialog(
            initialEntry = state.editingWeight,
            onDismiss = { viewModel.closeDialog() },
            onSave = { kg, notes, date -> viewModel.saveWeight(kg, notes, date) },
            onDelete = { viewModel.deleteWeight(it) }
        )
    }
}

@Composable
private fun AddEditWeightDialog(
    initialEntry: WeightEntity?,
    onDismiss: () -> Unit,
    onSave: (kg: Double, notes: String, date: String) -> Unit,
    onDelete: (WeightEntity) -> Unit
) {
    var weightText by remember { mutableStateOf(initialEntry?.weightKg?.toString() ?: "") }
    var notesText by remember { mutableStateOf(initialEntry?.notes ?: "") }
    var dateText by remember { mutableStateOf(initialEntry?.date ?: LocalDate.now().toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && initialEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DarkNavyCard,
            title = { Text("Delete Weight Entry?", color = TextWhite) },
            text = { Text("Are you sure you want to delete this weight log of ${initialEntry.weightKg} kg on ${initialEntry.date}?", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = { onDelete(initialEntry); showDeleteConfirm = false }) {
                    Text("Delete", color = NeonMagenta)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (initialEntry == null) "Log Body Weight" else "Edit Weight Entry",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextWhite
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (in kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (e.g. Morning fasted, post-workout)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialEntry != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = NeonMagenta)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Delete Entry")
                    }
                }
            }
        },
        confirmButton = {
            HolographicButton(
                text = "Save",
                enabled = (weightText.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = {
                    val kg = weightText.toDoubleOrNull() ?: return@HolographicButton
                    onSave(kg, notesText.trim(), dateText.trim())
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

