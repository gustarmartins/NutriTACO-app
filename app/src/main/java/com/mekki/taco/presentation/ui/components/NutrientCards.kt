package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.theme.LocalNutrientColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DynamicMacroGrid(
    proteinas: Double?,
    carboidratos: Double?,
    lipidios: Double?,
    modifier: Modifier = Modifier
) {
    val p = proteinas ?: 0.0
    val c = carboidratos ?: 0.0
    val f = lipidios ?: 0.0
    val nutrientColors = LocalNutrientColors.current

    data class MacroItem(
        val label: String,
        val value: Double,
        val color: Color,
        val icon: ImageVector
    )

    // Abbreviate labels when space is tight
    val effectiveScale = rememberEffectiveScale()
    val isCompact = effectiveScale > 1.15f

    val items = listOf(
        MacroItem(
            if (isCompact) "Prot." else "Proteínas",
            p, nutrientColors.protein, Icons.Default.FitnessCenter
        ),
        MacroItem("Carbs", c, nutrientColors.carbs, Icons.Default.Grain),
        MacroItem(
            if (isCompact) "Gord." else "Gorduras",
            f, nutrientColors.fat, Icons.Default.WaterDrop
        )
    ).sortedByDescending { it.value }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            VerticalNutrientCard(
                label = item.label,
                value = item.value,
                unit = "g",
                color = item.color,
                icon = item.icon,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactMacroGrid(
    energiaKcal: Double?,
    proteinas: Double?,
    carboidratos: Double?,
    lipidios: Double?,
    modifier: Modifier = Modifier
) {
    val nutrientColors = LocalNutrientColors.current
    val effectiveScale = rememberEffectiveScale()
    val isVeryCompact = effectiveScale > 1.35f

    data class NutrientItem(
        val label: String,
        val value: Double,
        val unit: String,
        val color: Color,
        val icon: ImageVector
    )

    val macros = listOf(
        NutrientItem(
            if (isVeryCompact) "Prot." else "Proteínas",
            proteinas ?: 0.0,
            "g",
            nutrientColors.protein,
            Icons.Default.FitnessCenter
        ),
        NutrientItem("Carbs", carboidratos ?: 0.0, "g", nutrientColors.carbs, Icons.Default.Grain),
        NutrientItem(
            if (isVeryCompact) "Gord." else "Gorduras",
            lipidios ?: 0.0, "g", nutrientColors.fat, Icons.Default.WaterDrop
        )
    ).sortedByDescending { it.value }

    val allItems = listOf(
        NutrientItem(
            if (isVeryCompact) "Kcal" else "Calorias",
            energiaKcal ?: 0.0,
            if (isVeryCompact) "" else "kcal",
            Color(0xFFA83C3C),
            Icons.Default.Bolt
        )
    ) + macros

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isVeryCompact) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        allItems.forEach { item ->
            VerticalNutrientCard(
                label = item.label,
                value = item.value,
                unit = item.unit,
                color = item.color,
                icon = item.icon,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EditableMacroGrid(
    protein: String,
    onProteinChange: (String) -> Unit,
    carbs: String,
    onCarbsChange: (String) -> Unit,
    fat: String,
    onFatChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val nutrientColors = LocalNutrientColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EditableVerticalNutrientCard(
            label = "Proteínas",
            value = protein,
            onValueChange = onProteinChange,
            unit = "g",
            color = nutrientColors.protein,
            icon = Icons.Default.FitnessCenter,
            modifier = Modifier.weight(1f)
        )
        EditableVerticalNutrientCard(
            label = "Carbs",
            value = carbs,
            onValueChange = onCarbsChange,
            unit = "g",
            color = nutrientColors.carbs,
            icon = Icons.Default.Grain,
            modifier = Modifier.weight(1f)
        )
        EditableVerticalNutrientCard(
            label = "Gorduras",
            value = fat,
            onValueChange = onFatChange,
            unit = "g",
            color = nutrientColors.fat,
            icon = Icons.Default.WaterDrop,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecondaryStatsGrid(fibra: Double?, colesterol: Double?, sodio: Double?) {
    val nutrientColors = LocalNutrientColors.current
    val effectiveScale = rememberEffectiveScale()
    // Only go vertical at a higher threshold so it stays horizontal longer
    val isCompact = effectiveScale > 1.15f

    if (isCompact) {
        // 2+1 layout via FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            VerticalNutrientCard(
                label = "Fibra",
                value = fibra,
                unit = "g",
                color = nutrientColors.fiber,
                icon = Icons.Default.Spa,
                modifier = Modifier.weight(1f)
            )
            VerticalNutrientCard(
                label = "Colest.",
                value = colesterol,
                unit = "mg",
                color = nutrientColors.cholesterol,
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
            )
            VerticalNutrientCard(
                label = "Sódio",
                value = sodio,
                unit = "mg",
                color = nutrientColors.sodium,
                icon = Icons.Default.Waves,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VerticalNutrientCard(
                label = "Fibra",
                value = fibra,
                unit = "g",
                color = nutrientColors.fiber,
                icon = Icons.Default.Spa,
                modifier = Modifier.weight(1f)
            )
            VerticalNutrientCard(
                label = "Colesterol",
                value = colesterol,
                unit = "mg",
                color = nutrientColors.cholesterol,
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
            )
            VerticalNutrientCard(
                label = "Sódio",
                value = sodio,
                unit = "mg",
                color = nutrientColors.sodium,
                icon = Icons.Default.Waves,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EditableSecondaryStatsGrid(
    fiber: String,
    onFiberChange: (String) -> Unit,
    cholest: String,
    onCholestChange: (String) -> Unit,
    sodium: String,
    onSodiumChange: (String) -> Unit
) {
    val nutrientColors = LocalNutrientColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EditableVerticalNutrientCard(
            label = "Fibra",
            value = fiber,
            onValueChange = onFiberChange,
            unit = "g",
            color = nutrientColors.fiber,
            icon = Icons.Default.Spa,
            modifier = Modifier.weight(1f)
        )
        EditableVerticalNutrientCard(
            label = "Colest.",
            value = cholest,
            onValueChange = onCholestChange,
            unit = "mg",
            color = nutrientColors.cholesterol,
            icon = Icons.Default.Favorite,
            modifier = Modifier.weight(1f)
        )
        EditableVerticalNutrientCard(
            label = "Sódio",
            value = sodium,
            onValueChange = onSodiumChange,
            unit = "mg",
            color = nutrientColors.sodium,
            icon = Icons.Default.Waves,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun VerticalNutrientCard(
    label: String,
    value: Double?,
    unit: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val effectiveScale = rememberEffectiveScale()
    val isCompact = effectiveScale > 1.2f
    val iconSize = if (isCompact) 24.dp else 32.dp
    val vertPadding = if (isCompact) 12.dp else 16.dp

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = vertPadding, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (value == null) "-" else {
                    val num = "%.1f".format(value)
                    val trimmed = if (num.endsWith(".0")) num.dropLast(2) else num
                    if (unit.isEmpty()) trimmed else "$trimmed $unit"
                },
                style = if (isCompact)
                    MaterialTheme.typography.titleSmall
                else
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun EditableVerticalNutrientCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MicronutrientsPanel(food: Food) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        NutrientPanel(
            title = "Minerais",
            items = listOf(
                Triple("Cálcio", food.calcio, "mg"),
                Triple("Ferro", food.ferro, "mg"),
                Triple("Magnésio", food.magnesio, "mg"),
                Triple("Fósforo", food.fosforo, "mg"),
                Triple("Potássio", food.potassio, "mg"),
                Triple("Zinco", food.zinco, "mg"),
                Triple("Cobre", food.cobre, "mg"),
                Triple("Manganês", food.manganes, "mg")
            )
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.15f
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Vitaminas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val vitamins = listOf(
                    Triple("Ácido Ascórbico (C)", food.vitaminaC, "mg"),
                    Triple("Retinol (A)", food.retinol, "mcg"),
                    Triple("Tiamina (B1)", food.tiamina, "mg"),
                    Triple("Riboflavina (B2)", food.riboflavina, "mg"),
                    Triple("Niacina (B3)", food.niacina, "mg"),
                    Triple("Piridoxina (B6)", food.piridoxina, "mg")
                )

                ResponsiveNutrientGrid(items = vitamins)
            }
        }
    }
}

@Composable
fun EditableMicronutrientsPanel(
    fields: Map<String, String>,
    onFieldChange: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditableNutrientPanel(
            title = "Minerais",
            items = listOf(
                "Cálcio" to ("calcio" to fields["calcio"].orEmpty()),
                "Ferro" to ("ferro" to fields["ferro"].orEmpty()),
                "Magnésio" to ("magnesio" to fields["magnesio"].orEmpty()),
                "Fósforo" to ("fosforo" to fields["fosforo"].orEmpty()),
                "Potássio" to ("potassio" to fields["potassio"].orEmpty()),
                "Zinco" to ("zinco" to fields["zinco"].orEmpty()),
                "Cobre" to ("cobre" to fields["cobre"].orEmpty()),
                "Manganês" to ("manganes" to fields["manganes"].orEmpty())
            ),
            unit = "mg",
            onFieldChange = onFieldChange
        )

        EditableNutrientPanel(
            title = "Vitaminas",
            items = listOf(
                "Ácido Ascórbico (C)" to ("vitc" to fields["vitc"].orEmpty()),
                "Retinol (A)" to ("retinol" to fields["retinol"].orEmpty()),
                "Tiamina (B1)" to ("tiamina" to fields["tiamina"].orEmpty()),
                "Riboflavina (B2)" to ("riboflavina" to fields["riboflavina"].orEmpty()),
                "Niacina (B3)" to ("niacina" to fields["niacina"].orEmpty()),
                "Piridoxina (B6)" to ("piridoxina" to fields["piridoxina"].orEmpty())
            ),
            unit = "mg/mcg",
            onFieldChange = onFieldChange
        )
    }
}

@Composable
fun NutrientPanel(title: String, items: List<Triple<String, Double?, String>>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.15f
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ResponsiveNutrientGrid(items = items)
        }
    }
}

@Composable
fun EditableNutrientPanel(
    title: String,
    items: List<Pair<String, Pair<String, String>>>,
    unit: String,
    onFieldChange: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.15f
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { (label, fieldData) ->
                            val (fieldKey, fieldValue) = fieldData
                            Box(Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    OutlinedTextField(
                                        value = fieldValue,
                                        onValueChange = { onFieldChange(fieldKey, it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        suffix = {
                                            Text(
                                                unit,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun PanelRow(label: String, value: Double?, unit: String, isCompact: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = if (isCompact) Int.MAX_VALUE else 1,
            overflow = if (isCompact) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Text(
            text = if (value == null || value == 0.0) "-" else "%.1f %s".format(value, unit)
                .replace(".0 ", " "),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResponsiveNutrientGrid(items: List<Triple<String, Double?, String>>) {
    val effectiveScale = rememberEffectiveScale()

    // Scale minimum width with effectiveScale instead of raw fontScale
    // At baseline (1.0) → 150dp, at 1.2 → 180dp, forces single column earlier
    val minItemWidth = (150 * effectiveScale).dp

    val isTextLarge = effectiveScale > 1.1f

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        items.forEach { (label, value, unit) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = minItemWidth)
            ) {
                PanelRow(
                    label = label,
                    value = value,
                    unit = unit,
                    isCompact = isTextLarge
                )
            }
        }
    }
}
