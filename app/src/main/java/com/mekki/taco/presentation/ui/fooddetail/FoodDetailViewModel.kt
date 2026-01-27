package com.mekki.taco.presentation.ui.fooddetail

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.UUID
import javax.inject.Inject

@Parcelize
data class FoodDetailState(
    val isLoading: Boolean = true,
    val portion: String = "100",
    val displayFood: Food? = null,
    val isEditMode: Boolean = false,
    val hasUnsavedChanges: Boolean = false,

    // Edit State
    val editName: String = "",
    val editPortionBase: String = "100",
    val editFields: Map<String, String> = emptyMap()
) : Parcelable

private const val KEY_PORTION = "portion"
private const val KEY_IS_EDIT_MODE = "is_edit_mode"
private const val KEY_EDIT_STATE = "edit_state"

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val foodDao: FoodDao,
    private val dietDao: DietDao
) : ViewModel() {

    private val alimentoId: Int = savedStateHandle.get<Int>("foodId") ?: 0
    private val initialEditMode: Boolean = savedStateHandle.get<Boolean>("edit") ?: false

    val availableDiets = dietDao.getAllDiets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val portion = savedStateHandle.getStateFlow(KEY_PORTION, "100")
    private val _baseFood = MutableStateFlow<Food?>(null)

    // Edit mode flow
    private val isEditMode = savedStateHandle.getStateFlow(KEY_IS_EDIT_MODE, initialEditMode)
    
    // Persist edit state (saves user draft)
    private val editState = savedStateHandle.getStateFlow(KEY_EDIT_STATE, FoodDetailState())

    init {
        loadFood()
    }

    private fun loadFood() {
        if (alimentoId == 0) {
            val newUuid = UUID.randomUUID().toString()
            val newFood = Food(
                id = 0,
                tacoID = "CUSTOM-$newUuid",
                uuid = newUuid,
                name = "",
                category = "Meus Alimentos",
                isCustom = true,
                energiaKcal = 0.0, energiaKj = 0.0, proteina = 0.0, colesterol = 0.0,
                carboidratos = 0.0, fibraAlimentar = 0.0, cinzas = 0.0, calcio = 0.0,
                magnesio = 0.0, manganes = 0.0, fosforo = 0.0, ferro = 0.0, sodio = 0.0,
                potassio = 0.0, cobre = 0.0, zinco = 0.0, retinol = 0.0, RE = 0.0, RAE = 0.0,
                tiamina = 0.0, riboflavina = 0.0, piridoxina = 0.0, niacina = 0.0,
                vitaminaC = 0.0, umidade = 0.0, lipidios = null, aminoacidos = null
            )
            _baseFood.value = newFood
            
            // Only initialize edit state if it's empty/default (not restored)
            if (editState.value.editName.isEmpty() && editState.value.editFields.isEmpty()) {
                savedStateHandle[KEY_IS_EDIT_MODE] = true
                updateEditState {
                    it.copy(
                        editName = "",
                        editPortionBase = "100",
                        editFields = emptyMap()
                    )
                }
            }
        } else {
            viewModelScope.launch {
                foodDao.getFoodById(alimentoId).collect { food ->
                    _baseFood.value = food
                    
                    if (editState.value.editName.isEmpty() && food != null) {
                         updateEditState {
                            it.copy(
                                editName = food.name,
                                editPortionBase = "100",
                                editFields = mapOf(
                                    "kcal" to food.energiaKcal.formatDouble(),
                                    "protein" to food.proteina.formatDouble(),
                                    "carbs" to food.carboidratos.formatDouble(),
                                    "fat" to food.lipidios?.total.formatDouble(),
                                    "fiber" to food.fibraAlimentar.formatDouble(),
                                    "colest" to food.colesterol.formatDouble(),
                                    "sodio" to food.sodio.formatDouble(),
                                    "calcio" to food.calcio.formatDouble(),
                                    "magnesio" to food.magnesio.formatDouble(),
                                    "manganes" to food.manganes.formatDouble(),
                                    "fosforo" to food.fosforo.formatDouble(),
                                    "ferro" to food.ferro.formatDouble(),
                                    "potassio" to food.potassio.formatDouble(),
                                    "cobre" to food.cobre.formatDouble(),
                                    "zinco" to food.zinco.formatDouble(),
                                    "vitc" to food.vitaminaC.formatDouble(),
                                    "retinol" to food.retinol.formatDouble(),
                                    "tiamina" to food.tiamina.formatDouble(),
                                    "riboflavina" to food.riboflavina.formatDouble(),
                                    "niacina" to food.niacina.formatDouble(),
                                    "piridoxina" to food.piridoxina.formatDouble()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateEditState(transform: (FoodDetailState) -> FoodDetailState) {
        val current = savedStateHandle.get<FoodDetailState>(KEY_EDIT_STATE) ?: FoodDetailState()
        savedStateHandle[KEY_EDIT_STATE] = transform(current)
    }

    private fun Double?.formatDouble(): String {
        if (this == null) return ""
        // Round to 3 decimal places
        return java.math.BigDecimal(this).setScale(3, java.math.RoundingMode.HALF_UP)
            .stripTrailingZeros().toPlainString()
    }

    private fun Double?.round(decimals: Int = 3): Double? {
        if (this == null) return null
        return java.math.BigDecimal(this).setScale(decimals, java.math.RoundingMode.HALF_UP)
            .toDouble()
    }

    val uiState: StateFlow<FoodDetailState> =
        combine(
            _baseFood,
            portion,
            isEditMode,
            editState
        ) { base, portionVal, isEditing, editStateVal ->
            if (base == null) {
                FoodDetailState(isLoading = true)
            } else {
                val newPortion = portionVal.toDoubleOrNull() ?: 100.0
                val ratio = newPortion / 100.0
                val recalculatedAlimento = base.copy(
                    energiaKcal = base.energiaKcal?.times(ratio),
                    proteina = base.proteina?.times(ratio),
                    carboidratos = base.carboidratos?.times(ratio),
                    fibraAlimentar = base.fibraAlimentar?.times(ratio),
                    colesterol = base.colesterol?.times(ratio),
                    lipidios = base.lipidios?.copy(
                        total = base.lipidios.total?.times(ratio),
                        saturados = base.lipidios.saturados?.times(ratio),
                        monoinsaturados = base.lipidios.monoinsaturados?.times(ratio),
                        poliinsaturados = base.lipidios.poliinsaturados?.times(ratio)
                    ),
                    sodio = base.sodio?.times(ratio),
                    calcio = base.calcio?.times(ratio),
                    ferro = base.ferro?.times(ratio),
                    magnesio = base.magnesio?.times(ratio),
                    manganes = base.manganes?.times(ratio),
                    fosforo = base.fosforo?.times(ratio),
                    potassio = base.potassio?.times(ratio),
                    cobre = base.cobre?.times(ratio),
                    zinco = base.zinco?.times(ratio),
                    vitaminaC = base.vitaminaC?.times(ratio),
                    retinol = base.retinol?.times(ratio),
                    tiamina = base.tiamina?.times(ratio),
                    riboflavina = base.riboflavina?.times(ratio),
                    piridoxina = base.piridoxina?.times(ratio),
                    niacina = base.niacina?.times(ratio)
                )

                // Checks for unsaved changes
                val hasChanges = isEditing && (
                        editStateVal.editName != base.name ||
                                editStateVal.editPortionBase != "100" ||
                                editStateVal.editFields.any { (key, value) ->
                                    val original = when (key) {
                                        "kcal" -> base.energiaKcal.formatDouble()
                                        "protein" -> base.proteina.formatDouble()
                                        "carbs" -> base.carboidratos.formatDouble()
                                        "fat" -> base.lipidios?.total.formatDouble()
                                        "fiber" -> base.fibraAlimentar.formatDouble()
                                        "colest" -> base.colesterol.formatDouble()
                                        "sodio" -> base.sodio.formatDouble()
                                        "calcio" -> base.calcio.formatDouble()
                                        "magnesio" -> base.magnesio.formatDouble()
                                        "manganes" -> base.manganes.formatDouble()
                                        "fosforo" -> base.fosforo.formatDouble()
                                        "ferro" -> base.ferro.formatDouble()
                                        "potassio" -> base.potassio.formatDouble()
                                        "cobre" -> base.cobre.formatDouble()
                                        "zinco" -> base.zinco.formatDouble()
                                        "vitc" -> base.vitaminaC.formatDouble()
                                        "retinol" -> base.retinol.formatDouble()
                                        "tiamina" -> base.tiamina.formatDouble()
                                        "riboflavina" -> base.riboflavina.formatDouble()
                                        "niacina" -> base.niacina.formatDouble()
                                        "piridoxina" -> base.piridoxina.formatDouble()
                                        else -> ""
                                    }
                                    value != original
                                }
                        )

                FoodDetailState(
                    isLoading = false,
                    portion = portionVal,
                    displayFood = recalculatedAlimento,
                    isEditMode = isEditing,
                    hasUnsavedChanges = hasChanges,
                    editName = editStateVal.editName,
                    editPortionBase = editStateVal.editPortionBase,
                    editFields = editStateVal.editFields
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodDetailState()
        )

    fun updatePortion(newPortion: String) {
        if (newPortion.all { it.isDigit() || it == '.' } && newPortion.length <= 5) {
            savedStateHandle[KEY_PORTION] = newPortion
        }
    }

    fun onEditToggle() {
        // Toggle edit mode
        savedStateHandle[KEY_IS_EDIT_MODE] = !isEditMode.value
        
        // If entering edit mode and edit state is empty, populate it
        if (isEditMode.value) {
            val base = _baseFood.value
            if (base != null && editState.value.editName.isEmpty()) {
                 updateEditState {
                    it.copy(
                        editName = base.name,
                        editPortionBase = "100",
                        editFields = mapOf(
                            "kcal" to base.energiaKcal.formatDouble(),
                            "protein" to base.proteina.formatDouble(),
                            "carbs" to base.carboidratos.formatDouble(),
                            "fat" to base.lipidios?.total.formatDouble(),
                            "fiber" to base.fibraAlimentar.formatDouble(),
                            "colest" to base.colesterol.formatDouble(),
                            "sodio" to base.sodio.formatDouble(),
                            "calcio" to base.calcio.formatDouble(),
                            "magnesio" to base.magnesio.formatDouble(),
                            "manganes" to base.manganes.formatDouble(),
                            "fosforo" to base.fosforo.formatDouble(),
                            "ferro" to base.ferro.formatDouble(),
                            "potassio" to base.potassio.formatDouble(),
                            "cobre" to base.cobre.formatDouble(),
                            "zinco" to base.zinco.formatDouble(),
                            "vitc" to base.vitaminaC.formatDouble(),
                            "retinol" to base.retinol.formatDouble(),
                            "tiamina" to base.tiamina.formatDouble(),
                            "riboflavina" to base.riboflavina.formatDouble(),
                            "niacina" to base.niacina.formatDouble(),
                            "piridoxina" to base.piridoxina.formatDouble()
                        )
                    )
                }
            }
        }
    }

    fun onEditFieldChange(field: String, value: String) {
        updateEditState {
            if (field == "name") {
                it.copy(editName = value)
            } else if (field == "portionBase") {
                it.copy(editPortionBase = value)
            } else {
                val newMap = it.editFields.toMutableMap()
                newMap[field] = value
                it.copy(editFields = newMap)
            }
        }
    }

    fun saveChanges(onSuccess: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val currentBase = _baseFood.value ?: return@launch
            val editData = editState.value

            val baseQty = editData.editPortionBase.toDoubleOrNull() ?: 100.0
            if (baseQty <= 0.0) return@launch

            val factor = 100.0 / baseQty

            fun String?.toFactorDouble() = (this?.toDoubleOrNull() ?: 0.0) * factor

            val fields = editData.editFields
            val updatedFood = currentBase.copy(
                name = editData.editName.ifBlank { "Novo Alimento" },
                energiaKcal = fields["kcal"].toFactorDouble(),
                proteina = fields["protein"].toFactorDouble(),
                carboidratos = fields["carbs"].toFactorDouble(),
                fibraAlimentar = fields["fiber"].toFactorDouble(),
                colesterol = fields["colest"].toFactorDouble(),
                sodio = fields["sodio"].toFactorDouble(),
                calcio = fields["calcio"].toFactorDouble(),
                magnesio = fields["magnesio"].toFactorDouble(),
                manganes = fields["manganes"].toFactorDouble(),
                fosforo = fields["fosforo"].toFactorDouble(),
                ferro = fields["ferro"].toFactorDouble(),
                potassio = fields["potassio"].toFactorDouble(),
                cobre = fields["cobre"].toFactorDouble(),
                zinco = fields["zinco"].toFactorDouble(),
                vitaminaC = fields["vitc"].toFactorDouble(),
                retinol = fields["retinol"].toFactorDouble(),
                tiamina = fields["tiamina"].toFactorDouble(),
                riboflavina = fields["riboflavina"].toFactorDouble(),
                niacina = fields["niacina"].toFactorDouble(),
                piridoxina = fields["piridoxina"].toFactorDouble(),
                lipidios = (currentBase.lipidios ?: Lipidios(0.0, 0.0, 0.0, 0.0)).copy(
                    total = fields["fat"].toFactorDouble()
                )
            )

            if (currentBase.id == 0) {
                val newId = foodDao.insertFood(updatedFood).toInt()
                onSuccess(newId)
            } else {
                // UPDATE EXISTING
                foodDao.updateFood(updatedFood)
                savedStateHandle[KEY_IS_EDIT_MODE] = false
                onSuccess(updatedFood.id)
            }
        }
    }

    suspend fun cloneAndGetId(): Int {
        val current = _baseFood.value ?: return 0

        // 1. Identify Core Name
        // Matches "Name (Cópia)" or "Name (Cópia #2)"
        val copyRegex = Regex("^(.*) \\(Cópia(?: #(\\d+))?\\)$")
        val match = copyRegex.matchEntire(current.name)
        val coreName = if (match != null) match.groupValues[1] else current.name

        // 2. Find all existing copies
        val pattern = "$coreName (Cópia%"
        val existingFoods = foodDao.findFoodsByNameLike(pattern)

        // 3. Calculate next index
        var maxIndex = 0
        var hasUnnumberedCopy = false

        existingFoods.forEach { food ->
            val m = copyRegex.matchEntire(food.name)
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

        val newSuffix = if (maxIndex == 0 && !hasUnnumberedCopy) {
            " (Cópia)"
        } else {
            " (Cópia #${maxIndex + 1})"
        }

        val newName = "$coreName$newSuffix"
        val newUuid = UUID.randomUUID().toString()

        // Round values to avoid floating point artifacts on view
        val clonedFood = current.copy(
            id = 0,
            tacoID = "CUSTOM-$newUuid",
            uuid = newUuid,
            name = newName,
            isCustom = true,
            category = "Meus Alimentos",
            energiaKcal = current.energiaKcal.round(),
            proteina = current.proteina.round(),
            carboidratos = current.carboidratos.round(),
            fibraAlimentar = current.fibraAlimentar.round(),
            colesterol = current.colesterol.round(),
            sodio = current.sodio.round(),
            calcio = current.calcio.round(),
            magnesio = current.magnesio.round(),
            manganes = current.manganes.round(),
            fosforo = current.fosforo.round(),
            ferro = current.ferro.round(),
            potassio = current.potassio.round(),
            cobre = current.cobre.round(),
            zinco = current.zinco.round(),
            vitaminaC = current.vitaminaC.round(),
            retinol = current.retinol.round(),
            tiamina = current.tiamina.round(),
            riboflavina = current.riboflavina.round(),
            piridoxina = current.piridoxina.round(),
            niacina = current.niacina.round(),
            lipidios = current.lipidios?.copy(
                total = current.lipidios.total.round(),
                saturados = current.lipidios.saturados.round(),
                monoinsaturados = current.lipidios.monoinsaturados.round(),
                poliinsaturados = current.lipidios.poliinsaturados.round()
            )
        )
        return foodDao.insertFood(clonedFood).toInt()
    }

    fun deleteFood(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val current = _baseFood.value ?: return@launch
            if (current.isCustom) {
                foodDao.deleteFood(current)
                onSuccess()
            }
        }
    }
}