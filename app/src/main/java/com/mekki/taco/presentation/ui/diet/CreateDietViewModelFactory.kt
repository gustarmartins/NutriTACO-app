package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao

class CreateDietViewModelFactory(
    private val dietaDao: DietaDao,
    private val itemDietaDao: ItemDietaDao,
    private val alimentoDao: AlimentoDao,
    private val dietIdToEdit: Int? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateDietViewModel::class.java)) {
            return CreateDietViewModel(
                dietaDao = dietaDao,
                itemDietaDao = itemDietaDao,
                alimentoDao = alimentoDao,
                dietIdToEdit = dietIdToEdit
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CreateDiet")
    }
}
