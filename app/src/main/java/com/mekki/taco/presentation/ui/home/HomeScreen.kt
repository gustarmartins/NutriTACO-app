package com.mekki.taco.presentation.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.presentation.ui.components.FilterBottomSheet
import com.mekki.taco.presentation.ui.components.rememberEffectiveScale
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSource
import com.mekki.taco.presentation.ui.search.getNutrientDisplayInfo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Custom Colors based on the image/requirements (approximations)
private val COLOR_PROTEIN = Color(0xFF2E7A7A)
private val COLOR_CARBS = Color(0xFF4F9FDF) // Slightly lighter blue/cyan
private val COLOR_FAT = Color(0xFFECA345)   // Orange/Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToCreateDiet: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToDetail: (foodId: Int, initialPortion: String?) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToDietDetail: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by homeViewModel.state.collectAsState()
    val availableDiets by homeViewModel.availableDiets.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var foodToAddToDiet by remember { mutableStateOf<Food?>(null) }
    var quantityToAddToDiet by remember { mutableStateOf("100") }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // flag + position approach that handles auto-scrolling
    // not ideal but keeps the list the way I want in HomeScreen
    var prevShowAllResults by remember { mutableStateOf(state.showAllResults) }
    var pendingScrollToId by remember { mutableStateOf<Int?>(null) }
    var hasMeasured by remember { mutableStateOf(false) }
    var measuredItemY by remember { mutableIntStateOf(0) }

    // 250ms delay ensures it works on most devices
    LaunchedEffect(state.showAllResults) {
        val justExpanded = state.showAllResults && !prevShowAllResults
        prevShowAllResults = state.showAllResults

        if (justExpanded && state.expandedAlimentoId != null) {
            val index = state.searchResults.indexOfFirst { it.id == state.expandedAlimentoId }
            if (index > 2) {
                kotlinx.coroutines.delay(250)
                hasMeasured = false
                pendingScrollToId = state.expandedAlimentoId
            }
        }
    }

    // When the item reports its position (in root coordinates), scroll to it
    LaunchedEffect(measuredItemY) {
        if (pendingScrollToId != null && measuredItemY > 0) {
            // measuredItemY is the item's Y position relative to root (screen top)
            // Lower offset = item appears closer to viewport top = more scrolling
            val targetOffset = with(density) { 60.dp.toPx().toInt() }
            // scrollTarget = how much to scroll so item appears at targetOffset from viewport top
            val scrollTarget = (measuredItemY - targetOffset).coerceAtLeast(0)
            scrollState.animateScrollTo(scrollTarget)
            pendingScrollToId = null
            measuredItemY = 0
        }
    }

    var mealToLog by remember { mutableStateOf<DashboardMealGroup?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = state.isSearchExpanded) {
        if (state.expandedAlimentoId != null) {
            homeViewModel.onAlimentoToggled(state.expandedAlimentoId!!)
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
            homeViewModel.setSearchExpanded(false)
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.effects
            .onEach { effect ->
                when (effect) {
                    is HomeEffect.ShowSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = effect.actionLabel,
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            effect.action?.let { action ->
                                when (action) {
                                    is SnackbarAction.GoToDiary -> onNavigateToDiary()
                                    is SnackbarAction.GoToDiet -> onNavigateToDietDetail(action.dietId)
                                }
                            }
                        }
                    }
                }
            }
            .launchIn(this)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val showCollapseFab =
                state.isSearchExpanded && state.showAllResults && state.searchResults.size > 3
            val showScrollFab = scrollState.value > 600 && !state.isSearchExpanded

            AnimatedVisibility(
                visible = showCollapseFab,
                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                exit = fadeOut(tween(150)) + scaleOut(tween(150))
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        homeViewModel.setShowAllResults(false)
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Mostrar menos"
                    )
                }
            }

            AnimatedVisibility(
                visible = showScrollFab && !showCollapseFab,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { scrollState.animateScrollTo(0) } },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Voltar ao topo"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Combined Header Section (Green Background) + Hero Card
                Box(
                    modifier = Modifier.animateContentSize(tween(300))
                ) {
                    // Green Background (Dynamically sized based on search state)
                    val headerHeight by animateDpAsState(
                        targetValue = if (state.isSearchExpanded) 400.dp else 280.dp,
                        animationSpec = tween(300),
                        label = "headerHeight"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Column(
                        modifier = Modifier.animateContentSize(tween(300))
                    ) {
                        HomeTopBarWithSearch(
                            isSearchActive = state.isSearchExpanded,
                            searchTerm = state.searchTerm,
                            filterState = state.filterState,
                            onSearchClick = { homeViewModel.setSearchExpanded(true) },
                            onSearchClose = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                homeViewModel.setSearchExpanded(false)
                            },
                            onSearchTermChange = homeViewModel::onSearchTermChange,
                            onShowFilters = { showFilterSheet = true },
                            onClearSearch = homeViewModel::cleanSearch,
                            onProfileClick = { showBottomSheet = true },
                            onSettingsClick = onNavigateToSettings,
                            onSourceClear = { homeViewModel.onSourceFilterChange(FoodSource.ALL) },
                            onCategoryClear = { homeViewModel.onCategoryToggle(it) },
                            onAdvancedFiltersClear = { homeViewModel.clearAdvancedFilters() }
                        )

                        // Hero Card
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            DailyProgressHeroCard(
                                progress = state.dailyProgress,
                                onClick = {
                                    val mainDiet = state.dietSummaries.find { it.diet.isMain }
                                    mainDiet?.diet?.id?.let { onNavigateToDietDetail(it) }
                                }
                            )
                        }
                    }
                }

                // Inline Search Results Card (appears when search is active and has results)
                AnimatedVisibility(
                    visible = state.isSearchExpanded && state.searchTerm.length >= 2,
                    enter = slideInVertically(tween(200)) { -it / 2 } + fadeIn(tween(200)),
                    exit = slideOutVertically(tween(150)) { -it / 2 } + fadeOut(tween(150))
                ) {
                    InlineSearchResultsCard(
                        state = state,
                        showAllResults = state.showAllResults,
                        pendingScrollToId = pendingScrollToId,
                        hasMeasured = hasMeasured,
                        onItemPositioned = { yPosition ->
                            measuredItemY = yPosition
                            hasMeasured = true
                        },
                        onToggleShowAll = { homeViewModel.setShowAllResults(!state.showAllResults) },
                        onItemToggle = homeViewModel::onAlimentoToggled,
                        onAmountChange = homeViewModel::onQuickAddAmountChange,
                        onNavigateToDetail = { foodId ->
                            onNavigateToDetail(
                                foodId,
                                state.quickAddAmount
                            )
                        },
                        onCloneAndEdit = { food ->
                            if (!food.isCustom) {
                                homeViewModel.cloneAndEdit(food) { newId ->
                                    onNavigateToEdit(newId)
                                }
                            } else {
                                onNavigateToEdit(food.id)
                            }
                        },
                        onLog = { food ->
                            val qty = state.quickAddAmount.toDoubleOrNull() ?: 100.0
                            homeViewModel.addFoodToLog(food, qty)
                        },
                        onAddToDiet = { food ->
                            foodToAddToDiet = food
                            quantityToAddToDiet = state.quickAddAmount
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Meals & Water
                MealsAndWaterRow(
                    nextMeal = state.nextMeal,
                    waterIntake = state.waterIntake,
                    onAddWater = { homeViewModel.addWater(250) },
                    onLogNextMeal = { state.nextMeal?.let { homeViewModel.addMealToLog(it) } },
                    onEditNextMeal = {
                        state.nextMeal?.let { mealToLog = it }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Timeline
                TimelineSection(
                    timeline = state.dailyTimeline,
                    onNavigateToDiary = onNavigateToDiary
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialogs
    if (foodToAddToDiet != null) {
        com.mekki.taco.presentation.ui.components.AddToDietDialog(
            food = foodToAddToDiet!!,
            diets = availableDiets,
            initialQuantity = quantityToAddToDiet,
            onDismiss = { foodToAddToDiet = null },
            onConfirm = { dietId, qty, meal, time ->
                homeViewModel.addFoodToDiet(dietId, foodToAddToDiet!!.id, qty, meal, time)
                foodToAddToDiet = null
            }
        )
    }


    // for now it only shows a confirmation dialog that lists items.
    if (mealToLog != null) {
        ConfirmLogDialog(
            meal = mealToLog!!,
            onDismiss = { mealToLog = null },
            onConfirm = { selectedItems ->
                if (selectedItems.isNotEmpty()) {
                    // Create a copy of the meal with only the selected items
                    val mealToLogWithItems = mealToLog!!.copy(items = selectedItems)
                    homeViewModel.addMealToLog(mealToLogWithItems)
                }
                mealToLog = null
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            ProfileSheetContent(
                viewModel = profileViewModel,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                },
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filterState = state.filterState,
            categories = state.categories,
            onDismiss = { showFilterSheet = false },
            onSourceChange = { homeViewModel.onSourceFilterChange(it) },
            onCategoryToggle = { homeViewModel.onCategoryToggle(it) },
            onClearCategories = { homeViewModel.onClearCategories() },
            onSortChange = { homeViewModel.onSortOptionSelected(it) },
            onResetFilters = { homeViewModel.onResetFilters() },
            onFilterStateChange = { homeViewModel.onFilterStateChange(it) },
            showCategories = true,
            showAdvancedFilters = true
        )
    }
}

@Composable
fun HomeTopBarWithSearch(
    isSearchActive: Boolean,
    searchTerm: String,
    filterState: FoodFilterState,
    onSearchClick: () -> Unit,
    onSearchClose: () -> Unit,
    onSearchTermChange: (String) -> Unit,
    onShowFilters: () -> Unit,
    onClearSearch: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSourceClear: () -> Unit,
    onCategoryClear: (String) -> Unit,
    onAdvancedFiltersClear: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var wasSearchActive by remember { mutableStateOf(isSearchActive) }

    val dateStr = remember {
        val now = LocalDate.now()
        val locale = Locale("pt", "BR")
        val formatter =
            DateTimeFormatter.ofPattern("EEE, d 'de' MMM", locale)
        "Hoje, " + now.format(formatter).replaceFirstChar { it.uppercase() }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive && !wasSearchActive) {
            focusRequester.requestFocus()
        }
        wasSearchActive = isSearchActive
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .animateContentSize(tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                label = "topBarContent"
            ) { searchActive ->
                if (searchActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchTerm,
                            onValueChange = onSearchTermChange,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    "Buscar alimento...",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            trailingIcon = if (searchTerm.isNotEmpty()) {
                                {
                                    IconButton(onClick = onClearSearch) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Limpar",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = 0.3f
                                ),
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = 0.1f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = 0.05f
                                )
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { keyboardController?.hide() }
                            )
                        )
                        IconButton(
                            onClick = onSearchClose
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar busca",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Busca",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onSearchClick
                                    )
                            )
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurações",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onSettingsClick
                                    )
                            )
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onProfileClick
                                    )
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    InputChip(
                        selected = true,
                        onClick = onShowFilters,
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

                if (filterState.activeAdvancedFilterCount > 0) {
                    item {
                        InputChip(
                            selected = true,
                            onClick = onAdvancedFiltersClear,
                            label = { Text("+${filterState.activeAdvancedFilterCount} filtros") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FilterList,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }

                item {
                    IconButton(onClick = onShowFilters) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtros avançados",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyProgressHeroCard(
    progress: DailyProgress,
    onClick: () -> Unit
) {
    val effectiveScale = rememberEffectiveScale(baselineDensityDp = 2.75f)
    // Compact when density+fontScale combination makes things too large
    val isCompact = effectiveScale > 1.2f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Progresso Do Dia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 24.dp))

            Box(contentAlignment = Alignment.Center) {
                val consumedRatio =
                    (progress.consumedKcal / progress.targetKcal).coerceIn(0.0, 1.0).toFloat()
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.outlineVariant

                // Responsive canvas: fraction of width + aspect ratio
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(if (isCompact) 0.5f else 0.6f)
                        .aspectRatio(2f)
                ) {
                    drawArc(
                        color = trackColor.copy(alpha = 0.3f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 40f, cap = StrokeCap.Round),
                        size = Size(size.width, size.height * 2)
                    )
                    // Progress Arc
                    drawArc(
                        color = primaryColor,
                        startAngle = 180f,
                        sweepAngle = 180f * consumedRatio,
                        useCenter = false,
                        style = Stroke(width = 40f, cap = StrokeCap.Round),
                        size = Size(size.width, size.height * 2)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = progress.consumedKcal.toInt().toString(),
                            // Downsize the headline at high effective scales
                            style = if (isCompact)
                                MaterialTheme.typography.headlineSmall
                            else
                                MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Text(
                            text = " / ${progress.targetKcal.toInt()} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = if (isCompact) 2.dp else 4.dp),
                            maxLines = 1
                        )
                    }
                    if (progress.consumedKcal > 0) {
                        val remaining = (progress.targetKcal - progress.consumedKcal)
                            .coerceAtLeast(0.0)
                        Text(
                            text = "Restam ${remaining.toInt()} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 32.dp))

            // Macros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroProgressItem(
                    label = if (isCompact) "Prot." else "Proteínas",
                    value = progress.consumedProtein,
                    target = progress.targetProtein,
                    color = COLOR_PROTEIN,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 16.dp))
                MacroProgressItem(
                    label = if (isCompact) "Carbs" else "Carboidratos",
                    value = progress.consumedCarbs,
                    target = progress.targetCarbs,
                    color = COLOR_CARBS,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 16.dp))
                MacroProgressItem(
                    label = if (isCompact) "Gord." else "Gorduras",
                    value = progress.consumedFat,
                    target = progress.targetFat,
                    color = COLOR_FAT,
                    isCompact = isCompact,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroProgressItem(
    label: String,
    value: Double,
    target: Double,
    color: Color,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${value.toInt()}g / ${target.toInt()}g",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))
        LinearProgressIndicator(
            progress = { (value / target).coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun MealsAndWaterRow(
    nextMeal: DashboardMealGroup?,
    waterIntake: WaterIntake,
    onAddWater: () -> Unit,
    onLogNextMeal: () -> Unit,
    onEditNextMeal: () -> Unit
) {
    val effectiveScale = rememberEffectiveScale(baselineDensityDp = 2.75f)
    val isCompact = effectiveScale > 1.2f
    val cardHeight = if (isCompact) 160.dp else 140.dp

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Refeições e Água",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Next Meal Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (nextMeal != null) {
                        Column {
                            Text(
                                text = "Próxima: ${nextMeal.mealType}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextMeal.items.joinToString(", ") { it.food.name },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${nextMeal.time} • ${nextMeal.items.sumOf { it.food.energiaKcal?.toInt() ?: 0 }} kcal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(onClick = onEditNextMeal)
                            )

                            Button(
                                onClick = onLogNextMeal,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    "Registrar",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Nada restante por hoje",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Water Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Consumo de Água",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val ptBr = Locale("pt", "BR")
                            val litters =
                                String.format(ptBr, "%.1fL", waterIntake.currentMl / 1000f)
                            val targetL =
                                String.format(ptBr, "%.0fL", waterIntake.targetMl / 1000f)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(
                                            litters
                                        )
                                    }
                                    append(" / $targetL")
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = onAddWater,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        //TODO: should be customizable a glass a bottle etc.
                        Text(
                            "Registrar Copo (300ml)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineSection(
    timeline: List<DashboardMealGroup>,
    onNavigateToDiary: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Cronograma do Dia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (timeline.isEmpty()) {
                    Text(
                        text = "Nenhuma refeição agendada para hoje.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    timeline.forEachIndexed { index, meal ->
                        TimelineRow(
                            meal = meal,
                            isLast = index == timeline.lastIndex
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TimelineRow(
    meal: DashboardMealGroup,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Time & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val color = when (meal.mealType.lowercase()) {
                    "café da manhã" -> Color(0xFFFFCC80)
                    "almoço" -> Color(0xFFA5D6A7)
                    "jantar" -> Color(0xFF90CAF9)
                    else -> Color(0xFFCE93D8)
                }
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)
        ) {
            val summary = meal.items.joinToString(", ") { it.food.name }
            val calories = meal.items.sumOf { it.food.energiaKcal?.toInt() ?: 0 }

            Text(
                text = "${meal.mealType}: $summary - $calories kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = meal.time, // e.g. "12:30"
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InlineSearchResultsCard(
    state: HomeState,
    showAllResults: Boolean,
    pendingScrollToId: Int?,
    hasMeasured: Boolean,
    onItemPositioned: (Int) -> Unit,
    onToggleShowAll: () -> Unit,
    onItemToggle: (Int) -> Unit,
    onAmountChange: (String) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onCloneAndEdit: (Food) -> Unit,
    onLog: (Food) -> Unit,
    onAddToDiet: (Food) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val previewCount = 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(tween(250)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            when {
                state.searchIsLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.searchResults.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nenhum alimento encontrado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    val itemsToShow = if (showAllResults) state.searchResults
                    else state.searchResults.take(previewCount)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.searchResults.size} resultados",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Column(
                        modifier = Modifier
                            .animateContentSize(tween(200))
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsToShow.forEachIndexed { index, food ->
                            val isExpanded = state.expandedAlimentoId == food.id
                            val shouldMeasure = food.id == pendingScrollToId && !hasMeasured
                            Box(
                                modifier = if (shouldMeasure) {
                                    Modifier.onGloballyPositioned { coordinates ->
                                        val yInRoot = coordinates.positionInRoot().y.toInt()
                                        onItemPositioned(yInRoot)
                                    }
                                } else Modifier
                            ) {
                                com.mekki.taco.presentation.ui.components.SearchItem(
                                    food = food,
                                    isExpanded = isExpanded,
                                    onToggle = {
                                        onItemToggle(food.id)
                                        keyboardController?.hide()
                                    },
                                    onNavigateToDetail = { onNavigateToDetail(it.id) },
                                    currentAmount = state.quickAddAmount,
                                    onAmountChange = onAmountChange,
                                    onLog = { onLog(food) },
                                    onAddToDiet = { onAddToDiet(food) },
                                    onFastEdit = { onCloneAndEdit(it) },
                                    showLogTutorial = state.showRegistrarTutorial,
                                    resultIndex = index + 1,
                                    highlightedNutrient = state.filterState.sortOption.getNutrientDisplayInfo()
                                        ?: state.filterState.getFirstActiveAdvancedFilterInfo()
                                )
                            }
                        }
                    }

                    if (state.searchResults.size > previewCount) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        TextButton(
                            onClick = onToggleShowAll,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (showAllResults)
                                    "Mostrar menos"
                                else
                                    "Ver mais resultados (${state.searchResults.size - previewCount})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmLogDialog(
    meal: DashboardMealGroup,
    onDismiss: () -> Unit,
    onConfirm: (List<DietItemWithFood>) -> Unit
) {
    val itemsToLog = remember(meal) { meal.items.toMutableStateList() }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Registrar ${meal.mealType}?") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Isso adicionará os seguintes itens ao seu diário de hoje:")
                Spacer(Modifier.height(16.dp))

                if (itemsToLog.isEmpty()) {
                    Text(
                        text = "Nenhum item selecionado para registro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    itemsToLog.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.food.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.dietItem.quantityGrams.toInt()}g • ${item.food.energiaKcal?.toInt() ?: 0} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { itemsToLog.remove(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover item",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(itemsToLog.toList()) },
                enabled = itemsToLog.isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}