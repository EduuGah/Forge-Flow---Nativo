package com.forgeflow.feature.workout.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.WeightUnit

@Immutable
data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val workout: ActiveWorkoutUiModel? = null,
    val includeLocation: Boolean = false,
    val isCapturingLocation: Boolean = false,
    val error: Boolean = false,
)

@Immutable
data class ActiveWorkoutUiModel(
    val id: String,
    val name: String,
    val elapsedSeconds: Long,
    val completedSets: Int,
    val totalSets: Int,
    val weightUnit: WeightUnit,
    val exercises: List<ActiveExerciseUiModel>,
)

@Immutable
data class ActiveExerciseUiModel(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val mediaUri: String? = null,
    val mediaType: ExerciseMediaType? = null,
    val mediaThumbnailUri: String? = null,
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
    data class IncludeLocationChanged(val enabled: Boolean) : ActiveWorkoutAction
    data object Finish : ActiveWorkoutAction
    data object Discard : ActiveWorkoutAction
}

sealed interface ActiveWorkoutEvent {
    data object Close : ActiveWorkoutEvent
}
