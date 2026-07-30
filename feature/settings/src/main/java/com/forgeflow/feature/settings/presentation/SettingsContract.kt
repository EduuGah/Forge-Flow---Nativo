package com.forgeflow.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.platform.health.HealthConnectAvailability

@Immutable
data class SettingsUiState(
    val theme: ThemePreference = ThemePreference.DARK,
    val accent: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val compactMode: Boolean = false,
    val healthConnectAvailability: HealthConnectAvailability =
        HealthConnectAvailability.UNAVAILABLE,
    val isCheckingHealthConnect: Boolean = true,
    val healthConnectHasPermissions: Boolean = false,
    val healthConnectSyncEnabled: Boolean = false,
    val isSyncingHealthHistory: Boolean = false,
    val healthSyncResult: HealthSyncUiResult? = null,
)

@Immutable
data class HealthSyncUiResult(
    val syncedCount: Int,
    val failedCount: Int,
)

sealed interface SettingsAction {
    data class ThemeChanged(val value: ThemePreference) : SettingsAction
    data class AccentChanged(val value: AccentColor) : SettingsAction
    data class WeightUnitChanged(val value: WeightUnit) : SettingsAction
    data class CompactModeChanged(val enabled: Boolean) : SettingsAction
    data class HealthConnectSyncChanged(val enabled: Boolean) : SettingsAction
    data class HealthConnectPermissionsResult(
        val grantedPermissions: Set<String>,
    ) : SettingsAction
    data object HealthConnectRefresh : SettingsAction
    data object HealthConnectSyncHistory : SettingsAction
    data object OpenHealthConnect : SettingsAction
    data object InstallHealthConnect : SettingsAction
}
