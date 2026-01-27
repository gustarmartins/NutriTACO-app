package com.mekki.taco.presentation.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DailyLogWithFood
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DiaryTotals(
    val consumedKcal: Double = 0.0,
    val consumedProtein: Double = 0.0,
    val consumedCarbs: Double = 0.0,
    val consumedFat: Double = 0.0,
    val goalKcal: Double = 2000.0,
    val proteinPerKg: Double = 0.0,
    val waterIntake: Int = 0,
    val waterGoal: Int = 2000, // TODO make it customizable
    val userWeight: Double? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyLogs: StateFlow<Map<String, List<DailyLogWithFood>>> = _currentDate
        .flatMapLatest { date ->
            repository.getDailyLogs(date.toString())
        }
        .map { logs ->
            logs.groupBy { item ->
                val instant = Instant.ofEpochMilli(item.log.entryTimestamp)
                val zoneId = ZoneId.systemDefault()
                val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
                localDateTime.format(DateTimeFormatter.ofPattern("HH:00"))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    // --- Actions ---

    fun changeDate(offset: Long) {
        _currentDate.value = _currentDate.value.plusDays(offset)
    }

    fun setDate(date: LocalDate) {
        _currentDate.value = date
    }

    fun goToToday() {
        _currentDate.value = LocalDate.now()
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
}
