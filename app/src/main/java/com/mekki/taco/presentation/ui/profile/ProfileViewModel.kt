package com.mekki.taco.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.utils.BMRCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// Data class para o estado da UI, incluindo os valores calculados
data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val tmb: Double = 0.0, // Taxa Metabólica Basal
    val tdee: Double = 0.0, // Gasto Calórico Diário Total
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val weightInput: String = "",
    val heightInput: String = "",
    val ageInput: String = ""
)

enum class ActivityLevel(val multiplier: Double, val displayName: String) {
    SEDENTARY(1.2, "Sedentário"),
    LIGHT(1.375, "Leve (1-3 dias/semana)"),
    MODERATE(1.55, "Moderado (3-5 dias/semana)"),
    ACTIVE(1.725, "Ativo (6-7 dias/semana)"),
    VERY_ACTIVE(1.9, "Muito Ativo (trabalho físico)")
}

class ProfileViewModel(
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
                    val tdee = tmb * profile.activityLevel.multiplier

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
        _uiState.update { it.copy(activityLevel = level, userProfile = updatedProfile, tdee = tdee) }
    }

    // do not reset text fields omg
    private fun updateProfileLogic(newProfile: UserProfile) {
        val tmb = BMRCalculator.calculateBMR(newProfile)
        val tdee = tmb * _uiState.value.activityLevel.multiplier
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