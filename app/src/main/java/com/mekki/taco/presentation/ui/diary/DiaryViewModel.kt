package com.mekki.taco.presentation.ui.diary

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.database.AppDatabase
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DailyCalorieEntry
import com.mekki.taco.data.model.DailyLogWithFood
import com.mekki.taco.data.model.DiarySummary
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.DiaryRepository
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.presentation.ui.search.FoodSearchManager
import com.mekki.taco.presentation.ui.search.FoodSearchState
import com.mekki.taco.presentation.undo.UndoManager
import com.mekki.taco.presentation.undo.UndoableAction
import com.mekki.taco.utils.NutrientCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class DiaryTotals(
    val consumedKcal: Double = 0.0,
    val consumedProtein: Double = 0.0,
    val consumedCarbs: Double = 0.0,
    val consumedFat: Double = 0.0,
    val goalKcal: Double = 2000.0,
    val proteinPerKg: Double = 0.0,
    val waterIntake: Int = 0,
    val waterGoal: Int = 2000,
    val userWeight: Double? = null
)

data class MealSection(
    val title: String,
    val totalKcal: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val logs: List<DailyLogWithFood>
)

enum class DiaryViewMode {
    DAILY, WEEKLY, MONTHLY
}

@OptIn(FlowPreview::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val database: AppDatabase,
    private val repository: DiaryRepository,
    private val dietDao: DietDao,
    private val foodDao: FoodDao,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    // --- State: Current Date ---
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate = _currentDate.asStateFlow()

    // --- State: Available Diets ---
    private val _availableDiets = MutableStateFlow<List<Diet>>(emptyList())
    val availableDiets = _availableDiets.asStateFlow()

    // --- State: Main Diet Calorie Goal ---
    private val _mainDietCalorieGoal = MutableStateFlow<Double?>(null)

    // --- State: User Profile ---
    private val _userProfile = MutableStateFlow(UserProfile())

    // --- State: View Mode ---
    private val _viewMode = MutableStateFlow(DiaryViewMode.DAILY)
    val viewMode = _viewMode.asStateFlow()

    // --- State: Week/Month Range ---
    private val _weekStart = MutableStateFlow(LocalDate.now().with(java.time.DayOfWeek.MONDAY))
    val weekStart = _weekStart.asStateFlow()

    private val _monthStart = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val monthStart = _monthStart.asStateFlow()

    // --- State: Selection Mode ---
    private val _selectedLogIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedLogIds = _selectedLogIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    val undoManager = UndoManager()
    val canUndo = undoManager.canUndo
    val canRedo = undoManager.canRedo

    private val _snackbarMessages = Channel<String>(Channel.BUFFERED)
    val snackbarMessages = _snackbarMessages.receiveAsFlow()

    private val _goalMode = MutableStateFlow(DiaryGoalMode.DEFICIT)
    val goalMode = _goalMode.asStateFlow()

    fun setGoalMode(mode: DiaryGoalMode) {
        _goalMode.value = mode
    }

    // --- State: Search ---
    val foodSearchManager = FoodSearchManager(foodDao, viewModelScope, savedStateHandle)
    val searchState: StateFlow<FoodSearchState> = foodSearchManager.state

    init {
        loadDiets()
        observeProfile()
    }

    private fun loadDiets() {
        viewModelScope.launch {
            dietDao.getAllDiets().collect { _availableDiets.value = it }
        }
        viewModelScope.launch {
            dietDao.getLatestDietWithItems().collect { dietWithItems ->
                _mainDietCalorieGoal.value = dietWithItems?.diet?.calorieGoals
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userProfileRepository.userProfileFlow.collect {
                _userProfile.value = it
            }
        }
    }

    private val mealOrder = listOf(
        "Café da Manhã", "Almoço", "Lanche", "Jantar", "Pré-treino", "Pós-treino", "Outros"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mealSections: StateFlow<List<MealSection>> = _currentDate
        .flatMapLatest { date ->
            repository.getDailyLogs(date.toString())
        }
        .map { logs ->
            val grouped = logs.groupBy { it.log.mealType }
            val sections = mutableListOf<MealSection>()

            mealOrder.forEach { type ->
                val logsForType = grouped[type]
                if (!logsForType.isNullOrEmpty()) {
                    sections.add(createMealSection(type, logsForType))
                }
            }

            grouped.keys.filterNot { it in mealOrder }.forEach { type ->
                val logsForType = grouped[type]
                if (!logsForType.isNullOrEmpty()) {
                    sections.add(createMealSection(type, logsForType))
                }
            }

            sections
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun createMealSection(title: String, logs: List<DailyLogWithFood>): MealSection {
        var k = 0.0
        var p = 0.0
        var c = 0.0
        var f = 0.0

        logs.forEach { item ->
            val n = NutrientCalculator.calcularNutrientesParaPorcao(
                item.food, item.log.quantityGrams
            )
            k += n.energiaKcal ?: 0.0
            p += n.proteina ?: 0.0
            c += n.carboidratos ?: 0.0
            f += n.lipidios?.total ?: 0.0
        }

        val sortedLogs = logs.sortedBy { it.log.entryTimestamp }

        return MealSection(title, k, p, c, f, sortedLogs)
    }

    // --- Water Log ---
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dailyWater = _currentDate.flatMapLatest { date ->
        repository.getWaterLog(date.toString())
    }

    // --- Real-time Totals ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyTotals: StateFlow<DiaryTotals> = combine(
        _currentDate.flatMapLatest { repository.getDailyLogs(it.toString()) },
        dailyWater,
        _userProfile,
        _mainDietCalorieGoal
    ) { logs, waterLog, profile, mainDietGoal ->
        calculateTotals(logs, waterLog?.quantityMl ?: 0, profile, mainDietGoal)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryTotals())

    private fun calculateTotals(
        logs: List<DailyLogWithFood>,
        waterMl: Int,
        profile: UserProfile,
        mainDietGoal: Double?
    ): DiaryTotals {
        var k = 0.0
        var p = 0.0
        var c = 0.0
        var f = 0.0

        logs.filter { it.log.isConsumed }.forEach { item ->
            val nutrients = NutrientCalculator.calcularNutrientesParaPorcao(
                foodBase = item.food,
                quantidadeDesejadaGramas = item.log.quantityGrams
            )
            k += nutrients.energiaKcal ?: 0.0
            p += nutrients.proteina ?: 0.0
            c += nutrients.carboidratos ?: 0.0
            f += nutrients.lipidios?.total ?: 0.0
        }

        val weight = profile.weight ?: 0.0
        val pPerKg = if (weight > 0) p / weight else 0.0
        val waterGoal = if (weight > 0) (weight * profile.waterGoalPerMlPerKg).toInt() else 2000
        val goalKcal = mainDietGoal ?: profile.calorieGoal ?: 2000.0

        return DiaryTotals(
            consumedKcal = k,
            consumedProtein = p,
            consumedCarbs = c,
            consumedFat = f,
            goalKcal = goalKcal,
            proteinPerKg = pPerKg,
            waterIntake = waterMl,
            waterGoal = waterGoal,
            userWeight = profile.weight
        )
    }

    // --- Weekly Summary ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklySummary: StateFlow<DiarySummary> = combine(
        _weekStart.flatMapLatest { start ->
            val end = start.plusDays(6)
            repository.getLogsForDateRange(start.toString(), end.toString())
        },
        _userProfile,
        _mainDietCalorieGoal
    ) { logs, profile, mainDietGoal ->
        calculatePeriodSummary(logs, profile, 7, mainDietGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiarySummary())

    // --- Monthly Summary ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlySummary: StateFlow<DiarySummary> = combine(
        _monthStart.flatMapLatest { start ->
            val end = start.plusMonths(1).minusDays(1)
            repository.getLogsForDateRange(start.toString(), end.toString())
        },
        _userProfile,
        _mainDietCalorieGoal
    ) { logs, profile, mainDietGoal ->
        val daysInMonth = _monthStart.value.lengthOfMonth()
        calculatePeriodSummary(logs, profile, daysInMonth, mainDietGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiarySummary())

    private fun calculatePeriodSummary(
        logs: List<DailyLogWithFood>,
        profile: UserProfile,
        periodDays: Int,
        mainDietGoal: Double?
    ): DiarySummary {
        val goalKcal = mainDietGoal ?: profile.calorieGoal ?: 2000.0
        val weight = profile.weight ?: 70.0

        var totalKcal = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var totalFiber = 0.0
        var totalCholesterol = 0.0
        var totalSodium = 0.0
        var totalCalcium = 0.0
        var totalIron = 0.0
        var totalMagnesium = 0.0
        var totalPhosphorus = 0.0
        var totalPotassium = 0.0
        var totalZinc = 0.0
        var totalVitaminC = 0.0
        var totalRetinol = 0.0
        var totalThiamine = 0.0
        var totalRiboflavin = 0.0
        var totalNiacin = 0.0
        var totalPyridoxine = 0.0

        val consumedLogs = logs.filter { it.log.isConsumed }

        consumedLogs.forEach { item ->
            val n =
                NutrientCalculator.calcularNutrientesParaPorcao(item.food, item.log.quantityGrams)
            totalKcal += n.energiaKcal ?: 0.0
            totalProtein += n.proteina ?: 0.0
            totalCarbs += n.carboidratos ?: 0.0
            totalFat += n.lipidios?.total ?: 0.0
            totalFiber += n.fibraAlimentar ?: 0.0
            totalCholesterol += n.colesterol ?: 0.0
            totalSodium += n.sodio ?: 0.0
            totalCalcium += n.calcio ?: 0.0
            totalIron += n.ferro ?: 0.0
            totalMagnesium += n.magnesio ?: 0.0
            totalPhosphorus += n.fosforo ?: 0.0
            totalPotassium += n.potassio ?: 0.0
            totalZinc += n.zinco ?: 0.0
            totalVitaminC += n.vitaminaC ?: 0.0
            totalRetinol += n.retinol ?: 0.0
            totalThiamine += n.tiamina ?: 0.0
            totalRiboflavin += n.riboflavina ?: 0.0
            totalNiacin += n.niacina ?: 0.0
            totalPyridoxine += n.piridoxina ?: 0.0
        }

        val logsByDate = consumedLogs.groupBy { it.log.date }
        val daysLogged = logsByDate.keys.size

        val dailyCalories = logsByDate.map { (dateStr, dayLogs) ->
            val date = LocalDate.parse(dateStr)
            val kcal = dayLogs.sumOf { item ->
                NutrientCalculator.calcularNutrientesParaPorcao(
                    item.food,
                    item.log.quantityGrams
                ).energiaKcal ?: 0.0
            }
            DailyCalorieEntry(date, kcal, goalKcal)
        }.sortedBy { it.date }

        val daysOnTarget = dailyCalories.count { it.isOnTarget }
        val avgKcal = if (daysLogged > 0) totalKcal / daysLogged else 0.0
        val avgProteinPerKg =
            if (weight > 0 && daysLogged > 0) (totalProtein / daysLogged) / weight else 0.0

        val currentStreak = dailyCalories
            .sortedByDescending { it.date }
            .takeWhile { it.isOnTarget }
            .count()

        return DiarySummary(
            totalKcal = totalKcal,
            avgKcal = avgKcal,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            totalFiber = totalFiber,
            totalCholesterol = totalCholesterol,
            totalSodium = totalSodium,
            totalCalcium = totalCalcium,
            totalIron = totalIron,
            totalMagnesium = totalMagnesium,
            totalPhosphorus = totalPhosphorus,
            totalPotassium = totalPotassium,
            totalZinc = totalZinc,
            totalVitaminC = totalVitaminC,
            totalRetinol = totalRetinol,
            totalThiamine = totalThiamine,
            totalRiboflavin = totalRiboflavin,
            totalNiacin = totalNiacin,
            totalPyridoxine = totalPyridoxine,
            avgProteinPerKg = avgProteinPerKg,
            dailyCalories = dailyCalories,
            daysLogged = daysLogged,
            daysOnTarget = daysOnTarget,
            currentStreak = currentStreak
        )
    }

    // --- Selection Actions ---

    fun toggleSelection(logId: Int) {
        val current = _selectedLogIds.value
        if (current.contains(logId)) {
            val newSet = current - logId
            _selectedLogIds.value = newSet
            if (newSet.isEmpty()) _isSelectionMode.value = false
        } else {
            _selectedLogIds.value = current + logId
            _isSelectionMode.value = true
        }
    }

    fun clearSelection() {
        _selectedLogIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteSelectedLogs() {
        val idsToDelete = _selectedLogIds.value
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            val currentSections = mealSections.value
            val allLogsWithFood = currentSections.flatMap { it.logs }
            val itemsToDelete = allLogsWithFood
                .filter { idsToDelete.contains(it.log.id) }
                .mapIndexed { index, item -> Triple(item.log, item.food, index) }

            if (itemsToDelete.isNotEmpty()) {
                undoManager.recordAction(UndoableAction.DeleteMultipleDailyLogs(itemsToDelete))
            }

            itemsToDelete.forEach { (log, _, _) -> repository.deleteLog(log) }
            clearSelection()

            val count = itemsToDelete.size
            _snackbarMessages.send("$count ${if (count == 1) "item removido" else "itens removidos"}")
        }
    }

    fun markSelectedAsConsumed(isConsumed: Boolean) {
        val ids = _selectedLogIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch {
            val currentSections = mealSections.value
            val allLogs = currentSections.flatMap { it.logs.map { l -> l.log } }
            val logsToUpdate = allLogs.filter { ids.contains(it.id) }

            logsToUpdate.forEach { log ->
                if (log.isConsumed != isConsumed) {
                    repository.toggleConsumed(log)
                }
            }
            clearSelection()
        }
    }

    fun moveSelectedToDate(targetDate: LocalDate) {
        val ids = _selectedLogIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch {
            val currentSections = mealSections.value
            val allLogs = currentSections.flatMap { it.logs.map { l -> l.log } }
            val logsToMove = allLogs.filter { ids.contains(it.id) }

            logsToMove.forEach { log ->
                val updatedLog = log.copy(date = targetDate.toString())
                repository.updateLog(updatedLog)
            }
            clearSelection()
        }
    }

    fun cloneSelectedToMeal(targetMealType: String) {
        val ids = _selectedLogIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch {
            val currentSections = mealSections.value
            val allLogs = currentSections.flatMap { it.logs }
            val logsToClone = allLogs.filter { ids.contains(it.log.id) }

            logsToClone.forEach { item ->
                val newLog = item.log.copy(
                    id = 0,
                    mealType = targetMealType,
                    isConsumed = false
                )
                repository.addLog(newLog)
            }
            clearSelection()
        }
    }

    fun moveSelectedToMeal(targetMealType: String) {
        val ids = _selectedLogIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch {
            val currentSections = mealSections.value
            val allLogs = currentSections.flatMap { it.logs.map { l -> l.log } }
            val logsToMove = allLogs.filter { ids.contains(it.id) }

            logsToMove.forEach { log ->
                val updatedLog = log.copy(mealType = targetMealType)
                repository.updateLog(updatedLog)
            }
            clearSelection()
        }
    }

    // --- Actions ---

    fun changeDate(offset: Long) {
        _currentDate.value = _currentDate.value.plusDays(offset)
        clearSelection()
    }

    fun setDate(date: LocalDate) {
        _currentDate.value = date
        clearSelection()
    }

    fun goToToday() {
        _currentDate.value = LocalDate.now()
        _weekStart.value = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        _monthStart.value = LocalDate.now().withDayOfMonth(1)
        clearSelection()
    }

    fun goToCurrentWeek() {
        _weekStart.value = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
    }

    fun goToCurrentMonth() {
        _monthStart.value = LocalDate.now().withDayOfMonth(1)
    }

    fun setViewMode(mode: DiaryViewMode) {
        _viewMode.value = mode
    }

    fun changeWeek(offset: Long) {
        _weekStart.value = _weekStart.value.plusWeeks(offset)
    }

    fun setWeekContaining(date: LocalDate) {
        _weekStart.value = date.with(java.time.DayOfWeek.MONDAY)
    }

    fun changeMonth(offset: Long) {
        _monthStart.value = _monthStart.value.plusMonths(offset)
    }

    fun setMonthContaining(date: LocalDate) {
        _monthStart.value = date.withDayOfMonth(1)
    }

    fun importDiet(dietId: Int) {
        viewModelScope.launch {
            repository.importDietPlanToDate(dietId, _currentDate.value.toString())
        }
    }

    fun toggleConsumed(log: DailyLog) {
        viewModelScope.launch {
            repository.toggleConsumed(log)
        }
    }

    fun updateQuantity(log: DailyLog, newQuantity: Double) {
        viewModelScope.launch {
            repository.updateQuantity(log, newQuantity)
        }
    }

    fun updateNotes(log: DailyLog, newNotes: String) {
        viewModelScope.launch {
            repository.updateNotes(log, newNotes)
        }
    }

    fun updateLog(log: DailyLog, quantity: Double, time: String, mealType: String) {
        viewModelScope.launch {
            val parts = time.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val date = try {
                LocalDate.parse(log.date)
            } catch (e: Exception) {
                LocalDate.now()
            }
            val localTime = LocalTime.of(hour, minute)
            val dateTime = LocalDateTime.of(date, localTime)
            val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val updatedLog = log.copy(
                quantityGrams = quantity,
                entryTimestamp = timestamp,
                mealType = mealType
            )
            repository.updateLog(updatedLog)
        }
    }

    fun moveLogToMeal(log: DailyLog, targetMeal: String) {
        viewModelScope.launch {
            val updatedLog = log.copy(mealType = targetMeal)
            repository.updateLog(updatedLog)
        }
    }

    fun cloneLogToMeal(logWithFood: DailyLogWithFood, targetMeal: String) {
        viewModelScope.launch {
            val newLog = logWithFood.log.copy(
                id = 0,
                mealType = targetMeal,
                isConsumed = false
            )
            repository.addLog(newLog)
        }
    }

    fun updateTimestamp(log: DailyLog, newHour: Int, newMinute: Int) {
        val date = try {
            LocalDate.parse(log.date)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val time = LocalTime.of(newHour, newMinute)
        val dateTime = LocalDateTime.of(date, time)
        val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            repository.updateTimestamp(log, timestamp)
        }
    }

    fun deleteLog(log: DailyLog) {
        viewModelScope.launch {
            val food = mealSections.value
                .flatMap { it.logs }
                .find { it.log.id == log.id }
                ?.food

            if (food != null) {
                undoManager.recordAction(
                    UndoableAction.DeleteDailyLog(
                        log,
                        food,
                        log.mealType,
                        log.sortOrder
                    )
                )
            }

            repository.deleteLog(log)
            _snackbarMessages.send("${food?.name ?: "Item"} removido")
        }
    }

    fun undo() {
        val action = undoManager.popUndo() ?: return
        viewModelScope.launch {
            performUndo(action)
            undoManager.confirmUndo(action)
            _snackbarMessages.send("Desfeito: ${action.description}")
        }
    }

    fun redo() {
        val action = undoManager.popRedo() ?: return
        viewModelScope.launch {
            performRedo(action)
            undoManager.confirmRedo(action)
            _snackbarMessages.send("Refeito: ${action.description}")
        }
    }

    private suspend fun performUndo(action: UndoableAction) {
        when (action) {
            is UndoableAction.DeleteDailyLog -> {
                repository.insertLog(action.log)
            }

            is UndoableAction.DeleteMultipleDailyLogs -> {
                action.logs.forEach { (log, _, _) ->
                    repository.insertLog(log)
                }
            }

            is UndoableAction.ToggleDailyLogConsumed -> {
                repository.updateConsumedById(action.logId, action.wasConsumed)
            }

            is UndoableAction.UpdateDailyLogPortion -> {
                repository.updatePortionById(action.logId, action.oldQuantity)
            }

            else -> { /* Other actions handled as needed */
            }
        }
    }

    private suspend fun performRedo(action: UndoableAction) {
        when (action) {
            is UndoableAction.DeleteDailyLog -> {
                repository.deleteLog(action.log)
            }

            is UndoableAction.DeleteMultipleDailyLogs -> {
                action.logs.forEach { (log, _, _) ->
                    repository.deleteLog(log)
                }
            }

            is UndoableAction.ToggleDailyLogConsumed -> {
                repository.updateConsumedById(action.logId, !action.wasConsumed)
            }

            is UndoableAction.UpdateDailyLogPortion -> {
                repository.updatePortionById(action.logId, action.newQuantity)
            }

            else -> { /* Other actions handled as needed */
            }
        }
    }

    // --- Water Actions ---
    fun addWater(amount: Int) {
        val current = dailyTotals.value.waterIntake
        val newAmount = (current + amount).coerceAtLeast(0)
        updateWater(newAmount)
    }

    private fun updateWater(amount: Int) {
        viewModelScope.launch {
            repository.updateWater(_currentDate.value.toString(), amount)
        }
    }

    // --- Profile Actions ---
    fun updateWeight(weight: Double) {
        viewModelScope.launch {
            userProfileRepository.saveWeight(weight)
        }
    }

    // --- Search & Add Food Actions ---
    fun onSearchTermChange(term: String) = foodSearchManager.onSearchTermChange(term)

    fun onFoodToggled(id: Int) = foodSearchManager.onFoodToggled(id)

    fun onQuickAddAmountChange(amount: String) = foodSearchManager.onQuickAddAmountChange(amount)

    fun addFoodToLog(food: Food, mealType: String, customTime: LocalTime? = null) {
        val amount = foodSearchManager.state.value.quickAddAmount.toDoubleOrNull() ?: 100.0

        val date = _currentDate.value
        val time = customTime ?: LocalTime.now()
        val dateTime = LocalDateTime.of(date, time)
        val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            val log = DailyLog(
                foodId = food.id,
                date = date.toString(),
                quantityGrams = amount,
                mealType = mealType,
                entryTimestamp = timestamp,
                isConsumed = false
            )
            repository.addLog(log)

            clearSearch()
        }
    }

    fun clearSearch() = foodSearchManager.clear()

    fun loadMockData() {
        viewModelScope.launch {
            com.mekki.taco.utils.MockDataLoader.loadMockDiaryData(database, application)
        }
    }
}