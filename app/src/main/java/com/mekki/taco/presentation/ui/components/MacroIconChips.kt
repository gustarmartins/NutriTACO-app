package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mekki.taco.presentation.ui.theme.LocalNutrientColors
import java.text.DecimalFormat

/**
 * : [💪 25] [🌾 38] [💧 12]
 */
@Composable
fun MacroIconRow(
    protein: Double,
    carbs: Double,
    fat: Double,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false
) {
    val nutrientColors = LocalNutrientColors.current
    val df = DecimalFormat("#")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MacroIconChip(
            icon = Icons.Default.FitnessCenter,
            value = df.format(protein),
            color = nutrientColors.protein,
            label = if (showLabels) "Prot." else null
        )
        MacroIconChip(
            icon = Icons.Default.Grain,
            value = df.format(carbs),
            color = nutrientColors.carbs,
            label = if (showLabels) "Carb." else null
        )
        MacroIconChip(
            icon = Icons.Default.WaterDrop,
            value = df.format(fat),
            color = nutrientColors.fat,
            label = if (showLabels) "Gord." else null
        )
    }
}

/**
 * Single chip with optional label prefix.
 */
@Composable
private fun MacroIconChip(
    icon: ImageVector,
    value: String,
    color: Color,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        if (label != null) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = "${value}g",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * labeled values
 * format: P:25g  C:38g  G:12g with icons
 */
@Composable
fun DietTotalsMacroRow(
    protein: Double,
    carbs: Double,
    fat: Double,
    modifier: Modifier = Modifier
) {
    MacroIconRow(
        protein = protein,
        carbs = carbs,
        fat = fat,
        modifier = modifier,
        showLabels = true
    )
}