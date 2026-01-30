package com.mekki.taco.presentation.ui.database

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.components.FilterBottomSheet
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSource
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDatabaseScreen(
    viewModel: FoodDatabaseViewModel,
    onNavigateBack: () -> Unit,
    onFoodClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    // Count active filters for badge
    val activeFilterCount = listOfNotNull(
        if (uiState.selectedSource != FoodSource.ALL) 1 else null,
        if (uiState.selectedCategories.isNotEmpty()) uiState.selectedCategories.size else null,
        if (uiState.sortOption != SortOption.NAME) 1 else null
    ).sum()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Banco de Alimentos",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    // only shown when filters are active
                    if (uiState.hasActiveFilters) {
                        IconButton(onClick = { viewModel.onResetFilters() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Resetar filtros",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Filter button with badge showing count of active filters
                    IconButton(onClick = { showFilterSheet = true }) {
                        if (activeFilterCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(activeFilterCount.toString())
                                    }
                                }
                            ) {
                                Icon(Icons.Default.FilterList, "Filtros")
                            }
                        } else {
                            Icon(Icons.Default.FilterList, "Filtros")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nome...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, "Limpar")
                        }
                    }
                } else null,
                singleLine = true
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.selectedSource != FoodSource.ALL) {
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.onSourceChange(FoodSource.ALL) },
                            label = { Text("Fonte:  ${uiState.selectedSource.displayName}") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                // one for each selected category
                items(uiState.selectedCategories.toList()) { category ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.onCategoryRemove(category) },
                        label = { Text(category) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                item {
                    InputChip(
                        selected = true,
                        onClick = { showFilterSheet = true },
                        label = { Text("Ordenar:  ${uiState.sortOption.displayName}") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.foods, key = { it.id }) { food ->
                        FoodDatabaseItem(food = food, onClick = { onFoodClick(food.id) })
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    if (uiState.foods.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhum alimento encontrado.",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filterState = FoodFilterState(
                searchQuery = uiState.searchQuery,
                source = uiState.selectedSource,
                sortOption = uiState.sortOption,
                selectedCategories = uiState.selectedCategories
            ),
            categories = uiState.categories,
            onDismiss = { showFilterSheet = false },
            onSourceChange = viewModel::onSourceChange,
            onCategoryToggle = viewModel::onCategoryToggle,
            onClearCategories = viewModel::onClearCategories,
            onSortChange = viewModel::onSortChange,
            onResetFilters = viewModel::onResetFilters,
            showAdvancedFilters = false
        )
    }
}

@Composable
fun FoodDatabaseItem(
    food: Food,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = food.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = food.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NutrientValue(
                        label = "Kcal",
                        value = food.energiaKcal,
                        color = Color(0xFFA83C3C)
                    )
                    NutrientValue(label = "Prot", value = food.proteina, color = Color(0xFF2E7A7A))
                    NutrientValue(
                        label = "Carb",
                        value = food.carboidratos,
                        color = Color(0xFFDCC48E)
                    )
                    NutrientValue(
                        label = "Gord",
                        value = food.lipidios?.total,
                        color = Color(0xFFC97C4A)
                    )
                }
            }
        },
        trailingContent = {
            if (food.isCustom) {
                Text(
                    "Usuário",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    "TACO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun NutrientValue(label: String, value: Double?, color: Color) {
    val df = remember { DecimalFormat("#") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (value == null) "-" else df.format(value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}