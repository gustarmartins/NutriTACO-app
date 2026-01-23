package com.mekki.taco.presentation.ui.fooddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.FoodDao

class FoodDetailViewModelFactory(
    private val alimentoId: Int,
    private val foodDao: FoodDao,
    private val dietDao: DietDao,
    private val initialEditMode: Boolean = false
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodDetailViewModel::class.java)) {
            return FoodDetailViewModel(alimentoId, foodDao, dietDao, initialEditMode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class para FoodDetailViewModelFactory: ${modelClass.name}")
    }
}