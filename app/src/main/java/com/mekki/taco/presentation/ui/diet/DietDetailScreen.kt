package com.mekki.taco.presentation.ui.diet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.data.model.DietWithItems
import com.mekki.taco.presentation.ui.components.DiscardChangesDialog
import com.mekki.taco.presentation.ui.components.FilterBottomSheet
import com.mekki.taco.presentation.ui.components.MacroIconRow
import com.mekki.taco.presentation.ui.components.NutritionalSummaryCard
import com.mekki.taco.presentation.ui.components.PortionControlInput
import com.mekki.taco.presentation.ui.components.ScanResultDialog
import com.mekki.taco.presentation.ui.components.SearchItem
import com.mekki.taco.presentation.ui.components.TimeControlInput
import com.mekki.taco.presentation.ui.components.UnifiedFoodItemActionsSheet
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSearchState
import com.mekki.taco.presentation.ui.search.FoodSortOption
import com.mekki.taco.presentation.ui.search.FoodSource
import com.mekki.taco.presentation.ui.search.getNutrientDisplayInfo
import com.mekki.taco.utils.NutrientCalculator
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.DecimalFormat


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DietDetailScreen(
    viewModel: DietDetailViewModel,
    profileViewModel: ProfileViewModel,
    onEditFood: (Int) -> Unit,
    onViewFood: (Int, Double?) -> Unit,
    onTitleChange: (String) -> Unit,
    onFabChange: (@Composable (() -> Unit)?) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDiary: () -> Unit = {}
) {
    val groupedItems by viewModel.groupedItems.collectAsState()
    val dietTotalNutrition by viewModel.dietTotalNutrition.collectAsState()
    val dietDetails by viewModel.dietDetails.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val itemToReplaceFood by viewModel.replacingItem.collectAsState()

    // Scanner State
    val isScanning by viewModel.isScanning.collectAsState()
    val isProcessingScan by viewModel.isProcessingScan.collectAsState()
    val showScanReview by viewModel.showScanReview.collectAsState()
    val scannedCandidates by viewModel.scannedCandidates.collectAsState()

    // Search State
    val focusedMealType by viewModel.focusedMealType.collectAsState()
    val foodSearchState by viewModel.foodSearchManager.state.collectAsState()

    // Smart Goals
    val userProfile by viewModel.userProfile.collectAsState()
    val suggestedGoals by viewModel.suggestedGoals.collectAsState()

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()

    val scope = rememberCoroutineScope()
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Profile Sheet State
    val profileSheetState = rememberModalBottomSheetState()
    var showProfileSheet by remember { mutableStateOf(false) }

    var showExitDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<DietItem?>(null) }
    var itemToClone by remember { mutableStateOf<DietItemWithFood?>(null) }
    var itemForSheet by remember { mutableStateOf<DietItemWithFood?>(null) }

    // Observe Navigation
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { onNavigateBack() }
    }
    LaunchedEffect(Unit) {
        viewModel.navigateToEditFood.collect { onEditFood(it) }
    }

    val dietName = dietDetails?.diet?.name ?: ""
    LaunchedEffect(dietName) {
        val title = dietName.ifBlank { "Nova Dieta" }
        onTitleChange(title)
    }

    val dietId = dietDetails?.diet?.id ?: -1
    val handleBackPress = {
        when {
            // Priority 1: Clear selection mode (UI state)
            isSelectionMode -> {
                viewModel.clearSelection()
            }
            // Priority 2: Standard exit flow
            isEditMode -> {
                if (hasUnsavedChanges) {
                    showExitDialog = true
                } else {
                    if (dietId != -1) viewModel.setEditMode(false) else onNavigateBack()
                }
            }

            else -> {
                onNavigateBack()
            }
        }
    }
    BackHandler(onBack = handleBackPress)

    // AI
    if (isScanning) {
        ScanDietScreen(
            onPhotoCaptured = viewModel::onPhotoCaptured, onCancel = viewModel::onCancelScan
        )
        return
    }
    if (isProcessingScan) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text("Processando...", Modifier.padding(top = 16.dp))
            }
        }
        return
    }
    if (showScanReview) {
        ScanResultDialog(
            candidates = scannedCandidates,
            mealTypes = viewModel.mealTypes,
            onConfirm = viewModel::onConfirmScanResults,
            onDismiss = viewModel::onDiscardScanResults
        )
    }

    // removed or handled elsewhere
    DisposableEffect(Unit) {
        onFabChange(null)
        onDispose { onFabChange(null) }
    }


    DietDetailContent(
        groupedItems = groupedItems,
        dietTotalNutrition = dietTotalNutrition,
        dietDetails = dietDetails,
        isEditMode = isEditMode,
        hasUnsavedChanges = hasUnsavedChanges,
        suggestedGoals = suggestedGoals,
        isProfileComplete = userProfile?.weight != null,
        mealTypes = viewModel.mealTypes,
        onBackClick = handleBackPress,
        onStartScan = viewModel::onStartScan,
        onEditModeChange = viewModel::setEditMode,
        onSaveDiet = viewModel::saveDiet,
        onReorderFoodItemsInMeal = viewModel::reorderFoodItemsInMeal,
        onDietNameChange = viewModel::onDietNameChange,
        onCalorieGoalChange = viewModel::onCalorieGoalChange,
        onApplyGoal = viewModel::applySmartGoal,
        onOpenProfile = { showProfileSheet = true },
        onSetFocusedMealType = viewModel::setFocusedMealType,
        onViewFood = onViewFood,
        onShowItemOptions = { itemForSheet = it },
        onSwipeAddToLog = { item ->
            viewModel.addToDailyLog(item)
        },
        canUndo = canUndo,
        canRedo = canRedo,
        onUndo = viewModel::undo,
        onRedo = viewModel::redo,
        isSelectionMode = isSelectionMode,
        selectedItemIds = selectedItemIds,
        onToggleSelection = viewModel::toggleSelection,
        onClearSelection = viewModel::clearSelection,
        onDeleteSelected = viewModel::deleteSelectedItems,
        onAddSelectedToLog = viewModel::addSelectedToDailyLog,
        onMoveSelectedToMeal = viewModel::moveSelectedToMeal,
        onCloneSelectedToMeal = viewModel::cloneSelectedToMeal
    )

    if (focusedMealType != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setFocusedMealType(null) },
            sheetState = searchSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SearchFoodSheetContent(
                mealType = focusedMealType!!,
                addedItems = groupedItems[focusedMealType] ?: emptyList(),
                searchState = foodSearchState,
                categories = viewModel.foodSearchManager.categories.collectAsState().value,
                onSearchTermChange = viewModel.foodSearchManager::onSearchTermChange,
                onSortOptionChange = viewModel.foodSearchManager::onSortOptionChange,
                onSourceChange = viewModel.foodSearchManager::onSourceFilterChange,
                onCategoryToggle = viewModel.foodSearchManager::onCategoryToggle,
                onClearCategories = viewModel.foodSearchManager::onClearCategories,
                onFilterStateChange = viewModel.foodSearchManager::onFilterStateChange,
                onResetFilters = viewModel.foodSearchManager::clear,
                onFoodToggled = viewModel.foodSearchManager::onFoodToggled,
                onAmountChange = viewModel.foodSearchManager::onQuickAddAmountChange,
                onAddFood = { food, qty ->
                    viewModel.addFoodToMeal(food, qty)
                },
                onRemoveItem = { item ->
                    viewModel.deleteItem(item.dietItem)
                },
                onNavigateToDetail = onViewFood,
                onNavigateToCreate = { viewModel.onStartCreateFood() }
            )
        }
    }

    // all dialogs
    itemToDelete?.let { currentItem ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Remover Alimento") },
            text = { Text("Tem certeza que deseja remover este alimento da dieta?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(currentItem)
                    itemToDelete = null
                }) { Text("Remover", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancelar") } })
    }

    itemToReplaceFood?.let { currentItem ->
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.setReplacingItem(null)
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ReplaceFoodSheetContent(
                currentFood = currentItem.food,
                searchState = foodSearchState,
                categories = viewModel.foodSearchManager.categories.collectAsState().value,
                onSearchTermChange = viewModel.foodSearchManager::onSearchTermChange,
                onSortOptionChange = viewModel.foodSearchManager::onSortOptionChange,
                onSourceChange = viewModel.foodSearchManager::onSourceFilterChange,
                onCategoryToggle = viewModel.foodSearchManager::onCategoryToggle,
                onClearCategories = viewModel.foodSearchManager::onClearCategories,
                onFilterStateChange = viewModel.foodSearchManager::onFilterStateChange,
                onResetFilters = viewModel.foodSearchManager::clear,
                onFoodToggled = viewModel.foodSearchManager::onFoodToggled,
                onAmountChange = viewModel.foodSearchManager::onQuickAddAmountChange,
                onSelectFood = { newFood ->
                    viewModel.replaceFood(currentItem, newFood)
                    viewModel.setReplacingItem(null)
                },
                onNavigateToDetail = onViewFood,
                onNavigateToCreate = { viewModel.onStartCreateFood() },
                onCancel = {
                    viewModel.setReplacingItem(null)
                }
            )
        }
    }

    itemForSheet?.let { currentItem ->
        UnifiedFoodItemActionsSheet(
            foodName = currentItem.food.name,
            food = currentItem.food,
            currentQuantity = currentItem.dietItem.quantityGrams,
            currentTime = currentItem.dietItem.consumptionTime ?: "",
            currentMealType = currentItem.dietItem.mealType ?: viewModel.mealTypes.first(),
            originalQuantity = currentItem.dietItem.quantityGrams,
            isEditMode = isEditMode,
            mealTypes = viewModel.mealTypes,
            onDismiss = { itemForSheet = null },
            onViewDetails = { onViewFood(currentItem.food.id, currentItem.dietItem.quantityGrams) },
            onUpdateItem = { qty, time, meal ->
                viewModel.updateItem(
                    currentItem.dietItem.copy(
                        quantityGrams = qty, consumptionTime = time, mealType = meal
                    )
                )
            },
            onEditNutrients = { viewModel.editFood(currentItem) },
            onReplaceFood = { viewModel.setReplacingItem(currentItem) },
            onCloneToMeal = { itemToClone = currentItem },
            onMoveToMeal = { targetMeal -> viewModel.moveItemToMeal(currentItem, targetMeal) },
            onAddToLog = { viewModel.addToDailyLog(currentItem) },
            onDelete = { itemToDelete = currentItem.dietItem }
        )
    }

    itemToClone?.let { currentItem ->
        CloneFoodDialog(
            item = currentItem,
            mealTypes = viewModel.mealTypes,
            onDismiss = { itemToClone = null },
            onConfirm = { targetMeal ->
                viewModel.cloneItemToMeal(currentItem, targetMeal)
                itemToClone = null
            })
    }

    if (showExitDialog) {
        DiscardChangesDialog(onDismissRequest = { showExitDialog = false }, onConfirmDiscard = {
            viewModel.discardChanges()
            showExitDialog = false
            if (isEditMode && dietId != -1) {
                viewModel.setEditMode(false)
            }
        })
    }

    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false }, sheetState = profileSheetState
        ) {
            ProfileSheetContent(
                viewModel = profileViewModel, onDismiss = {
                    scope.launch { profileSheetState.hide() }.invokeOnCompletion {
                        if (!profileSheetState.isVisible) {
                            showProfileSheet = false
                        }
                    }
                }, onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DietDetailContent(
    groupedItems: Map<String, List<DietItemWithFood>>,
    dietTotalNutrition: Food?,
    dietDetails: DietWithItems?,
    isEditMode: Boolean,
    hasUnsavedChanges: Boolean,
    suggestedGoals: List<SmartGoal>,
    isProfileComplete: Boolean,
    mealTypes: List<String>,
    onBackClick: () -> Unit,
    onStartScan: () -> Unit,
    onEditModeChange: (Boolean) -> Unit,
    onSaveDiet: () -> Unit,
    onReorderFoodItemsInMeal: (String, Int, Int) -> Unit,
    onDietNameChange: (String) -> Unit,
    onCalorieGoalChange: (Double) -> Unit,
    onApplyGoal: (SmartGoal) -> Unit,
    onOpenProfile: () -> Unit,
    onSetFocusedMealType: (String?) -> Unit,
    onViewFood: (Int, Double?) -> Unit,
    onShowItemOptions: (DietItemWithFood) -> Unit,
    onSwipeAddToLog: (DietItemWithFood) -> Unit = {},
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Int> = emptySet(),
    onToggleSelection: (Int) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onAddSelectedToLog: () -> Unit = {},
    onMoveSelectedToMeal: (String) -> Unit = {},
    onCloneSelectedToMeal: (String) -> Unit = {}
) {
    var showCopyMenu by remember { mutableStateOf(false) }
    var showCopyMealPicker by remember { mutableStateOf(false) }
    var showMoveMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedItemIds.size} selecionado(s)",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = onClearSelection) {
                            Icon(Icons.Default.Close, "Cancelar seleção")
                        }
                    },
                    actions = {
                        Box {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCopyMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Copiar para:",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            DropdownMenu(
                                expanded = showCopyMenu,
                                onDismissRequest = { showCopyMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Diário") },
                                    leadingIcon = { Icon(Icons.Default.Today, null) },
                                    onClick = {
                                        onAddSelectedToLog()
                                        showCopyMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Refeição...") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        showCopyMenu = false
                                        showCopyMealPicker = true
                                    }
                                )
                            }
                        }

                        Box {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showMoveMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Mover para:",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            DropdownMenu(
                                expanded = showMoveMenu,
                                onDismissRequest = { showMoveMenu = false }
                            ) {
                                mealTypes.forEach { meal ->
                                    DropdownMenuItem(
                                        text = { Text(meal) },
                                        onClick = {
                                            onMoveSelectedToMeal(meal)
                                            showMoveMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDeleteSelected() }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Excluir",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditMode && dietDetails?.diet?.name?.isBlank() == true) "Nova Dieta" else dietDetails?.diet?.name
                                ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isEditMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        titleContentColor = if (isEditMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = if (isEditMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = if (isEditMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ), navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                        }
                    }, actions = {
                        if (!isEditMode) {
                            IconButton(onClick = onStartScan) {
                                Icon(Icons.Default.CameraAlt, "Escanear")
                            }
                            IconButton(onClick = { onEditModeChange(true) }) {
                                Icon(Icons.Default.Edit, "Editar")
                            }
                        } else {
                            IconButton(
                                onClick = onUndo,
                                enabled = canUndo
                            ) {
                                Icon(
                                    Icons.Default.Undo,
                                    "Desfazer",
                                    tint = if (canUndo) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                )
                            }
                            IconButton(
                                onClick = onRedo,
                                enabled = canRedo
                            ) {
                                Icon(
                                    Icons.Default.Redo,
                                    "Refazer",
                                    tint = if (canRedo) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                )
                            }
                            if (hasUnsavedChanges) {
                                IconButton(onClick = onSaveDiet) {
                                    Icon(Icons.Default.Save, "Salvar")
                                }
                            }
                        }
                    })
            }
        }
    ) { innerPadding ->
        if (dietDetails == null) {
            Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            val lazyListState = rememberLazyListState()
            val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                val fromKey = from.key.toString()
                val toKey = to.key.toString()
                val fromMeal = fromKey.substringBeforeLast('_')
                val toMeal = toKey.substringBeforeLast('_')

                if (fromMeal == toMeal) {
                    val mealList = groupedItems[fromMeal] ?: return@rememberReorderableLazyListState
                    val fromId = fromKey.substringAfterLast('_').toIntOrNull()
                    val toId = toKey.substringAfterLast('_').toIntOrNull()
                    val fromIndex = mealList.indexOfFirst { it.dietItem.id == fromId }
                    val toIndex = mealList.indexOfFirst { it.dietItem.id == toId }

                    if (fromIndex != -1 && toIndex != -1) {
                        onReorderFoodItemsInMeal(fromMeal, fromIndex, toIndex)
                    }
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                item {
                    DietHeaderSection(
                        isEditMode = isEditMode,
                        dietName = dietDetails.diet.name,
                        calorieGoal = dietDetails.diet.calorieGoals ?: 0.0,
                        onNameChange = onDietNameChange,
                        onGoalChange = onCalorieGoalChange,
                        suggestedGoals = suggestedGoals,
                        onApplyGoal = onApplyGoal,
                        isProfileComplete = isProfileComplete,
                        onOpenProfile = onOpenProfile
                    )
                }

                // Macros Summary
                item {
                    val currentKcal = dietTotalNutrition?.energiaKcal ?: 0.0
                    val goalKcal = dietDetails.diet.calorieGoals ?: 0.0

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        if (goalKcal > 0.0) {
                            DietGoalProgressCard(currentKcal, goalKcal)
                            Spacer(Modifier.height(12.dp))
                        }
                        dietTotalNutrition?.let { totalFood ->
                            NutritionalSummaryCard(
                                food = totalFood,
                                label = "Total da Dieta",
                                initiallyExpanded = isEditMode
                            )
                        }
                    }
                }

                // Meals
                mealTypes.forEach { mealType ->
                    val items = groupedItems[mealType] ?: emptyList()
                    val mealTime = items.firstOrNull()?.dietItem?.consumptionTime ?: "00:00"

                    if (isEditMode || items.isNotEmpty()) {
                        item(key = "header_$mealType") {
                            MealHeader(
                                title = mealType,
                                time = mealTime,
                                isEditMode = isEditMode,
                                onAddClick = { onSetFocusedMealType(mealType) })
                        }
                    }

                    itemsIndexed(
                        items = items,
                        key = { _, it -> "${mealType}_${it.dietItem.id}" }) { _, dietItem ->
                        ReorderableItem(
                            reorderableState, key = "${mealType}_${dietItem.dietItem.id}"
                        ) { isDragging ->
                            if (isEditMode) {
                                // Edit mode: drag reorder disabled (TODO: fix reorder persistence)
                                CompactFoodItemRow(
                                    item = dietItem,
                                    isEditMode = true,
                                    isDragging = false,
                                    isSelected = selectedItemIds.contains(dietItem.dietItem.id),
                                    isSelectionMode = isSelectionMode,
                                    onShowOptions = { onShowItemOptions(dietItem) },
                                    onViewFood = {
                                        onViewFood(
                                            dietItem.food.id,
                                            dietItem.dietItem.quantityGrams
                                        )
                                    },
                                    onToggleSelection = { onToggleSelection(dietItem.dietItem.id) },
                                    onEnableEditModeAndShowOptions = {
                                        onEditModeChange(true)
                                        onShowItemOptions(dietItem)
                                    },
                                    handle = { it })
                            } else {
                                // View mode: swipe right to add to diary only
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                            onSwipeAddToLog(dietItem)
                                        }
                                        false // Never actually dismiss, just trigger the action
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val direction = dismissState.dismissDirection
                                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircleOutline,
                                                    contentDescription = "Adicionar ao diário",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    },
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = false // No delete in view mode!
                                ) {
                                    Surface(color = MaterialTheme.colorScheme.background) {
                                        CompactFoodItemRow(
                                            item = dietItem,
                                            isEditMode = false,
                                            isDragging = false,
                                            isSelected = selectedItemIds.contains(dietItem.dietItem.id),
                                            isSelectionMode = isSelectionMode,
                                            onShowOptions = { onShowItemOptions(dietItem) },
                                            onViewFood = {
                                                onViewFood(
                                                    dietItem.food.id,
                                                    dietItem.dietItem.quantityGrams
                                                )
                                            },
                                            onToggleSelection = { onToggleSelection(dietItem.dietItem.id) },
                                            onEnableEditModeAndShowOptions = {
                                                onEditModeChange(true)
                                                onShowItemOptions(dietItem)
                                            },
                                            handle = { it })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCopyMealPicker) {
        AlertDialog(
            onDismissRequest = { showCopyMealPicker = false },
            title = { Text("Escolha a refeição:") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    mealTypes.forEach { meal ->
                        TextButton(
                            onClick = {
                                onCloneSelectedToMeal(meal)
                                showCopyMealPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(meal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCopyMealPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DietHeaderSection(
    isEditMode: Boolean,
    dietName: String,
    calorieGoal: Double,
    onNameChange: (String) -> Unit,
    onGoalChange: (Double) -> Unit,
    suggestedGoals: List<SmartGoal>,
    onApplyGoal: (SmartGoal) -> Unit,
    isProfileComplete: Boolean,
    onOpenProfile: () -> Unit
) {
    if (isEditMode) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = dietName,
                onValueChange = onNameChange,
                label = { Text("Nome da Dieta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next
                )
            )

            if (suggestedGoals.isNotEmpty()) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = -1,
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {},
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    suggestedGoals.forEach { goal ->
                        val selected = calorieGoal.toInt() == goal.calories
                        FilterChip(
                            selected = selected,
                            onClick = { onApplyGoal(goal) },
                            label = { Text("${goal.label} (${goal.calories})") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            } else if (!isProfileComplete) {
                // Warning Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        .clickable(onClick = onOpenProfile)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Preencha seu perfil para ver metas inteligentes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = if (calorieGoal > 0) DecimalFormat("#").format(calorieGoal) else "",
                onValueChange = { onGoalChange(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Meta Calórica (kcal)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    } else {
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactFoodItemRow(
    item: DietItemWithFood,
    isEditMode: Boolean,
    isDragging: Boolean,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onShowOptions: () -> Unit,
    onViewFood: () -> Unit,
    onToggleSelection: () -> Unit = {},
    onEnableEditModeAndShowOptions: () -> Unit,
    handle: @Composable (Modifier) -> Modifier
) {
    val df = DecimalFormat("#.#")
    val calc = remember(item) {
        NutrientCalculator.calcularNutrientesParaPorcao(
            item.food, item.dietItem.quantityGrams
        )
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else if (isDragging) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isDragging) 4.dp else 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when {
                            isSelectionMode -> onToggleSelection()
                            isEditMode -> onShowOptions()
                            else -> onViewFood()
                        }
                    },
                    onLongClick = {
                        if (isEditMode) {
                            onToggleSelection()
                        } else {
                            onEnableEditModeAndShowOptions()
                        }
                    }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading: Checkbox (selection mode) or Drag handle (edit mode, not selection)
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else if (isEditMode) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Arrastar para reordenar",
                    modifier = handle(Modifier).padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    item.food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${df.format(item.dietItem.quantityGrams)}g",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val time = item.dietItem.consumptionTime
                    if (!time.isNullOrBlank() && time != "00:00") {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                MacroIconRow(
                    protein = calc.proteina ?: 0.0,
                    carbs = calc.carboidratos ?: 0.0,
                    fat = calc.lipidios?.total ?: 0.0
                )
            }

            Text(
                text = "${df.format(calc.energiaKcal ?: 0.0)} kcal",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Quick action icons (view mode only, not selection mode)
            if (!isSelectionMode && !isEditMode) {
                IconButton(
                    onClick = onViewFood,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "Ver detalhes",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Menu button (edit mode only, not selection mode)
            if (isEditMode && !isSelectionMode) {
                IconButton(onClick = onShowOptions) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemActionsSheet(
    item: DietItemWithFood,
    isEditMode: Boolean,
    mealTypes: List<String>,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onUpdateItem: (quantity: Double, time: String, mealType: String) -> Unit,
    onEditNutrients: () -> Unit,
    onReplaceFood: () -> Unit,
    onCloneToMeal: () -> Unit,
    onAddToLog: () -> Unit,
    onDelete: () -> Unit
) {
    val df = DecimalFormat("#.#")

    // Local state for editable fields
    var quantity by remember { mutableStateOf(df.format(item.dietItem.quantityGrams)) }
    var time by remember { mutableStateOf(item.dietItem.consumptionTime ?: "12:00") }
    var selectedMeal by remember { mutableStateOf(item.dietItem.mealType ?: mealTypes.first()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMealDropdown by remember { mutableStateOf(false) }

    // Track if user made changes
    val hasChanges = remember(quantity, time, selectedMeal) {
        val newQty = quantity.replace(",", ".").toDoubleOrNull() ?: item.dietItem.quantityGrams
        newQty != item.dietItem.quantityGrams || time != item.dietItem.consumptionTime || selectedMeal != item.dietItem.mealType
    }

    ModalBottomSheet(onDismissRequest = {
        // Auto-save on dismiss if there are changes
        if (hasChanges && isEditMode) {
            val newQty = quantity.replace(",", ".").toDoubleOrNull() ?: item.dietItem.quantityGrams
            onUpdateItem(newQty, time, selectedMeal)
        }
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with food name
            Text(
                text = item.food.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Quick edit controls (only in edit mode)
            if (isEditMode) {
                HorizontalDivider()

                // Portion & Time Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PortionControlInput(
                        portion = quantity,
                        onPortionChange = { quantity = it },
                        step = 5.0
                    )
                    TimeControlInput(
                        time = time,
                        onClick = { showTimePicker = true }
                    )
                }

                // Meal type selector - OUTSIDE the Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                ) {
                    OutlinedTextField(
                        value = selectedMeal,
                        onValueChange = { },
                        label = { Text("Refeição") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showMealDropdown = true })
                    DropdownMenu(
                        expanded = showMealDropdown,
                        onDismissRequest = { showMealDropdown = false }) {
                        mealTypes.forEach { meal ->
                            DropdownMenuItem(text = { Text(meal) }, onClick = {
                                selectedMeal = meal
                                showMealDropdown = false
                            }, leadingIcon = {
                                if (meal == selectedMeal) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            })
                        }
                    }
                }

                // Quick quantity buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(50, 100, 150, 200).forEach { preset ->
                        FilterChip(
                            selected = quantity == preset.toString(),
                            onClick = { quantity = preset.toString() },
                            label = { Text("${preset}g") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider()

            // View action (always visible)
            ActionItem(
                icon = Icons.Default.Info,
                title = "Ver Detalhes",
                subtitle = "Informações nutricionais completas",
                onClick = { onDismiss(); onViewDetails() })

            // Edit actions section
            if (isEditMode) {
                SectionHeader("Editar")

                ActionItem(
                    icon = Icons.Default.Edit,
                    title = "Personalizar Nutrientes",
                    subtitle = "Criar cópia editável deste alimento",
                    onClick = { onDismiss(); onEditNutrients() })

                ActionItem(
                    icon = Icons.Filled.SwapHoriz,
                    title = "Trocar Alimento",
                    subtitle = "Substituir por outro alimento",
                    onClick = { onDismiss(); onReplaceFood() })
            }

            SectionHeader("Copiar")

            ActionItem(
                icon = Icons.Default.ContentCopy,
                title = "Clonar para outra refeição",
                subtitle = "Duplicar este item em outro horário",
                onClick = { onDismiss(); onCloneToMeal() })

            ActionItem(
                icon = Icons.Default.AddCircleOutline,
                title = "Adicionar ao Diário",
                subtitle = "Registrar consumo de hoje",
                onClick = { onDismiss(); onAddToLog() })

            if (isEditMode) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ActionItem(
                    icon = Icons.Default.Delete,
                    title = "Remover da Dieta",
                    subtitle = null,
                    onClick = { onDismiss(); onDelete() },
                    isDestructive = true
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onConfirm = { newTime ->
                time = newTime
                showTimePicker = false
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    val parts = initialTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 12
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour, initialMinute = initialMinute, is24Hour = true
    )

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val hour = timePickerState.hour.toString().padStart(2, '0')
            val minute = timePickerState.minute.toString().padStart(2, '0')
            onConfirm("$hour:$minute")
        }) {
            Text("OK")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancelar")
        }
    }, text = {
        TimePicker(state = timePickerState)
    })
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, style = MaterialTheme.typography.bodyLarge, color = contentColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun LabeledMacroChip(label: String, value: Double?, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = DecimalFormat("#").format(value ?: 0.0),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReplaceFoodSheetContent(
    currentFood: Food,
    searchState: FoodSearchState,
    categories: List<String>,
    onSearchTermChange: (String) -> Unit,
    onSortOptionChange: (FoodSortOption) -> Unit,
    onSourceChange: (FoodSource) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onClearCategories: () -> Unit,
    onFilterStateChange: (FoodFilterState) -> Unit,
    onResetFilters: () -> Unit,
    onFoodToggled: (Int) -> Unit,
    onAmountChange: (String) -> Unit,
    onSelectFood: (Food) -> Unit,
    onNavigateToDetail: (Int, Double?) -> Unit,
    onNavigateToCreate: () -> Unit,
    onCancel: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showFilters by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxHeight(0.9f)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Trocar Alimento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }

        // Current food indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Substituindo:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currentFood.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Search Field with Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchState.searchTerm,
                onValueChange = onSearchTermChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar novo alimento") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchState.searchTerm.isNotEmpty()) {
                        IconButton(onClick = { onSearchTermChange("") }) {
                            Icon(Icons.Default.Close, "Limpar")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(onClick = { showFilters = true }) {
                val activeFilterCount = listOfNotNull(
                    if (searchState.filterState.source != FoodSource.ALL) 1 else null,
                    if (searchState.filterState.selectedCategories.isNotEmpty()) searchState.filterState.selectedCategories.size else null
                ).sum()

                if (activeFilterCount > 0) {
                    BadgedBox(
                        badge = { Badge { Text(activeFilterCount.toString()) } }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Filter chips row (HomeScreen pattern)
        if (searchState.searchTerm.length >= 2 || searchState.filterState.hasActiveFilters) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InputChip(
                    selected = true,
                    onClick = { showFilters = true },
                    label = { Text("Ordenar: ${searchState.filterState.sortOption.label}") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                if (searchState.filterState.source != FoodSource.ALL) {
                    InputChip(
                        selected = true,
                        onClick = { onSourceChange(FoodSource.ALL) },
                        label = { Text("Fonte: ${searchState.filterState.source.displayName}") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                searchState.filterState.selectedCategories.forEach { category ->
                    InputChip(
                        selected = true,
                        onClick = { onCategoryToggle(category) },
                        label = { Text(category) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                if (searchState.filterState.activeAdvancedFilterCount > 0) {
                    InputChip(
                        selected = true,
                        onClick = { onFilterStateChange(FoodFilterState.DEFAULT) },
                        label = { Text("+${searchState.filterState.activeAdvancedFilterCount} filtros") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Results
        when {
            searchState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            searchState.results.isEmpty() && searchState.searchTerm.length >= 2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nenhum resultado encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToCreate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cadastrar novo alimento")
                    }
                }
            }

            else -> {
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val expandedIndex =
                    searchState.results.indexOfFirst { it.id == searchState.expandedFoodId }
                val isExpandedVisible = remember(
                    listState.firstVisibleItemIndex,
                    listState.layoutInfo.visibleItemsInfo.size,
                    expandedIndex
                ) {
                    if (expandedIndex < 0) true
                    else listState.layoutInfo.visibleItemsInfo.any { it.index == expandedIndex }
                }

                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${searchState.results.size} resultados",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        itemsIndexed(
                            searchState.results,
                            key = { _, food -> food.id }) { index, food ->
                            val isCurrentFood = food.id == currentFood.id

                            if (isCurrentFood) {
                                // Show current food as disabled
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.5f
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            food.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "(atual)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                // Normal selectable food item
                                SearchItem(
                                    food = food,
                                    isExpanded = searchState.expandedFoodId == food.id,
                                    onToggle = {
                                        onFoodToggled(food.id)
                                        keyboardController?.hide()
                                    },
                                    onNavigateToDetail = {
                                        onNavigateToDetail(
                                            food.id,
                                            searchState.quickAddAmount.toDoubleOrNull()
                                        )
                                    },
                                    currentAmount = searchState.quickAddAmount,
                                    onAmountChange = onAmountChange,
                                    onAddToDiet = { onSelectFood(food) },
                                    isAddToDietPrimary = true,
                                    actionButtonLabel = "Selecionar",
                                    resultIndex = index + 1,
                                    highlightedNutrient = searchState.filterState.sortOption.getNutrientDisplayInfo()
                                        ?: searchState.filterState.getFirstActiveAdvancedFilterInfo()
                                )
                            }
                        }
                    }

                    // Scroll-to-selected FAB
                    androidx.compose.animation.AnimatedVisibility(
                        visible = expandedIndex >= 0 && !isExpandedVisible,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(expandedIndex)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                Icons.Default.CenterFocusStrong,
                                contentDescription = "Ir para item selecionado"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterBottomSheet(
            filterState = searchState.filterState,
            categories = categories,
            onDismiss = { showFilters = false },
            onSourceChange = onSourceChange,
            onCategoryToggle = onCategoryToggle,
            onClearCategories = onClearCategories,
            onSortChange = onSortOptionChange,
            onResetFilters = onResetFilters,
            onFilterStateChange = onFilterStateChange,
            showCategories = true,
            showAdvancedFilters = true
        )
    }
}

@Composable
fun CloneFoodDialog(
    item: DietItemWithFood,
    mealTypes: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clonar para qual refeição?") },
        text = {
            Column {
                mealTypes.forEach { meal ->
                    TextButton(
                        onClick = { onConfirm(meal) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(meal, modifier = Modifier.weight(1f))
                        if (meal == item.dietItem.mealType) {
                            Icon(
                                Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
fun SearchFoodSheetContent(
    mealType: String,
    addedItems: List<DietItemWithFood>,
    searchState: FoodSearchState,
    categories: List<String>,
    onSearchTermChange: (String) -> Unit,
    onSortOptionChange: (FoodSortOption) -> Unit,
    onSourceChange: (FoodSource) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onClearCategories: () -> Unit,
    onFilterStateChange: (FoodFilterState) -> Unit,
    onResetFilters: () -> Unit,
    onFoodToggled: (Int) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddFood: (Food, Double) -> Unit,
    onRemoveItem: (DietItemWithFood) -> Unit,
    onNavigateToDetail: (Int, Double?) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var showFilters by remember { mutableStateOf(false) }

    // Auto-scroll to end when item is added
    LaunchedEffect(addedItems.size) {
        if (addedItems.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        Modifier
            .fillMaxHeight(0.9f)
            .padding(16.dp)
    ) {
        Text(
            "Adicionar ao $mealType",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (addedItems.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Já na refeição (toque para remover):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                addedItems.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onRemoveItem(item) }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.food.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${DecimalFormat("#").format(item.dietItem.quantityGrams)}g",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remover",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Search Field with Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchState.searchTerm,
                onValueChange = onSearchTermChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar alimento") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchState.searchTerm.isNotEmpty()) {
                        IconButton(onClick = { onSearchTermChange("") }) {
                            Icon(Icons.Default.Close, "Limpar")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(onClick = { showFilters = true }) {
                val activeFilterCount = listOfNotNull(
                    if (searchState.filterState.source != FoodSource.ALL) 1 else null,
                    if (searchState.filterState.selectedCategories.isNotEmpty()) searchState.filterState.selectedCategories.size else null
                ).sum()

                if (activeFilterCount > 0) {
                    BadgedBox(
                        badge = { Badge { Text(activeFilterCount.toString()) } }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Filter chips row (HomeScreen pattern)
        if (searchState.searchTerm.length >= 2 || searchState.results.isNotEmpty() || searchState.filterState.hasActiveFilters) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InputChip(
                    selected = true,
                    onClick = { showFilters = true },
                    label = { Text("Ordenar: ${searchState.filterState.sortOption.label}") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                if (searchState.filterState.source != FoodSource.ALL) {
                    InputChip(
                        selected = true,
                        onClick = { onSourceChange(FoodSource.ALL) },
                        label = { Text("Fonte: ${searchState.filterState.source.displayName}") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                searchState.filterState.selectedCategories.forEach { category ->
                    InputChip(
                        selected = true,
                        onClick = { onCategoryToggle(category) },
                        label = { Text(category) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                if (searchState.filterState.activeAdvancedFilterCount > 0) {
                    InputChip(
                        selected = true,
                        onClick = { onFilterStateChange(FoodFilterState.DEFAULT) },
                        label = { Text("+${searchState.filterState.activeAdvancedFilterCount} filtros") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Results
        when {
            searchState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            searchState.results.isEmpty() && searchState.searchTerm.length >= 2 -> {
                Text(
                    "Nenhum resultado encontrado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val expandedIndex =
                    searchState.results.indexOfFirst { it.id == searchState.expandedFoodId }
                val isExpandedVisible = remember(
                    listState.firstVisibleItemIndex,
                    listState.layoutInfo.visibleItemsInfo.size,
                    expandedIndex
                ) {
                    if (expandedIndex < 0) true
                    else listState.layoutInfo.visibleItemsInfo.any { it.index == expandedIndex }
                }

                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${searchState.results.size} resultados",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        itemsIndexed(
                            searchState.results,
                            key = { _, food -> food.id }) { index, food ->
                            SearchItem(
                                food = food,
                                isExpanded = searchState.expandedFoodId == food.id,
                                onToggle = {
                                    onFoodToggled(food.id)
                                    keyboardController?.hide()
                                },
                                onNavigateToDetail = {
                                    onNavigateToDetail(
                                        food.id,
                                        searchState.quickAddAmount.toDoubleOrNull()
                                    )
                                },
                                currentAmount = searchState.quickAddAmount,
                                onAmountChange = onAmountChange,
                                onAddToDiet = { amount ->
                                    val qty = amount.toDoubleOrNull() ?: 100.0
                                    onAddFood(food, qty)
                                },
                                isAddToDietPrimary = true,
                                actionButtonLabel = "Adicionar",
                                resultIndex = index + 1,
                                highlightedNutrient = searchState.filterState.sortOption.getNutrientDisplayInfo()
                                    ?: searchState.filterState.getFirstActiveAdvancedFilterInfo()
                            )
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onNavigateToCreate,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                // TODO after food is created by user, should be displayed
                                // on search screen or be added already.
                                Text("Cadastrar novo alimento")
                            }
                        }
                    }

                    // Scroll-to-selected FAB
                    androidx.compose.animation.AnimatedVisibility(
                        visible = expandedIndex >= 0 && !isExpandedVisible,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(expandedIndex)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                Icons.Default.CenterFocusStrong,
                                contentDescription = "Ir para item selecionado"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterBottomSheet(
            filterState = searchState.filterState,
            categories = categories,
            onDismiss = { showFilters = false },
            onSourceChange = onSourceChange,
            onCategoryToggle = onCategoryToggle,
            onClearCategories = onClearCategories,
            onSortChange = onSortOptionChange,
            onResetFilters = onResetFilters,
            onFilterStateChange = onFilterStateChange,
            showCategories = true,
            showAdvancedFilters = true
        )
    }
}

@Composable
fun DietGoalProgressCard(current: Double, goal: Double) {
    val progress = (current / goal).toFloat().coerceIn(0f, 1f)
    val color = when {
        progress > 1f -> MaterialTheme.colorScheme.error
        progress > 0.9f -> Color(0xFFE6B800)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Progresso da Meta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${current.toInt()} / ${goal.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun MealHeader(title: String, time: String, isEditMode: Boolean, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            // Optional time display
            if (time.isNotBlank() && time != "00:00") {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        if (isEditMode) {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}