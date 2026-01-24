package com.mekki.taco.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.components.ChartType
import com.mekki.taco.presentation.ui.components.MacroBarChart
import com.mekki.taco.presentation.ui.components.MacroPieChart
import com.mekki.taco.presentation.ui.components.PieChartData
import com.mekki.taco.presentation.ui.components.SearchItem
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val COLOR_CARBS = Color(0xFFDCC48E)
private val COLOR_PROTEIN = Color(0xFF2E7A7A)
private val COLOR_FAT = Color(0xFFC97C4A)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToDietList: () -> Unit,
    onNavigateToCreateDiet: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToDietDetail: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToDatabase: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by homeViewModel.state.collectAsState()
    val availableDiets by homeViewModel.availableDiets.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var foodToAddToDiet by remember { mutableStateOf<Food?>(null) }
    var quantityToAddToDiet by remember { mutableStateOf("100") }
    val scrollState = rememberScrollState()

    var isFabExpanded by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

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
            HomeExpandableFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onActionDiet = {
                    isFabExpanded = false
                    onNavigateToCreateDiet()
                },
                onActionDiary = {
                    isFabExpanded = false
                    onNavigateToDiary()
                },
                onActionFood = {
                    isFabExpanded = false
                    onNavigateToDetail(0)
                }
            )
        }
        // start of dashboard items
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeHeader(
                    username = "Usuário", // UserProfile lacks name field for now
                    onNavigateToDiary = onNavigateToDiary,
                    onSettingsClick = onNavigateToSettings,
                    onProfileClick = { showBottomSheet = true }
                )
                QuickSearchCard(
                    state = state,
                    viewModel = homeViewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToEdit = onNavigateToEdit,
                    isExpanded = isSearchExpanded,
                    onExpandChange = { isSearchExpanded = it },
                    onAddToDietRequest = { food ->
                        foodToAddToDiet = food
                        quantityToAddToDiet = state.quickAddAmount
                    }
                )

                // Charts (Plano Atual)
                val mainDiet = state.dietSummaries.find { it.diet.isMain }
                    ?: state.dietSummaries.firstOrNull()

                MainPlanSection(
                    mainDiet = mainDiet,
                    onNavigateToCreateDiet = onNavigateToCreateDiet,
                    onNavigateToDietDetail = onNavigateToDietDetail,
                    onUpdateChartPreference = { dietId, type ->
                        homeViewModel.updateDietChartPreference(
                            dietId,
                            type
                        )
                    }
                )

                // Next Meal
                state.nextMeal?.let { nextMealGroup ->
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        NextMealHeroCard(
                            meal = nextMealGroup,
                            onLogMeal = { homeViewModel.addMealToLog(nextMealGroup) }
                        )
                    }
                }

                // Quick Access
                Column {
                    PaddingHeader(title = "Acesso Rápido", onClick = { })
                    QuickAccessRow(
                        onNavigateToDietList = onNavigateToDietList,
                        onNavigateToDiary = onNavigateToDiary,
                        onNavigateToDatabase = onNavigateToDatabase
                    )
                }

                // Timeline
                if (state.dailyTimeline.isNotEmpty()) {
                    DayTimelineSection(
                        timeline = state.dailyTimeline,
                        onMealClick = { }
                    )
                }

                // Other Plans
                val otherDiets = if (mainDiet != null) {
                    state.dietSummaries.filter { it.diet.id != mainDiet.diet.id }
                } else {
                    emptyList()
                }

                if (otherDiets.isNotEmpty()) {
                    OtherPlansSection(
                        otherDiets = otherDiets,
                        onNavigateToDietDetail = onNavigateToDietDetail,
                        onSetDietAsDefault = { dietId -> homeViewModel.setMainDiet(dietId) }
                    )
                }

                Spacer(Modifier.height(80.dp))
            }

            AnimatedVisibility(
                visible = scrollState.value > 600,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
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

            if (isFabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { isFabExpanded = false }
                )
            }
        }
    }

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
}

@Composable
fun HomeHeader(
    username: String,
    onNavigateToDiary: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val dateStr = remember {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale("pt", "BR"))
        now.format(formatter).replaceFirstChar { it.uppercase() }
    }

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> "Bom dia"
            in 12..18 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.clickable(onClick = onNavigateToDiary)
        ) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = "$greeting, $username",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Configurações")
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
fun QuickSearchCard(
    state: HomeState,
    viewModel: HomeViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onAddToDietRequest: (Food) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // We want to make sure expansion is reset in case it does not apply for new search
    LaunchedEffect(state.searchTerm) {
        if (state.searchTerm.isEmpty()) {
            onExpandChange(false)
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = state.searchTerm,
                onValueChange = viewModel::onSearchTermChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar um alimento") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                singleLine = true,
                trailingIcon = {
                    if (state.searchTerm.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.cleanSearch()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar Busca")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (state.searchTerm.length >= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FoodSortOption.values().forEach { option ->
                        FilterChip(
                            selected = state.sortOption == option,
                            onClick = { viewModel.onSortOptionSelected(option) },
                            label = { Text(option.label) },
                            leadingIcon = if (state.sortOption == option) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            AnimatedVisibility(visible = state.searchTerm.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    when {
                        state.searchIsLoading -> {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }

                        state.searchResults.isEmpty() && state.searchTerm.length >= 2 -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Nenhum resultado encontrado.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        else -> {
                            val results = state.searchResults
                            val PREVIEW_COUNT = 5
                            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                            val configuration = LocalConfiguration.current
                            val screenHeight = configuration.screenHeightDp.dp

                            LaunchedEffect(isExpanded) {
                                if (isExpanded) {
                                    val expandedId = state.expandedAlimentoId
                                    val selectedIndex = if (expandedId != null) {
                                        results.indexOfFirst { it.id == expandedId }
                                    } else -1

                                    val scrollTarget =
                                        if (selectedIndex >= 0) selectedIndex else PREVIEW_COUNT
                                    try {
                                        listState.animateScrollToItem(scrollTarget)
                                    } catch (e: Exception) {
                                    }
                                }
                            }

                            if (!isExpanded && results.size > PREVIEW_COUNT) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    results.take(PREVIEW_COUNT).forEach { alimento ->
                                        SearchItem(
                                            food = alimento,
                                            isExpanded = state.expandedAlimentoId == alimento.id,
                                            onToggle = {
                                                viewModel.onAlimentoToggled(alimento.id)
                                                keyboardController?.hide()
                                            },
                                            onNavigateToDetail = {
                                                onNavigateToDetail(alimento.id)
                                            },
                                            onFastEdit = {
                                                // upon editing a food, we must check if it belongs to TACO preload so it can get cloned first
                                                // original taco db should never be editable
                                                if (!alimento.isCustom) {
                                                    viewModel.cloneAndEdit(alimento) { newId ->
                                                        onNavigateToEdit(newId)
                                                    }
                                                } else {
                                                    onNavigateToEdit(alimento.id)
                                                }
                                            },
                                            currentAmount = state.quickAddAmount,
                                            onAmountChange = viewModel::onQuickAddAmountChange,
                                            onLog = {
                                                val qty =
                                                    state.quickAddAmount.toDoubleOrNull() ?: 100.0
                                                viewModel.addFoodToLog(alimento, qty)
                                            },
                                            onAddToDiet = { onAddToDietRequest(alimento) },
                                            showLogTutorial = state.showRegistrarTutorial,
                                            onDismissLogTutorial = viewModel::dismissRegistrarTutorial
                                        )
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(
                                            alpha = 0.5f
                                        )
                                    )

                                    TextButton(
                                        onClick = { onExpandChange(true) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver mais ${results.size - PREVIEW_COUNT} resultados")
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.KeyboardArrowDown, null)
                                    }
                                }
                            } else {
                                Column {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = screenHeight),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(results.size) { index ->
                                            val alimento = results[index]
                                            SearchItem(
                                                food = alimento,
                                                isExpanded = state.expandedAlimentoId == alimento.id,
                                                onToggle = {
                                                    viewModel.onAlimentoToggled(alimento.id)
                                                    keyboardController?.hide()
                                                },
                                                onNavigateToDetail = {
                                                    onNavigateToDetail(alimento.id)
                                                },
                                                onFastEdit = {
                                                    if (!alimento.isCustom) {
                                                        viewModel.cloneAndEdit(alimento) { newId ->
                                                            onNavigateToEdit(newId)
                                                        }
                                                    } else {
                                                        onNavigateToEdit(alimento.id)
                                                    }
                                                },
                                                currentAmount = state.quickAddAmount,
                                                onAmountChange = viewModel::onQuickAddAmountChange,
                                                onLog = {
                                                    val qty = state.quickAddAmount.toDoubleOrNull()
                                                        ?: 100.0
                                                    viewModel.addFoodToLog(alimento, qty)
                                                },
                                                onAddToDiet = { onAddToDietRequest(alimento) },
                                                showLogTutorial = state.showRegistrarTutorial,
                                                onDismissLogTutorial = viewModel::dismissRegistrarTutorial
                                            )
                                        }
                                    }

                                    if (results.size > PREVIEW_COUNT) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onExpandChange(false) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Ver menos",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessRow(
    onNavigateToDietList: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToDatabase: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { QuickAccessCard("Gerenciar\nDietas", Icons.Default.Book, onNavigateToDietList) }
        item { QuickAccessCard("Diário", Icons.Default.EditCalendar, onNavigateToDiary) }
        item { QuickAccessCard("Banco de\nAlimentos", Icons.Default.Search, onNavigateToDatabase) }
    }
}

@Composable
fun QuickAccessCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.size(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NextMealHeroCard(
    meal: DashboardMealGroup,
    onLogMeal: () -> Unit
) {
    val summary = remember(meal.items) {
        meal.items.joinToString(", ") { it.food.name }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = meal.time,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = meal.mealType,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onLogMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Registrar Refeição")
            }
        }
    }
}

@Composable
fun DayTimelineSection(
    timeline: List<DashboardMealGroup>,
    onMealClick: (DashboardMealGroup) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        PaddingHeader(title = "Cronograma de Hoje", onClick = { })

        Column(Modifier.padding(horizontal = 24.dp)) {
            timeline.forEachIndexed { index, meal ->
                TimelineItem(
                    meal = meal,
                    isLast = index == timeline.lastIndex,
                    onClick = { onMealClick(meal) }
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    meal: DashboardMealGroup,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (meal.isPassed) 0.5f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
    ) {
        Text(
            text = meal.time,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(50.dp)
                .padding(top = 4.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (meal.isPassed) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary)
                    .padding(top = 4.dp)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = 24.dp, top = 0.dp)) {
            Text(
                text = meal.mealType,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${meal.items.size} itens • ${meal.items.sumOf { it.food.energiaKcal?.toInt() ?: 0 }} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MainPlanSection(
    mainDiet: DietSummary?,
    onNavigateToCreateDiet: () -> Unit,
    onNavigateToDietDetail: (Int) -> Unit,
    onUpdateChartPreference: (Int, ChartType) -> Unit
) {
    Column {
        PaddingHeader(title = "Plano Atual", onClick = { })

        if (mainDiet == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Você ainda não criou nenhuma dieta.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onNavigateToCreateDiet) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Criar Dieta")
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                DietGraphicCard(
                    title = mainDiet.diet.name,
                    food = mainDiet.totalNutrition,
                    chartType = mainDiet.chartType,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { mainDiet.diet.id?.let { onNavigateToDietDetail(it) } },
                    onToggleChartType = {
                        val newType =
                            if (mainDiet.chartType == ChartType.PIE) ChartType.BAR else ChartType.PIE
                        mainDiet.diet.id?.let { id -> onUpdateChartPreference(id, newType) }
                    }
                )
            }
        }
    }
}

@Composable
fun OtherPlansSection(
    otherDiets: List<DietSummary>,
    onNavigateToDietDetail: (Int) -> Unit,
    onSetDietAsDefault: (Int) -> Unit
) {
    Column {
        PaddingHeader(title = "Outros Planos", onClick = { })

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(otherDiets) { summary ->
                CompactDietCard(
                    summary = summary,
                    onClick = { summary.diet.id?.let { onNavigateToDietDetail(it) } },
                    onSetDefault = { summary.diet.id?.let { onSetDietAsDefault(it) } }
                )
            }
        }
    }
}

@Composable
fun CompactDietCard(
    summary: DietSummary,
    onClick: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.width(180.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = summary.diet.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${summary.totalNutrition.energiaKcal?.toInt() ?: 0} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onSetDefault,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.StarOutline,
                        contentDescription = "Definir como padrão",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val total = summary.totalNutrition.energiaKcal ?: 1.0
            val p = (summary.totalNutrition.proteina ?: 0.0) * 4
            val c = (summary.totalNutrition.carboidratos ?: 0.0) * 4
            val f = (summary.totalNutrition.lipidios?.total ?: 0.0) * 9

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (total > 0) {
                    Box(
                        Modifier
                            .weight((p / total).toFloat())
                            .fillMaxSize()
                            .background(COLOR_PROTEIN)
                    )
                    Box(
                        Modifier
                            .weight((c / total).toFloat())
                            .fillMaxSize()
                            .background(COLOR_CARBS)
                    )
                    Box(
                        Modifier
                            .weight((f / total).toFloat())
                            .fillMaxSize()
                            .background(COLOR_FAT)
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DietGraphicCard(
    title: String,
    food: Food,
    chartType: ChartType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleChartType: () -> Unit
) {
    var showTip by rememberSaveable { mutableStateOf(true) }

    val pieData = listOf(
        PieChartData(food.carboidratos?.toFloat() ?: 0f, COLOR_CARBS, "Carboidratos"),
        PieChartData(food.proteina?.toFloat() ?: 0f, COLOR_PROTEIN, "Proteínas"),
        PieChartData(food.lipidios?.total?.toFloat() ?: 0f, COLOR_FAT, "Gorduras")
    )
    val totalKcal = food.energiaKcal ?: 0.0

    Card(
        modifier = modifier
            .animateContentSize()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onToggleChartType()
                    showTip = false
                }
            ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (showTip) {
                            Text(
                                text = "Segure para alterar visualização",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Crossfade(targetState = chartType, label = "ChartSwitcher") { type ->
                    when (type) {
                        ChartType.PIE -> {
                            MacroPieChart(
                                data = pieData,
                                totalValue = totalKcal,
                                totalUnit = "kcal",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        ChartType.BAR -> {
                            MacroBarChart(
                                data = pieData,
                                totalValue = totalKcal,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeExpandableFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onActionDiet: () -> Unit,
    onActionDiary: () -> Unit,
    onActionFood: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabActionRow(
                    text = "Novo Alimento",
                    icon = Icons.Default.RestaurantMenu,
                    onClick = onActionFood,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                FabActionRow(
                    text = "Registrar no Diário",
                    icon = Icons.Default.EditCalendar,
                    onClick = onActionDiary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                FabActionRow(
                    text = "Criar Nova Dieta",
                    icon = Icons.AutoMirrored.Filled.NoteAdd,
                    onClick = onActionDiet,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Menu de Ações",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
fun FabActionRow(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Icon(icon, contentDescription = null)
        }
    }
}

@Composable
fun PaddingHeader(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ver todos",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
