package com.jd.fitness.ui.screens.motivation

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.data.local.entity.MotivationCategory
import com.jd.fitness.ui.components.GlassCard
import com.jd.fitness.ui.components.HolographicButton
import com.jd.fitness.ui.components.HolographicStatBadge
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

@Composable
fun MotivationScreen(
    viewModel: MotivationViewModel,
    onBack: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filteredQuotes = state.quotes.filter {
        state.selectedCategory == "ALL" || it.category == state.selectedCategory
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
                title = "MOTIVATION HUB",
                subtitle = "Daily discipline principles, challenges & focus",
                onBack = onBack,
                onHome = onHome,
                actionIcon = Icons.Default.Add,
                actionDescription = "Add Principle",
                onAction = { viewModel.setShowAddQuoteDialog(true) }
            )
        }

        // --- HERO DAILY QUOTE CARD ---
        state.todayQuote?.let { quote ->
            item {
                GlassCard(borderColor = NeonAmber.copy(alpha = 0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HolographicStatBadge(
                            label = "CATEGORY",
                            value = quote.category.replace("_", " "),
                            accentColor = NeonAmber
                        )
                        Row {
                            IconButton(onClick = { viewModel.toggleFavorite(quote.id, !quote.isFavorite) }) {
                                Icon(
                                    imageVector = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (quote.isFavorite) Color(0xFFFF4D4D) else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.shareQuote(context, quote.quote, quote.category) }) {
                                Icon(Icons.Default.Share, "Share", tint = NeonElectricBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "\"${quote.quote}\"",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = TextWhite,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // --- ADAPTIVE PROGRESS ENCOURAGEMENT ---
        if (state.adaptiveEncouragement.isNotBlank()) {
            item {
                GlassCard(borderColor = NeonCyan.copy(alpha = 0.5f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Energy",
                            tint = NeonCyan,
                            modifier = Modifier.padding(top = 2.dp, end = 10.dp).size(22.dp)
                        )
                        Column {
                            Text(
                                text = "ADAPTIVE PROGRESS INSIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.adaptiveEncouragement,
                                fontSize = 13.sp,
                                color = TextWhite,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // --- TODAY'S DISCIPLINE CHALLENGE ---
        if (state.dailyDisciplineChallenge.isNotBlank()) {
            item {
                GlassCard(borderColor = NeonPurple.copy(alpha = 0.5f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Challenge",
                            tint = NeonMagenta,
                            modifier = Modifier.padding(top = 2.dp, end = 10.dp).size(22.dp)
                        )
                        Column {
                            Text(
                                text = "TODAY'S DISCIPLINE CHALLENGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonMagenta,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.dailyDisciplineChallenge,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // --- CATEGORY FILTERS ---
        item {
            val categories = listOf("ALL") + MotivationCategory.values().map { it.name }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = state.selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonElectricBlue else DarkNavyCard)
                            .clickable { viewModel.setSelectedCategory(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DeepBlackNavy else TextWhite
                        )
                    }
                }
            }
        }

        // --- PRINCIPLES VAULT LIST ---
        item {
            Text(
                text = "DISCIPLINE VAULT (${filteredQuotes.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
        }

        items(filteredQuotes) { quote ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x2216203B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = quote.category.replace("_", " "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(quote.id, !quote.isFavorite) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (quote.isFavorite) Color(0xFFFF4D4D) else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.shareQuote(context, quote.quote, quote.category) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Share, "Share", tint = NeonElectricBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${quote.quote}\"",
                    fontSize = 13.sp,
                    color = TextWhite,
                    lineHeight = 18.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- ADD CUSTOM PRINCIPLE DIALOG ---
    if (state.showAddQuoteDialog) {
        var newQuoteText by remember { mutableStateOf("") }
        var chosenCategory by remember { mutableStateOf(MotivationCategory.DISCIPLINE) }

        AlertDialog(
            onDismissRequest = { viewModel.setShowAddQuoteDialog(false) },
            containerColor = DarkNavyCard,
            title = { Text("Add Personal Principle", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newQuoteText,
                        onValueChange = { newQuoteText = it },
                        label = { Text("Quote / Principle") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonElectricBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Text("Category:", fontSize = 11.sp, color = TextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MotivationCategory.values()) { cat ->
                            val isSel = chosenCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonCyan else Color(0x2216203B))
                                    .clickable { chosenCategory = cat }
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newQuoteText.isNotBlank()) {
                        viewModel.addCustomQuote(newQuoteText, chosenCategory)
                    }
                }) {
                    Text("Save Principle", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAddQuoteDialog(false) }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

