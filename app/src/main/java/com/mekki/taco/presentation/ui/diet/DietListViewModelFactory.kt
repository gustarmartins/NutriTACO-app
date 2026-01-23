package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao

class DietListViewModelFactory(
    private val dietDao: DietDao,
    private val dietItemDao: DietItemDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietListViewModel::class.java)) {
            return DietListViewModel(dietDao, dietItemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for DietList")
    }
}