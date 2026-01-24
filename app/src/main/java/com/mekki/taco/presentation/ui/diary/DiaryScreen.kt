package com.mekki.taco.presentation.ui.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.model.DailyLogWithFood
import com.mekki.taco.presentation.ui.components.MacroPieChart
import com.mekki.taco.presentation.ui.components.PieChartData
import com.mekki.taco.presentation.ui.components.SearchItem
import com.mekki.taco.presentation.ui.components.TimePickerDialog
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
    onNavigateToDetail: (Int) -> Unit,
    onActionsChange: ((@Composable () -> Unit)?) -> Unit
) {
    val currentDate by viewModel.currentDate.collectAsState()
    val dailyLogs by viewModel.dailyLogs.collectAsState()
    val totals by viewModel.dailyTotals.collectAsState()
    val availableDiets by viewModel.availableDiets.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var logToEditTime by remember { mutableStateOf<com.mekki.taco.data.db.entity.DailyLog?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val showImportInList = dailyLogs.isEmpty()

    // Ensure we don't have duplicate actions from the global top bar
    LaunchedEffect(Unit) {
        onActionsChange(null)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    DateSelectorTitle(
                        date = currentDate,
                        onPrev = { viewModel.changeDate(-1) },
                        onNext = { viewModel.changeDate(1) },
                        onClick = { showDatePicker = true }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (!showImportInList) {
                        IconButton(onClick = { showImportDialog = true }) {
                            Icon(Icons.Default.Download, contentDescription = "Importar Dieta")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSearchSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Alimento")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        Spacer(Modifier.width(8.dp))
                        Text("Importar de uma Dieta")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // 4. Meal Lists
            if (dailyLogs.isEmpty()) {
                item {
                    EmptyStateMessage(onAddClick = { showSearchSheet = true })
                }
            } else {
                val sortedGroups = dailyLogs.toSortedMap()

                sortedGroups.forEach { (timeBlock, logs) ->
                    item {
                        Text(
                            text = timeBlock,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(logs, key = { it.log.id }) { item ->
                        DiaryLogItem(
                            item = item,
                            onToggle = { viewModel.toggleConsumed(item.log) },
                            onQuantityChange = { newQty ->
                                viewModel.updateQuantity(
                                    item.log,
                                    newQty
                                )
                            },
                            onNotesChange = { newNotes ->
                                viewModel.updateNotes(item.log, newNotes)
                            },
                            onDelete = { viewModel.deleteLog(item.log) },
                            onTimeClick = { logToEditTime = item.log }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
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

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Dia Anterior",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Próximo Dia",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
    onNavigateToDetail: (Int) -> Unit,
    onClose: () -> Unit
) {
    val searchTerm by viewModel.searchTerm.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.searchIsLoading.collectAsState()
    val expandedId by viewModel.expandedFoodId.collectAsState()
    val quickAddAmount by viewModel.quickAddAmount.collectAsState()

    val mealTypes =
        listOf("Café da Manhã", "Almoço", "Jantar", "Lanche", "Pré-treino", "Pós-treino", "Outros")
    var selectedMealType by remember { mutableStateOf(mealTypes.first()) }
    var selectedTime by remember { mutableStateOf(java.time.LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }

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

        OutlinedTextField(
            value = searchTerm,
            onValueChange = viewModel::onSearchTermChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar um alimento") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchTerm.isNotEmpty()) {
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

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { food ->
                    SearchItem(
                        food = food,
                        isExpanded = expandedId == food.id,
                        onToggle = { viewModel.onFoodToggled(food.id) },
                        currentAmount = quickAddAmount,
                        onAmountChange = viewModel::onQuickAddAmountChange,
                        onNavigateToDetail = { onNavigateToDetail(food.id) },
                        onAddToDiet = {
                            viewModel.addFoodToLog(food, selectedMealType, selectedTime)
                            viewModel.clearSearch()
                            onClose()
                        },
                        isAddToDietPrimary = true,
                        actionButtonLabel = "Registrar"
                    )
                }

                if (searchResults.isEmpty() && searchTerm.length >= 2) {
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
        }
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
fun DiaryLogItem(
    item: DailyLogWithFood,
    onToggle: () -> Unit,
    onQuantityChange: (Double) -> Unit,
    onNotesChange: (String) -> Unit,
    onDelete: () -> Unit,
    onTimeClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var sliderValue by remember(item.log.quantityGrams) { mutableFloatStateOf(item.log.quantityGrams.toFloat()) }

    val strikeColor by animateColorAsState(
        targetValue = if (item.log.isConsumed) Color.Gray else MaterialTheme.colorScheme.onSurface,
        label = "color"
    )

    val timeStr = remember(item.log.entryTimestamp) {
        val instant = Instant.ofEpochMilli(item.log.entryTimestamp)
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 4.dp)
            .background(Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Checkbox
            IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (item.log.isConsumed) MaterialTheme.colorScheme.primary else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.log.isConsumed) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. Food Name & Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onTimeClick() }
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = item.food.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = strikeColor,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${item.log.quantityGrams.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // 3. Slider Section (Expanded)
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp, top = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ajustar quantidade:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${sliderValue.toInt()}g",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(40.dp)
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onQuantityChange(sliderValue.toDouble()) },
                        valueRange = 0f..2000f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = item.log.notes ?: "",
                    onValueChange = onNotesChange,
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )

                // 4. Comparison Graph (for imported diet items)
                item.log.originalQuantityGrams?.let { target ->
                    val current = sliderValue
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
                                text = if (diff == 0f) "Atingida" else if (diff > 0) "+${diff.toInt()}g" else "${diff.toInt()}g",
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
        title = { Text("Importar Dieta") },
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