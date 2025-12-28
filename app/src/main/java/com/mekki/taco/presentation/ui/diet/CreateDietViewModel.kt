package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Dieta
import com.mekki.taco.data.db.entity.ItemDieta
import com.mekki.taco.data.model.DietItemWithFood
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateDietState(
    val dietName: String = "",
    val mealItems: Map<String, List<DietItemWithFood>> = emptyMap(),
    val searchTerm: String = "",
    val searchResults: List<Food> = emptyList(),
    val searchIsLoading: Boolean = false,
    val expandedAlimentoId: Int? = null,
    val quickAddAmount: String = "100",
    val focusedMealType: String? = null,
    val lastMealTimes: Map<String, String> = emptyMap(),
    val existingDietId: Int? = null,
    val isEditMode: Boolean = false
)

@OptIn(FlowPreview::class)
class CreateDietViewModel(
    private val dietaDao: DietaDao,
    private val itemDietaDao: ItemDietaDao,
    private val alimentoDao: AlimentoDao,
    private val dietIdToEdit: Int? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateDietState())
    val state: StateFlow<CreateDietState> = _state.asStateFlow()

    private val _navigateBackEvent = MutableSharedFlow<Unit>()
    val navigateBackEvent = _navigateBackEvent.asSharedFlow()

    val mealTypes = listOf("Café da Manhã", "Almoço", "Jantar", "Lanche")

    private val _searchTerm = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Food>> = _searchTerm
        .debounce(300)
        .flatMapLatest { term ->
            if (term.length > 2) {
                _state.update { it.copy(searchIsLoading = true) }
                alimentoDao.buscarAlimentosPorNome(term)
            } else {
                flowOf(emptyList())
            }
        }
        .onEach { _state.update { it.copy(searchIsLoading = false) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            searchResults.collect { results ->
                _state.update { it.copy(searchResults = results) }
            }
        }
    }

    // Carrega a dieta para edição
    init {
        if (dietIdToEdit != null) {
            loadDietForEditing(dietIdToEdit)
        }
    }

    private fun loadDietForEditing(dietId: Int) {
        viewModelScope.launch {
            // Pega a dieta com itens uma única vez
            val dietaComItens = dietaDao.getDietaComItens(dietId).firstOrNull() ?: return@launch

            // Agrupa por tipo de refeição, como já é esperado pelo state
            val grouped = dietaComItens.itens.groupBy { it.itemDieta.tipoRefeicao ?: "Sem Categoria" }

            // Atualiza lastMealTimes usando o último item de cada refeição
            val lastTimes = grouped.mapValues { (_, items) ->
                items.lastOrNull()?.itemDieta?.horaConsumo ?: ""
            }

            _state.update { current ->
                current.copy(
                    dietName = dietaComItens.dieta.nome,
                    mealItems = grouped,
                    lastMealTimes = lastTimes,
                    existingDietId = dietaComItens.dieta.id,
                    isEditMode = true
                )
            }
        }
    }


    fun onDietNameChange(newName: String) {
        _state.update { it.copy(dietName = newName) }
    }

    fun onSearchTermChange(term: String) {
        _state.update { it.copy(searchTerm = term) }
        _searchTerm.value = term
    }

    fun onAlimentoToggled(alimentoId: Int) {
        _state.update {
            val newId = if (it.expandedAlimentoId == alimentoId) null else alimentoId
            it.copy(expandedAlimentoId = newId)
        }
    }

    fun onQuickAddAmountChange(amount: String) {
        _state.update { it.copy(quickAddAmount = amount) }
    }

    fun setFocusedMealType(mealType: String?) {
        _state.update { it.copy(focusedMealType = mealType, searchTerm = "", searchResults = emptyList()) }
    }

    fun addFoodToMeal(food: Food) {
        val currentState = _state.value
        val mealType = currentState.focusedMealType ?: return
        val amount = currentState.quickAddAmount.toDoubleOrNull() ?: 100.0

        // Get the last time used for this meal, or a default
        val lastTime = currentState.lastMealTimes[mealType]
        val mealList = currentState.mealItems[mealType]
        val consumptionTime = lastTime ?: mealList?.lastOrNull()?.itemDieta?.horaConsumo ?: getDefaultTimeForMeal(mealType)

        val newItem = DietItemWithFood(
            itemDieta = ItemDieta(
                id = UUID.randomUUID().hashCode(),
                dietaId = 0,
                alimentoId = food.id,
                quantidadeGramas = amount,
                tipoRefeicao = mealType,
                horaConsumo = consumptionTime
            ),
            food = food
        )

        _state.update {
            val updatedMeals = it.mealItems.toMutableMap()
            val currentItems = updatedMeals.getOrDefault(mealType, emptyList())
            updatedMeals[mealType] = currentItems + newItem
            it.copy(mealItems = updatedMeals)
        }
    }
    
    private fun getDefaultTimeForMeal(mealType: String): String {
        return when (mealType) {
            "Café da Manhã" -> "08:00"
            "Almoço" -> "12:00"
            "Jantar" -> "19:00"
            "Lanche" -> "15:00"
            else -> "12:00"
        }
    }

    fun removeFoodFromMeal(item: DietItemWithFood) {
        val mealType = item.itemDieta.tipoRefeicao ?: return
        _state.update {
            val updatedMeals = it.mealItems.toMutableMap()
            val currentItems = updatedMeals.getOrDefault(mealType, emptyList())
            updatedMeals[mealType] = currentItems.filterNot { it.itemDieta.id == item.itemDieta.id }
            it.copy(mealItems = updatedMeals)
        }
    }

    fun updateFoodItem(
        item: DietItemWithFood,
        newQuantity: Double,
        newTime: String,
        newMealType: String
    ) {
        val originalMealType = item.itemDieta.tipoRefeicao ?: return
        val targetMealType = newMealType.ifBlank { originalMealType }

        _state.update { current ->
            val meals = current.mealItems.toMutableMap()

            // Remove from original meal
            val originalList = meals[originalMealType]?.toMutableList() ?: mutableListOf()
            val index = originalList.indexOfFirst { it.itemDieta.id == item.itemDieta.id }
            if (index != -1) {
                originalList.removeAt(index)
                meals[originalMealType] = originalList
            }

            // Build updated item
            val updatedItem = item.copy(
                itemDieta = item.itemDieta.copy(
                    quantidadeGramas = newQuantity,
                    horaConsumo = newTime,
                    tipoRefeicao = targetMealType
                )
            )

            // Add to target meal
            val targetList = meals[targetMealType]?.toMutableList() ?: mutableListOf()
            targetList.add(updatedItem)
            meals[targetMealType] = targetList

            // Update sticky time for that meal
            val newLastTimes = current.lastMealTimes.toMutableMap()
            newLastTimes[targetMealType] = newTime

            current.copy(
                mealItems = meals,
                lastMealTimes = newLastTimes
            )
        }
    }

    fun reorderFoodItemsInMeal(mealType: String, from: Int, to: Int) {
        _state.update { currentState ->
            val currentMeals = currentState.mealItems.toMutableMap()
            val mealList = currentMeals[mealType]?.toMutableList() ?: return@update currentState

            val movedItem = mealList.removeAt(from)
            mealList.add(to, movedItem)

            currentMeals[mealType] = mealList
            currentState.copy(mealItems = currentMeals)
        }
    }

    fun replaceFoodInItem(target: DietItemWithFood, newFood: Food) {
        _state.update { current ->
            val updatedMeals = current.mealItems.mapValues { (_, list) ->
                list.map { existing ->
                    if (existing.itemDieta.id == target.itemDieta.id) {
                        existing.copy(
                            itemDieta = existing.itemDieta.copy(alimentoId = newFood.id),
                            food = newFood
                        )
                    } else {
                        existing
                    }
                }
            }
            current.copy(mealItems = updatedMeals)
        }
    }


    suspend fun searchFoodsByName(term: String): List<Food> {
        if (term.length <= 2) return emptyList()
        return alimentoDao.buscarAlimentosPorNome(term).first()
    }

    fun saveDiet() {
        viewModelScope.launch {
            val currentState = _state.value
            val dietName = currentState.dietName.ifBlank { "Dieta" }

            if (currentState.isEditMode && currentState.existingDietId != null) {
                val dietId = currentState.existingDietId

                // 1) Atualiza só o nome
                dietaDao.atualizarNomeDieta(dietId, dietName)

                // 2) Limpa os itens antigos
                itemDietaDao.deletarTodosItensDeUmaDieta(dietId)

                // 3) Insere a versão atual das refeições da tela
                val allItems = currentState.mealItems.values.flatten()
                if (allItems.isNotEmpty()) {
                    itemDietaDao.inserirItensDieta(
                        allItems.map { it.itemDieta.copy(dietaId = dietId) }
                    )
                }
            } else {
                // Criação de nova dieta (como já era antes)
                val newDietId = dietaDao.inserirDieta(
                    Dieta(
                        nome = dietName,
                        dataCriacao = System.currentTimeMillis()
                    )
                ).toInt()

                if (currentState.dietName.isBlank()) {
                    _state.update { it.copy(dietName = "Dieta") }
                }

                val allItems = currentState.mealItems.values.flatten()
                if (allItems.isNotEmpty()) {
                    itemDietaDao.inserirItensDieta(
                        allItems.map { it.itemDieta.copy(dietaId = newDietId) }
                    )
                }
            }

            _navigateBackEvent.emit(Unit)
        }
    }

    fun cleanSearch() {
        _state.update { it.copy(searchTerm = "", searchResults = emptyList()) }
        _searchTerm.value = ""
    }
}