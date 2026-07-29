package com.forgeflow.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    val uiState = repository.observeSettings()
        .map { settings ->
            SettingsUiState(
                theme = settings.themePreference,
                accent = settings.accentColor,
                weightUnit = settings.weightUnit,
                compactMode = settings.compactMode,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.AccentChanged -> repository.setAccentColor(action.value)
                is SettingsAction.CompactModeChanged -> repository.setCompactMode(action.enabled)
                is SettingsAction.ThemeChanged -> repository.setThemePreference(action.value)
                is SettingsAction.WeightUnitChanged -> repository.setWeightUnit(action.value)
            }
        }
    }
}
