package com.mekki.taco.presentation.ui.fooddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.FoodDao // Ajuste o import

class AlimentoDetailViewModelFactory(
    private val alimentoId: Int,
    private val foodDao: FoodDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlimentoDetailViewModel::class.java)) {
            return AlimentoDetailViewModel(alimentoId, foodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class para AlimentoDetailViewModelFactory: ${modelClass.name}")
    }
}