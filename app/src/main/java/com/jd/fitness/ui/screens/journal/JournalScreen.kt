package com.jd.fitness.ui.screens.journal

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.jd.fitness.data.local.entity.JournalEntity
import com.jd.fitness.data.local.entity.JournalMood
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
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
fun JournalScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }

    var entryToDelete by remember { mutableStateOf<JournalEntity?>(null) }
    var showPromptsHelp by remember { mutableStateOf(false) }

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
                title = "MY PRIVATE JOURNAL",
                subtitle = "Encrypted-feel local reflections, insights & mood tracking",
                onBack = onBack,
                onHome = onHome,
                actionIcon = Icons.Default.Add,
                actionDescription = "New Entry",
                onAction = { viewModel.setShowAddDialog(true) }
            )
        }

        // --- STRICT PRIVACY NOTICE ---
        item {
            GlassCard(borderColor = NeonPurple.copy(alpha = 0.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = NeonPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STRICT DATA PRIVACY GUARANTEE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                        Text(
                            text = "Your journal entries are stored strictly on-device. They are NEVER sent to AI or cloud models without your explicit opt-in.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- SEARCH BAR & PROMPTS BUTTON ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search journal entries...", color = TextSubtle, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = NeonElectricBlue, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonElectricBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2216203B))
                        .clickable { showPromptsHelp = !showPromptsHelp }
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = if (showPromptsHelp) "Hide Prompts" else "Prompts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }

        // --- DAILY REFLECTION PROMPTS CARD ---
        if (showPromptsHelp) {
            item {
                GlassCard(borderColor = NeonCyan.copy(alpha = 0.5f)) {
                    Text("DAILY REFLECTION FRAMEWORK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf(
                        "1. What did I accomplish today?",
                        "2. What went exceptionally well?",
                        "3. What distracted me or drained my momentum?",
                        "4. What single thing can I improve tomorrow?",
                        "5. What am I genuinely grateful for?"
                    ).forEach { prompt ->
                        Text(prompt, fontSize = 11.sp, color = TextWhite, lineHeight = 16.sp)
                    }
                }
            }
        }

        // --- JOURNAL ENTRIES FEED ---
        item {
            Text(
                text = if (state.isSearching) "SEARCH RESULTS (${state.entries.size})" else "YOUR JOURNAL ENTRIES (${state.entries.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
        }

        if (state.entries.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = if (state.isSearching) "No journal entries match your search." else "No entries logged yet. Tap '+ New Entry' to capture your daily thoughts.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(state.entries) { entry ->
                val moodColor = when (entry.mood) {
                    JournalMood.GREAT.name -> NeonGreen
                    JournalMood.GOOD.name -> NeonCyan
                    JournalMood.NEUTRAL.name -> NeonElectricBlue
                    JournalMood.TIRED.name -> NeonAmber
                    JournalMood.STRESSED.name -> Color(0xFFFF5555)
                    else -> NeonCyan
                }

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(moodColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = entry.mood,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = moodColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.date,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = timeFormatter.format(Date(entry.timestamp)),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { viewModel.setEditingEntry(entry) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Edit, "Edit", tint = TextMuted, modifier = Modifier.size(15.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { entryToDelete = entry }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Delete, "Delete", tint = TextMuted, modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    if (entry.title.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    if (entry.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.content,
                            fontSize = 13.sp,
                            color = Color(0xFFD0D7DE),
                            lineHeight = 18.sp
                        )
                    }

                    if (entry.accomplishments.isNotBlank() || entry.challenges.isNotBlank() || entry.tomorrowGoals.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (entry.accomplishments.isNotBlank()) {
                                Text("• Accomplished: ${entry.accomplishments}", fontSize = 11.sp, color = NeonGreen)
                            }
                            if (entry.challenges.isNotBlank()) {
                                Text("• Challenge: ${entry.challenges}", fontSize = 11.sp, color = NeonAmber)
                            }
                            if (entry.tomorrowGoals.isNotBlank()) {
                                Text("• Tomorrow's Target: ${entry.tomorrowGoals}", fontSize = 11.sp, color = NeonCyan)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- ADD / EDIT ENTRY DIALOG ---
    if (state.showAddEditDialog) {
        val editing = state.editingEntry
        var title by remember { mutableStateOf(editing?.title ?: "") }
        var content by remember { mutableStateOf(editing?.content ?: "") }
        var mood by remember { mutableStateOf(editing?.mood?.let { runCatching { JournalMood.valueOf(it) }.getOrNull() } ?: JournalMood.GOOD) }
        var accomplishments by remember { mutableStateOf(editing?.accomplishments ?: "") }
        var challenges by remember { mutableStateOf(editing?.challenges ?: "") }
        var lessons by remember { mutableStateOf(editing?.lessonsLearned ?: "") }
        var tomorrowGoals by remember { mutableStateOf(editing?.tomorrowGoals ?: "") }
        var allowAi by remember { mutableStateOf(editing?.allowAiInsights ?: false) }

        AlertDialog(
            onDismissRequest = { viewModel.setShowAddDialog(false) },
            containerColor = DarkNavyCard,
            title = { Text(if (editing == null) "New Journal Reflection" else "Edit Entry", color = TextWhite) },
            text = {
                LazyColumn(
                    modifier = Modifier.height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title / Summary") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonElectricBlue,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    item {
                        Text("Mood:", fontSize = 11.sp, color = TextMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(JournalMood.values()) { m ->
                                val isSel = mood == m
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) NeonCyan else Color(0x2216203B))
                                        .clickable { mood = m }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = m.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) DeepBlackNavy else TextWhite
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Reflection / Free-text") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonElectricBlue,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = accomplishments,
                            onValueChange = { accomplishments = it },
                            label = { Text("What did I accomplish?") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = challenges,
                            onValueChange = { challenges = it },
                            label = { Text("Challenges faced / Distractions") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonAmber,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tomorrowGoals,
                            onValueChange = { tomorrowGoals = it },
                            label = { Text("Tomorrow's primary goal") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Allow AI Coach Insights from this entry", fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1f))
                            Switch(
                                checked = allowAi,
                                onCheckedChange = { allowAi = it },
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
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        viewModel.saveEntry(title, content, mood, accomplishments, challenges, lessons, tomorrowGoals, allowAi)
                    }
                }) {
                    Text("Save", color = NeonCyan)
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
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            containerColor = DarkNavyCard,
            title = { Text("Delete Journal Entry", color = TextWhite) },
            text = { Text("Are you sure you want to delete this private reflection? This cannot be undone.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    entryToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF5555))
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

