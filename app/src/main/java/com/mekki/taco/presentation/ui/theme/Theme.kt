package com.mekki.taco.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPalette.Tone80,
    onPrimary = PrimaryPalette.Tone20,
    primaryContainer = PrimaryPalette.Tone30,
    onPrimaryContainer = PrimaryPalette.Tone90,

    secondary = SecondaryPalette.Tone80,
    onSecondary = SecondaryPalette.Tone30,
    secondaryContainer = SecondaryPalette.Tone30,
    onSecondaryContainer = SecondaryPalette.Tone90,

    tertiary = TertiaryPalette.Tone80,
    onTertiary = TertiaryPalette.Tone30,
    tertiaryContainer = TertiaryPalette.Tone30,
    onTertiaryContainer = TertiaryPalette.Tone90,

    surface = NeutralPalette.Tone6,
    surfaceContainer = NeutralPalette.Tone12,
    surfaceContainerLow = NeutralPalette.Tone6,
    surfaceContainerHigh = NeutralVariantPalette.Tone30,
    surfaceVariant = NeutralVariantPalette.Tone30,
    onSurface = NeutralPalette.Tone90,
    onSurfaceVariant = NeutralPalette.Tone90,

    background = NeutralPalette.Tone6,
    onBackground = NeutralPalette.Tone90,

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPalette.Tone40,
    onPrimary = NeutralPalette.Tone100,
    primaryContainer = PrimaryPalette.Tone90,
    onPrimaryContainer = PrimaryPalette.Tone10,

    secondary = SecondaryPalette.Tone40,
    onSecondary = NeutralPalette.Tone100,
    secondaryContainer = SecondaryPalette.Tone90,
    onSecondaryContainer = SecondaryPalette.Tone30,

    tertiary = TertiaryPalette.Tone40,
    onTertiary = NeutralPalette.Tone100,
    tertiaryContainer = TertiaryPalette.Tone90,
    onTertiaryContainer = TertiaryPalette.Tone30,

    surface = NeutralPalette.Tone98,
    surfaceContainer = NeutralPalette.Tone100,
    surfaceContainerLow = NeutralPalette.Tone98,
    surfaceContainerHigh = NeutralVariantPalette.Tone90,
    surfaceVariant = NeutralVariantPalette.Tone90,
    onSurface = NeutralPalette.Tone10,
    onSurfaceVariant = NeutralPalette.Tone10,

    background = NeutralPalette.Tone98,
    onBackground = NeutralPalette.Tone10,

    error = Color(0xFFB3261E),
    onError = NeutralPalette.Tone100
)

@Composable
fun NutriTACOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // available on Android 12+
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

    val nutrientColors = if (darkTheme) DarkNutrientColors else LightNutrientColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalNutrientColors provides nutrientColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
