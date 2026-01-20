package com.mekki.taco.presentation.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.manager.BackupManager
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userProfile: UserProfile = UserProfile(),
    val isBackupLoading: Boolean = false,
    val backupMessage: String? = null
)

class SettingsViewModel(
    private val repository: UserProfileRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    fun toggleTheme(isDark: Boolean) {
        val currentProfile = _uiState.value.userProfile
        val updatedProfile = currentProfile.copy(isDarkMode = isDark)

        _uiState.update { it.copy(userProfile = updatedProfile) }
        
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
        }
    }

    fun onExportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = null) }
            val result = backupManager.exportData(uri)
            val message = if (result.isSuccess) "Dados exportados com sucesso!" else "Erro ao exportar: ${result.exceptionOrNull()?.message}"
            _uiState.update { it.copy(isBackupLoading = false, backupMessage = message) }
        }
    }

    fun onImportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = null) }
            val result = backupManager.importData(uri)
            val message = if (result.isSuccess) "Dados importados com sucesso!" else "Erro ao importar: ${result.exceptionOrNull()?.message}"
            _uiState.update { it.copy(isBackupLoading = false, backupMessage = message) }
        }
    }
}

class SettingsViewModelFactory(
    private val repository: UserProfileRepository,
    private val backupManager: BackupManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository, backupManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
