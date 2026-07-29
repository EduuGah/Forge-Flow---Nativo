package com.forgeflow.feature.routines.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup

@Immutable
data class RoutinesUiState(
    val isLoading: Boolean = true,
    val routines: List<RoutineUiModel> = emptyList(),
    val exercises: List<RoutineExercisePickerModel> = emptyList(),
    val editor: RoutineEditorUiState? = null,
    val isSaving: Boolean = false,
    val error: RoutinesError? = null,
)

@Immutable
data class RoutineUiModel(
    val id: String,
    val name: String,
    val description: String,
    val exerciseNames: List<String>,
    val totalSets: Int,
)

@Immutable
data class RoutineExercisePickerModel(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
)

@Immutable
data class RoutineEditorUiState(
    val routineId: String? = null,
    val name: String = "",
    val description: String = "",
    val query: String = "",
    val selectedExerciseIds: List<String> = emptyList(),
)

enum class RoutinesError {
    LOAD_FAILED,
    SAVE_FAILED,
    START_FAILED,
}

sealed interface RoutinesAction {
    data object CreateRoutine : RoutinesAction
    data class EditRoutine(val id: String) : RoutinesAction
    data object CloseEditor : RoutinesAction
    data class NameChanged(val value: String) : RoutinesAction
    data class DescriptionChanged(val value: String) : RoutinesAction
    data class SearchChanged(val value: String) : RoutinesAction
    data class ExerciseToggled(val id: String) : RoutinesAction
    data object SaveRoutine : RoutinesAction
    data class ArchiveRoutine(val id: String) : RoutinesAction
    data class StartRoutine(val id: String) : RoutinesAction
    data object DismissError : RoutinesAction
}

sealed interface RoutinesEvent {
    data object OpenActiveWorkout : RoutinesEvent
}
