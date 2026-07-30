package com.forgeflow.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.platform.health.HealthConnectAvailability
import com.forgeflow.core.platform.health.HealthConnectManager
import com.forgeflow.core.platform.health.HealthConnectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val workoutRepository: WorkoutRepository,
    private val healthConnectManager: HealthConnectManager,
) : ViewModel() {
    private val healthStatus = MutableStateFlow(
        HealthConnectStatus(HealthConnectAvailability.UNAVAILABLE),
    )
    private val operation = MutableStateFlow(SettingsOperationState())

    val uiState = combine(
        settingsRepository.observeSettings(),
        healthStatus,
        operation,
    ) { settings, currentHealthStatus, currentOperation ->
            SettingsUiState(
                theme = settings.themePreference,
                accent = settings.accentColor,
                weightUnit = settings.weightUnit,
                compactMode = settings.compactMode,
                healthConnectAvailability = currentHealthStatus.availability,
                isCheckingHealthConnect = currentOperation.isCheckingHealthConnect,
                healthConnectHasPermissions = currentHealthStatus.hasPermissions,
                healthConnectSyncEnabled = settings.healthConnectSyncEnabled,
                isSyncingHealthHistory = currentOperation.isSyncingHealthHistory,
                healthSyncResult = currentOperation.healthSyncResult,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    init {
        refreshHealthConnect()
    }

    fun createHealthPermissionContract() =
        healthConnectManager.createPermissionRequestContract()

    val requiredHealthPermissions: Set<String>
        get() = healthConnectManager.requiredPermissions

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.AccentChanged -> updateSetting {
                settingsRepository.setAccentColor(action.value)
            }
            is SettingsAction.CompactModeChanged -> updateSetting {
                settingsRepository.setCompactMode(action.enabled)
            }
            is SettingsAction.ThemeChanged -> updateSetting {
                settingsRepository.setThemePreference(action.value)
            }
            is SettingsAction.WeightUnitChanged -> updateSetting {
                settingsRepository.setWeightUnit(action.value)
            }
            is SettingsAction.HealthConnectSyncChanged -> updateSetting {
                settingsRepository.setHealthConnectSyncEnabled(
                    action.enabled && healthStatus.value.hasPermissions,
                )
            }
            is SettingsAction.HealthConnectPermissionsResult -> {
                val granted = action.grantedPermissions.containsAll(
                    healthConnectManager.requiredPermissions,
                )
                viewModelScope.launch {
                    if (granted) {
                        settingsRepository.setHealthConnectSyncEnabled(true)
                    }
                    refreshHealthConnect()
                }
            }
            SettingsAction.HealthConnectRefresh -> refreshHealthConnect()
            SettingsAction.HealthConnectSyncHistory -> syncHealthHistory()
            SettingsAction.OpenHealthConnect -> healthConnectManager.openHealthConnect()
            SettingsAction.InstallHealthConnect -> healthConnectManager.openInstallOrUpdate()
        }
    }

    private fun updateSetting(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private fun refreshHealthConnect() {
        viewModelScope.launch {
            operation.update { it.copy(isCheckingHealthConnect = true) }
            val currentStatus = healthConnectManager.status()
            healthStatus.value = currentStatus
            if (!currentStatus.hasPermissions) {
                settingsRepository.setHealthConnectSyncEnabled(false)
            }
            operation.update { it.copy(isCheckingHealthConnect = false) }
        }
    }

    private fun syncHealthHistory() {
        if (
            operation.value.isSyncingHealthHistory ||
            !healthStatus.value.hasPermissions
        ) {
            return
        }
        viewModelScope.launch {
            operation.update {
                it.copy(
                    isSyncingHealthHistory = true,
                    healthSyncResult = null,
                )
            }
            val settings = settingsRepository.observeSettings().first()
            val history = workoutRepository.observeHistory().first()
            val result = when (history) {
                is DataResult.Failure -> HealthSyncUiResult(
                    syncedCount = 0,
                    failedCount = 1,
                )
                is DataResult.Success -> {
                    val syncResult = healthConnectManager.syncWorkouts(
                        workouts = history.value,
                        weightUnit = settings.weightUnit,
                    )
                    HealthSyncUiResult(
                        syncedCount = syncResult.syncedCount,
                        failedCount = syncResult.failedCount,
                    )
                }
            }
            operation.update {
                it.copy(
                    isSyncingHealthHistory = false,
                    healthSyncResult = result,
                )
            }
        }
    }

    private data class SettingsOperationState(
        val isCheckingHealthConnect: Boolean = true,
        val isSyncingHealthHistory: Boolean = false,
        val healthSyncResult: HealthSyncUiResult? = null,
    )
}
