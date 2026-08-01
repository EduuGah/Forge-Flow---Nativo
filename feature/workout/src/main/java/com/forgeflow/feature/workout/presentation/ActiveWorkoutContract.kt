package com.forgeflow.feature.workout.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutSetType
import com.forgeflow.core.model.Equipment

@Immutable
data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val workout: ActiveWorkoutUiModel? = null,
    val isCapturingLocation: Boolean = false,
    val error: Boolean = false,
    val availableExercises: List<ActiveExercisePickerUiModel> = emptyList(),
)

@Immutable
data class ActiveWorkoutUiModel(
    val id: String,
    val routineId: String?,
    val name: String,
    val startedAtEpochMillis: Long,
    val elapsedSeconds: Long,
    val completedSets: Int,
    val totalSets: Int,
    val totalVolume: String,
    val personalRecordCount: Int,
    val weightUnit: WeightUnit,
    val exercises: List<ActiveExerciseUiModel>,
)

@Immutable
data class ActiveExerciseUiModel(
    val id: String,
    val exerciseId: String?,
    val name: String,
    val muscleGroup: MuscleGroup,
    val mediaUri: String? = null,
    val mediaType: ExerciseMediaType? = null,
    val mediaThumbnailUri: String? = null,
    val lastPerformance: String? = null,
    val notes: String = "",
    val sets: List<ActiveSetUiModel>,
)

@Immutable
data class ActiveExercisePickerUiModel(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val mediaUri: String? = null,
    val mediaThumbnailUri: String? = null,
    val searchTerms: String = name,
)

enum class RoutineFinishAction {
    KEEP_ORIGINAL,
    UPDATE_ORIGINAL,
    SAVE_COPY,
}

@Immutable
data class ActiveSetUiModel(
    val id: String,
    val number: Int,
    val weight: String,
    val repetitions: String,
    val completed: Boolean,
    val type: WorkoutSetType,
    val previous: String? = null,
    val personalRecordTypes: Set<PersonalRecordType> = emptySet(),
)

val ActiveSetUiModel.isPersonalRecord: Boolean
    get() = personalRecordTypes.isNotEmpty()

sealed interface ActiveWorkoutAction {
    data class WeightChanged(val setId: String, val value: String) : ActiveWorkoutAction
    data class RepetitionsChanged(val setId: String, val value: String) : ActiveWorkoutAction
    data class CompletionChanged(val setId: String, val completed: Boolean) : ActiveWorkoutAction
    data class AddSet(val sessionExerciseId: String) : ActiveWorkoutAction
    data class SetTypeChanged(
        val setId: String,
        val type: WorkoutSetType,
    ) : ActiveWorkoutAction
    data class DeleteSet(val setId: String) : ActiveWorkoutAction
    data class ExerciseNotesChanged(
        val sessionExerciseId: String,
        val notes: String,
    ) : ActiveWorkoutAction
    data class AddExercise(val exerciseId: String) : ActiveWorkoutAction
    data class ReplaceExercise(
        val sessionExerciseId: String,
        val exerciseId: String,
    ) : ActiveWorkoutAction
    data class DeleteExercise(val sessionExerciseId: String) : ActiveWorkoutAction
    data class MoveExerciseToPosition(
        val sessionExerciseId: String,
        val targetPosition: Int,
    ) : ActiveWorkoutAction
    data class Finish(
        val includeLocation: Boolean,
        val locationLabel: String,
        val routineAction: RoutineFinishAction = RoutineFinishAction.KEEP_ORIGINAL,
    ) : ActiveWorkoutAction
    data object Discard : ActiveWorkoutAction
}

sealed interface ActiveWorkoutEvent {
    data object Close : ActiveWorkoutEvent
}
