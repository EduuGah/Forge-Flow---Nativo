package com.forgeflow.feature.workout.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val workout: ActiveWorkoutUiModel? = null,
    val error: Boolean = false,
)

@Immutable
data class ActiveWorkoutUiModel(
    val id: String,
    val name: String,
    val elapsedSeconds: Long,
    val completedSets: Int,
    val totalSets: Int,
    val exercises: List<ActiveExerciseUiModel>,
)

@Immutable
data class ActiveExerciseUiModel(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val sets: List<ActiveSetUiModel>,
)

@Immutable
data class ActiveSetUiModel(
    val id: String,
    val number: Int,
    val weight: String,
    val repetitions: String,
    val completed: Boolean,
)

sealed interface ActiveWorkoutAction {
    data class WeightChanged(val setId: String, val value: String) : ActiveWorkoutAction
    data class RepetitionsChanged(val setId: String, val value: String) : ActiveWorkoutAction
    data class CompletionChanged(val setId: String, val completed: Boolean) : ActiveWorkoutAction
    data class AddSet(val sessionExerciseId: String) : ActiveWorkoutAction
    data object Finish : ActiveWorkoutAction
    data object Discard : ActiveWorkoutAction
}

sealed interface ActiveWorkoutEvent {
    data object Close : ActiveWorkoutEvent
}
