package com.forgeflow.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.WeightUnit

@Immutable
data class SettingsUiState(
    val theme: ThemePreference = ThemePreference.DARK,
    val accent: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val compactMode: Boolean = false,
)

sealed interface SettingsAction {
    data class ThemeChanged(val value: ThemePreference) : SettingsAction
    data class AccentChanged(val value: AccentColor) : SettingsAction
    data class WeightUnitChanged(val value: WeightUnit) : SettingsAction
    data class CompactModeChanged(val enabled: Boolean) : SettingsAction
}
