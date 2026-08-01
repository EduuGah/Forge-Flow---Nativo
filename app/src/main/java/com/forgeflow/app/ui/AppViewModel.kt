package com.forgeflow.app.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.app.icon.LauncherIconManager
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.platform.notification.ActiveWorkoutNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

@Immutable
data class AppUiState(
    val isSettingsLoaded: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val weeklyWorkoutGoal: Int = 3,
    val compactMode: Boolean = false,
    val showTutorial: Boolean = false,
    val showGuidedWorkoutTutorial: Boolean = false,
    val hasRequestedNotificationPermission: Boolean = false,
    val activeWorkout: AppActiveWorkoutUiModel? = null,
)

@Immutable
data class AppActiveWorkoutUiModel(
    val id: String,
    val name: String,
    val completedSets: Int,
    val totalSets: Int,
    val exerciseCount: Int,
    val totalVolumeLabel: String,
    val startedAtEpochMillis: Long,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    workoutRepository: WorkoutRepository,
    private val activeWorkoutNotifier: ActiveWorkoutNotifier,
    private val launcherIconManager: LauncherIconManager,
) : ViewModel() {
    private val tutorialRequested = MutableStateFlow(false)
    private val tutorialDismissedForSession = MutableStateFlow(false)
    private val guidedWorkoutTutorialRequested = MutableStateFlow(false)

    val uiState = combine(
        settingsRepository.observeSettings(),
        workoutRepository.observeActiveWorkout(),
        tutorialRequested,
        tutorialDismissedForSession,
        guidedWorkoutTutorialRequested,
    ) { settings, workoutResult, tutorialRequested, tutorialDismissed, guidedRequested ->
            val activeWorkout = (workoutResult as? DataResult.Success)
                ?.value
                ?.let { workout ->
                    AppActiveWorkoutUiModel(
                        id = workout.session.id.value,
                        name = workout.session.name,
                        completedSets = workout.completedSetCount,
                        totalSets = workout.totalSetCount,
                        exerciseCount = workout.exercises.size,
                        totalVolumeLabel = "${
                            workout.totalVolumeGrams
                                .gramsIn(settings.weightUnit)
                                .asNotificationValue()
                        } ${settings.weightUnit.shortLabel()}",
                        startedAtEpochMillis = workout.session.startedAt.toEpochMilli(),
                    )
                }
            val showTutorial = shouldShowTutorial(
                hasCompletedOnboarding = settings.hasCompletedOnboarding,
                requested = tutorialRequested,
                dismissedForSession = tutorialDismissed,
            )
            AppUiState(
                isSettingsLoaded = true,
                themePreference = settings.themePreference,
                accentColor = settings.accentColor,
                weightUnit = settings.weightUnit,
                weeklyWorkoutGoal = settings.weeklyWorkoutGoal,
                compactMode = settings.compactMode,
                showTutorial = showTutorial,
                showGuidedWorkoutTutorial = guidedRequested && !showTutorial,
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
            settingsRepository.observeSettings()
                .map { it.accentColor }
                .distinctUntilChanged()
                .collect(launcherIconManager::scheduleUpdate)
        }
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

    fun onTutorialRequested() {
        tutorialDismissedForSession.value = false
        tutorialRequested.value = true
    }

    fun onTutorialCompleted(weightUnit: WeightUnit, weeklyWorkoutGoal: Int) {
        tutorialDismissedForSession.value = true
        tutorialRequested.value = false
        guidedWorkoutTutorialRequested.value = true
        viewModelScope.launch {
            settingsRepository.setWeightUnit(weightUnit)
            settingsRepository.setWeeklyWorkoutGoal(weeklyWorkoutGoal)
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun onGuidedWorkoutTutorialRequested() {
        guidedWorkoutTutorialRequested.value = true
    }

    fun onGuidedWorkoutTutorialDismissed() {
        guidedWorkoutTutorialRequested.value = false
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
            exerciseCount = workout.exerciseCount,
            totalVolumeLabel = workout.totalVolumeLabel,
            startedAtEpochMillis = workout.startedAtEpochMillis,
        )
    }

    private fun Double.asNotificationValue(): String =
        NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 1
        }.format(this)

    private fun WeightUnit.shortLabel(): String = when (this) {
        WeightUnit.KILOGRAM -> "kg"
        WeightUnit.POUND -> "lb"
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

internal fun shouldShowTutorial(
    hasCompletedOnboarding: Boolean,
    requested: Boolean,
    dismissedForSession: Boolean,
): Boolean = requested || (!hasCompletedOnboarding && !dismissedForSession)
