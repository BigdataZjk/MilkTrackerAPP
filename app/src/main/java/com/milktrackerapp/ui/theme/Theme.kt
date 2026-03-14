package com.milktrackerapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Accent,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    primaryContainer = Color(0x336366F1),
    secondaryContainer = Color(0x3310B981),
    tertiaryContainer = Color(0x33F59E0B),
    onPrimaryContainer = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Accent,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onTertiary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    primaryContainer = PrimaryLight,
    secondaryContainer = SecondaryLight,
    tertiaryContainer = AccentLight,
    onPrimaryContainer = OnBackground,
    onSecondaryContainer = OnBackground,
    onTertiaryContainer = OnBackground
)

@Composable
fun MilkTrackerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}