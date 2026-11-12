package com.jd.fitness.ui.screens.skincare

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
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

import com.jd.fitness.ui.components.SubScreenHeader

@Composable
fun SkincareScreen(
    viewModel: SkincareViewModel,
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
                title = "SKINCARE CONSISTENCY",
                subtitle = "Independent morning protection and night cellular recovery",
                onBack = onBack,
                onHome = onHome
            )
        }

        // --- MORNING ROUTINE CARD ---
        item {
            val isMorningDone = state.morningLog?.isCompleted == true
            RoutineCard(
                title = "MORNING ROUTINE",
                subtitle = "Cleanser • Moisturizer • Sunscreen (SPF 50+)",
                icon = Icons.Default.WbSunny,
                iconTint = NeonAmber,
                isCompleted = isMorningDone,
                onToggleCompleted = { viewModel.toggleRoutine(SkincareRoutineType.MORNING, it) },
                steps = state.morningSteps,
                onAddStep = { viewModel.openAddStep(SkincareRoutineType.MORNING) },
                onEditStep = { viewModel.openEditStep(it) }
            )
        }

        // --- NIGHT ROUTINE CARD ---
        item {
            val isNightDone = state.nightLog?.isCompleted == true
            RoutineCard(
                title = "NIGHT ROUTINE",
                subtitle = "Face Wash • Hydration • Active Treatment / Serum",
                icon = Icons.Default.NightlightRound,
                iconTint = NeonPurple,
                isCompleted = isNightDone,
                onToggleCompleted = { viewModel.toggleRoutine(SkincareRoutineType.NIGHT, it) },
                steps = state.nightSteps,
                onAddStep = { viewModel.openAddStep(SkincareRoutineType.NIGHT) },
                onEditStep = { viewModel.openEditStep(it) }
            )
        }

        // Safety & Guidance Note
        item {
            GlassCard {
                Text(
                    text = "DISCIPLINE STANDARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Morning and night skincare are tracked independently in your 13 daily habits. Never automatically marked completed: deliberate daily action builds authentic discipline.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (state.showEditStepDialog && state.editingStep != null) {
        EditStepDialog(
            step = state.editingStep!!,
            onDismiss = { viewModel.closeDialog() },
            onSave = { title, product -> viewModel.saveStep(title, product) },
            onDelete = { viewModel.deleteStep(it) }
        )
    }
}

@Composable
private fun RoutineCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isCompleted: Boolean,
    onToggleCompleted: (Boolean) -> Unit,
    steps: List<SkincareRoutineEntity>,
    onAddStep: () -> Unit,
    onEditStep: (SkincareRoutineEntity) -> Unit
) {
    GlassCard(
        borderColor = if (isCompleted) NeonGreen.copy(alpha = 0.5f) else GlassBorder,
        borderWidth = if (isCompleted) 1.5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
                }
            }

            Checkbox(
                checked = isCompleted,
                onCheckedChange = onToggleCompleted,
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonGreen,
                    uncheckedColor = TextSubtle,
                    checkmarkColor = DeepBlackNavy
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "ROUTINE STEPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
        Spacer(modifier = Modifier.height(6.dp))

        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.width(20.dp)
                    )
                    Column {
                        Text(text = step.stepTitle, fontSize = 14.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                        if (step.defaultProduct.isNotBlank()) {
                            Text(text = step.defaultProduct, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                IconButton(onClick = { onEditStep(step) }, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Step", tint = TextSubtle, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddStep() }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonElectricBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Add Custom Step", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeonElectricBlue)
        }
    }
}

@Composable
private fun EditStepDialog(
    step: SkincareRoutineEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, product: String) -> Unit,
    onDelete: (SkincareRoutineEntity) -> Unit
) {
    var title by remember { mutableStateOf(step.stepTitle) }
    var product by remember { mutableStateOf(step.defaultProduct) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Edit Skincare Step", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Step Name (e.g. Toner, Serum)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonElectricBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Product / Brand Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonElectricBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (step.id > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { onDelete(step) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = NeonMagenta)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Step", color = NeonMagenta)
                    }
                }
            }
        },
        confirmButton = {
            HolographicButton(
                text = "Save",
                enabled = title.isNotBlank(),
                onClick = { onSave(title.trim(), product.trim()) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

