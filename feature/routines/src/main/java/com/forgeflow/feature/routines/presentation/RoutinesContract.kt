package com.forgeflow.feature.routines.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup

@Immutable
data class RoutinesUiState(
    val isLoading: Boolean = true,
    val routines: List<RoutineUiModel> = emptyList(),
    val folders: List<RoutineFolderUiModel> = emptyList(),
    val exercises: List<RoutineExercisePickerModel> = emptyList(),
    val editor: RoutineEditorUiState? = null,
    val folderEditor: RoutineFolderEditorUiState? = null,
    val isSaving: Boolean = false,
    val error: RoutinesError? = null,
)

@Immutable
data class RoutineUiModel(
    val id: String,
    val folderId: String?,
    val name: String,
    val description: String,
    val exerciseNames: List<String>,
    val totalSets: Int,
)

@Immutable
data class RoutineFolderUiModel(
    val id: String,
    val name: String,
    val routineCount: Int,
)

@Immutable
data class RoutineExercisePickerModel(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val mediaUri: String? = null,
    val mediaType: ExerciseMediaType? = null,
    val mediaThumbnailUri: String? = null,
)

@Immutable
data class RoutineEditorUiState(
    val routineId: String? = null,
    val name: String = "",
    val description: String = "",
    val folderId: String? = null,
    val query: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val selectedExerciseIds: List<String> = emptyList(),
)

@Immutable
data class RoutineFolderEditorUiState(
    val id: String? = null,
    val name: String = "",
    val selectAfterSave: Boolean = false,
)

enum class RoutinesError {
    LOAD_FAILED,
    SAVE_FAILED,
    START_FAILED,
}

sealed interface RoutinesAction {
    data object CreateRoutine : RoutinesAction
    data object CreateFolder : RoutinesAction
    data object CreateFolderInEditor : RoutinesAction
    data class EditRoutine(val id: String) : RoutinesAction
    data class EditFolder(val id: String) : RoutinesAction
    data object CloseEditor : RoutinesAction
    data object CloseFolderEditor : RoutinesAction
    data class NameChanged(val value: String) : RoutinesAction
    data class DescriptionChanged(val value: String) : RoutinesAction
    data class SearchChanged(val value: String) : RoutinesAction
    data class MuscleGroupChanged(val value: MuscleGroup?) : RoutinesAction
    data class FolderChanged(val id: String?) : RoutinesAction
    data class FolderNameChanged(val value: String) : RoutinesAction
    data class ExerciseToggled(val id: String) : RoutinesAction
    data object SaveRoutine : RoutinesAction
    data object SaveFolder : RoutinesAction
    data class ArchiveRoutine(val id: String) : RoutinesAction
    data class DeleteFolder(val id: String) : RoutinesAction
    data class StartRoutine(val id: String) : RoutinesAction
    data object DismissError : RoutinesAction
}

sealed interface RoutinesEvent {
    data object OpenActiveWorkout : RoutinesEvent
}
