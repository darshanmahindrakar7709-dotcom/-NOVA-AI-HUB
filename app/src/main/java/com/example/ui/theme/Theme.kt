package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NovaViolet,
    onPrimary = Color.White,
    primaryContainer = NovaVioletDark,
    onPrimaryContainer = Color.White,
    secondary = NovaCyan,
    onSecondary = Color.Black,
    secondaryContainer = NovaCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = NovaPink,
    onTertiary = Color.White,
    background = SpaceBackground,
    onBackground = TextPrimaryDark,
    surface = SpaceSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SpaceSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = SpaceSurfaceBorder,
    outlineVariant = SpaceSurfaceBorder,
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NovaVioletDark,
    onPrimary = Color.White,
    primaryContainer = NovaVioletLight,
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = NovaCyanDark,
    onSecondary = Color.White,
    secondaryContainer = NovaCyanLight,
    onSecondaryContainer = Color(0xFF083344),
    tertiary = NovaPink,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightSurfaceBorder,
    outlineVariant = LightSurfaceBorder,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun NovaTheme(
    darkTheme: Boolean = true, // Dark by default per NOVA design specs
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
