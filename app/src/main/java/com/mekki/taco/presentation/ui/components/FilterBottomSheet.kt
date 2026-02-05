package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSortOption
import com.mekki.taco.presentation.ui.search.FoodSource
import com.mekki.taco.presentation.ui.search.SortCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filterState: FoodFilterState,
    categories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSourceChange: (FoodSource) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onClearCategories: () -> Unit,
    onSortChange: (FoodSortOption) -> Unit,
    onResetFilters: () -> Unit,
    onFilterStateChange: (FoodFilterState) -> Unit = {},
    showCategories: Boolean = true,
    showAdvancedFilters: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMacroFilters by remember { mutableStateOf(filterState.hasMacroFilters) }
    var showMineralFilters by remember { mutableStateOf(filterState.hasMineralFilters) }
    var showVitaminFilters by remember { mutableStateOf(filterState.hasVitaminFilters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (filterState.hasActiveFilters) {
                    TextButton(onClick = onResetFilters) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Resetar")
                    }
                }
            }

            Text("Fonte", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodSource.entries.forEach { source ->
                    FilterChip(
                        selected = filterState.source == source,
                        onClick = { onSourceChange(source) },
                        label = { Text(source.displayName) }
                    )
                }
            }

            Text("Ordenar Por", style = MaterialTheme.typography.titleMedium)
            SortCategory.entries.forEach { category ->
                val categoryOptions = FoodSortOption.entries.filter { it.category == category }
                if (categoryOptions.isNotEmpty()) {
                    Text(
                        category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categoryOptions.forEach { option ->
                            FilterChip(
                                selected = filterState.sortOption == option,
                                onClick = { onSortChange(option) },
                                label = { Text(option.displayName) }
                            )
                        }
                    }
                }
            }

            if (showCategories && categories.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categoria", style = MaterialTheme.typography.titleMedium)

                    if (filterState.selectedCategories.isNotEmpty()) {
                        TextButton(onClick = onClearCategories) {
                            Text(
                                "Limpar (${filterState.selectedCategories.size})",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat in filterState.selectedCategories,
                            onClick = { onCategoryToggle(cat) },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            if (showAdvancedFilters) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ExpandableFilterSection(
                    title = "Macronutrientes",
                    isExpanded = showMacroFilters,
                    hasActiveFilters = filterState.hasMacroFilters,
                    onToggle = { showMacroFilters = !showMacroFilters }
                ) {
                    MacroFiltersContent(
                        filterState = filterState,
                        onFilterStateChange = onFilterStateChange
                    )
                }

                ExpandableFilterSection(
                    title = "Minerais",
                    isExpanded = showMineralFilters,
                    hasActiveFilters = filterState.hasMineralFilters,
                    onToggle = { showMineralFilters = !showMineralFilters }
                ) {
                    MineralFiltersContent(
                        filterState = filterState,
                        onFilterStateChange = onFilterStateChange
                    )
                }

                ExpandableFilterSection(
                    title = "Vitaminas",
                    isExpanded = showVitaminFilters,
                    hasActiveFilters = filterState.hasVitaminFilters,
                    onToggle = { showVitaminFilters = !showVitaminFilters }
                ) {
                    VitaminFiltersContent(
                        filterState = filterState,
                        onFilterStateChange = onFilterStateChange
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableFilterSection(
    title: String,
    isExpanded: Boolean,
    hasActiveFilters: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (hasActiveFilters) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "●",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            content()
        }
    }
}

@Composable
private fun MacroFiltersContent(
    filterState: FoodFilterState,
    onFilterStateChange: (FoodFilterState) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NutrientRangeRow(
            label = "Calorias (kcal)",
            minValue = filterState.minCalories,
            maxValue = filterState.maxCalories,
            onMinChange = { onFilterStateChange(filterState.copy(minCalories = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxCalories = it)) }
        )
        NutrientRangeRow(
            label = "Proteínas (g)",
            minValue = filterState.minProtein,
            maxValue = filterState.maxProtein,
            onMinChange = { onFilterStateChange(filterState.copy(minProtein = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxProtein = it)) }
        )
        NutrientRangeRow(
            label = "Carboidratos (g)",
            minValue = filterState.minCarbs,
            maxValue = filterState.maxCarbs,
            onMinChange = { onFilterStateChange(filterState.copy(minCarbs = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxCarbs = it)) }
        )
        NutrientRangeRow(
            label = "Gorduras Totais (g)",
            minValue = filterState.minFat,
            maxValue = filterState.maxFat,
            onMinChange = { onFilterStateChange(filterState.copy(minFat = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxFat = it)) }
        )
        NutrientRangeRow(
            label = "Fibra (g)",
            minValue = filterState.minFibra,
            maxValue = filterState.maxFibra,
            onMinChange = { onFilterStateChange(filterState.copy(minFibra = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxFibra = it)) }
        )
        NutrientRangeRow(
            label = "Colesterol (mg)",
            minValue = filterState.minColesterol,
            maxValue = filterState.maxColesterol,
            onMinChange = { onFilterStateChange(filterState.copy(minColesterol = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxColesterol = it)) }
        )

        Spacer(Modifier.size(4.dp))
        Text(
            "Gorduras por tipo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        NutrientRangeRow(
            label = "Saturadas (g)",
            minValue = filterState.minSaturados,
            maxValue = filterState.maxSaturados,
            onMinChange = { onFilterStateChange(filterState.copy(minSaturados = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxSaturados = it)) }
        )
        NutrientRangeRow(
            label = "Monoinsaturadas (g)",
            minValue = filterState.minMonoinsaturados,
            maxValue = filterState.maxMonoinsaturados,
            onMinChange = { onFilterStateChange(filterState.copy(minMonoinsaturados = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxMonoinsaturados = it)) }
        )
        NutrientRangeRow(
            label = "Poliinsaturadas (g)",
            minValue = filterState.minPoliinsaturados,
            maxValue = filterState.maxPoliinsaturados,
            onMinChange = { onFilterStateChange(filterState.copy(minPoliinsaturados = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxPoliinsaturados = it)) }
        )
    }
}

@Composable
private fun MineralFiltersContent(
    filterState: FoodFilterState,
    onFilterStateChange: (FoodFilterState) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NutrientRangeRow(
            label = "Cálcio (mg)",
            minValue = filterState.minCalcio,
            maxValue = filterState.maxCalcio,
            onMinChange = { onFilterStateChange(filterState.copy(minCalcio = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxCalcio = it)) }
        )
        NutrientRangeRow(
            label = "Ferro (mg)",
            minValue = filterState.minFerro,
            maxValue = filterState.maxFerro,
            onMinChange = { onFilterStateChange(filterState.copy(minFerro = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxFerro = it)) }
        )
        NutrientRangeRow(
            label = "Sódio (mg)",
            minValue = filterState.minSodio,
            maxValue = filterState.maxSodio,
            onMinChange = { onFilterStateChange(filterState.copy(minSodio = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxSodio = it)) }
        )
        NutrientRangeRow(
            label = "Potássio (mg)",
            minValue = filterState.minPotassio,
            maxValue = filterState.maxPotassio,
            onMinChange = { onFilterStateChange(filterState.copy(minPotassio = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxPotassio = it)) }
        )
        NutrientRangeRow(
            label = "Magnésio (mg)",
            minValue = filterState.minMagnesio,
            maxValue = filterState.maxMagnesio,
            onMinChange = { onFilterStateChange(filterState.copy(minMagnesio = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxMagnesio = it)) }
        )
        NutrientRangeRow(
            label = "Zinco (mg)",
            minValue = filterState.minZinco,
            maxValue = filterState.maxZinco,
            onMinChange = { onFilterStateChange(filterState.copy(minZinco = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxZinco = it)) }
        )
        NutrientRangeRow(
            label = "Cobre (mg)",
            minValue = filterState.minCobre,
            maxValue = filterState.maxCobre,
            onMinChange = { onFilterStateChange(filterState.copy(minCobre = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxCobre = it)) }
        )
        NutrientRangeRow(
            label = "Fósforo (mg)",
            minValue = filterState.minFosforo,
            maxValue = filterState.maxFosforo,
            onMinChange = { onFilterStateChange(filterState.copy(minFosforo = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxFosforo = it)) }
        )
        NutrientRangeRow(
            label = "Manganês (mg)",
            minValue = filterState.minManganes,
            maxValue = filterState.maxManganes,
            onMinChange = { onFilterStateChange(filterState.copy(minManganes = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxManganes = it)) }
        )
    }
}

@Composable
private fun VitaminFiltersContent(
    filterState: FoodFilterState,
    onFilterStateChange: (FoodFilterState) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NutrientRangeRow(
            label = "Vitamina C (mg)",
            minValue = filterState.minVitaminaC,
            maxValue = filterState.maxVitaminaC,
            onMinChange = { onFilterStateChange(filterState.copy(minVitaminaC = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxVitaminaC = it)) }
        )
        NutrientRangeRow(
            label = "Vitamina A / Retinol (µg)",
            minValue = filterState.minRetinol,
            maxValue = filterState.maxRetinol,
            onMinChange = { onFilterStateChange(filterState.copy(minRetinol = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxRetinol = it)) }
        )
        NutrientRangeRow(
            label = "Tiamina / B1 (mg)",
            minValue = filterState.minTiamina,
            maxValue = filterState.maxTiamina,
            onMinChange = { onFilterStateChange(filterState.copy(minTiamina = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxTiamina = it)) }
        )
        NutrientRangeRow(
            label = "Riboflavina / B2 (mg)",
            minValue = filterState.minRiboflavina,
            maxValue = filterState.maxRiboflavina,
            onMinChange = { onFilterStateChange(filterState.copy(minRiboflavina = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxRiboflavina = it)) }
        )
        NutrientRangeRow(
            label = "Niacina / B3 (mg)",
            minValue = filterState.minNiacina,
            maxValue = filterState.maxNiacina,
            onMinChange = { onFilterStateChange(filterState.copy(minNiacina = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxNiacina = it)) }
        )
        NutrientRangeRow(
            label = "Piridoxina / B6 (mg)",
            minValue = filterState.minPiridoxina,
            maxValue = filterState.maxPiridoxina,
            onMinChange = { onFilterStateChange(filterState.copy(minPiridoxina = it)) },
            onMaxChange = { onFilterStateChange(filterState.copy(maxPiridoxina = it)) }
        )
    }
}

@Composable
private fun NutrientRangeRow(
    label: String,
    minValue: Double?,
    maxValue: Double?,
    onMinChange: (Double?) -> Unit,
    onMaxChange: (Double?) -> Unit
) {
    var minText by remember(minValue) {
        mutableStateOf(minValue?.let { formatFilterValue(it) } ?: "")
    }
    var maxText by remember(maxValue) {
        mutableStateOf(maxValue?.let { formatFilterValue(it) } ?: "")
    }

    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = minText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                        .replace(',', '.')
                    if (filtered.count { it == '.' } <= 1) {
                        minText = filtered
                        onMinChange(filtered.toDoubleOrNull()?.coerceAtLeast(0.0))
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mín.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                trailingIcon = if (minText.isNotEmpty()) {
                    {
                        IconButton(onClick = { minText = ""; onMinChange(null) }) {
                            Icon(Icons.Default.Clear, "Limpar", Modifier.size(18.dp))
                        }
                    }
                } else null
            )
            Text("–")
            OutlinedTextField(
                value = maxText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                        .replace(',', '.')
                    if (filtered.count { it == '.' } <= 1) {
                        maxText = filtered
                        onMaxChange(filtered.toDoubleOrNull()?.coerceAtLeast(0.0))
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Máx.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                trailingIcon = if (maxText.isNotEmpty()) {
                    {
                        IconButton(onClick = { maxText = ""; onMaxChange(null) }) {
                            Icon(Icons.Default.Clear, "Limpar", Modifier.size(18.dp))
                        }
                    }
                } else null
            )
        }
    }
}

private fun formatFilterValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

/**
 * (min only)
 * potential use in the future.
 */
@Composable
fun NutrientMinRow(
    label: String,
    value: Double?,
    onChange: (Double?) -> Unit
) {
    var text by remember(value) { mutableStateOf(value?.let { formatFilterValue(it) } ?: "") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                    .replace(',', '.')
                if (filtered.count { it == '.' } <= 1) {
                    text = filtered
                    onChange(filtered.toDoubleOrNull()?.coerceAtLeast(0.0))
                }
            },
            modifier = Modifier.width(100.dp),
            placeholder = { Text("Mín") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            trailingIcon = if (text.isNotEmpty()) {
                {
                    IconButton(onClick = { text = ""; onChange(null) }) {
                        Icon(Icons.Default.Clear, "Limpar", Modifier.size(16.dp))
                    }
                }
            } else null
        )
    }
}