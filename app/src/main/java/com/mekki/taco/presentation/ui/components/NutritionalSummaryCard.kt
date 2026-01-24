package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food

private val COLOR_KCAL = Color(0xFFA83C3C)

@Composable
fun NutritionalSummaryCard(
    food: Food,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    label: String? = null
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- HEADER (Label + Expand Icon) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Spacer(Modifier.height(0.dp))
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- COLLAPSED VIEW (Always Visible: Calories + Macros) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories Card
                VerticalNutrientCard(
                    label = "Calorias",
                    value = food.energiaKcal,
                    unit = "kcal",
                    color = COLOR_KCAL,
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f)
                )

                // Macros (using the shared component, but integrated into the row logic via weight)
                // DynamicMacroGrid expects a modifier. We give it weight(3f) to take up the rest of the space.
                // However, DynamicMacroGrid creates its own Row.
                // Nesting Rows might cause spacing issues if we want them all to look like equal columns.
                // DynamicMacroGrid uses spacedBy(12.dp).
                // Our parent Row uses spacedBy(12.dp).
                // So DynamicMacroGrid inside will work fine as a block.
                DynamicMacroGrid(
                    proteinas = food.proteina,
                    carboidratos = food.carboidratos,
                    lipidios = food.lipidios?.total,
                    modifier = Modifier.weight(3f)
                )
            }

            // --- EXPANDED VIEW (Secondary + Micros) ---
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryStatsGrid(
                        fibra = food.fibraAlimentar,
                        colesterol = food.colesterol,
                        sodio = food.sodio
                    )

                    MicronutrientsPanel(food = food)
                }
            }
        }
    }
}
