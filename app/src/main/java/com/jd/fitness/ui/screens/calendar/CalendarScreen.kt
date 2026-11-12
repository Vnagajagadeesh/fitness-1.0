package com.jd.fitness.ui.screens.calendar

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicStatBadge
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DarkNavySurface
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.components.SubScreenHeader
import com.jd.fitness.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val monthTitle = state.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))

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
                title = "CHALLENGE CALENDAR",
                subtitle = "Complete historical log review and retrospective editing",
                onBack = onBack,
                onHome = onHome
            )
        }

        // Calendar Month Card
        item {
            GlassCard {
                // Month Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = NeonElectricBlue)
                    }

                    Text(
                        text = monthTitle.uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )

                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = NeonElectricBlue)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days of week header (Sun - Sat)
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (day in daysOfWeek) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid (6 rows max)
                val firstDayOfWeek = state.currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
                val daysInMonth = state.currentMonth.lengthOfMonth()
                val totalSlots = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

                for (row in 0 until (totalSlots / 7)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..6) {
                            val slotIndex = (row * 7) + col
                            val dayNumber = slotIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = state.currentMonth.atDay(dayNumber)
                                val isSelected = date == state.selectedDate
                                val isToday = date == LocalDate.now()

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> NeonElectricBlue.copy(alpha = 0.25f)
                                                isToday -> Color(0xFF1B284B)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                            color = if (isSelected) NeonElectricBlue else if (isToday) NeonCyan.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.selectDate(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNumber",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) NeonElectricBlue else if (isToday) NeonCyan else TextWhite
                                        )
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }
        }

        // Details for Selected Date
        item {
            val sum = state.selectedDaySummary
            val selectedDateStr = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US))

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CHECK-IN DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = selectedDateStr,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    HolographicStatBadge(
                        label = "SCORE",
                        value = "${(sum?.percentage ?: 0f).toInt()}%",
                        accentColor = if ((sum?.percentage ?: 0f) >= 100f) NeonGreen else NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                state.selectedDateLogs.forEach { (habit, isChecked) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleHabitForSelectedDate(habit.id, !isChecked) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleHabitForSelectedDate(habit.id, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonGreen,
                                uncheckedColor = TextSubtle,
                                checkmarkColor = DeepBlackNavy
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = habit.title,
                            fontSize = 13.sp,
                            color = if (isChecked) TextMuted else TextWhite
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
