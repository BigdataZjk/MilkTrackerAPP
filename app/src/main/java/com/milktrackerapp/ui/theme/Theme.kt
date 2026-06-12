package com.milktrackerapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Scheme = lightColorScheme(
    primary = Navy,
    onPrimary = Tile,
    primaryContainer = NavyMuted,
    onPrimaryContainer = Ink,
    secondary = Frost,
    onSecondary = Ink,
    secondaryContainer = Frost,
    onSecondaryContainer = Ink,
    background = Page,
    onBackground = Ink,
    surface = Tile,
    onSurface = Ink,
    surfaceVariant = Frost,
    onSurfaceVariant = Slate,
    outline = Rule,
    outlineVariant = Rule,
    error = Danger,
    onError = Tile
)

@Composable
fun MilkTrackerAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            WindowCompat.getInsetsController(
                (view.context as Activity).window, view
            ).isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = Scheme,
        typography = MilkTrackerTypography,
        content = content
    )
}
