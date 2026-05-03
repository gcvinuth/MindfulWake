package com.mindfulwake.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// === MD3 Color Palette (Deep Purple / Teal seed - matches MindfulWake screenshots) ===
val md_theme_dark_primary = Color(0xFF9B8FFF)           // Soft lavender-blue
val md_theme_dark_onPrimary = Color(0xFF1A0070)
val md_theme_dark_primaryContainer = Color(0xFF2D0096)
val md_theme_dark_onPrimaryContainer = Color(0xFFE2DAFF)
val md_theme_dark_secondary = Color(0xFF5ECBBD)         // Teal accent
val md_theme_dark_onSecondary = Color(0xFF003731)
val md_theme_dark_secondaryContainer = Color(0xFF005048)
val md_theme_dark_onSecondaryContainer = Color(0xFF79E8D9)
val md_theme_dark_tertiary = Color(0xFFBD96FF)
val md_theme_dark_onTertiary = Color(0xFF350067)
val md_theme_dark_tertiaryContainer = Color(0xFF4D0090)
val md_theme_dark_onTertiaryContainer = Color(0xFFE8D9FF)
val md_theme_dark_error = Color(0xFFFF8A80)
val md_theme_dark_onError = Color(0xFF690001)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF0E0E1A)        // Deep navy-black
val md_theme_dark_onBackground = Color(0xFFE5E1F0)
val md_theme_dark_surface = Color(0xFF13131F)
val md_theme_dark_onSurface = Color(0xFFE5E1F0)
val md_theme_dark_surfaceVariant = Color(0xFF1E1E30)
val md_theme_dark_onSurfaceVariant = Color(0xFFC8C3DC)
val md_theme_dark_outline = Color(0xFF928DAD)
val md_theme_dark_outlineVariant = Color(0xFF322D4A)
val md_theme_dark_inverseSurface = Color(0xFFE5E1F0)
val md_theme_dark_inverseOnSurface = Color(0xFF1E1B2E)
val md_theme_dark_inversePrimary = Color(0xFF5B00CF)
val md_theme_dark_surfaceTint = Color(0xFF9B8FFF)
val md_theme_dark_surfaceContainerLowest = Color(0xFF090914)
val md_theme_dark_surfaceContainerLow = Color(0xFF181826)
val md_theme_dark_surfaceContainer = Color(0xFF1C1C2E)
val md_theme_dark_surfaceContainerHigh = Color(0xFF232336)
val md_theme_dark_surfaceContainerHighest = Color(0xFF2D2D42)

// Light theme for completeness
val md_theme_light_primary = Color(0xFF5B00CF)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFE2DAFF)
val md_theme_light_onPrimaryContainer = Color(0xFF1A0070)
val md_theme_light_secondary = Color(0xFF006B60)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFF79E8D9)
val md_theme_light_onSecondaryContainer = Color(0xFF00201C)
val md_theme_light_tertiary = Color(0xFF6B00BA)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFE8D9FF)
val md_theme_light_onTertiaryContainer = Color(0xFF240049)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFEF7FF)
val md_theme_light_onBackground = Color(0xFF1D1B23)
val md_theme_light_surface = Color(0xFFFEF7FF)
val md_theme_light_onSurface = Color(0xFF1D1B23)
val md_theme_light_surfaceVariant = Color(0xFFE8E0F0)
val md_theme_light_onSurfaceVariant = Color(0xFF4A4556)
val md_theme_light_outline = Color(0xFF7B7487)
val md_theme_light_outlineVariant = Color(0xFFCCC4D4)
val md_theme_light_inverseSurface = Color(0xFF323039)
val md_theme_light_inverseOnSurface = Color(0xFFF5EEFF)
val md_theme_light_inversePrimary = Color(0xFF9B8FFF)
val md_theme_light_surfaceTint = Color(0xFF5B00CF)
val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFF8F0FF)
val md_theme_light_surfaceContainer = Color(0xFFF2EAFB)
val md_theme_light_surfaceContainerHigh = Color(0xFFECE4F5)
val md_theme_light_surfaceContainerHighest = Color(0xFFE6DFEF)

// Liquid Glass accent for blurred surfaces
val LiquidGlassLight = Color(0x40FFFFFF)
val LiquidGlassDark = Color(0x25FFFFFF)
val LiquidGlassBorder = Color(0x30FFFFFF)
val TealGlow = Color(0xFF005F6B)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
)

@Composable
fun MindfulWakeTheme(
    darkTheme: Boolean = true, // Default dark per design
    dynamicColor: Boolean = true,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MindfulWakeTypography,
        shapes = MindfulWakeShapes,
        content = content
    )
}