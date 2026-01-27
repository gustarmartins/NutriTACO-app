package com.mekki.taco.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.model.ActivityLevel
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.utils.BMRCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val tmb: Double = 0.0, // Taxa Metabólica Basal
    val tdee: Double = 0.0, // Gasto Calórico Diário Total
    val activityLevel: ActivityLevel? = null,
    val weightInput: String = "",
    val heightInput: String = "",
    val ageInput: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()
    private val df = DecimalFormat("#.##")

    init {
        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                _uiState.update { currentState ->
                    val tmb = BMRCalculator.calculateBMR(profile)
                    val tdee = profile.activityLevel?.let { tmb * it.multiplier } ?: 0.0

                    // sync the inputs when loading from disk
                    currentState.copy(
                        userProfile = profile,
                        activityLevel = profile.activityLevel,
                        tmb = tmb,
                        tdee = tdee,
                        weightInput = profile.weight?.let { df.format(it) } ?: "",
                        heightInput = profile.height?.let { df.format(it) } ?: "",
                        ageInput = profile.age?.toString() ?: ""
                    )
                }
            }
        }
    }

    fun onWeightChange(input: String) {
        _uiState.update { it.copy(weightInput = input) }

        val newWeight = input.replace(',', '.').toDoubleOrNull()
        if (newWeight != null) {
            updateProfileLogic(_uiState.value.userProfile.copy(weight = newWeight))
        } else if (input.isBlank()) {
            updateProfileLogic(_uiState.value.userProfile.copy(weight = null))
        }
    }

    fun onHeightChange(input: String) {
        _uiState.update { it.copy(heightInput = input) }

        val newHeight = input.replace(',', '.').toDoubleOrNull()
        if (newHeight != null) {
            updateProfileLogic(_uiState.value.userProfile.copy(height = newHeight))
        } else if (input.isBlank()) {
            updateProfileLogic(_uiState.value.userProfile.copy(height = null))
        }
    }

    fun onAgeChange(input: String) {
        val filtered = input.filter { it.isDigit() }
        _uiState.update { it.copy(ageInput = filtered) }

        val newAge = filtered.toIntOrNull()
        updateProfileLogic(_uiState.value.userProfile.copy(age = newAge))
    }

    fun onSexChange(sex: String) {
        updateProfileLogic(_uiState.value.userProfile.copy(sex = sex))
    }

    fun onActivityLevelChange(level: ActivityLevel) {
        val tdee = _uiState.value.tmb * level.multiplier
        val updatedProfile = _uiState.value.userProfile.copy(activityLevel = level)
        _uiState.update {
            it.copy(
                activityLevel = level,
                userProfile = updatedProfile,
                tdee = tdee
            )
        }
    }

    private fun updateProfileLogic(newProfile: UserProfile) {
        val tmb = BMRCalculator.calculateBMR(newProfile)
        val tdee = _uiState.value.activityLevel?.let { tmb * it.multiplier } ?: 0.0
        _uiState.update {
            it.copy(
                userProfile = newProfile,
                tmb = tmb,
                tdee = tdee
            )
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            repository.saveProfile(_uiState.value.userProfile)
        }
    }
}
