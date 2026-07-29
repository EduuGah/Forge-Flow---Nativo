package com.forgeflow.feature.routines.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.data.routine.RoutineRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.RepetitionRange
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineDraft
import com.forgeflow.core.model.RoutineExerciseDraft
import com.forgeflow.core.model.RoutineFolder
import com.forgeflow.core.model.RoutineFolderId
import com.forgeflow.core.model.RoutineId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    exerciseRepository: ExerciseRepository,
) : ViewModel() {
    private val editors = MutableStateFlow(EditorsState())
    private val operationState = MutableStateFlow(OperationState())
    private var routineDetails: List<RoutineDetails> = emptyList()
    private var routineFolders: List<RoutineFolder> = emptyList()

    val events = MutableSharedFlow<RoutinesEvent>()

    val uiState = combine(
        routineRepository.observeRoutines(),
        routineRepository.observeFolders(),
        exerciseRepository.observeExercises(),
        editors,
        operationState,
    ) { routinesResult, foldersResult, exercisesResult, currentEditors, operation ->
        buildUiState(
            routinesResult,
            foldersResult,
            exercisesResult,
            currentEditors,
            operation,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoutinesUiState(),
    )

    fun onAction(action: RoutinesAction) {
        when (action) {
            RoutinesAction.CreateRoutine -> editors.update {
                it.copy(routine = RoutineEditorUiState())
            }
            RoutinesAction.CreateFolder -> editors.update {
                it.copy(folder = RoutineFolderEditorUiState())
            }
            RoutinesAction.CreateFolderInEditor -> editors.update {
                it.copy(
                    folder = RoutineFolderEditorUiState(selectAfterSave = true),
                )
            }
            RoutinesAction.CloseEditor -> editors.update { it.copy(routine = null) }
            RoutinesAction.CloseFolderEditor -> editors.update { it.copy(folder = null) }
            RoutinesAction.DismissError -> operationState.update { it.copy(error = null) }
            is RoutinesAction.DescriptionChanged -> updateEditor { copy(description = action.value) }
            is RoutinesAction.NameChanged -> updateEditor { copy(name = action.value) }
            is RoutinesAction.SearchChanged -> updateEditor { copy(query = action.value) }
            is RoutinesAction.MuscleGroupChanged -> updateEditor {
                copy(selectedMuscleGroup = action.value)
            }
            is RoutinesAction.FolderChanged -> updateEditor { copy(folderId = action.id) }
            is RoutinesAction.FolderNameChanged -> editors.update {
                it.copy(folder = it.folder?.copy(name = action.value))
            }
            is RoutinesAction.ExerciseToggled -> toggleExercise(action.id)
            is RoutinesAction.EditRoutine -> openEditor(action.id)
            is RoutinesAction.EditFolder -> openFolderEditor(action.id)
            is RoutinesAction.ArchiveRoutine -> archiveRoutine(action.id)
            is RoutinesAction.DeleteFolder -> deleteFolder(action.id)
            is RoutinesAction.StartRoutine -> startRoutine(action.id)
            RoutinesAction.SaveRoutine -> saveRoutine()
            RoutinesAction.SaveFolder -> saveFolder()
        }
    }

    private fun buildUiState(
        routinesResult: DataResult<List<RoutineDetails>>,
        foldersResult: DataResult<List<RoutineFolder>>,
        exercisesResult: DataResult<List<Exercise>>,
        currentEditors: EditorsState,
        operation: OperationState,
    ): RoutinesUiState {
        val routines = (routinesResult as? DataResult.Success)?.value.orEmpty()
        val folders = (foldersResult as? DataResult.Success)?.value.orEmpty()
        val exercises = (exercisesResult as? DataResult.Success)?.value.orEmpty()
        routineDetails = routines
        routineFolders = folders
        return RoutinesUiState(
            isLoading = routinesResult !is DataResult.Success ||
                foldersResult !is DataResult.Success ||
                exercisesResult !is DataResult.Success,
            routines = routines.map { details ->
                RoutineUiModel(
                    id = details.routine.id.value,
                    folderId = details.routine.folderId?.value,
                    name = details.routine.name,
                    description = details.routine.description,
                    exerciseNames = details.exercises.map { it.exercise.name },
                    totalSets = details.exercises.sumOf { it.routineExercise.plannedSets },
                )
            },
            folders = folders.map { folder ->
                RoutineFolderUiModel(
                    id = folder.id.value,
                    name = folder.name,
                    routineCount = routines.count { it.routine.folderId == folder.id },
                )
            },
            exercises = exercises.map { exercise ->
                RoutineExercisePickerModel(
                    id = exercise.id.value,
                    name = exercise.name,
                    muscleGroup = exercise.primaryMuscleGroup,
                    equipment = exercise.equipment,
                    mediaUri = exercise.media?.uri,
                    mediaType = exercise.media?.type,
                    mediaThumbnailUri = exercise.media?.thumbnailUri,
                )
            },
            editor = currentEditors.routine,
            folderEditor = currentEditors.folder,
            isSaving = operation.isSaving,
            error = operation.error ?: if (
                routinesResult is DataResult.Failure ||
                    foldersResult is DataResult.Failure ||
                    exercisesResult is DataResult.Failure
            ) {
                RoutinesError.LOAD_FAILED
            } else {
                null
            },
        )
    }

    private fun openEditor(id: String) {
        val details = routineDetails.firstOrNull { it.routine.id.value == id } ?: return
        editors.update {
            it.copy(
                routine = RoutineEditorUiState(
                    routineId = id,
                    name = details.routine.name,
                    description = details.routine.description,
                    folderId = details.routine.folderId?.value,
                    selectedExerciseIds = details.exercises.map { item ->
                        item.exercise.id.value
                    },
                ),
            )
        }
    }

    private fun openFolderEditor(id: String) {
        val folder = routineFolders.firstOrNull { it.id.value == id } ?: return
        editors.update {
            it.copy(
                folder = RoutineFolderEditorUiState(
                    id = id,
                    name = folder.name,
                ),
            )
        }
    }

    private fun toggleExercise(id: String) = updateEditor {
        copy(
            selectedExerciseIds = if (id in selectedExerciseIds) {
                selectedExerciseIds - id
            } else {
                selectedExerciseIds + id
            },
        )
    }

    private fun saveRoutine() {
        val current = editors.value.routine ?: return
        if (current.name.isBlank() || current.selectedExerciseIds.isEmpty()) {
            operationState.update { it.copy(error = RoutinesError.SAVE_FAILED) }
            return
        }
        viewModelScope.launch {
            operationState.value = OperationState(isSaving = true)
            val result = routineRepository.saveRoutine(
                RoutineDraft(
                    id = current.routineId?.let(::RoutineId),
                    folderId = current.folderId?.let(::RoutineFolderId),
                    name = current.name,
                    description = current.description,
                    exercises = current.selectedExerciseIds.map { id ->
                        RoutineExerciseDraft(
                            exerciseId = ExerciseId(id),
                            plannedSets = 3,
                            plannedRepetitions = RepetitionRange(8, 12),
                            restSeconds = 90,
                        )
                    },
                ),
            )
            operationState.value = OperationState(
                error = if (result is DataResult.Failure) RoutinesError.SAVE_FAILED else null,
            )
            if (result is DataResult.Success) {
                editors.update { it.copy(routine = null) }
            }
        }
    }

    private fun saveFolder() {
        val current = editors.value.folder ?: return
        if (current.name.isBlank()) return
        viewModelScope.launch {
            val result = routineRepository.saveFolder(
                id = current.id?.let(::RoutineFolderId),
                name = current.name,
            )
            if (result is DataResult.Success) {
                editors.update { state ->
                    state.copy(
                        routine = if (current.selectAfterSave) {
                            state.routine?.copy(folderId = result.value.value)
                        } else {
                            state.routine
                        },
                        folder = null,
                    )
                }
            }
        }
    }

    private fun deleteFolder(id: String) {
        viewModelScope.launch {
            routineRepository.deleteFolder(RoutineFolderId(id))
        }
    }

    private fun archiveRoutine(id: String) {
        viewModelScope.launch {
            routineRepository.archiveRoutine(RoutineId(id))
        }
    }

    private fun startRoutine(id: String) {
        viewModelScope.launch {
            when (workoutRepository.startRoutine(RoutineId(id))) {
                is DataResult.Success -> events.emit(RoutinesEvent.OpenActiveWorkout)
                is DataResult.Failure -> {
                    operationState.update { it.copy(error = RoutinesError.START_FAILED) }
                }
            }
        }
    }

    private fun updateEditor(transform: RoutineEditorUiState.() -> RoutineEditorUiState) {
        editors.update { current ->
            current.copy(routine = current.routine?.transform())
        }
    }

    private data class EditorsState(
        val routine: RoutineEditorUiState? = null,
        val folder: RoutineFolderEditorUiState? = null,
    )

    private data class OperationState(
        val isSaving: Boolean = false,
        val error: RoutinesError? = null,
    )
}
