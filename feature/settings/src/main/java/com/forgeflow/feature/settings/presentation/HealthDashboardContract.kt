package com.forgeflow.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.platform.health.HealthConnectAvailability

@Immutable
data class HealthDashboardUiState(
    val isLoading: Boolean = true,
    val availability: HealthConnectAvailability = HealthConnectAvailability.UNAVAILABLE,
    val hasPermissions: Boolean = false,
    val period: HealthDashboardPeriod = HealthDashboardPeriod.SEVEN_DAYS,
    val sources: List<String> = emptyList(),
    val daily: List<HealthDailyUiModel> = emptyList(),
    val error: Boolean = false,
)

enum class HealthDashboardPeriod(val days: Long) {
    SEVEN_DAYS(7),
    THIRTY_DAYS(30),
}

@Immutable
data class HealthDailyUiModel(
    val dateLabel: String,
    val fullDateLabel: String,
    val steps: Long,
    val distanceKilometers: Double,
    val caloriesKilocalories: Double,
    val averageHeartRate: Long?,
    val sleepMinutes: Long,
    val exerciseSessions: Int,
    val exerciseMinutes: Long,
)

sealed interface HealthDashboardAction {
    data class PeriodChanged(val period: HealthDashboardPeriod) : HealthDashboardAction
    data class PermissionsResult(val granted: Set<String>) : HealthDashboardAction
    data object Refresh : HealthDashboardAction
    data object OpenHealthConnect : HealthDashboardAction
}
