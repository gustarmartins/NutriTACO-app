package com.mekki.taco.presentation.ui.fooddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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
)

class FoodDetailViewModel(
    private val alimentoId: Int,
    private val foodDao: FoodDao,
    private val dietDao: DietDao,
    initialEditMode: Boolean = false
) : ViewModel() {

    val availableDiets = dietDao.getAllDiets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _portion = MutableStateFlow("100")
    private val _baseFood = MutableStateFlow<Food?>(null)

    private val _isEditMode = MutableStateFlow(initialEditMode)
    private val _editState = MutableStateFlow(FoodDetailState())

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
            _isEditMode.value = true
            _editState.update {
                it.copy(
                    editName = "",
                    editPortionBase = "100",
                    editFields = emptyMap()
                )
            }
        } else {
            viewModelScope.launch {
                foodDao.getFoodById(alimentoId).collect { food ->
                    _baseFood.value = food
                    food?.let { f ->
                        _editState.update {
                            it.copy(
                                editName = f.name,
                                editPortionBase = "100",
                                editFields = mapOf(
                                    "kcal" to f.energiaKcal.formatDouble(),
                                    "protein" to f.proteina.formatDouble(),
                                    "carbs" to f.carboidratos.formatDouble(),
                                    "fat" to f.lipidios?.total.formatDouble(),
                                    "fiber" to f.fibraAlimentar.formatDouble(),
                                    "colest" to f.colesterol.formatDouble(),
                                    "sodio" to f.sodio.formatDouble(),
                                    "calcio" to f.calcio.formatDouble(),
                                    "magnesio" to f.magnesio.formatDouble(),
                                    "manganes" to f.manganes.formatDouble(),
                                    "fosforo" to f.fosforo.formatDouble(),
                                    "ferro" to f.ferro.formatDouble(),
                                    "potassio" to f.potassio.formatDouble(),
                                    "cobre" to f.cobre.formatDouble(),
                                    "zinco" to f.zinco.formatDouble(),
                                    "vitc" to f.vitaminaC.formatDouble(),
                                    "retinol" to f.retinol.formatDouble(),
                                    "tiamina" to f.tiamina.formatDouble(),
                                    "riboflavina" to f.riboflavina.formatDouble(),
                                    "niacina" to f.niacina.formatDouble(),
                                    "piridoxina" to f.piridoxina.formatDouble()
                                )
                            )
                        }
                    }
                }
            }
        }
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
            _portion,
            _isEditMode,
            _editState
        ) { base, portion, isEditing, editState ->
            if (base == null) {
                FoodDetailState(isLoading = true)
            } else {
                val newPortion = portion.toDoubleOrNull() ?: 100.0
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
                        editState.editName != base.name ||
                                editState.editPortionBase != "100" ||
                                editState.editFields.any { (key, value) ->
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
                    portion = portion,
                    displayFood = recalculatedAlimento,
                    isEditMode = isEditing,
                    hasUnsavedChanges = hasChanges,
                    editName = editState.editName,
                    editPortionBase = editState.editPortionBase,
                    editFields = editState.editFields
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodDetailState()
        )

    fun updatePortion(newPortion: String) {
        if (newPortion.all { it.isDigit() || it == '.' } && newPortion.length <= 5) {
            _portion.value = newPortion
        }
    }

    fun onEditToggle() {
        _isEditMode.value = !_isEditMode.value
    }

    fun onEditFieldChange(field: String, value: String) {
        _editState.update {
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
            val editData = _editState.value

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
                _isEditMode.value = false
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