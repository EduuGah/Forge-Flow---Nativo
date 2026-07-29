package com.forgeflow.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.routine.RoutineRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    val uiState = combine(
        routineRepository.observeRoutines(),
        workoutRepository.observeHistory(),
        workoutRepository.observeActiveWorkout(),
    ) { routinesResult, historyResult, activeResult ->
        val routines = (routinesResult as? DataResult.Success)?.value.orEmpty()
        val history = (historyResult as? DataResult.Success)?.value.orEmpty()
        val active = (activeResult as? DataResult.Success)?.value
        val weekStart = Instant.now().minus(7, ChronoUnit.DAYS)
        HomeUiState(
            isLoading = false,
            routineCount = routines.size,
            workoutsLastSevenDays = history.count { it.session.startedAt >= weekStart },
            totalVolumeKg = history.sumOf { it.totalVolumeGrams } / 1_000,
            latestWorkoutName = history.firstOrNull()?.session?.name,
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
}
