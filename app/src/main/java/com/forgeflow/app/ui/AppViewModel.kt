package com.forgeflow.app.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.platform.notification.ActiveWorkoutNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

@Immutable
data class AppUiState(
    val themePreference: ThemePreference = ThemePreference.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    val compactMode: Boolean = false,
    val hasRequestedNotificationPermission: Boolean = false,
    val activeWorkout: AppActiveWorkoutUiModel? = null,
)

@Immutable
data class AppActiveWorkoutUiModel(
    val id: String,
    val name: String,
    val completedSets: Int,
    val totalSets: Int,
    val startedAtEpochMillis: Long,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    workoutRepository: WorkoutRepository,
    private val activeWorkoutNotifier: ActiveWorkoutNotifier,
) : ViewModel() {
    val uiState = combine(
        settingsRepository.observeSettings(),
        workoutRepository.observeActiveWorkout(),
    ) { settings, workoutResult ->
            val activeWorkout = (workoutResult as? DataResult.Success)
                ?.value
                ?.let { workout ->
                    AppActiveWorkoutUiModel(
                        id = workout.session.id.value,
                        name = workout.session.name,
                        completedSets = workout.completedSetCount,
                        totalSets = workout.totalSetCount,
                        startedAtEpochMillis = workout.session.startedAt.toEpochMilli(),
                    )
                }
            AppUiState(
                themePreference = settings.themePreference,
                accentColor = settings.accentColor,
                compactMode = settings.compactMode,
                hasRequestedNotificationPermission =
                    settings.hasRequestedNotificationPermission,
                activeWorkout = activeWorkout,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppUiState(),
        )

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                state.activeWorkout?.let { activeWorkoutNotifier.show(it) }
                    ?: activeWorkoutNotifier.cancel()
            }
        }
    }

    fun onNotificationPermissionRequested() {
        viewModelScope.launch {
            settingsRepository.setNotificationPermissionRequested(true)
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (!granted) return
        uiState.value.activeWorkout?.let { activeWorkoutNotifier.show(it) }
    }

    private fun ActiveWorkoutNotifier.show(workout: AppActiveWorkoutUiModel) {
        show(
            workoutName = workout.name,
            completedSets = workout.completedSets,
            totalSets = workout.totalSets,
            startedAtEpochMillis = workout.startedAtEpochMillis,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
