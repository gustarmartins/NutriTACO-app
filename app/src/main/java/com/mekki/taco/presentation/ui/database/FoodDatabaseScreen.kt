package com.mekki.taco.presentation.ui.database

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.components.FilterBottomSheet
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSortOption
import com.mekki.taco.presentation.ui.search.FoodSource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DecimalFormat

private val COLOR_KCAL = Color(0xFFA83C3C)
private val COLOR_PROTEIN = Color(0xFF2E7A7A)
private val COLOR_CARB = Color(0xFFDCC48E)
private val COLOR_FAT = Color(0xFFC97C4A)

private const val MIN_ITEMS_FOR_SCROLL_FAB = 100
private const val GO_TO_BOTTOM_THRESHOLD = 0.25f // Show "go to bottom" in first 25% of list

private enum class ScrollFabTarget {
    NONE,
    TOP,
    BOTTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDatabaseScreen(
    viewModel: FoodDatabaseViewModel,
    onNavigateBack: () -> Unit,
    onFoodClick: (Int) -> Unit,
    onAddFood: () -> Unit,
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var portionGrams by rememberSaveable { mutableFloatStateOf(100f) }
    var showPortionControl by rememberSaveable { mutableStateOf(false) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    val totalItems = uiState.foods.size
    val hasEnoughItems = totalItems >= MIN_ITEMS_FOR_SCROLL_FAB

    // Calculate the threshold index for switching from "go to bottom" to "go to top"
    val switchThresholdIndex = remember(totalItems) {
        (totalItems * GO_TO_BOTTOM_THRESHOLD).toInt().coerceAtLeast(1)
    }

    val currentIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }

    val isAtTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 100
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastVisibleItem == null || layoutInfo.totalItemsCount == 0) {
                false
            } else {
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 1
            }
        }
    }

    // Check if user is in the "go to bottom" zone (first 25% of list)
    val isInGoToBottomZone by remember(switchThresholdIndex) {
        derivedStateOf {
            currentIndex < switchThresholdIndex
        }
    }

    // FAB target - derived from scroll position with threshold logic
    val fabTarget by remember(hasEnoughItems, switchThresholdIndex) {
        derivedStateOf {
            when {
                !hasEnoughItems -> ScrollFabTarget.NONE
                isAtBottom -> ScrollFabTarget.TOP
                isInGoToBottomZone -> ScrollFabTarget.BOTTOM
                else -> ScrollFabTarget.TOP
            }
        }
    }

    LaunchedEffect(lazyListState) {
        var previousIndex = 0
        var previousOffset = 0
        var accumulatedDelta = 0

        snapshotFlow {
            Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset)
        }
            .distinctUntilChanged()
            .map { (index, offset) ->
                val delta = when {
                    index > previousIndex -> 100
                    index < previousIndex -> -100
                    else -> offset - previousOffset
                }
                previousIndex = index
                previousOffset = offset
                delta
            }
            .collect { delta ->
                accumulatedDelta += delta

                when {
                    accumulatedDelta > 150 -> {
                        onBottomBarVisibilityChange(false)
                        accumulatedDelta = 0
                    }

                    accumulatedDelta < -150 -> {
                        onBottomBarVisibilityChange(true)
                        accumulatedDelta = 0
                    }
                }

                if (lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 50
                ) {
                    onBottomBarVisibilityChange(true)
                    accumulatedDelta = 0
                }

                val layoutInfo = lazyListState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                if (lastVisibleItem != null && lastVisibleItem.index >= layoutInfo.totalItemsCount - 1) {
                    onBottomBarVisibilityChange(true)
                    accumulatedDelta = 0
                }
            }
    }

    val isSearchExpanded = isSearchActive || uiState.searchQuery.isNotEmpty()
    val showSearchIcon = !(isSearchExpanded && showPortionControl)

    BackHandler(
        enabled = isSearchActive || showPortionControl || showFilterSheet
    ) {
        when {
            showFilterSheet -> showFilterSheet = false
            isSearchActive -> {
                focusManager.clearFocus()
                isSearchActive = false
            }

            showPortionControl -> showPortionControl = false
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    val activeFilterCount = listOfNotNull(
        if (uiState.selectedSource != FoodSource.ALL) 1 else null,
        if (uiState.selectedCategories.isNotEmpty()) uiState.selectedCategories.size else null
    ).sum()

    val hasClearableFilters = uiState.selectedSource != FoodSource.ALL ||
            uiState.selectedCategories.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                title = {
                    TopBarContent(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        isSearchExpanded = isSearchExpanded,
                        showPortionControl = showPortionControl,
                        portionGrams = portionGrams,
                        onPortionChange = { portionGrams = it },
                        searchFocusRequester = searchFocusRequester,
                        onSearchDone = { focusManager.clearFocus() },
                        onClearSearch = {
                            viewModel.onSearchQueryChange("")
                            isSearchActive = false
                            focusManager.clearFocus()
                        }
                    )
                },
                actions = {
                    AnimatedVisibility(
                        visible = showSearchIcon,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                if (isSearchExpanded) {
                                    focusManager.clearFocus()
                                    isSearchActive = false
                                } else {
                                    isSearchActive = true
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = if (isSearchExpanded) "Fechar busca" else "Buscar",
                                tint = if (isSearchExpanded)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            showPortionControl = !showPortionControl
                            if (showPortionControl) {
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Balance,
                            contentDescription = "Porção: ${portionGrams.toInt()}g",
                            modifier = Modifier.size(22.dp),
                            tint = if (showPortionControl || portionGrams != 100f)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onAddFood) {
                        Icon(Icons.Default.Add, "Adicionar Alimento")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabTarget != ScrollFabTarget.NONE,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            when (fabTarget) {
                                ScrollFabTarget.TOP -> {
                                    lazyListState.animateScrollToItem(0)
                                }

                                ScrollFabTarget.BOTTOM -> {
                                    lazyListState.animateScrollToItem(totalItems - 1)
                                }

                                ScrollFabTarget.NONE -> { /* Won't happen */
                                }
                            }
                        }
                    },
                    containerColor = when (fabTarget) {
                        ScrollFabTarget.TOP -> MaterialTheme.colorScheme.primaryContainer
                        ScrollFabTarget.BOTTOM -> MaterialTheme.colorScheme.secondaryContainer
                        ScrollFabTarget.NONE -> MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = when (fabTarget) {
                        ScrollFabTarget.TOP -> MaterialTheme.colorScheme.onPrimaryContainer
                        ScrollFabTarget.BOTTOM -> MaterialTheme.colorScheme.onSecondaryContainer
                        ScrollFabTarget.NONE -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = when (fabTarget) {
                            ScrollFabTarget.TOP -> Icons.Default.KeyboardDoubleArrowUp
                            ScrollFabTarget.BOTTOM -> Icons.Default.KeyboardDoubleArrowDown
                            ScrollFabTarget.NONE -> Icons.Default.KeyboardDoubleArrowUp
                        },
                        contentDescription = when (fabTarget) {
                            ScrollFabTarget.TOP -> "Ir ao topo"
                            ScrollFabTarget.BOTTOM -> "Ir ao fundo"
                            ScrollFabTarget.NONE -> null
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterChipsRow(
                uiState = uiState,
                activeFilterCount = activeFilterCount,
                hasClearableFilters = hasClearableFilters,
                onSourceClear = { viewModel.onSourceChange(FoodSource.ALL) },
                onCategoryRemove = viewModel::onCategoryRemove,
                onClearFilters = viewModel::onClearFilters,
                onOpenFilters = { showFilterSheet = true }
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    itemsIndexed(uiState.foods, key = { _, food -> food.id }) { index, food ->
                        FoodDatabaseItem(
                            food = food,
                            position = index + 1,
                            totalCount = uiState.foods.size,
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
private fun TopBarContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    showPortionControl: Boolean,
    portionGrams: Float,
    onPortionChange: (Float) -> Unit,
    searchFocusRequester: FocusRequester,
    onSearchDone: () -> Unit,
    onClearSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = showPortionControl,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            CompactPortionControl(
                portion = portionGrams.toInt().toString(),
                onPortionChange = { newValue ->
                    val parsed = newValue.toFloatOrNull() ?: 100f
                    onPortionChange(parsed.coerceIn(1f, 9999f))
                }
            )
        }

        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut(),
            modifier = if (showPortionControl && isSearchExpanded) {
                Modifier.weight(1f)
            } else {
                Modifier
            }
        ) {
            CompactSearchInput(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClear = onClearSearch,
                focusRequester = searchFocusRequester,
                onDone = onSearchDone,
                modifier = if (showPortionControl) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.width(200.dp)
                }
            )
        }

        AnimatedVisibility(
            visible = !isSearchExpanded && !showPortionControl,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "Alimentos",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactSearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onDone() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            "Buscar...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (query.isNotEmpty()) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Limpar",
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onClear() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactPortionControl(
    portion: String,
    onPortionChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                focusManager.clearFocus()
                val current = portion.toIntOrNull() ?: 100
                val new = (current - 10).coerceAtLeast(10)
                onPortionChange(new.toString())
            },
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Diminuir",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        BasicTextField(
            value = portion,
            onValueChange = { newValue ->
                if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                    onPortionChange(newValue)
                }
            },
            modifier = Modifier
                .width(32.dp)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )

        Text(
            text = "g",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        IconButton(
            onClick = {
                focusManager.clearFocus()
                val current = portion.toIntOrNull() ?: 100
                val new = (current + 10).coerceAtMost(9999)
                onPortionChange(new.toString())
            },
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Aumentar",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    uiState: FoodDatabaseState,
    activeFilterCount: Int,
    hasClearableFilters: Boolean,
    onSourceClear: () -> Unit,
    onCategoryRemove: (String) -> Unit,
    onClearFilters: () -> Unit,
    onOpenFilters: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                text = "${uiState.foods.size} itens",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        item {
            InputChip(
                selected = activeFilterCount > 0,
                onClick = onOpenFilters,
                label = { Text("Filtros") },
                leadingIcon = {
                    if (activeFilterCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(activeFilterCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }

        item {
            InputChip(
                selected = uiState.sortOption != FoodSortOption.NAME,
                onClick = onOpenFilters,
                label = {
                    Text(
                        text = "Ordenar: ${uiState.sortOption.displayName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        if (uiState.selectedSource != FoodSource.ALL) {
            item {
                InputChip(
                    selected = true,
                    onClick = onSourceClear,
                    label = { Text(uiState.selectedSource.displayName) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        items(uiState.selectedCategories.toList()) { category ->
            InputChip(
                selected = true,
                onClick = { onCategoryRemove(category) },
                label = { Text(category, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        if (hasClearableFilters) {
            item {
                InputChip(
                    selected = false,
                    onClick = onClearFilters,
                    label = { Text("Limpar filtros") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FoodDatabaseItem(
    food: Food,
    position: Int,
    totalCount: Int,
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
            .padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "$position",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
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

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${portionGrams.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = if (food.isCustom)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (food.isCustom) "Usuário" else "TACO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (food.isCustom)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = food.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NutrientLabel(
                    icon = Icons.Default.Bolt,
                    value = kcal,
                    color = COLOR_KCAL,
                    isHighlighted = sortOption == FoodSortOption.CALORIES,
                    df = df,
                    unit = "kcal"
                )
                NutrientLabel(
                    icon = Icons.Default.FitnessCenter,
                    value = protein,
                    color = COLOR_PROTEIN,
                    isHighlighted = sortOption == FoodSortOption.PROTEIN,
                    df = df
                )
                NutrientLabel(
                    icon = Icons.Default.Grain,
                    value = carbs,
                    color = COLOR_CARB,
                    isHighlighted = sortOption == FoodSortOption.CARBS,
                    df = df
                )
                NutrientLabel(
                    icon = Icons.Default.WaterDrop,
                    value = fat,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Double?,
    color: Color,
    isHighlighted: Boolean,
    df: DecimalFormat,
    unit: String = "g"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = if (value == null) "-" else "${df.format(value)}$unit",
            style = if (isHighlighted) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}