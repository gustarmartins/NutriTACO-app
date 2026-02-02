package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSortOption
import com.mekki.taco.presentation.ui.search.FoodSource

@Composable
fun ActiveFilterChips(
    filterState: FoodFilterState,
    onSortClick: () -> Unit,
    onSourceClear: () -> Unit,
    onCategoryClear: (String) -> Unit,
    onAdvancedFiltersClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val advancedFilterCount = filterState.activeAdvancedFilterCount

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            InputChip(
                selected = true,
                onClick = onSortClick,
                label = { Text("Ordenar: ${filterState.sortOption.label}") },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        if (filterState.source != FoodSource.ALL) {
            item {
                InputChip(
                    selected = true,
                    onClick = onSourceClear,
                    label = { Text("Fonte: ${filterState.source.displayName}") },
                    trailingIcon = {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        items(filterState.selectedCategories.toList()) { category ->
            InputChip(
                selected = true,
                onClick = { onCategoryClear(category) },
                label = { Text(category) },
                trailingIcon = {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                }
            )
        }

        if (advancedFilterCount > 0) {
            item {
                InputChip(
                    selected = true,
                    onClick = onAdvancedFiltersClear,
                    label = { Text("+$advancedFilterCount filtros") },
                    leadingIcon = {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}
