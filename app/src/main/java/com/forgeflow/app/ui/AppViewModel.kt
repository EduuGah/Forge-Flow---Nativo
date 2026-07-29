package com.forgeflow.app.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class AppUiState(
    val themePreference: ThemePreference = ThemePreference.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = settingsRepository.observeSettings()
        .map { settings ->
            AppUiState(
                themePreference = settings.themePreference,
                accentColor = settings.accentColor,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppUiState(),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
