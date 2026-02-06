package com.mekki.taco.presentation.ui.diary

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.model.DailyLogWithFood
import com.mekki.taco.presentation.ui.components.FilterBottomSheet
import com.mekki.taco.presentation.ui.components.MacroIconRow
import com.mekki.taco.presentation.ui.components.MacroPieChart
import com.mekki.taco.presentation.ui.components.PieChartData
import com.mekki.taco.presentation.ui.components.PortionControlInput
import com.mekki.taco.presentation.ui.components.SearchItem
import com.mekki.taco.presentation.ui.components.TimePickerDialog
import com.mekki.taco.presentation.ui.components.UnifiedFoodItemActionsSheet
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSource
import com.mekki.taco.presentation.ui.search.getNutrientDisplayInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int, Double?) -> Unit,
    onActionsChange: ((@Composable () -> Unit)?) -> Unit
) {
    val currentDate by viewModel.currentDate.collectAsState()
    val mealSections by viewModel.mealSections.collectAsState()
    val totals by viewModel.dailyTotals.collectAsState()
    val availableDiets by viewModel.availableDiets.collectAsState()

    val selectedLogIds by viewModel.selectedLogIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val dismissRevision by viewModel.dismissRevision.collectAsState()

    val viewMode by viewModel.viewMode.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val weekStart by viewModel.weekStart.collectAsState()
    val monthStart by viewModel.monthStart.collectAsState()
    val goalMode by viewModel.goalMode.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var logToEditTime by remember { mutableStateOf<com.mekki.taco.data.db.entity.DailyLog?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPeriodDatePicker by remember { mutableStateOf(false) }
    var previousViewMode by remember { mutableStateOf<DiaryViewMode?>(null) }
    var selectedLogForOptions by remember { mutableStateOf<DailyLogWithFood?>(null) }

    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    BackHandler(enabled = viewMode == DiaryViewMode.DAILY && previousViewMode != null && !isSelectionMode) {
        previousViewMode?.let { viewModel.setViewMode(it) }
        previousViewMode = null
    }

    val showImportInList = mealSections.isEmpty()

    // Top Bar Actions
    LaunchedEffect(isSelectionMode, selectedLogIds.size) {
        if (isSelectionMode) {
            onActionsChange {
                Row {
                    IconButton(onClick = { viewModel.markSelectedAsConsumed(true) }) {
                        Icon(Icons.Default.Check, contentDescription = "Marcar como consumido")
                    }
                    IconButton(onClick = { viewModel.deleteSelectedLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir selecionados")
                    }
                }
            }
        } else {
            onActionsChange {
                if (!showImportInList && viewMode == DiaryViewMode.DAILY) {
                    TextButton(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Importar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    var showMoveDialog by remember { mutableStateOf(false) }
    var showCopyMenu by remember { mutableStateOf(false) }
    var showMoveMenu by remember { mutableStateOf(false) }
    val mealTypes = listOf(
        "Café da Manhã", "Almoço", "Lanche", "Jantar",
        "Pré-treino", "Pós-treino", "Ceia"
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val canUndo by viewModel.canUndo.collectAsState()
    val scope = rememberCoroutineScope()
    var pendingDeleteCount by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.undoManager.clear()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            val isDeletionMessage = message.contains("removido")
            scope.launch {
                val displayMessage = if (isDeletionMessage) {
                    pendingDeleteCount++
                    if (pendingDeleteCount > 1) "($pendingDeleteCount) $message" else message
                } else {
                    message
                }
                val result = snackbarHostState.showSnackbar(
                    message = displayMessage,
                    actionLabel = if (isDeletionMessage && canUndo) "DESFAZER" else null,
                    duration = SnackbarDuration.Short
                )
                if (isDeletionMessage) pendingDeleteCount--
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undo()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        topBar = {
            if (isSelectionMode) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "${selectedLogIds.size} selecionado(s)",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar seleção")
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
                                    "Copiar",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            DropdownMenu(
                                expanded = showCopyMenu,
                                onDismissRequest = { showCopyMenu = false }
                            ) {
                                mealTypes.forEach { meal ->
                                    DropdownMenuItem(
                                        text = { Text(meal) },
                                        onClick = {
                                            viewModel.cloneSelectedToMeal(meal)
                                            showCopyMenu = false
                                        }
                                    )
                                }
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
                                    "Mover",
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
                                            viewModel.moveSelectedToMeal(meal)
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
                                .clickable { viewModel.deleteSelectedLogs() }
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
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        when (viewMode) {
                            DiaryViewMode.DAILY -> DateSelectorTitle(
                                date = currentDate,
                                onPrev = { viewModel.changeDate(-1) },
                                onNext = { viewModel.changeDate(1) },
                                onClick = { showDatePicker = true }
                            )

                            DiaryViewMode.WEEKLY -> PeriodSelectorTitle(
                                label = formatWeekLabel(weekStart),
                                onPrev = { viewModel.changeWeek(-1) },
                                onNext = { viewModel.changeWeek(1) },
                                onLabelClick = { showPeriodDatePicker = true }
                            )

                            DiaryViewMode.MONTHLY -> PeriodSelectorTitle(
                                label = formatMonthLabel(monthStart),
                                onPrev = { viewModel.changeMonth(-1) },
                                onNext = { viewModel.changeMonth(1) },
                                onLabelClick = { showPeriodDatePicker = true }
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        if (!showImportInList) {
                            TextButton(
                                onClick = { showImportDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Importar", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (com.mekki.taco.BuildConfig.DEBUG) {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🧪 Carregar dados de teste") },
                                    onClick = {
                                        viewModel.loadMockData()
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode && viewMode == DiaryViewMode.DAILY) {
                FloatingActionButton(
                    onClick = { showSearchSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Alimento")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            @OptIn(ExperimentalMaterial3Api::class)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                DiaryViewMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = DiaryViewMode.entries.size
                        ),
                        onClick = {
                            if (viewMode == mode) {
                                when (mode) {
                                    DiaryViewMode.DAILY -> viewModel.goToToday()
                                    DiaryViewMode.WEEKLY -> viewModel.goToCurrentWeek()
                                    DiaryViewMode.MONTHLY -> viewModel.goToCurrentMonth()
                                }
                            } else {
                                viewModel.setViewMode(mode)
                            }
                        },
                        selected = viewMode == mode
                    ) {
                        Text(
                            when (mode) {
                                DiaryViewMode.DAILY -> "Dia"
                                DiaryViewMode.WEEKLY -> "Semana"
                                DiaryViewMode.MONTHLY -> "Mês"
                            }
                        )
                    }
                }
            }

            when (viewMode) {
                DiaryViewMode.WEEKLY -> {
                    GoalModeSelector(
                        selectedMode = goalMode,
                        onModeSelected = { viewModel.setGoalMode(it) }
                    )
                    DiarySummaryView(
                        summary = weeklySummary,
                        isMonthlyMode = false,
                        goalMode = goalMode,
                        onDayClick = { date ->
                            previousViewMode = DiaryViewMode.WEEKLY
                            viewModel.setDate(date)
                            viewModel.setViewMode(DiaryViewMode.DAILY)
                        }
                    )
                }

                DiaryViewMode.MONTHLY -> {
                    GoalModeSelector(
                        selectedMode = goalMode,
                        onModeSelected = { viewModel.setGoalMode(it) }
                    )
                    DiarySummaryView(
                        summary = monthlySummary,
                        isMonthlyMode = true,
                        goalMode = goalMode,
                        onDayClick = { date ->
                            previousViewMode = DiaryViewMode.MONTHLY
                            viewModel.setDate(date)
                            viewModel.setViewMode(DiaryViewMode.DAILY)
                        }
                    )
                }

                DiaryViewMode.DAILY -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // 1. Summary Card
                        item {
                            DiarySummaryCard(
                                totals = totals,
                                onUpdateWeight = { showWeightDialog = true }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 2. Water Tracker
                        item {
                            WaterTrackerCard(
                                currentMl = totals.waterIntake,
                                goalMl = totals.waterGoal,
                                onAdd = { viewModel.addWater(it) }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // 3. Import Button
                        if (showImportInList) {
                            item {
                                OutlinedButton(
                                    onClick = { showImportDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, null)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Importar de uma dieta",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }

                        // 4. Meal Lists
                        if (mealSections.isEmpty()) {
                            item {
                                EmptyStateMessage(onAddClick = { showSearchSheet = true })
                            }
                        } else {
                            mealSections.forEach { section ->
                                item {
                                    MealSectionHeader(section = section)
                                }
                                items(
                                    items = section.logs,
                                    key = { "${it.log.id}_${it.log.isConsumed}_$dismissRevision" }
                                ) { item ->
                                    val isConsumed = item.log.isConsumed
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { value ->
                                            when (value) {
                                                SwipeToDismissBoxValue.EndToStart -> {
                                                    viewModel.deleteLog(item.log)
                                                    true
                                                }

                                                SwipeToDismissBoxValue.StartToEnd -> {
                                                    viewModel.toggleConsumed(item.log)
                                                    false
                                                }

                                                else -> false
                                            }
                                        }
                                    )

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                            val direction = dismissState.dismissDirection
                                            val color = when (direction) {
                                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                                SwipeToDismissBoxValue.StartToEnd -> if (isConsumed) {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                }

                                                else -> Color.Transparent
                                            }
                                            val icon = when (direction) {
                                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                                SwipeToDismissBoxValue.StartToEnd -> if (isConsumed) {
                                                    Icons.Default.Clear
                                                } else {
                                                    Icons.Default.Check
                                                }

                                                else -> null
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(color)
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = when (direction) {
                                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                                    else -> Alignment.Center
                                                }
                                            ) {
                                                icon?.let {
                                                    Icon(
                                                        imageVector = it,
                                                        contentDescription = null,
                                                        tint = when (direction) {
                                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        enableDismissFromStartToEnd = !isSelectionMode,
                                        enableDismissFromEndToStart = !isSelectionMode
                                    ) {
                                        Surface(color = MaterialTheme.colorScheme.background) {
                                            DiaryLogItem(
                                                item = item,
                                                isSelected = selectedLogIds.contains(item.log.id),
                                                isSelectionMode = isSelectionMode,
                                                onLongClick = { viewModel.toggleSelection(item.log.id) },
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        viewModel.toggleSelection(item.log.id)
                                                    }
                                                },
                                                onShowOptions = { selectedLogForOptions = item },
                                                onToggleConsumed = { viewModel.toggleConsumed(item.log) },
                                                onQuantityChange = { newQty: Double ->
                                                    viewModel.updateQuantity(item.log, newQty)
                                                },
                                                onNotesChange = { newNotes: String ->
                                                    viewModel.updateNotes(item.log, newNotes)
                                                },
                                                onDelete = { viewModel.deleteLog(item.log) },
                                                onTimeClick = { logToEditTime = item.log }
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                item { Spacer(Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate =
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        viewModel.setDate(selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showPeriodDatePicker) {
        val initialMillis = when (viewMode) {
            DiaryViewMode.WEEKLY -> weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()

            DiaryViewMode.MONTHLY -> monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()

            else -> currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPeriodDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate =
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        when (viewMode) {
                            DiaryViewMode.WEEKLY -> viewModel.setWeekContaining(selectedDate)
                            DiaryViewMode.MONTHLY -> viewModel.setMonthContaining(selectedDate)
                            else -> {}
                        }
                    }
                    showPeriodDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPeriodDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (logToEditTime != null) {
        val log = logToEditTime!!
        val instant = Instant.ofEpochMilli(log.entryTimestamp)
        val localTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime()

        TimePickerDialog(
            onDismissRequest = { logToEditTime = null },
            initialHour = localTime.hour,
            initialMinute = localTime.minute,
            onConfirm = { h, m ->
                viewModel.updateTimestamp(log, h, m)
                logToEditTime = null
            }
        )
    }

    if (showImportDialog) {
        ImportDietDialog(
            diets = availableDiets,
            onDismiss = { showImportDialog = false },
            onSelect = { diet ->
                viewModel.importDiet(diet.id)
                showImportDialog = false
            }
        )
    }

    if (showSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
            DiarySearchSheetContent(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail,
                onClose = { showSearchSheet = false }
            )
        }
    }

    if (showWeightDialog) {
        UpdateWeightDialog(
            currentWeight = totals.userWeight ?: 70.0,
            onDismiss = { showWeightDialog = false },
            onConfirm = { newWeight ->
                viewModel.updateWeight(newWeight)
                showWeightDialog = false
            }
        )
    }

    if (showMoveDialog) {
        MoveSelectedDialog(
            currentDate = currentDate,
            onDismiss = { showMoveDialog = false },
            onMoveToDate = { targetDate ->
                viewModel.moveSelectedToDate(targetDate)
                showMoveDialog = false
            },
            onCloneToMeal = { mealType ->
                viewModel.cloneSelectedToMeal(mealType)
                showMoveDialog = false
            }
        )
    }

    selectedLogForOptions?.let { item ->
        val timeStr = remember(item.log.entryTimestamp) {
            val instant = Instant.ofEpochMilli(item.log.entryTimestamp)
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        UnifiedFoodItemActionsSheet(
            foodName = item.food.name,
            food = item.food,
            currentQuantity = item.log.quantityGrams,
            currentTime = timeStr,
            currentMealType = item.log.mealType,
            originalQuantity = item.log.quantityGrams,
            isEditMode = true,
            mealTypes = mealTypes,
            onDismiss = { selectedLogForOptions = null },
            onViewDetails = {
                selectedLogForOptions = null
                onNavigateToDetail(item.food.id, item.log.quantityGrams)
            },
            onUpdateItem = { quantity, time, mealType ->
                viewModel.updateLog(item.log, quantity, time, mealType)
            },
            onCloneToMeal = {
                viewModel.cloneLogToMeal(item, item.log.mealType)
                selectedLogForOptions = null
            },
            onMoveToMeal = { targetMeal ->
                viewModel.moveLogToMeal(item.log, targetMeal)
                selectedLogForOptions = null
            },
            onDelete = {
                viewModel.deleteLog(item.log)
                selectedLogForOptions = null
            }
        )
    }
}

@Composable
fun DateSelectorTitle(
    date: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.forLanguageTag("pt-BR"))
    val isToday = date == LocalDate.now()
    val dateText =
        if (isToday) "Hoje" else date.format(formatter).replaceFirstChar { it.uppercase() }

    var offsetX by remember { mutableFloatStateOf(0f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 50) onPrev()
                        else if (offsetX < -50) onNext()
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Dia Anterior",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Próximo Dia",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PeriodSelectorTitle(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onLabelClick: () -> Unit = {}
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 50) onPrev()
                        else if (offsetX < -50) onNext()
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Período Anterior",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onLabelClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Próximo Período",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoalModeSelector(
    selectedMode: DiaryGoalMode,
    onModeSelected: (DiaryGoalMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        DiaryGoalMode.entries.forEach { mode ->
            val label = when (mode) {
                DiaryGoalMode.DEFICIT -> "Déficit"
                DiaryGoalMode.MAINTAIN -> "Manter"
                DiaryGoalMode.SURPLUS -> "Superávit"
            }
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun WaterTrackerCard(
    currentMl: Int,
    goalMl: Int,
    onAdd: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalDrink, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Água",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${currentMl}ml / ${goalMl}ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (currentMl >= goalMl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FilledIconButton(
                    onClick = { onAdd(-100) },
                    enabled = currentMl >= 100,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(Icons.Default.Remove, null)
                }

                Spacer(Modifier.width(24.dp))

                Text(
                    "${(currentMl / 1000.0)} L",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(24.dp))

                FilledIconButton(
                    onClick = { onAdd(100) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    }
}

@Composable
fun DiarySearchSheetContent(
    viewModel: DiaryViewModel,
    onNavigateToDetail: (Int, Double?) -> Unit,
    onClose: () -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()
    val categories by viewModel.foodSearchManager.categories.collectAsState()

    val mealTypes =
        listOf("Café da Manhã", "Almoço", "Jantar", "Lanche", "Pré-treino", "Pós-treino", "Outros")
    var selectedMealType by remember { mutableStateOf(mealTypes.first()) }
    var selectedTime by remember { mutableStateOf(java.time.LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxHeight(0.9f)
    ) {
        Text("Adicionar Alimento", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mealTypes.forEach { label ->
                FilterChip(
                    selected = selectedMealType == label,
                    onClick = { selectedMealType = label },
                    label = { Text(label) },
                    leadingIcon = if (selectedMealType == label) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.AccessTime, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Horário: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                initialHour = selectedTime.hour,
                initialMinute = selectedTime.minute,
                onConfirm = { h, m ->
                    selectedTime = java.time.LocalTime.of(h, m)
                    showTimePicker = false
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchState.searchTerm,
                onValueChange = viewModel::onSearchTermChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar um alimento") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchState.searchTerm.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
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
        AnimatedVisibility(visible = searchState.results.isNotEmpty() || searchState.filterState.hasActiveFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
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
                        onClick = { viewModel.foodSearchManager.onSourceFilterChange(FoodSource.ALL) },
                        label = { Text("Fonte: ${searchState.filterState.source.displayName}") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                searchState.filterState.selectedCategories.forEach { category ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.foodSearchManager.onCategoryToggle(category) },
                        label = { Text(category) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                if (searchState.filterState.activeAdvancedFilterCount > 0) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.foodSearchManager.onFilterStateChange(FoodFilterState.DEFAULT) },
                        label = { Text("+${searchState.filterState.activeAdvancedFilterCount} filtros") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        if (searchState.isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
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
                    itemsIndexed(searchState.results, key = { _, food -> food.id }) { index, food ->
                        SearchItem(
                            food = food,
                            isExpanded = searchState.expandedFoodId == food.id,
                            onToggle = {
                                viewModel.onFoodToggled(food.id)
                                keyboardController?.hide()
                            },
                            currentAmount = searchState.quickAddAmount,
                            onAmountChange = viewModel::onQuickAddAmountChange,
                            onNavigateToDetail = {
                                onNavigateToDetail(
                                    food.id,
                                    searchState.quickAddAmount.toDoubleOrNull()
                                )
                            },
                            onAddToDiet = {
                                viewModel.addFoodToLog(food, selectedMealType, selectedTime)
                                viewModel.clearSearch()
                                onClose()
                            },
                            isAddToDietPrimary = true,
                            actionButtonLabel = "Registrar",
                            resultIndex = index + 1,
                            highlightedNutrient = searchState.filterState.sortOption.getNutrientDisplayInfo()
                                ?: searchState.filterState.getFirstActiveAdvancedFilterInfo()
                        )
                    }

                    if (searchState.results.isEmpty() && searchState.searchTerm.length >= 2) {
                        item {
                            Text(
                                "Nenhum resultado encontrado.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
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

    if (showFilters) {
        FilterBottomSheet(
            filterState = searchState.filterState,
            categories = categories,
            onDismiss = { showFilters = false },
            onSourceChange = { viewModel.foodSearchManager.onSourceFilterChange(it) },
            onCategoryToggle = { viewModel.foodSearchManager.onCategoryToggle(it) },
            onClearCategories = { viewModel.foodSearchManager.onClearCategories() },
            onSortChange = { viewModel.foodSearchManager.onSortOptionChange(it) },
            onResetFilters = { viewModel.clearSearch() },
            onFilterStateChange = { viewModel.foodSearchManager.onFilterStateChange(it) },
            showCategories = true,
            showAdvancedFilters = true
        )
    }
}

@Composable
fun UpdateWeightDialog(
    currentWeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(currentWeight.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atualizar Peso") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                text.toDoubleOrNull()?.let { onConfirm(it) }
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DiarySummaryCard(
    totals: DiaryTotals,
    onUpdateWeight: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Resumo Diário",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${totals.consumedKcal.toInt()} / ${totals.goalKcal.toInt()} kcal",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (totals.consumedKcal > totals.goalKcal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(onClick = onUpdateWeight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonitorWeight, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${totals.userWeight ?: "--"}kg",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    MacroPieChart(
                        data = listOf(
                            PieChartData(totals.consumedCarbs.toFloat(), Color(0xFFDCC48E), ""),
                            PieChartData(totals.consumedProtein.toFloat(), Color(0xFF2E7A7A), ""),
                            PieChartData(totals.consumedFat.toFloat(), Color(0xFFC97C4A), "")
                        ),
                        modifier = Modifier.size(100.dp),
                        totalValue = totals.consumedKcal,
                        totalUnit = "",
                        showCenterText = false
                    )
                    val progress =
                        if (totals.goalKcal > 0) (totals.consumedKcal / totals.goalKcal * 100).toInt()
                            .coerceAtMost(100) else 0
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroMiniStat("Carbs", totals.consumedCarbs, Color(0xFFDCC48E))
                        MacroMiniStat("Proteínas", totals.consumedProtein, Color(0xFF2E7A7A))
                        MacroMiniStat("Gorduras", totals.consumedFat, Color(0xFFC97C4A))
                    }

                    HorizontalDivider()

                    Text(
                        text = "Proteína consumida: %.1f g/kg".format(totals.proteinPerKg),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MacroMiniStat(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "${value.toInt()}g",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}

@Composable
fun MealSectionHeader(section: MealSection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MacroIconRow(
                protein = section.totalProtein,
                carbs = section.totalCarbs,
                fat = section.totalFat
            )
            Text(
                text = "${section.totalKcal.toInt()} kcal",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DiaryLogItem(
    item: DailyLogWithFood,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onShowOptions: () -> Unit,
    onToggleConsumed: () -> Unit,
    onQuantityChange: (Double) -> Unit,
    onNotesChange: (String) -> Unit,
    onDelete: () -> Unit,
    onTimeClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var portionValue by remember(item.log.quantityGrams) {
        mutableStateOf(
            item.log.quantityGrams.toInt().toString()
        )
    }
    var notesValue by remember(item.log.id) { mutableStateOf(item.log.notes ?: "") }

    LaunchedEffect(notesValue) {
        if (notesValue != (item.log.notes ?: "")) {
            delay(500)
            onNotesChange(notesValue)
        }
    }

    // Visual State
    val isConsumed = item.log.isConsumed
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    if (isConsumed) 0.6f else 1f
    val textColor =
        if (isConsumed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .pointerInput(isSelectionMode) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                    onTap = {
                        if (isSelectionMode) onClick()
                        else isExpanded = !isExpanded
                    }
                )
            }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Leading Checkbox (selection mode only)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isSelected,
                        onCheckedChange = null
                    )
                }
            }

            // 2. Food Name & Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isSelectionMode) {
                        val timeStr = remember(item.log.entryTimestamp) {
                            val instant = Instant.ofEpochMilli(item.log.entryTimestamp)
                            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { onTimeClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = item.food.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isConsumed) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${item.log.quantityGrams.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val ratio = item.log.quantityGrams / 100.0
                    MacroIconRow(
                        protein = (item.food.proteina ?: 0.0) * ratio,
                        carbs = (item.food.carboidratos ?: 0.0) * ratio,
                        fat = (item.food.lipidios?.total ?: 0.0) * ratio
                    )
                    val scaledKcal =
                        ((item.food.energiaKcal ?: 0.0) * item.log.quantityGrams / 100).toInt()
                    Text(
                        text = "$scaledKcal kcal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isSelectionMode) {
                IconButton(
                    onClick = onShowOptions,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opções",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Expanded Slider Section (Only when not selecting)
        AnimatedVisibility(visible = isExpanded && !isSelectionMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp, top = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ajustar quantidade:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PortionControlInput(
                        portion = portionValue,
                        onPortionChange = { newValue ->
                            portionValue = newValue
                            newValue.toDoubleOrNull()?.let { onQuantityChange(it) }
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesValue,
                    onValueChange = { notesValue = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )

                // 4. Comparison Graph (for imported diet items)
                item.log.originalQuantityGrams?.let { target ->
                    val current = portionValue.toFloatOrNull() ?: 0f
                    val diff = current - target.toFloat()
                    val maxScaleVal = 2000f

                    Spacer(Modifier.height(8.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Meta: ${target.toInt()}g",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when {
                                    diff == 0f -> "Atingida"
                                    diff > 0 -> "+${diff.toInt()}g (acima)"
                                    else -> "${diff.toInt()}g (abaixo)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (kotlin.math.abs(diff) < 5) MaterialTheme.colorScheme.primary
                                else if (diff > 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            // Target Range (Ghost Bar)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((target.toFloat() / maxScaleVal).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            )

                            // Current Fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((current / maxScaleVal).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        when {
                                            current > target -> MaterialTheme.colorScheme.error
                                            current < target -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                            )

                            // Target Line Marker
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((target.toFloat() / maxScaleVal).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Today,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Seu diário está vazio hoje.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ImportDietDialog(
    diets: List<Diet>,
    onDismiss: () -> Unit,
    onSelect: (Diet) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importar itens") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                if (diets.isEmpty()) {
                    item { Text("Você ainda não criou nenhuma dieta.") }
                }
                items(diets) { diet ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(diet) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text(diet.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun MoveSelectedDialog(
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onMoveToDate: (LocalDate) -> Unit,
    onCloneToMeal: (String) -> Unit
) {
    val mealTypes = listOf("Café da Manhã", "Almoço", "Lanche", "Jantar")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ações em lote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mover para:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onMoveToDate(currentDate.minusDays(1)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dia anterior", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onMoveToDate(currentDate.plusDays(1)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Próximo dia", maxLines = 1)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Clonar para refeição:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    mealTypes.forEach { meal ->
                        TextButton(
                            onClick = { onCloneToMeal(meal) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(meal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}