package com.mekki.taco.presentation.ui.diet

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.data.model.DietWithItems
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.data.service.DietScannerService
import com.mekki.taco.data.service.SmartFoodMatcher
import com.mekki.taco.presentation.ui.search.FoodSearchManager
import com.mekki.taco.utils.BMRCalculator
import com.mekki.taco.utils.NutrientCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

enum class DietGoalType {
    CUT, MAINTAIN, BULK
}

data class SmartGoal(
    val type: DietGoalType,
    val calories: Int,
    val label: String,
    val description: String
)

private const val KEY_IS_EDIT_MODE = "is_edit_mode"
private const val KEY_DIET_DETAILS = "diet_details"
private const val KEY_HAS_UNSAVED = "has_unsaved_changes"
private const val KEY_FOCUSED_MEAL = "focused_meal_type"
private const val KEY_PENDING_CREATE_FOOD = "pending_create_food"

@HiltViewModel
class DietDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dietDao: DietDao,
    private val dietItemDao: DietItemDao,
    private val foodDao: FoodDao,
    private val dailyLogDao: DailyLogDao,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    val dietId: Int = savedStateHandle.get<Int>("dietId") ?: -1

    val isEditMode = savedStateHandle.getStateFlow(KEY_IS_EDIT_MODE, dietId == -1)
    val foodSearchManager = FoodSearchManager(foodDao, viewModelScope, savedStateHandle)

    fun toggleEditMode() {
        savedStateHandle[KEY_IS_EDIT_MODE] = !isEditMode.value
    }

    fun setEditMode(enabled: Boolean) {
        savedStateHandle[KEY_IS_EDIT_MODE] = enabled
    }

    fun setFocusedMealType(mealType: String?) {
        savedStateHandle[KEY_FOCUSED_MEAL] = mealType
        if (mealType == null) {
            foodSearchManager.clear()
        }
    }

    // --- Core Diet State ---
    val dietDetails = savedStateHandle.getStateFlow<DietWithItems?>(KEY_DIET_DETAILS, null)

    private val _groupedItems = MutableStateFlow<Map<String, List<DietItemWithFood>>>(emptyMap())
    val groupedItems = _groupedItems.asStateFlow()

    private val _dietTotalNutrition = MutableStateFlow<Food?>(null)
    val dietTotalNutrition = _dietTotalNutrition.asStateFlow()

    val hasUnsavedChanges = savedStateHandle.getStateFlow(KEY_HAS_UNSAVED, false)

    val focusedMealType = savedStateHandle.getStateFlow<String?>(KEY_FOCUSED_MEAL, null)

    // --- Scanner State ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _isProcessingScan = MutableStateFlow(false)
    val isProcessingScan = _isProcessingScan.asStateFlow()

    private val _showScanReview = MutableStateFlow(false)
    val showScanReview = _showScanReview.asStateFlow()

    private val _scannedCandidates = MutableStateFlow<List<DietItemWithFood>>(emptyList())
    val scannedCandidates = _scannedCandidates.asStateFlow()

    // --- Smart Goals State ---
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _suggestedGoals = MutableStateFlow<List<SmartGoal>>(emptyList())
    val suggestedGoals = _suggestedGoals.asStateFlow()

    private var pendingCreateFood: Boolean
        get() = savedStateHandle[KEY_PENDING_CREATE_FOOD] ?: false
        set(value) {
            savedStateHandle[KEY_PENDING_CREATE_FOOD] = value
        }

    // --- Navigation & UI Events ---
    private val _navigateToEditFood = Channel<Int>(Channel.BUFFERED)
    val navigateToEditFood = _navigateToEditFood.receiveAsFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    private val _snackbarMessages = Channel<String>(Channel.BUFFERED)
    val snackbarMessages = _snackbarMessages.receiveAsFlow()

    private val scannerService = DietScannerService(foodDao)
    private val foodMatcher = SmartFoodMatcher(foodDao)

    val mealTypes =
        listOf("Café da Manhã", "Almoço", "Lanche", "Pré-treino", "Pós-treino", "Jantar")

    init {
        loadDietDetails()
        observeUserProfile()

        // Recalculates from SSh
        dietDetails.onEach { details ->
            if (details != null) {
                processDietItems(details.items)
            }
        }.launchIn(viewModelScope)
    }

    private fun loadDietDetails() {
        viewModelScope.launch {
            if (dietId == -1) {
                // Only initialize if not already present e.g from process death
                if (dietDetails.value == null) {
                    val newDiet =
                        Diet(
                            name = "",
                            creationDate = System.currentTimeMillis(),
                            calorieGoals = 0.0
                        )
                    val empty = DietWithItems(newDiet, emptyList())

                    savedStateHandle[KEY_DIET_DETAILS] = empty
                    savedStateHandle[KEY_HAS_UNSAVED] = true
                    savedStateHandle[KEY_IS_EDIT_MODE] = true
                }
            } else {
                dietDao.getDietWithItemsById(dietId).collect { diet ->
                    // Only update from DB if we don't have unsaved changes
                    if (!hasUnsavedChanges.value) {
                        savedStateHandle[KEY_DIET_DETAILS] = diet
                    }
                }
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            userProfileRepository.userProfileFlow.collect { profile ->
                _userProfile.value = profile
                if (profile.weight != null && profile.height != null) {
                    _suggestedGoals.value = calculateSmartGoals(profile)
                }
            }
        }
    }

    private fun calculateSmartGoals(profile: UserProfile): List<SmartGoal> {
        val bmr = BMRCalculator.calculateBMR(profile)
        val multiplier = profile.activityLevel?.multiplier ?: 1.2
        val tdee = (bmr * multiplier).toInt()

        return listOf(
            SmartGoal(
                DietGoalType.CUT,
                (tdee - 500).coerceAtLeast(1200),
                "Déficit",
                "Perda de peso"
            ),
            SmartGoal(DietGoalType.MAINTAIN, tdee, "Manter", "Manter peso"),
            SmartGoal(DietGoalType.BULK, tdee + 300, "Superávit", "Ganho de massa")
        )
    }

    fun onDietNameChange(newName: String) {
        val current = dietDetails.value ?: return
        if (current.diet.name != newName) {
            updateLocalState(current.copy(diet = current.diet.copy(name = newName)))
        }
    }

    fun onCalorieGoalChange(newGoal: Double) {
        val current = dietDetails.value ?: return
        if (current.diet.calorieGoals != newGoal) {
            updateLocalState(current.copy(diet = current.diet.copy(calorieGoals = newGoal)))
        }
    }

    fun applySmartGoal(goal: SmartGoal) {
        onCalorieGoalChange(goal.calories.toDouble())
    }

    fun onStartCreateFood() {
        pendingCreateFood = true
        viewModelScope.launch {
            _navigateToEditFood.send(0)
        }
    }

    fun addFoodToMeal(food: Food, quantity: Double = 100.0) {
        val mealType = focusedMealType.value ?: return
        val currentDiet = dietDetails.value ?: return

        // Determine default time based on meal type or last item
        val existingInMeal = _groupedItems.value[mealType] ?: emptyList()
        val defaultTime = existingInMeal.lastOrNull()?.dietItem?.consumptionTime
            ?: getDefaultTimeForMeal(mealType)

        val newItem = DietItemWithFood(
            dietItem = DietItem(
                id = UUID.randomUUID().hashCode(), // Temporary ID
                dietId = if (dietId == -1) 0 else dietId,
                foodId = food.id,
                quantityGrams = quantity,
                mealType = mealType,
                consumptionTime = defaultTime
            ),
            food = food
        )

        val newItems = currentDiet.items + newItem
        updateLocalState(currentDiet.copy(items = newItems))

        viewModelScope.launch {
            foodDao.incrementUsageCount(food.id)
            _snackbarMessages.send("${food.name} adicionado ao $mealType")
        }
    }

    // --- Clone Feature ---
    fun cloneItemToMeal(item: DietItemWithFood, targetMeal: String) {
        val currentDiet = dietDetails.value ?: return
        val defaultTime = getDefaultTimeForMeal(targetMeal)

        val newItem = item.copy(
            dietItem = item.dietItem.copy(
                id = UUID.randomUUID().hashCode(), // New ID
                mealType = targetMeal,
                consumptionTime = defaultTime
            )
        )

        val newItems = currentDiet.items + newItem
        updateLocalState(currentDiet.copy(items = newItems))

        viewModelScope.launch {
            _snackbarMessages.send("${item.food.name} copiado para $targetMeal")
        }
    }

    // --- Scanner Logic ---

    fun onStartScan() {
        _isScanning.value = true
    }

    fun onCancelScan() {
        _isScanning.value = false
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        _isScanning.value = false
        _isProcessingScan.value = true

        viewModelScope.launch {
            try {
                val scanned = scannerService.scanAndParseDiet(bitmap)
                val matched = foodMatcher.matchItems(scanned)
                _scannedCandidates.value = matched
                _showScanReview.value = true
            } catch (e: Exception) {
                _snackbarMessages.send("Erro ao processar imagem: ${e.message}")
            } finally {
                _isProcessingScan.value = false
            }
        }
    }

    fun onConfirmScanResults(candidates: List<DietItemWithFood>) {
        val currentDiet = dietDetails.value ?: return
        val newItems = currentDiet.items.toMutableList()

        candidates.forEach { candidate ->
            // Ensure unique IDs
            val uniqueItem = candidate.copy(
                dietItem = candidate.dietItem.copy(id = UUID.randomUUID().hashCode())
            )
            newItems.add(uniqueItem)
        }

        updateLocalState(currentDiet.copy(items = newItems))

        viewModelScope.launch {
            candidates.forEach { foodDao.incrementUsageCount(it.food.id) }
        }

        _showScanReview.value = false
        _scannedCandidates.value = emptyList()
    }

    fun onDiscardScanResults() {
        _showScanReview.value = false
        _scannedCandidates.value = emptyList()
    }


    // --- CRUD ---

    fun updateItem(item: DietItem) {
        val currentDiet = dietDetails.value ?: return
        val updatedItems = currentDiet.items.map {
            if (it.dietItem.id == item.id) it.copy(dietItem = item) else it
        }
        updateLocalState(currentDiet.copy(items = updatedItems))
    }

    fun deleteItem(item: DietItem) {
        val currentDiet = dietDetails.value ?: return
        val updatedItems = currentDiet.items.filterNot { it.dietItem.id == item.id }
        updateLocalState(currentDiet.copy(items = updatedItems))
    }

    fun replaceFood(item: DietItemWithFood, newFood: Food) {
        val currentDiet = dietDetails.value ?: return
        val updatedItems = currentDiet.items.map {
            if (it.dietItem.id == item.dietItem.id) {
                it.copy(dietItem = it.dietItem.copy(foodId = newFood.id), food = newFood)
            } else it
        }
        updateLocalState(currentDiet.copy(items = updatedItems))
    }

    fun reorderFoodItemsInMeal(mealType: String, fromIndex: Int, toIndex: Int) {
        val currentDiet = dietDetails.value ?: return
        val mealItems = _groupedItems.value[mealType]?.toMutableList() ?: return

        if (fromIndex !in mealItems.indices || toIndex !in mealItems.indices) return

        // Reorder in local meal list
        val item = mealItems.removeAt(fromIndex)
        mealItems.add(toIndex, item)

        // Reconstruct full list (keeping other meals intact)
        val otherItems = currentDiet.items.filter { it.dietItem.mealType != mealType }
        val newFullList = otherItems + mealItems

        updateLocalState(currentDiet.copy(items = newFullList))
    }

    fun editFood(item: DietItemWithFood) {
        viewModelScope.launch {
            // Logic to fork food if needed, then navigate
            if (item.food.isCustom) {
                _navigateToEditFood.send(item.food.id)
            } else {
                cloneAndEdit(item)
            }
        }
    }

    private suspend fun cloneAndEdit(item: DietItemWithFood) {
        val food = item.food
        val newName = "${food.name} (Cópia)"
        val newUuid = UUID.randomUUID().toString()

        val clonedFood = food.copy(
            id = 0,
            tacoID = "CUSTOM-$newUuid",
            uuid = newUuid,
            name = newName,
            isCustom = true,
            category = "Meus Alimentos"
        )
        val newId = foodDao.insertFood(clonedFood).toInt()

        val currentDiet = dietDetails.value ?: return
        val updatedItems = currentDiet.items.map {
            if (it.dietItem.id == item.dietItem.id) {
                it.copy(
                    dietItem = it.dietItem.copy(foodId = newId),
                    food = clonedFood.copy(id = newId)
                )
            } else it
        }
        updateLocalState(currentDiet.copy(items = updatedItems))
        _navigateToEditFood.send(newId)
    }

    // --- Persistence ---

    fun saveDiet() {
        viewModelScope.launch {
            val currentDiet = dietDetails.value ?: return@launch
            var dietToSave = currentDiet.diet

            if (dietToSave.name.isBlank()) {
                dietToSave = dietToSave.copy(name = "Nova Dieta")
            }

            val savedDietId = dietDao.insertOrReplaceDiet(dietToSave).toInt()

            // Delete old items and insert new ones
            dietItemDao.deleteAllItemsByDietId(savedDietId)

            val itemsToSave = currentDiet.items.map {
                it.dietItem.copy(dietId = savedDietId)
            }
            if (itemsToSave.isNotEmpty()) {
                dietItemDao.insertDietItems(itemsToSave)
            }

            savedStateHandle[KEY_HAS_UNSAVED] = false
            savedStateHandle[KEY_IS_EDIT_MODE] = false

            if (dietId == -1) {
                // If it was new, we are done, navigate back
                _navigateBack.emit(Unit)
            } else {
                _snackbarMessages.send("Dieta salva com sucesso.")
            }
        }
    }

    fun discardChanges() {
        if (dietId == -1) {
            viewModelScope.launch { _navigateBack.emit(Unit) }
        } else {
            loadDietDetails()
            savedStateHandle[KEY_HAS_UNSAVED] = false
        }
    }

    // --- Helpers ---

    private fun updateLocalState(newDiet: DietWithItems) {
        savedStateHandle[KEY_DIET_DETAILS] = newDiet
        savedStateHandle[KEY_HAS_UNSAVED] = true
    }

    private fun processDietItems(items: List<DietItemWithFood>) {
        _groupedItems.value = items.groupBy { it.dietItem.mealType ?: "Sem Categoria" }

        var accKcal = 0.0
        var accKj = 0.0
        var accProt = 0.0
        var accCarb = 0.0

        // Gorduras
        var accLipTotal = 0.0
        var accLipSat = 0.0
        var accLipMono = 0.0
        var accLipPoli = 0.0

        var accFibra = 0.0
        var accColesterol = 0.0
        var accSodio = 0.0
        var accCalcio = 0.0
        var accFerro = 0.0
        var accMagnesio = 0.0
        var accFosforo = 0.0
        var accPotassio = 0.0
        var accZinco = 0.0
        var accCobre = 0.0
        var accManganes = 0.0
        var accVitaminaC = 0.0
        var accRetinol = 0.0
        var accRE = 0.0
        var accRAE = 0.0
        var accTiamina = 0.0
        var accRiboflavina = 0.0
        var accNiacina = 0.0
        var accPiridoxina = 0.0
        var accCinzas = 0.0

        items.forEach { item ->
            val n = NutrientCalculator.calcularNutrientesParaPorcao(
                item.food,
                item.dietItem.quantityGrams
            )
            accKcal += n.energiaKcal ?: 0.0
            accKj += n.energiaKj ?: 0.0
            accProt += n.proteina ?: 0.0
            accCarb += n.carboidratos ?: 0.0

            accLipTotal += n.lipidios?.total ?: 0.0
            accLipSat += n.lipidios?.saturados ?: 0.0
            accLipMono += n.lipidios?.monoinsaturados ?: 0.0
            accLipPoli += n.lipidios?.poliinsaturados ?: 0.0

            accFibra += n.fibraAlimentar ?: 0.0
            accColesterol += n.colesterol ?: 0.0
            accSodio += n.sodio ?: 0.0
            accCalcio += n.calcio ?: 0.0
            accFerro += n.ferro ?: 0.0
            accMagnesio += n.magnesio ?: 0.0
            accFosforo += n.fosforo ?: 0.0
            accPotassio += n.potassio ?: 0.0
            accZinco += n.zinco ?: 0.0
            accCobre += n.cobre ?: 0.0
            accManganes += n.manganes ?: 0.0
            accVitaminaC += n.vitaminaC ?: 0.0
            accRetinol += n.retinol ?: 0.0
            accRE += n.RE ?: 0.0
            accRAE += n.RAE ?: 0.0
            accTiamina += n.tiamina ?: 0.0
            accRiboflavina += n.riboflavina ?: 0.0
            accNiacina += n.niacina ?: 0.0
            accPiridoxina += n.piridoxina ?: 0.0
            accCinzas += n.cinzas ?: 0.0
        }

        _dietTotalNutrition.value = Food(
            id = -1, name = "Total", category = "", tacoID = "",
            energiaKcal = accKcal,
            energiaKj = accKj,
            proteina = accProt,
            carboidratos = accCarb,
            lipidios = Lipidios(accLipTotal, accLipSat, accLipMono, accLipPoli),
            fibraAlimentar = accFibra,
            colesterol = accColesterol,
            sodio = accSodio,
            calcio = accCalcio,
            ferro = accFerro,
            magnesio = accMagnesio,
            fosforo = accFosforo,
            potassio = accPotassio,
            zinco = accZinco,
            cobre = accCobre,
            manganes = accManganes,
            vitaminaC = accVitaminaC,
            retinol = accRetinol,
            RE = accRE,
            RAE = accRAE,
            tiamina = accTiamina,
            riboflavina = accRiboflavina,
            niacina = accNiacina,
            piridoxina = accPiridoxina,
            cinzas = accCinzas,
            umidade = 0.0,
            aminoacidos = null,
        )
    }

    fun getDefaultTimeForMeal(mealType: String): String {
        return when (mealType) {
            "Café da Manhã" -> "08:00"
            "Almoço" -> "12:00"
            "Jantar" -> "19:00"
            "Lanche" -> "15:00"
            else -> "12:00"
        }
    }

    // --- Public access for DailyLog ---
    // TODO: needs to respect time of meal when ENTERING the log
    fun addToDailyLog(item: DietItemWithFood) {
        viewModelScope.launch {
            val log = DailyLog(
                foodId = item.food.id,
                date = LocalDate.now().toString(),
                quantityGrams = item.dietItem.quantityGrams,
                mealType = item.dietItem.mealType ?: "Outros",
                isConsumed = true
            )
            dailyLogDao.insertLog(log)
            foodDao.incrementUsageCount(item.food.id)
            _snackbarMessages.send("Adicionado ao diário.")
        }
    }

    // For Food Update callback
    fun onFoodUpdated(foodId: Int) {
        viewModelScope.launch {
            val updatedFood = foodDao.getFoodById(foodId).firstOrNull() ?: return@launch

            if (pendingCreateFood) {
                if (focusedMealType.value != null) {
                    addFoodToMeal(updatedFood)
                    setFocusedMealType(null)
                }
                pendingCreateFood = false
            }

            val currentDiet = dietDetails.value ?: return@launch
            val updatedList = currentDiet.items.map {
                if (it.dietItem.foodId == foodId) it.copy(food = updatedFood) else it
            }
            // Updates local state without flagging as unsaved if only food content changed
            savedStateHandle[KEY_DIET_DETAILS] = currentDiet.copy(items = updatedList)
        }
    }
}