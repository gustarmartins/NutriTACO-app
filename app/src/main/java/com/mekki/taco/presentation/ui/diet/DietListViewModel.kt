package com.mekki.taco.presentation.ui.diet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.model.DietItemWithFood
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DietListViewModel(
    private val dietDao: DietDao,
    private val dietItemDao: DietItemDao
) : ViewModel() {

    companion object {
        private const val TAG = "DietListViewModel"
    }

    private val _dietas = MutableStateFlow<List<Diet>>(emptyList())
    val dietas: StateFlow<List<Diet>> = _dietas.asStateFlow()

    private val _nomeNovaDieta = MutableStateFlow("")
    val nomeNovaDieta: StateFlow<String> = _nomeNovaDieta.asStateFlow()

    private val _temporaryFoodList = MutableStateFlow<List<DietItemWithFood>>(emptyList())
    val temporaryFoodList: StateFlow<List<DietItemWithFood>> = _temporaryFoodList.asStateFlow()

    private val _dietaSalvaEvent = MutableSharedFlow<Unit>()
    val dietaSalvaEvent: SharedFlow<Unit> = _dietaSalvaEvent.asSharedFlow()

    init {
        carregarDietas()
    }

    private fun carregarDietas() {
        viewModelScope.launch {
            dietDao.getAllDiets()
                .catch { e -> Log.e(TAG, "Error loading diets", e) }
                .collect { _dietas.value = it }
        }
    }

    fun onNomeNovaDietaChange(nome: String) {
        _nomeNovaDieta.value = nome
    }

    fun addFoodToTemporaryList(item: DietItemWithFood) {
        _temporaryFoodList.value += item
    }

    fun removeTemporaryFoodItem(item: DietItemWithFood) {
        _temporaryFoodList.value =
            _temporaryFoodList.value.filter { it.dietItem.id != item.dietItem.id }
    }

    fun salvarNovaDieta() {
        viewModelScope.launch {
            val nomeLimpo = _nomeNovaDieta.value.trim()
            if (nomeLimpo.isEmpty() || _temporaryFoodList.value.isEmpty()) {
                return@launch
            }

            val isFirst = _dietas.value.isEmpty()

            val novaDiet = Diet(
                name = nomeLimpo,
                creationDate = System.currentTimeMillis(),
                calorieGoals = null,
                isMain = isFirst
            )

            try {
                val newDietId = dietDao.insertOrReplaceDiet(novaDiet)

                val itemsParaSalvar = _temporaryFoodList.value.map { tempItem ->
                    DietItem(
                        dietId = newDietId.toInt(),
                        foodId = tempItem.food.id,
                        quantityGrams = tempItem.dietItem.quantityGrams,
                        mealType = tempItem.dietItem.mealType
                    )
                }

                dietItemDao.insertAll(itemsParaSalvar)
                _dietaSalvaEvent.emit(Unit)

                _nomeNovaDieta.value = ""
                _temporaryFoodList.value = emptyList()

            } catch (e: Exception) {
                Log.e(TAG, "Error saving new diet", e)
            }
        }
    }

    fun deletarDieta(diet: Diet) {
        viewModelScope.launch {
            dietDao.deleteDiet(diet)
        }
    }

    fun setMainDiet(diet: Diet) {
        viewModelScope.launch {
            dietDao.setAsMainDiet(diet.id)
        }
    }
}