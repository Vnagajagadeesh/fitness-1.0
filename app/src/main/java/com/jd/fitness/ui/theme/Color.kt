package com.jd.fitness.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Backgrounds
val DeepBlackNavy = Color(0xFF070B14)
val DarkNavyCard = Color(0xFF0D1326)
val DarkNavySurface = Color(0xFF121B33)
val DarkNavySurfaceVariant = Color(0xFF18223E)

// Holographic & Neon Accents
val NeonElectricBlue = Color(0xFF00E5FF)
val NeonRoyalBlue = Color(0xFF0077FE)
val NeonPurple = Color(0xFF9D4EDD)
val NeonDeepPurple = Color(0xFF7928CA)
val NeonMagenta = Color(0xFFFF007F)
val NeonCyan = Color(0xFF00F5D4)
val NeonGreen = Color(0xFF00F59B)
val NeonAmber = Color(0xFFFFB703)

// Text Colors
val TextWhite = Color(0xFFF6F8FD)
val TextMuted = Color(0xFF8E9EB8)
val TextSubtle = Color(0xFF5E6E88)

// Borders & Glass
val GlassBorder = Color(0x33446699)
val GlassGlowCyan = Color(0x4400E5FF)
val GlassGlowPurple = Color(0x449D4EDD)

// Gradients
val HolographicGradient = Brush.horizontalGradient(
    colors = listOf(NeonCyan, NeonElectricBlue, NeonPurple, NeonMagenta)
)

val HolographicCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0x3300E5FF), Color(0x11121B33))
)

val HolographicButtonGradient = Brush.horizontalGradient(
    colors = listOf(NeonRoyalBlue, NeonPurple)
)

val DarkCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF141C36), Color(0xFF0D1326))
)

