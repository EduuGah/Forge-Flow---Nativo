package com.forgeflow.feature.exercises.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup

@Immutable
data class ExercisesUiState(
    val query: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val isLoading: Boolean = true,
    val exercises: List<ExerciseUiModel> = emptyList(),
    val totalCount: Int = 0,
    val customCount: Int = 0,
    val editor: ExerciseEditorUiState? = null,
    val isSaving: Boolean = false,
    val error: ExercisesError? = null,
)

@Immutable
data class ExerciseUiModel(
    val id: String,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val equipment: Equipment,
    val instructions: String = "",
    val isCustom: Boolean = false,
)

enum class ExercisesError {
    LOAD_FAILED,
    SAVE_FAILED,
}

@Immutable
data class ExerciseEditorUiState(
    val id: String? = null,
    val name: String = "",
    val muscleGroup: MuscleGroup = MuscleGroup.CHEST,
    val equipment: Equipment = Equipment.BARBELL,
    val instructions: String = "",
)

sealed interface ExercisesAction {
    data class SearchChanged(val query: String) : ExercisesAction
    data class MuscleGroupChanged(val muscleGroup: MuscleGroup?) : ExercisesAction
    data object CreateExercise : ExercisesAction
    data class EditExercise(val id: String) : ExercisesAction
    data object CloseEditor : ExercisesAction
    data class EditorNameChanged(val value: String) : ExercisesAction
    data class EditorMuscleChanged(val value: MuscleGroup) : ExercisesAction
    data class EditorEquipmentChanged(val value: Equipment) : ExercisesAction
    data class EditorInstructionsChanged(val value: String) : ExercisesAction
    data object SaveExercise : ExercisesAction
    data class DeleteExercise(val id: String) : ExercisesAction
    data object Retry : ExercisesAction
}
