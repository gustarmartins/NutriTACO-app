package com.mekki.taco.presentation.ui.fooddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import kotlinx.coroutines.flow.*

data class AlimentoDetailUiState(
    val isLoading: Boolean = true,
    val portion: String = "100",
    val displayFood: Food? = null
)

class AlimentoDetailViewModel(
    alimentoId: Int,
    foodDao: FoodDao
) : ViewModel() {

    private val _portion = MutableStateFlow("100")
    private val _baseFood: StateFlow<Food?> = foodDao.buscarAlimentoPorId(alimentoId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val uiState: StateFlow<AlimentoDetailUiState> =
        combine(_baseFood, _portion) { base, portion ->
            if (base == null) {
                AlimentoDetailUiState(isLoading = true)
            } else {
                val newPortion = portion.toDoubleOrNull() ?: 100.0
                val ratio = newPortion / 100.0
                val recalculatedAlimento = base.copy(

                    energiaKcal = base.energiaKcal?.times(ratio),
                    energiaKj = base.energiaKj?.times(ratio),
                    umidade = base.umidade?.times(ratio),
                    cinzas = base.cinzas?.times(ratio),
                    // Macros
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
                    // Minerais
                    calcio = base.calcio?.times(ratio),
                    magnesio = base.magnesio?.times(ratio),
                    manganes = base.manganes?.times(ratio),
                    fosforo = base.fosforo?.times(ratio),
                    ferro = base.ferro?.times(ratio),
                    sodio = base.sodio?.times(ratio),
                    potassio = base.potassio?.times(ratio),
                    cobre = base.cobre?.times(ratio),
                    zinco = base.zinco?.times(ratio),
                    // Vitaminas
                    retinol = base.retinol?.times(ratio),
                    RE = base.RE?.times(ratio),
                    RAE = base.RAE?.times(ratio),
                    tiamina = base.tiamina?.times(ratio),
                    riboflavina = base.riboflavina?.times(ratio),
                    piridoxina = base.piridoxina?.times(ratio),
                    niacina = base.niacina?.times(ratio),
                    vitaminaC = base.vitaminaC?.times(ratio),
                    // Amino Ácidos
                    aminoacidos = base.aminoacidos?.copy(
                        triptofano = base.aminoacidos.triptofano?.times(ratio),
                        treonina = base.aminoacidos.treonina?.times(ratio),
                        isoleucina = base.aminoacidos.isoleucina?.times(ratio),
                        leucina = base.aminoacidos.leucina?.times(ratio),
                        lisina = base.aminoacidos.lisina?.times(ratio),
                        metionina = base.aminoacidos.metionina?.times(ratio),
                        cistina = base.aminoacidos.cistina?.times(ratio),
                        fenilalanina = base.aminoacidos.fenilalanina?.times(ratio),
                        tirosina = base.aminoacidos.tirosina?.times(ratio),
                        valina = base.aminoacidos.valina?.times(ratio),
                        arginina = base.aminoacidos.arginina?.times(ratio),
                        histidina = base.aminoacidos.histidina?.times(ratio),
                        alanina = base.aminoacidos.alanina?.times(ratio),
                        acidoAspartico = base.aminoacidos.acidoAspartico?.times(ratio),
                        acidoGlutamico = base.aminoacidos.acidoGlutamico?.times(ratio),
                        glicina = base.aminoacidos.glicina?.times(ratio),
                        prolina = base.aminoacidos.prolina?.times(ratio),
                        serina = base.aminoacidos.serina?.times(ratio)
                    )
                )
                AlimentoDetailUiState(
                    isLoading = false,
                    portion = portion,
                    displayFood = recalculatedAlimento
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlimentoDetailUiState()
        )

    fun updatePortion(newPortion: String) {
        if (newPortion.all { it.isDigit() } && newPortion.length <= 5) {
            _portion.value = newPortion
        }
    }
}