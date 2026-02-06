package com.mekki.taco.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

/**
 * Found myself having to deal with UI issues across different devices very often
 * We attempt to take into account font scaling AND density changes (display size / zoom).
 *
 * @param baselineDensityDp takes the density (dpi/160) where screen still looks good
 *   440 dpi = 2.75f
 *   480 dpi = 3.0f
 *   540 dpi = 3.375f
 */
@Composable
fun rememberEffectiveScale(baselineDensityDp: Float = 2.75f): Float {
    val density = LocalDensity.current
    return remember(density.density, density.fontScale) {
        (density.density / baselineDensityDp) * density.fontScale
    }
}