package com.mekki.taco.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object PrimaryPalette {
    val Tone10 = Color(0xFF21005D)
    val Tone20 = Color(0xFF381E72)
    val Tone30 = Color(0xFF4F378B)
    val Tone40 = Color(0xFF6750A4)
    val Tone80 = Color(0xFFD0BCFF)
    val Tone90 = Color(0xFFEADDFF)
}

object TertiaryPalette {
    val Tone30 = Color(0xFF633B48)
    val Tone40 = Color(0xFF7D5260)
    val Tone80 = Color(0xFFEFB8C8)
    val Tone90 = Color(0xFFFFD8E4)
}

object SecondaryPalette {
    val Tone30 = Color(0xFF4A4458)
    val Tone40 = Color(0xFF625B71)
    val Tone80 = Color(0xFFCCC2DC)
    val Tone90 = Color(0xFFE8DEF8)
}

object NeutralPalette {
    val Tone6 = Color(0xFF121212)
    val Tone10 = Color(0xFF1D1B20)
    val Tone12 = Color(0xFF1E1E24)
    val Tone90 = Color(0xFFE6E1E5)
    val Tone98 = Color(0xFFFDF8FD)
    val Tone100 = Color(0xFFFFFFFF)
}

object NeutralVariantPalette {
    val Tone30 = Color(0xFF49454F)
    val Tone90 = Color(0xFFE7E0EC)
}

@Immutable
data class NutrientColors(
    val protein: Color,
    val carbs: Color,
    val fat: Color,
    val fiber: Color,
    val cholesterol: Color,
    val sodium: Color
)

val DarkNutrientColors = NutrientColors(
    protein = Color(0xFFFFD54F),
    carbs = Color(0xFF4DD0E1),
    fat = Color(0xFFBCAAA4),
    fiber = Color(0xFFA5D6A7),
    cholesterol = Color(0xFFF48FB1),
    sodium = Color(0xFF81D4FA)
)

val LightNutrientColors = NutrientColors(
    protein = Color(0xFFFF8F00),
    carbs = Color(0xFF00838F),
    fat = Color(0xFF5D4037),
    fiber = Color(0xFF388E3C),
    cholesterol = Color(0xFFC2185B),
    sodium = Color(0xFF0288D1)
)

val LocalNutrientColors = staticCompositionLocalOf { LightNutrientColors }