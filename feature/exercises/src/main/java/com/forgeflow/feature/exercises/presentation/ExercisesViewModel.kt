package com.forgeflow.feature.exercises.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.matchesSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val repository: ExerciseRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedMuscle = MutableStateFlow<MuscleGroup?>(null)
    private val editor = MutableStateFlow<ExerciseEditorUiState?>(null)
    private val operation = MutableStateFlow(OperationState())
    private val retrySignal = MutableStateFlow(0)
    private var latestExercises: List<Exercise> = emptyList()

    private val exerciseResult = retrySignal.flatMapLatest {
        repository.observeExercises()
    }

    val uiState = combine(
        query,
        selectedMuscle,
        editor,
        operation,
        exerciseResult,
    ) { currentQuery, muscleGroup, currentEditor, operationState, result ->
        result.toUiState(
            currentQuery = currentQuery,
            muscleGroup = muscleGroup,
            editor = currentEditor,
            operation = operationState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExercisesUiState(),
    )

    fun onAction(action: ExercisesAction) {
        when (action) {
            is ExercisesAction.SearchChanged -> query.value = action.query
            is ExercisesAction.MuscleGroupChanged -> selectedMuscle.value = action.muscleGroup
            ExercisesAction.CreateExercise -> editor.value = ExerciseEditorUiState()
            ExercisesAction.CloseEditor -> editor.value = null
            is ExercisesAction.EditExercise -> openEditor(action.id)
            is ExercisesAction.EditorNameChanged -> updateEditor { copy(name = action.value) }
            is ExercisesAction.EditorMuscleChanged -> updateEditor {
                copy(muscleGroup = action.value)
            }
            is ExercisesAction.EditorEquipmentChanged -> updateEditor {
                copy(equipment = action.value)
            }
            is ExercisesAction.EditorInstructionsChanged -> updateEditor {
                copy(instructions = action.value)
            }
            is ExercisesAction.EditorPhotoSelected -> updateEditor {
                copy(
                    mediaUri = action.uri,
                    pendingMediaUri = action.uri,
                    removeMedia = false,
                )
            }
            ExercisesAction.RemoveEditorPhoto -> updateEditor {
                copy(
                    mediaUri = null,
                    pendingMediaUri = null,
                    removeMedia = true,
                )
            }
            ExercisesAction.SaveExercise -> saveExercise()
            is ExercisesAction.DeleteExercise -> deleteExercise(action.id)
            ExercisesAction.Retry -> retrySignal.update { it + 1 }
        }
    }

    private fun DataResult<List<Exercise>>.toUiState(
        currentQuery: String,
        muscleGroup: MuscleGroup?,
        editor: ExerciseEditorUiState?,
        operation: OperationState,
    ): ExercisesUiState = when (this) {
        is DataResult.Failure -> ExercisesUiState(
            query = currentQuery,
            selectedMuscleGroup = muscleGroup,
            isLoading = false,
            editor = editor,
            error = operation.error ?: ExercisesError.LOAD_FAILED,
        )
        is DataResult.Success -> {
            latestExercises = value
            ExercisesUiState(
                query = currentQuery,
                selectedMuscleGroup = muscleGroup,
                isLoading = false,
                exercises = value
                    .asSequence()
                    .filter { exercise ->
                        exercise.matchesSearch(currentQuery)
                    }
                    .filter { exercise ->
                        muscleGroup == null || exercise.primaryMuscleGroup == muscleGroup
                    }
                    .map { exercise -> exercise.toUiModel() }
                    .toList(),
                totalCount = value.size,
                customCount = value.count(Exercise::isCustom),
                editor = editor,
                isSaving = operation.isSaving,
                error = operation.error,
            )
        }
    }

    private fun openEditor(id: String) {
        val exercise = latestExercises.firstOrNull { it.id.value == id } ?: return
        if (!exercise.isCustom) return
        editor.value = ExerciseEditorUiState(
            id = exercise.id.value,
            name = exercise.name,
            muscleGroup = exercise.primaryMuscleGroup,
            equipment = exercise.equipment,
            instructions = exercise.instructions,
            mediaUri = exercise.media?.thumbnailUri ?: exercise.media?.uri,
        )
    }

    private fun saveExercise() {
        val current = editor.value ?: return
        if (current.name.isBlank()) {
            operation.update { it.copy(error = ExercisesError.SAVE_FAILED) }
            return
        }
        viewModelScope.launch {
            operation.value = OperationState(isSaving = true)
            val result = repository.saveCustomExercise(
                id = current.id?.let(::ExerciseId),
                name = current.name,
                muscleGroup = current.muscleGroup,
                equipment = current.equipment,
                instructions = current.instructions,
                sourceMediaUri = current.pendingMediaUri,
                removeMedia = current.removeMedia,
            )
            operation.value = OperationState(
                error = if (result is DataResult.Failure) ExercisesError.SAVE_FAILED else null,
            )
            if (result is DataResult.Success) editor.value = null
        }
    }

    private fun deleteExercise(id: String) {
        viewModelScope.launch {
            repository.deleteCustomExercise(ExerciseId(id))
        }
    }

    private fun updateEditor(transform: ExerciseEditorUiState.() -> ExerciseEditorUiState) {
        editor.update { current -> current?.transform() }
    }

    private fun Exercise.toUiModel(): ExerciseUiModel = ExerciseUiModel(
        id = id.value,
        name = name,
        primaryMuscleGroup = primaryMuscleGroup,
        equipment = equipment,
        instructions = instructions,
        mediaUri = media?.uri,
        mediaType = media?.type,
        mediaThumbnailUri = media?.thumbnailUri,
        isCustom = isCustom,
    )

    private data class OperationState(
        val isSaving: Boolean = false,
        val error: ExercisesError? = null,
    )
}
