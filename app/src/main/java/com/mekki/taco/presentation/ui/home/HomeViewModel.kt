package com.mekki.taco.presentation.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DailyWaterLogDao
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.DailyWaterLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import com.mekki.taco.data.model.DailyLogWithFood
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.data.model.DietWithItems
import com.mekki.taco.data.repository.OnboardingRepository
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.presentation.ui.components.ChartType
import com.mekki.taco.utils.NutrientCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class DietSummary(
    val diet: Diet, val totalNutrition: Food, val chartType: ChartType = ChartType.PIE
)

data class DashboardMealGroup(
    val time: String,
    val mealType: String,
    val items: List<DietItemWithFood>,
    val isPassed: Boolean = false
)

data class DailyProgress(
    val consumedKcal: Double = 0.0,
    val targetKcal: Double = 2000.0,
    val consumedProtein: Double = 0.0,
    val targetProtein: Double = 100.0,
    val consumedCarbs: Double = 0.0,
    val targetCarbs: Double = 250.0,
    val consumedFat: Double = 0.0,
    val targetFat: Double = 70.0
)

data class WaterIntake(
    val currentMl: Int = 0,
    val targetMl: Int = 2000
)

data class HomeState(
    val dietSummaries: List<DietSummary> = emptyList(),
    val dailyProgress: DailyProgress = DailyProgress(),
    val waterIntake: WaterIntake = WaterIntake(),

    val nextMeal: DashboardMealGroup? = null,
    val dailyTimeline: List<DashboardMealGroup> = emptyList(),

    val isSearchExpanded: Boolean = false,
    val showAllResults: Boolean = false,
    val searchTerm: String = "",
    val searchIsLoading: Boolean = false,
    val searchResults: List<Food> = emptyList(),
    val expandedAlimentoId: Int? = null,
    val quickAddAmount: String = "100",
    val sortOption: FoodSortOption = FoodSortOption.RELEVANCE,
    val showRegistrarTutorial: Boolean = false
)

enum class FoodSortOption(val label: String) {
    RELEVANCE("Relevância"), PROTEIN("Proteínas"), CARBS("Carboidratos"), FAT("Gorduras"), CALORIES(
        "Calorias"
    )
}

sealed class HomeEffect {
    data class ShowSnackbar(
        val message: String, val actionLabel: String? = null, val action: SnackbarAction? = null
    ) : HomeEffect()
}

sealed class SnackbarAction {
    data object GoToDiary : SnackbarAction()
    data class GoToDiet(val dietId: Int) : SnackbarAction()
}

private const val KEY_SEARCH_TERM = "search_term"
private const val KEY_QUICK_ADD_AMOUNT = "quick_add_amount"
private const val KEY_SORT_OPTION = "sort_option"
private const val KEY_EXPANDED_FOOD_ID = "expanded_food_id"
private const val KEY_SEARCH_EXPANDED = "search_expanded"
private const val KEY_SHOW_ALL_RESULTS = "show_all_results"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dietDao: DietDao,
    private val foodDao: FoodDao,
    private val dietItemDao: DietItemDao,
    private val dailyLogDao: DailyLogDao,
    private val dailyWaterLogDao: DailyWaterLogDao,
    private val onboardingRepository: OnboardingRepository,
    private val userProfileRepository: UserProfileRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val availableDiets =
        dietDao.getAllDiets().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Persisted State
    private val searchTerm = savedStateHandle.getStateFlow(KEY_SEARCH_TERM, "")
    private val quickAddAmount = savedStateHandle.getStateFlow(KEY_QUICK_ADD_AMOUNT, "100")
    private val sortOption =
        savedStateHandle.getStateFlow(KEY_SORT_OPTION, FoodSortOption.RELEVANCE)
    private val expandedAlimentoId = savedStateHandle.getStateFlow<Int?>(KEY_EXPANDED_FOOD_ID, null)
    private val isSearchExpanded = savedStateHandle.getStateFlow(KEY_SEARCH_EXPANDED, false)
    private val showAllResults = savedStateHandle.getStateFlow(KEY_SHOW_ALL_RESULTS, false)

    private val _searchIsLoading = MutableStateFlow(false)
    private val _rawSearchResults = MutableStateFlow<List<Food>>(emptyList())
    private val _showRegistrarTutorial = MutableStateFlow(false)

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val today = LocalDate.now().toString()

    private val dailyLogsFlow = dailyLogDao.getLogsForDate(today)
    private val waterLogFlow = dailyWaterLogDao.getWaterLog(today)

    private val dashboardData = combine(
        dietDao.getAllDietsWithItems(),
        userProfileRepository.dietChartPreferencesFlow,
        dailyLogsFlow,
        waterLogFlow
    ) { dietList, preferences, todayLogs, waterLog ->
        val summaries = dietList.map { dietWithItems ->
            val totalFood = calculateTotalNutrition(dietWithItems)
            val storedType = preferences[dietWithItems.diet.id]
            val chartType = if (storedType != null) {
                try {
                    ChartType.valueOf(storedType)
                } catch (e: Exception) {
                    ChartType.PIE
                }
            } else {
                ChartType.PIE
            }

            DietSummary(
                diet = dietWithItems.diet, totalNutrition = totalFood, chartType = chartType
            )
        }

        val mainDiet = dietList.find { it.diet.isMain } ?: dietList.firstOrNull()
        val timeline = mainDiet?.let { calculateTimeline(it.items) } ?: emptyList()
        val nextMeal = calculateNextMeal(timeline)

        // Calculate Daily Progress
        val targetKcal = summaries.find { it.diet.isMain }?.totalNutrition?.energiaKcal ?: 2000.0
        val targetProtein = summaries.find { it.diet.isMain }?.totalNutrition?.proteina ?: 100.0
        val targetCarbs = summaries.find { it.diet.isMain }?.totalNutrition?.carboidratos ?: 250.0
        val targetFat = summaries.find { it.diet.isMain }?.totalNutrition?.lipidios?.total ?: 70.0

        val (consumedKcal, consumedProt, consumedCarbs, consumedFat) = calculateConsumedNutrients(
            todayLogs
        )

        val dailyProgress = DailyProgress(
            consumedKcal = consumedKcal,
            targetKcal = targetKcal,
            consumedProtein = consumedProt,
            targetProtein = targetProtein,
            consumedCarbs = consumedCarbs,
            targetCarbs = targetCarbs,
            consumedFat = consumedFat,
            targetFat = targetFat
        )

        val waterIntake = WaterIntake(
            currentMl = waterLog?.quantityMl ?: 0,
            targetMl = 3000 // Default target, ideally from settings
        )

        DashboardData(summaries, nextMeal, timeline, dailyProgress, waterIntake)
    }

    data class DashboardData(
        val summaries: List<DietSummary>,
        val nextMeal: DashboardMealGroup?,
        val timeline: List<DashboardMealGroup>,
        val dailyProgress: DailyProgress,
        val waterIntake: WaterIntake
    )

    val state: StateFlow<HomeState> = combine(
        listOf(
            dashboardData,
            searchTerm,
            _searchIsLoading,
            _rawSearchResults,
            expandedAlimentoId,
            quickAddAmount,
            sortOption,
            _showRegistrarTutorial,
            isSearchExpanded,
            showAllResults
        )
    ) { args ->
        val dashboard = args[0] as DashboardData
        val term = args[1] as String
        val isLoading = args[2] as Boolean
        val rawResults = args[3] as List<Food>
        val expandedId = args[4] as Int?
        val amount = args[5] as String
        val sort = args[6] as FoodSortOption
        val showTutorial = args[7] as Boolean
        val searchExpanded = args[8] as Boolean
        val allResults = args[9] as Boolean

        val sortedResults = when (sort) {
            FoodSortOption.RELEVANCE -> rawResults
            FoodSortOption.PROTEIN -> rawResults.sortedByDescending { it.proteina ?: 0.0 }
            FoodSortOption.CARBS -> rawResults.sortedByDescending { it.carboidratos ?: 0.0 }
            FoodSortOption.FAT -> rawResults.sortedByDescending { it.lipidios?.total ?: 0.0 }
            FoodSortOption.CALORIES -> rawResults.sortedByDescending { it.energiaKcal ?: 0.0 }
        }

        HomeState(
            dietSummaries = dashboard.summaries,
            nextMeal = dashboard.nextMeal,
            dailyTimeline = dashboard.timeline,
            dailyProgress = dashboard.dailyProgress,
            waterIntake = dashboard.waterIntake,
            isSearchExpanded = searchExpanded,
            showAllResults = allResults,
            searchTerm = term,
            searchIsLoading = isLoading,
            searchResults = sortedResults,
            expandedAlimentoId = expandedId,
            quickAddAmount = amount,
            sortOption = sort,
            showRegistrarTutorial = showTutorial
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState()
    )

    init {
        observeSearchTerm()
        observeTutorials()
    }

    private fun observeTutorials() {
        viewModelScope.launch {
            onboardingRepository.hasSeenTutorial("registrar_tutorial").collect { seen ->
                // TODO: Re-enable when tooltip dismiss is fixed
                // _showRegistrarTutorial.value = !seen
            }
        }
    }

    fun dismissRegistrarTutorial() {
        viewModelScope.launch {
            onboardingRepository.markTutorialSeen("registrar_tutorial")
            _showRegistrarTutorial.value = false
        }
    }

    fun updateDietChartPreference(dietId: Int, chartType: ChartType) {
        viewModelScope.launch {
            userProfileRepository.saveDietChartPreference(dietId, chartType.name)
        }
    }

    fun addFoodToDiet(dietId: Int, foodId: Int, quantity: Double, mealType: String, time: String) {
        viewModelScope.launch {
            val item = DietItem(
                dietId = dietId,
                foodId = foodId,
                quantityGrams = quantity,
                mealType = mealType,
                consumptionTime = time
            )
            dietItemDao.insertDietItem(item)
            foodDao.incrementUsageCount(foodId)
            _effects.send(
                HomeEffect.ShowSnackbar(
                    "Adicionado à dieta com sucesso.", "Ver", SnackbarAction.GoToDiet(dietId)
                )
            )
        }
    }

    fun addFoodToLog(food: Food, quantity: Double, mealType: String = "Outros") {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val log = DailyLog(
                foodId = food.id,
                date = today,
                quantityGrams = quantity,
                mealType = mealType,
                isConsumed = true
            )
            dailyLogDao.insertLog(log)
            foodDao.incrementUsageCount(food.id)
            _effects.send(
                HomeEffect.ShowSnackbar(
                    "Adicionado ao diário com sucesso.", "Ver", SnackbarAction.GoToDiary
                )
            )
        }
    }

    fun addMealToLog(meal: DashboardMealGroup) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val time = try {
                LocalTime.parse(meal.time)
            } catch (e: Exception) {
                LocalTime.now()
            }
            val dateTime = LocalDateTime.of(today, time)
            val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            meal.items.forEach { item ->
                val log = DailyLog(
                    foodId = item.food.id,
                    date = today.toString(),
                    quantityGrams = item.dietItem.quantityGrams,
                    mealType = meal.mealType,
                    entryTimestamp = timestamp,
                    isConsumed = true
                )
                dailyLogDao.insertLog(log)
                foodDao.incrementUsageCount(item.food.id)
            }
            _effects.send(
                HomeEffect.ShowSnackbar(
                    "${meal.mealType} registrado com sucesso!", "Ver", SnackbarAction.GoToDiary
                )
            )
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val currentLog = dailyWaterLogDao.getWaterLog(today).first()
            val newAmount = (currentLog?.quantityMl ?: 0) + amountMl
            dailyWaterLogDao.insertOrUpdate(DailyWaterLog(date = today, quantityMl = newAmount))
        }
    }

    private fun calculateConsumedNutrients(logs: List<DailyLogWithFood>): List<Double> {
        var kcal = 0.0
        var prot = 0.0
        var carb = 0.0
        var fat = 0.0

        logs.forEach { log ->
            val nutrients = NutrientCalculator.calcularNutrientesParaPorcao(
                foodBase = log.food,
                quantidadeDesejadaGramas = log.log.quantityGrams
            )
            kcal += nutrients.energiaKcal ?: 0.0
            prot += nutrients.proteina ?: 0.0
            carb += nutrients.carboidratos ?: 0.0
            fat += nutrients.lipidios?.total ?: 0.0
        }
        return listOf(kcal, prot, carb, fat)
    }

    private fun calculateTimeline(items: List<DietItemWithFood>): List<DashboardMealGroup> {
        if (items.isEmpty()) return emptyList()

        val now = LocalTime.now()
        val currentTimeStr = String.format(java.util.Locale.US, "%02d:%02d", now.hour, now.minute)

        return items.filter { !it.dietItem.consumptionTime.isNullOrBlank() }
            .groupBy { it.dietItem.consumptionTime!! }.mapValues { entry ->
                val firstMealType = entry.value.firstOrNull()?.dietItem?.mealType ?: "Refeição"
                DashboardMealGroup(
                    time = entry.key,
                    mealType = firstMealType,
                    items = entry.value,
                    isPassed = entry.key < currentTimeStr
                )
            }.values.sortedBy { it.time }
    }

    private fun calculateNextMeal(timeline: List<DashboardMealGroup>): DashboardMealGroup? {
        if (timeline.isEmpty()) return null

        val now = LocalTime.now()
        val currentTimeStr = String.format(java.util.Locale.US, "%02d:%02d", now.hour, now.minute)

        // Find first meal after current time
        return timeline.firstOrNull { it.time >= currentTimeStr }
            ?: timeline.firstOrNull() // If no more meals today, show first of day (optional)
    }

    private fun calculateTotalNutrition(diet: DietWithItems): Food {
        var accKcal = 0.0
        var accProt = 0.0
        var accCarb = 0.0
        var accLipTotal = 0.0
        var accLipSat = 0.0
        var accLipMono = 0.0
        var accLipPoly = 0.0

        var accFibra = 0.0
        var accColest = 0.0
        var accSodio = 0.0
        var accCalcio = 0.0
        var accFerro = 0.0
        var accMagnesio = 0.0
        var accFosforo = 0.0
        var accPotassio = 0.0
        var accZinco = 0.0
        var accCobre = 0.0
        var accManganes = 0.0

        var accVitC = 0.0
        var accRetinol = 0.0
        var accTiamina = 0.0
        var accRiboflavina = 0.0
        var accNiacina = 0.0
        var accPiridoxina = 0.0

        diet.items.forEach { item ->
            val n = NutrientCalculator.calcularNutrientesParaPorcao(
                foodBase = item.food, quantidadeDesejadaGramas = item.dietItem.quantityGrams
            )
            accKcal += n.energiaKcal ?: 0.0
            accProt += n.proteina ?: 0.0
            accCarb += n.carboidratos ?: 0.0
            accLipTotal += n.lipidios?.total ?: 0.0
            accLipSat += n.lipidios?.saturados ?: 0.0
            accLipMono += n.lipidios?.monoinsaturados ?: 0.0
            accLipPoly += n.lipidios?.poliinsaturados ?: 0.0

            accFibra += n.fibraAlimentar ?: 0.0
            accColest += n.colesterol ?: 0.0
            accSodio += n.sodio ?: 0.0
            accCalcio += n.calcio ?: 0.0
            accFerro += n.ferro ?: 0.0
            accMagnesio += n.magnesio ?: 0.0
            accFosforo += n.fosforo ?: 0.0
            accPotassio += n.potassio ?: 0.0
            accZinco += n.zinco ?: 0.0
            accCobre += n.cobre ?: 0.0
            accManganes += n.manganes ?: 0.0

            accVitC += n.vitaminaC ?: 0.0
            accRetinol += n.retinol ?: 0.0
            accTiamina += n.tiamina ?: 0.0
            accRiboflavina += n.riboflavina ?: 0.0
            accNiacina += n.niacina ?: 0.0
            accPiridoxina += n.piridoxina ?: 0.0
        }

        return Food(
            id = -1,
            tacoID = "",
            name = diet.diet.name, // Use diet name as label
            category = if (diet.diet.isMain) "Dieta Principal" else "Dieta",
            energiaKcal = accKcal,
            energiaKj = 0.0,
            proteina = accProt,
            carboidratos = accCarb,
            lipidios = Lipidios(accLipTotal, accLipSat, accLipMono, accLipPoly),
            fibraAlimentar = accFibra,
            colesterol = accColest,
            cinzas = 0.0,
            umidade = 0.0,
            sodio = accSodio,
            calcio = accCalcio,
            ferro = accFerro,
            magnesio = accMagnesio,
            fosforo = accFosforo,
            potassio = accPotassio,
            zinco = accZinco,
            cobre = accCobre,
            manganes = accManganes,
            vitaminaC = accVitC,
            retinol = accRetinol,
            RE = 0.0,
            RAE = 0.0,
            tiamina = accTiamina,
            riboflavina = accRiboflavina,
            niacina = accNiacina,
            piridoxina = accPiridoxina,
            aminoacidos = null
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeSearchTerm() {
        viewModelScope.launch {
            searchTerm.debounce(300).distinctUntilChanged()
                .flatMapLatest { term ->
                    if (term.length < 2) {
                        flowOf(emptyList())
                    } else {
                        _searchIsLoading.value = true
                        foodDao.getFoodsByName(term)
                    }
                }.catch {
                    _searchIsLoading.value = false
                    // Handle error (maybe clear results or show toast)
                }.collect { results ->
                    _rawSearchResults.value = results
                    _searchIsLoading.value = false
                }
        }
    }

    fun onSearchTermChange(term: String) {
        savedStateHandle[KEY_SEARCH_TERM] = term
        savedStateHandle[KEY_EXPANDED_FOOD_ID] = null
    }

    fun onSortOptionSelected(option: FoodSortOption) {
        savedStateHandle[KEY_SORT_OPTION] = option
    }

    fun onAlimentoToggled(alimentoId: Int) {
        val currentId = savedStateHandle.get<Int?>(KEY_EXPANDED_FOOD_ID)
        if (currentId == alimentoId) {
            savedStateHandle[KEY_EXPANDED_FOOD_ID] = null
        } else {
            savedStateHandle[KEY_EXPANDED_FOOD_ID] = alimentoId
            savedStateHandle[KEY_QUICK_ADD_AMOUNT] = "100"
        }
    }

    fun onQuickAddAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        savedStateHandle[KEY_QUICK_ADD_AMOUNT] = filtered
    }

    fun cleanSearch() {
        savedStateHandle[KEY_SEARCH_TERM] = ""
        savedStateHandle[KEY_EXPANDED_FOOD_ID] = null
        savedStateHandle[KEY_QUICK_ADD_AMOUNT] = "100"
        savedStateHandle[KEY_SORT_OPTION] = FoodSortOption.RELEVANCE
        _rawSearchResults.value = emptyList()
    }

    fun setSearchExpanded(expanded: Boolean) {
        savedStateHandle[KEY_SEARCH_EXPANDED] = expanded
        if (!expanded) {
            savedStateHandle[KEY_SHOW_ALL_RESULTS] = false
        }
    }

    fun setShowAllResults(showAll: Boolean) {
        savedStateHandle[KEY_SHOW_ALL_RESULTS] = showAll
    }

    fun cloneAndEdit(food: Food, onNavigateToEdit: (Int) -> Unit) {
        viewModelScope.launch {
            // generates the "Food (Cópia)"
            val copyRegex = Regex("^(.*) \\(Cópia(?: #(\\d+))?\\)$")
            val match = copyRegex.matchEntire(food.name)
            val coreName = if (match != null) match.groupValues[1] else food.name
            val pattern = "$coreName (Cópia%"

            // We use the DAO to check existing copies to increment the # number
            val existingFoods = foodDao.findFoodsByNameLike(pattern)

            var maxIndex = 0
            var hasUnnumberedCopy = false

            existingFoods.forEach { f ->
                val m = copyRegex.matchEntire(f.name)
                if (m != null && m.groupValues[1] == coreName) {
                    val indexStr = m.groupValues[2]
                    if (indexStr.isEmpty()) {
                        hasUnnumberedCopy = true
                        if (maxIndex < 1) maxIndex = 1
                    } else {
                        val index = indexStr.toIntOrNull() ?: 0
                        if (index > maxIndex) maxIndex = index
                    }
                }
            }

            val newSuffix =
                if (maxIndex == 0 && !hasUnnumberedCopy) " (Cópia)" else " (Cópia #${maxIndex + 1})"
            val newName = "$coreName$newSuffix"

            // now it Creates the Copy
            val clonedFood = food.copy(
                id = 0,
                tacoID = "CUSTOM-${UUID.randomUUID()}",
                name = newName,
                isCustom = true,
                category = "Meus Alimentos"
            )

            val newId = foodDao.insertFood(clonedFood).toInt()
            onNavigateToEdit(newId)
        }
    }

    fun setMainDiet(dietId: Int) {
        viewModelScope.launch {
            val currentDiets = availableDiets.first()
            currentDiets.forEach { diet ->
                if (diet.isMain && diet.id != dietId) {
                    dietDao.updateDiet(diet.copy(isMain = false))
                } else if (!diet.isMain && diet.id == dietId) {
                    dietDao.updateDiet(diet.copy(isMain = true))
                }
            }
        }
    }
}