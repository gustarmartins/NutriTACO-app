package com.mekki.taco.presentation.ui.database

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.mekki.taco.presentation.ui.search.FoodSortOption
import com.mekki.taco.presentation.ui.search.FoodSource
import java.text.DecimalFormat

private val COLOR_KCAL = Color(0xFFA83C3C)
private val COLOR_PROTEIN = Color(0xFF2E7A7A)
private val COLOR_CARB = Color(0xFFDCC48E)
private val COLOR_FAT = Color(0xFFC97C4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDatabaseScreen(
    viewModel: FoodDatabaseViewModel,
    onNavigateBack: () -> Unit,
    onFoodClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var portionGrams by remember { mutableFloatStateOf(100f) }
    var showPortionSlider by remember { mutableStateOf(false) }

    val activeFilterCount = listOfNotNull(
        if (uiState.selectedSource != FoodSource.ALL) 1 else null,
        if (uiState.selectedCategories.isNotEmpty()) uiState.selectedCategories.size else null,
        if (uiState.sortOption != FoodSortOption.NAME) 1 else null
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
                    if (uiState.hasActiveFilters) {
                        IconButton(onClick = { viewModel.onResetFilters() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Resetar filtros",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = { showFilterSheet = true }) {
                        if (activeFilterCount > 0) {
                            BadgedBox(
                                badge = { Badge { Text(activeFilterCount.toString()) } }
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

            // Portion Control Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { showPortionSlider = !showPortionSlider },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Valores por porção:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${portionGrams.toInt()}g",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (showPortionSlider) {
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = portionGrams,
                            onValueChange = { portionGrams = it },
                            valueRange = 10f..500f,
                            steps = 48
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(50, 100, 150, 200).forEach { preset ->
                                InputChip(
                                    selected = portionGrams.toInt() == preset,
                                    onClick = { portionGrams = preset.toFloat() },
                                    label = { Text("${preset}g") }
                                )
                            }
                        }
                    }
                }
            }

            // Active Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.selectedSource != FoodSource.ALL) {
                    item {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.onSourceChange(FoodSource.ALL) },
                            label = { Text("Fonte: ${uiState.selectedSource.displayName}") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }

                items(uiState.selectedCategories.toList()) { category ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.onCategoryRemove(category) },
                        label = { Text(category) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                item {
                    InputChip(
                        selected = true,
                        onClick = { showFilterSheet = true },
                        label = { Text("Ordenar: ${uiState.sortOption.displayName}") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Sort, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            // Results Count
            Text(
                text = "${uiState.foods.size} alimentos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            HorizontalDivider()

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(uiState.foods, key = { _, food -> food.id }) { index, food ->
                        FoodDatabaseItem(
                            food = food,
                            index = index + 1,
                            portionGrams = portionGrams,
                            sortOption = uiState.sortOption,
                            onClick = { onFoodClick(food.id) }
                        )
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
    index: Int,
    portionGrams: Float,
    sortOption: FoodSortOption,
    onClick: () -> Unit
) {
    val df = remember { DecimalFormat("#.#") }
    val portionMultiplier = portionGrams / 100f

    val kcal = food.energiaKcal?.times(portionMultiplier)
    val protein = food.proteina?.times(portionMultiplier)
    val carbs = food.carboidratos?.times(portionMultiplier)
    val fat = food.lipidios?.total?.times(portionMultiplier)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
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

            Text(
                text = food.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NutrientLabel(
                    label = "Energia",
                    value = kcal,
                    unit = "kcal",
                    color = COLOR_KCAL,
                    isHighlighted = sortOption == FoodSortOption.CALORIES,
                    df = df
                )
                NutrientLabel(
                    label = "Proteína",
                    value = protein,
                    unit = "g",
                    color = COLOR_PROTEIN,
                    isHighlighted = sortOption == FoodSortOption.PROTEIN,
                    df = df
                )
                NutrientLabel(
                    label = "Carboidrato",
                    value = carbs,
                    unit = "g",
                    color = COLOR_CARB,
                    isHighlighted = sortOption == FoodSortOption.CARBS,
                    df = df
                )
                NutrientLabel(
                    label = "Gordura",
                    value = fat,
                    unit = "g",
                    color = COLOR_FAT,
                    isHighlighted = sortOption == FoodSortOption.FAT,
                    df = df
                )
            }
        }
    }
}

@Composable
private fun NutrientLabel(
    label: String,
    value: Double?,
    unit: String,
    color: Color,
    isHighlighted: Boolean,
    df: DecimalFormat
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = if (value == null) "-" else "${df.format(value)}$unit",
            style = if (isHighlighted) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted) color else color.copy(alpha = 0.7f),
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}