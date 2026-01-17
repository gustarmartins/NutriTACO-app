package com.mekki.taco.presentation.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.repository.DiaryRepository
import com.mekki.taco.data.repository.UserProfileRepository

class DiaryViewModelFactory(
    private val repository: DiaryRepository,
    private val dietDao: DietDao,
    private val foodDao: FoodDao,
    private val userProfileRepository: UserProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            return DiaryViewModel(repository, dietDao, foodDao, userProfileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for Diary")
    }
}
