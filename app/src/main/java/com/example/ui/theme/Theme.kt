package com.example.ui.theme

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
    primary = StudioPurpleLight,
    onPrimary = Color.White,
    primaryContainer = StudioPurpleDark,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = StudioRoseSecondary,
    onSecondary = Color.White,
    tertiary = StudioCoralTertiary,
    onTertiary = Color.White,
    background = StudioSurfaceDark,
    surface = StudioSurfaceCardDark,
    surfaceVariant = Color(0xFF332147),
    onSurface = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFFD8B4FE)
)

private val LightColorScheme = lightColorScheme(
    primary = StudioPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = StudioPurpleDark,
    secondary = StudioRoseSecondary,
    onSecondary = Color.White,
    tertiary = StudioCoralTertiary,
    onTertiary = Color.White,
    background = StudioSurfaceLight,
    surface = Color.White,
    surfaceVariant = StudioSurfaceCardLight,
    onSurface = Color(0xFF2E1065),
    onSurfaceVariant = Color(0xFF581C87)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep our brand colors consistent for the Studio theme
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
