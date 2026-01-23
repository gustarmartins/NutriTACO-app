package com.mekki.taco.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.repository.OnboardingRepository
import com.mekki.taco.data.repository.UserProfileRepository

class HomeViewModelFactory(
    private val dietDao: DietDao,
    private val foodDao: FoodDao,
    private val dietItemDao: DietItemDao,
    private val dailyLogDao: DailyLogDao,
    private val onboardingRepository: OnboardingRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                dietDao,
                foodDao,
                dietItemDao,
                dailyLogDao,
                onboardingRepository,
                userProfileRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}