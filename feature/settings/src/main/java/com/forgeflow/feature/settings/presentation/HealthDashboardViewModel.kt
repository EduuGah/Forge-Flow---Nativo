package com.forgeflow.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.model.HealthConnectDataType
import com.forgeflow.core.platform.health.HealthConnectAvailability
import com.forgeflow.core.platform.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HealthDashboardViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HealthDashboardUiState())
    val uiState: StateFlow<HealthDashboardUiState> = mutableUiState.asStateFlow()

    val requiredPermissions: Set<String>
        get() = healthConnectManager.permissionsFor(HEALTH_DATA_TYPES)

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun createPermissionContract() = healthConnectManager.createPermissionRequestContract()

    fun onAction(action: HealthDashboardAction) {
        when (action) {
            is HealthDashboardAction.PeriodChanged -> {
                mutableUiState.value = mutableUiState.value.copy(period = action.period)
                refresh()
            }
            is HealthDashboardAction.PermissionsResult -> refresh()
            HealthDashboardAction.Refresh -> refresh()
            HealthDashboardAction.OpenHealthConnect -> healthConnectManager.openHealthConnect()
        }
    }

    private fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val period = mutableUiState.value.period
            mutableUiState.value = mutableUiState.value.copy(isLoading = true, error = false)
            val status = healthConnectManager.status(requiredPermissions)
            if (
                status.availability != HealthConnectAvailability.AVAILABLE ||
                !status.hasPermissions
            ) {
                mutableUiState.value = mutableUiState.value.copy(
                    isLoading = false,
                    availability = status.availability,
                    hasPermissions = status.hasPermissions,
                    daily = emptyList(),
                )
                return@launch
            }
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val firstDay = today.minusDays(period.days - 1)
            val origins = healthConnectManager.readDataOrigins(
                dataTypes = HEALTH_DATA_TYPES,
                startTime = firstDay.atStartOfDay(zone).toInstant(),
                endTime = today.plusDays(1).atStartOfDay(zone).toInstant(),
            )
            val days = (0 until period.days).map { offset ->
                val date = firstDay.plusDays(offset)
                val start = date.atStartOfDay(zone).toInstant()
                val end = date.plusDays(1).atStartOfDay(zone).toInstant()
                val result = healthConnectManager.readData(HEALTH_DATA_TYPES, start, end)
                HealthDailyUiModel(
                    dateLabel = SHORT_DATE_FORMATTER.format(date),
                    fullDateLabel = FULL_DATE_FORMATTER.format(date),
                    steps = result?.steps ?: 0,
                    distanceKilometers = (result?.distanceMeters ?: 0.0) / 1_000.0,
                    caloriesKilocalories = result?.caloriesKilocalories ?: 0.0,
                    averageHeartRate = result?.averageHeartRate,
                    sleepMinutes = result?.sleepMinutes ?: 0,
                    exerciseSessions = result?.exerciseSessionCount ?: 0,
                    exerciseMinutes = result?.exerciseDurationMinutes ?: 0,
                )
            }
            mutableUiState.value = mutableUiState.value.copy(
                isLoading = false,
                availability = status.availability,
                hasPermissions = true,
                sources = origins.map(::sourceLabel).distinct().sorted(),
                daily = days,
            )
        }
    }

    private fun sourceLabel(packageName: String): String = when (packageName) {
        "com.sec.android.app.shealth" -> "Samsung Health"
        "com.google.android.apps.fitness" -> "Google Fit"
        "com.forgeflow.app" -> "ForgeFlow"
        "android" -> "Este celular"
        else -> if (packageName.startsWith("com.android.healthconnect.phone.")) {
            "Este celular"
        } else {
            packageName.substringAfterLast('.').replaceFirstChar(Char::uppercase)
        }
    }

    private companion object {
        val HEALTH_DATA_TYPES = HealthConnectDataType.entries.toSet()
        val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
        val FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "EEE, dd MMM",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
