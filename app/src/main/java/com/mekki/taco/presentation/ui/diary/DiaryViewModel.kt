package com.mekki.taco.presentation.ui.diary

import android.app.Application
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
import com.mekki.taco.utils.NutrientCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    // --- State: Search ---
    private val _searchTerm = MutableStateFlow("")
    val searchTerm = _searchTerm.asStateFlow()

    private val _searchIsLoading = MutableStateFlow(false)
    val searchIsLoading = _searchIsLoading.asStateFlow()

    private val _expandedFoodId = MutableStateFlow<Int?>(null)
    val expandedFoodId = _expandedFoodId.asStateFlow()

    private val _quickAddAmount = MutableStateFlow("100")
    val quickAddAmount = _quickAddAmount.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Food>> = _searchTerm
        .debounce(300)
        .flatMapLatest { term ->
            if (term.length >= 2) {
                _searchIsLoading.value = true
                foodDao.getFoodsByName(term).map {
                    _searchIsLoading.value = false
                    it
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDiets()
        observeProfile()
    }

    private fun loadDiets() {
        viewModelScope.launch {
            dietDao.getAllDiets().collect { _availableDiets.value = it }
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
        _userProfile
    ) { logs, waterLog, profile ->
        calculateTotals(logs, waterLog?.quantityMl ?: 0, profile)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryTotals())

    private fun calculateTotals(
        logs: List<DailyLogWithFood>,
        waterMl: Int,
        profile: UserProfile
    ): DiaryTotals {
        var k = 0.0;
        var p = 0.0;
        var c = 0.0;
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
        val goalKcal = profile.calorieGoal ?: 2000.0

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
        _userProfile
    ) { logs, profile ->
        calculatePeriodSummary(logs, profile, 7)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiarySummary())

    // --- Monthly Summary ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlySummary: StateFlow<DiarySummary> = combine(
        _monthStart.flatMapLatest { start ->
            val end = start.plusMonths(1).minusDays(1)
            repository.getLogsForDateRange(start.toString(), end.toString())
        },
        _userProfile
    ) { logs, profile ->
        val daysInMonth = _monthStart.value.lengthOfMonth()
        calculatePeriodSummary(logs, profile, daysInMonth)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiarySummary())

    private fun calculatePeriodSummary(
        logs: List<DailyLogWithFood>,
        profile: UserProfile,
        periodDays: Int
    ): DiarySummary {
        val goalKcal = profile.calorieGoal ?: 2000.0
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
            daysOnTarget = daysOnTarget
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
            val allLogs = currentSections.flatMap { it.logs.map { l -> l.log } }
            val logsToDelete = allLogs.filter { idsToDelete.contains(it.id) }

            logsToDelete.forEach { repository.deleteLog(it) }
            clearSelection()
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

    fun setViewMode(mode: DiaryViewMode) {
        _viewMode.value = mode
    }

    fun changeWeek(offset: Long) {
        _weekStart.value = _weekStart.value.plusWeeks(offset)
    }

    fun changeMonth(offset: Long) {
        _monthStart.value = _monthStart.value.plusMonths(offset)
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
            repository.deleteLog(log)
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
    fun onSearchTermChange(term: String) {
        _searchTerm.value = term
    }

    fun onFoodToggled(id: Int) {
        if (_expandedFoodId.value == id) {
            _expandedFoodId.value = null
        } else {
            _expandedFoodId.value = id
            _quickAddAmount.value = "100"
        }
    }

    fun onQuickAddAmountChange(amount: String) {
        _quickAddAmount.value = amount
    }

    fun addFoodToLog(food: Food, mealType: String, customTime: LocalTime? = null) {
        val amount = _quickAddAmount.value.toDoubleOrNull() ?: 100.0

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

    fun clearSearch() {
        _searchTerm.value = ""
        _expandedFoodId.value = null
        _quickAddAmount.value = "100"
    }

    fun loadMockData() {
        viewModelScope.launch {
            com.mekki.taco.utils.MockDataLoader.loadMockDiaryData(database, application)
        }
    }
}
