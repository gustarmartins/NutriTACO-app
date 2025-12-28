package com.mekki.taco.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.FoodDao

class AlimentoViewModelFactory(
    private val foodDao: FoodDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlimentoViewModel::class.java)) {
            return AlimentoViewModel(foodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}