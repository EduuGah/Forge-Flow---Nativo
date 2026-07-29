package com.forgeflow.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.routine.RoutineRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.gramsIn
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = combine(
        routineRepository.observeRoutines(),
        workoutRepository.observeHistory(),
        workoutRepository.observeActiveWorkout(),
        settingsRepository.observeSettings(),
    ) { routinesResult, historyResult, activeResult, settings ->
        val routines = (routinesResult as? DataResult.Success)?.value.orEmpty()
        val history = (historyResult as? DataResult.Success)?.value.orEmpty()
        val active = (activeResult as? DataResult.Success)?.value
        val weekStart = Instant.now().minus(7, ChronoUnit.DAYS)
        val weeklyWorkouts = history.filter { it.session.startedAt >= weekStart }
        HomeUiState(
            isLoading = false,
            routineCount = routines.size,
            workoutsLastSevenDays = weeklyWorkouts.size,
            totalWorkoutCount = history.size,
            totalCompletedSets = history.sumOf(WorkoutDetails::completedSetCount),
            totalVolume = history.sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            weeklyVolume = weeklyWorkouts.sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            weightUnit = settings.weightUnit,
            latestWorkout = history.firstOrNull()?.toSummary(settings),
            activeWorkout = active?.let {
                HomeActiveWorkoutUiModel(
                    name = it.session.name,
                    completedSets = it.completedSetCount,
                    totalSets = it.totalSetCount,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private fun WorkoutDetails.toSummary(settings: UserSettings): HomeWorkoutSummaryUiModel {
        val duration = session.finishedAt
            ?.let { Duration.between(session.startedAt, it).toMinutes() }
            ?.coerceAtLeast(0)
            ?: 0
        return HomeWorkoutSummaryUiModel(
            name = session.name,
            date = DATE_FORMATTER.format(session.startedAt.atZone(ZoneId.systemDefault())),
            durationMinutes = duration,
            exerciseCount = exercises.size,
            completedSets = completedSetCount,
            volume = totalVolumeGrams.gramsIn(settings.weightUnit),
        )
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd MMM, HH:mm",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
