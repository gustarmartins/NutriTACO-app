package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.sharing.DietSharingManager

class DietListViewModelFactory(
    private val dietDao: DietDao,
    private val dietItemDao: DietItemDao,
    private val dietSharingManager: DietSharingManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietListViewModel::class.java)) {
            return DietListViewModel(dietDao, dietItemDao, dietSharingManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for DietList")
    }
}