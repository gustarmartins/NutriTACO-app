package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.repository.UserProfileRepository

class DietDetailViewModelFactory(
    private val dietId: Int,
    private val dietDao: DietDao,
    private val dietItemDao: DietItemDao,
    private val foodDao: FoodDao,
    private val dailyLogDao: DailyLogDao,
    private val userProfileRepository: UserProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietDetailViewModel::class.java)) {
            return DietDetailViewModel(
                dietId,
                dietDao,
                dietItemDao,
                foodDao,
                dailyLogDao,
                userProfileRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for DietDetail")
    }
}