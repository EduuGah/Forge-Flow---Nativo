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
    private val editor = MutableStateFlow<RoutineEditorUiState?>(null)
    private val operationState = MutableStateFlow(OperationState())
    private var routineDetails: List<RoutineDetails> = emptyList()

    val events = MutableSharedFlow<RoutinesEvent>()

    val uiState = combine(
        routineRepository.observeRoutines(),
        exerciseRepository.observeExercises(),
        editor,
        operationState,
    ) { routinesResult, exercisesResult, currentEditor, operation ->
        buildUiState(routinesResult, exercisesResult, currentEditor, operation)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoutinesUiState(),
    )

    fun onAction(action: RoutinesAction) {
        when (action) {
            RoutinesAction.CreateRoutine -> editor.value = RoutineEditorUiState()
            RoutinesAction.CloseEditor -> editor.value = null
            RoutinesAction.DismissError -> operationState.update { it.copy(error = null) }
            is RoutinesAction.DescriptionChanged -> updateEditor { copy(description = action.value) }
            is RoutinesAction.NameChanged -> updateEditor { copy(name = action.value) }
            is RoutinesAction.SearchChanged -> updateEditor { copy(query = action.value) }
            is RoutinesAction.ExerciseToggled -> toggleExercise(action.id)
            is RoutinesAction.EditRoutine -> openEditor(action.id)
            is RoutinesAction.ArchiveRoutine -> archiveRoutine(action.id)
            is RoutinesAction.StartRoutine -> startRoutine(action.id)
            RoutinesAction.SaveRoutine -> saveRoutine()
        }
    }

    private fun buildUiState(
        routinesResult: DataResult<List<RoutineDetails>>,
        exercisesResult: DataResult<List<Exercise>>,
        currentEditor: RoutineEditorUiState?,
        operation: OperationState,
    ): RoutinesUiState {
        val routines = (routinesResult as? DataResult.Success)?.value.orEmpty()
        val exercises = (exercisesResult as? DataResult.Success)?.value.orEmpty()
        routineDetails = routines
        return RoutinesUiState(
            isLoading = routinesResult !is DataResult.Success || exercisesResult !is DataResult.Success,
            routines = routines.map { details ->
                RoutineUiModel(
                    id = details.routine.id.value,
                    name = details.routine.name,
                    description = details.routine.description,
                    exerciseNames = details.exercises.map { it.exercise.name },
                    totalSets = details.exercises.sumOf { it.routineExercise.plannedSets },
                )
            },
            exercises = exercises.map { exercise ->
                RoutineExercisePickerModel(
                    id = exercise.id.value,
                    name = exercise.name,
                    muscleGroup = exercise.primaryMuscleGroup,
                    equipment = exercise.equipment,
                )
            },
            editor = currentEditor,
            isSaving = operation.isSaving,
            error = operation.error ?: if (
                routinesResult is DataResult.Failure || exercisesResult is DataResult.Failure
            ) {
                RoutinesError.LOAD_FAILED
            } else {
                null
            },
        )
    }

    private fun openEditor(id: String) {
        val details = routineDetails.firstOrNull { it.routine.id.value == id } ?: return
        editor.value = RoutineEditorUiState(
            routineId = id,
            name = details.routine.name,
            description = details.routine.description,
            selectedExerciseIds = details.exercises.map { it.exercise.id.value },
        )
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
        val current = editor.value ?: return
        if (current.name.isBlank() || current.selectedExerciseIds.isEmpty()) {
            operationState.update { it.copy(error = RoutinesError.SAVE_FAILED) }
            return
        }
        viewModelScope.launch {
            operationState.value = OperationState(isSaving = true)
            val result = routineRepository.saveRoutine(
                RoutineDraft(
                    id = current.routineId?.let(::RoutineId),
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
            if (result is DataResult.Success) editor.value = null
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
        editor.update { current -> current?.transform() }
    }

    private data class OperationState(
        val isSaving: Boolean = false,
        val error: RoutinesError? = null,
    )
}
