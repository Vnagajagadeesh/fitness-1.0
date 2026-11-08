package com.jd.fitness.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.backup.MigrationSummary
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.theme.DarkNavyCard
import com.jd.fitness.ui.theme.DarkNavySurface
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.GlassBorder
import com.jd.fitness.ui.theme.NeonAmber
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonMagenta
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextWhite

@Composable
fun BackupRestoreDialog(
    migrationSummary: MigrationSummary?,
    rawJsonToImport: String,
    onDismiss: () -> Unit,
    onAnalyze: (String) -> Unit,
    onConfirmImport: (rawJson: String, splitLegacySkincare: Boolean) -> Unit
) {
    var jsonInput by remember { mutableStateOf(rawJsonToImport) }
    var splitLegacySkincare by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (migrationSummary == null) "Import / Restore Backup" else "Review Migration Mapping",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextWhite
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (migrationSummary == null) {
                    Text(
                        text = "Paste your exported JSON backup file content below (supports native FITNESS 1.0 exports or legacy season10-data-v1 from your phone).",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        label = { Text("Backup JSON Content") },
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                } else {
                    // MIGRATION SUMMARY PREVIEW
                    Text(
                        text = "Source: ${migrationSummary.detectedSource}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "User Name: ${migrationSummary.detectedUserName ?: "JD"}",
                        fontSize = 12.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "Historical Check-ins: ${migrationSummary.totalLogsCount} logs",
                        fontSize = 12.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "Weight Records: ${migrationSummary.totalWeightEntries} entries",
                        fontSize = 12.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "Challenge Dates: ${migrationSummary.challengeStartDate} → ${migrationSummary.challengeEndDate}",
                        fontSize = 12.sp,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    migrationSummary.notices.forEach { notice ->
                        GlassCard(
                            borderColor = if (notice.contains("CRITICAL", ignoreCase = true)) NeonAmber else GlassBorder,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(text = notice, fontSize = 11.sp, color = TextWhite)
                        }
                    }

                    if (migrationSummary.isLegacyFormat && migrationSummary.legacySkincareDaysCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = splitLegacySkincare,
                                onCheckedChange = { splitLegacySkincare = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                            )
                            Column {
                                Text(
                                    text = "Split legacy skincare into separate Morning & Night check-ins",
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Default unchecked to avoid falsifying distinct routine completions.",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (migrationSummary == null) {
                HolographicButton(
                    text = "Analyze Backup",
                    enabled = jsonInput.isNotBlank(),
                    onClick = { onAnalyze(jsonInput.trim()) }
                )
            } else {
                HolographicButton(
                    text = "Confirm & Restore",
                    onClick = { onConfirmImport(jsonInput.trim(), splitLegacySkincare) }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

