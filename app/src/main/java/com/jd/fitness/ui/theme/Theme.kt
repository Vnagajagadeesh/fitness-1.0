package com.jd.fitness.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonElectricBlue,
    secondary = NeonPurple,
    tertiary = NeonCyan,
    background = DeepBlackNavy,
    surface = DarkNavySurface,
    surfaceVariant = DarkNavySurfaceVariant,
    onPrimary = DeepBlackNavy,
    onSecondary = TextWhite,
    onTertiary = DeepBlackNavy,
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = GlassBorder
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun FitnessTheme(
    darkTheme: Boolean = true, // Premium holographic dark theme is primary
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepBlackNavy.toArgb()
                window.navigationBarColor = DeepBlackNavy.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

