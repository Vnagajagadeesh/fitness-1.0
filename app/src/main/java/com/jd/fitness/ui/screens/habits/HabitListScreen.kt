package com.jd.fitness.ui.screens.habits

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.components.NeonProgressBar
import com.jd.fitness.ui.theme.DarkNavySurface
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
import com.jd.fitness.ui.theme.HolographicButtonGradient
import com.jd.fitness.ui.theme.NeonAmber
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite

@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    onNavigateToSkincare: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val filteredItems = state.habitItems.filter { item ->
        state.selectedCategory == null || item.habit.category == state.selectedCategory
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddHabit() },
                containerColor = NeonElectricBlue,
                contentColor = DeepBlackNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = DeepBlackNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // --- DATE SELECTOR BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.previousDay() }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = NeonElectricBlue)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.isToday) "TODAY" else state.selectedDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isToday) NeonCyan else TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = state.selectedDateDisplay,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    IconButton(onClick = { viewModel.nextDay() }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day", tint = NeonElectricBlue)
                    }
                }
            }

            // Daily Score Banner
            item {
                val sum = state.daySummary
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY SCORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSubtle
                            )
                            Text(
                                text = "${sum?.completedHabits ?: 0} / ${sum?.totalHabits ?: 13} Habits",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                        }
                        HolographicStatBadge(
                            label = "CONSISTENCY",
                            value = "${(sum?.percentage ?: 0f).toInt()}%",
                            accentColor = if ((sum?.percentage ?: 0f) >= 100f) NeonGreen else NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    NeonProgressBar(progress = (sum?.percentage ?: 0f) / 100f, height = 6.dp)
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    item {
                        val isAllSelected = state.selectedCategory == null
                        CategoryChip(
                            title = "All",
                            isSelected = isAllSelected,
                            onClick = { viewModel.setCategoryFilter(null) }
                        )
                    }
                    items(HabitCategory.values().toList()) { category ->
                        val isSelected = state.selectedCategory == category
                        CategoryChip(
                            title = category.getDisplayName(),
                            isSelected = isSelected,
                            onClick = { viewModel.setCategoryFilter(category) }
                        )
                    }
                }
            }

            // Checklist Items
            items(filteredItems, key = { it.habit.id }) { item ->
                val habit = item.habit
                val isChecked = item.isCompleted

                GlassCard(
                    borderColor = if (isChecked) NeonGreen.copy(alpha = 0.4f) else GlassBorder,
                    borderWidth = if (isChecked) 1.2.dp else 0.8.dp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    onClick = { viewModel.toggleHabit(habit.id, !isChecked) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleHabit(habit.id, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonGreen,
                                uncheckedColor = TextSubtle,
                                checkmarkColor = DeepBlackNavy
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = habit.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (isChecked) TextMuted else TextWhite
                                )
                                if (habit.isPrivate) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Private",
                                        tint = NeonPurple,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            if (habit.description.isNotBlank()) {
                                Text(
                                    text = habit.description,
                                    fontSize = 12.sp,
                                    color = TextSubtle
                                )
                            }

                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.category.getDisplayName().uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (habit.category) {
                                        HabitCategory.PERSONAL_CARE -> NeonCyan
                                        HabitCategory.STUDY -> NeonAmber
                                        HabitCategory.FITNESS -> NeonGreen
                                        HabitCategory.NUTRITION -> NeonElectricBlue
                                        HabitCategory.MINDSET -> NeonPurple
                                        HabitCategory.CUSTOM -> TextMuted
                                        else -> TextMuted
                                    }
                                )
                                if (habit.isSkincareSpecific) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• OPEN ROUTINE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonPurple,
                                        modifier = Modifier.clickable { onNavigateToSkincare() }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.openEditHabit(habit) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Habit",
                                tint = TextSubtle,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (state.showAddEditDialog) {
        HabitEditDialog(
            initialHabit = state.editingHabit,
            onDismiss = { viewModel.closeDialog() },
            onSave = { title, cat, desc, isPriv -> viewModel.saveHabit(title, cat, desc, isPriv) },
            onDelete = { viewModel.deleteHabit(it) }
        )
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonElectricBlue.copy(alpha = 0.2f) else DarkNavySurface)
            .border(1.dp, if (isSelected) NeonElectricBlue else GlassBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) NeonElectricBlue else TextMuted
        )
    }
}
