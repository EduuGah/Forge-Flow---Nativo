package com.forgeflow.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.data.routine.RoutineRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.RepetitionRange
import com.forgeflow.core.model.RoutineDetails
import com.forgeflow.core.model.RoutineDraft
import com.forgeflow.core.model.RoutineExerciseDraft
import com.forgeflow.core.model.RoutineId
import com.forgeflow.core.model.SessionExerciseId
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSessionStatus
import com.forgeflow.core.model.WorkoutSet
import com.forgeflow.core.model.WorkoutSetId
import com.forgeflow.core.model.WorkoutSetType
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.model.personalRecordsAgainst
import com.forgeflow.core.model.searchTerms
import com.forgeflow.core.platform.health.HealthConnectManager
import com.forgeflow.core.platform.location.CurrentLocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    settingsRepository: SettingsRepository,
    private val currentLocationProvider: CurrentLocationProvider,
    private val healthConnectManager: HealthConnectManager,
) : ViewModel() {
    private val drafts = MutableStateFlow<Map<String, SetDraft>>(emptyMap())
    private val exerciseNotes = MutableStateFlow<Map<String, String>>(emptyMap())
    private val operation = MutableStateFlow(WorkoutOperationState())
    private val noteSaveJobs = mutableMapOf<String, Job>()
    private var currentWorkout: WorkoutDetails? = null
    private var currentWeightUnit = WeightUnit.KILOGRAM
    private var healthConnectSyncEnabled = false

    val events = MutableSharedFlow<ActiveWorkoutEvent>()

    private val ticker = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    private val workoutSources = combine(
        repository.observeActiveWorkout(),
        repository.observeHistory(),
        exerciseRepository.observeExercises(),
        routineRepository.observeRoutines(),
    ) { active, history, exercises, routines ->
        WorkoutSources(active, history, exercises, routines)
    }

    private val editorState = combine(drafts, exerciseNotes, operation) {
            currentDrafts,
            currentNotes,
            currentOperation,
        ->
        WorkoutEditorState(currentDrafts, currentNotes, currentOperation)
    }

    val uiState = combine(
        workoutSources,
        editorState,
        ticker,
        settingsRepository.observeSettings(),
    ) { sources, editor, now, settings ->
        currentWeightUnit = settings.weightUnit
        healthConnectSyncEnabled = settings.healthConnectSyncEnabled
        when (val result = sources.active) {
            is DataResult.Failure -> ActiveWorkoutUiState(
                isLoading = false,
                isCapturingLocation = editor.operation.isCapturingLocation,
                error = true,
            )
            is DataResult.Success -> {
                currentWorkout = result.value
                val routines = (sources.routines as? DataResult.Success)?.value.orEmpty()
                val scopedHistory = (sources.history as? DataResult.Success)?.value.orEmpty()
                    .scopedFor(result.value, routines)
                ActiveWorkoutUiState(
                    isLoading = false,
                    isCapturingLocation = editor.operation.isCapturingLocation,
                    workout = result.value?.toUiModel(
                        currentDrafts = editor.setDrafts,
                        currentNotes = editor.exerciseNotes,
                        now = now,
                        weightUnit = settings.weightUnit,
                        history = scopedHistory,
                    ),
                    availableExercises = (sources.exercises as? DataResult.Success)
                        ?.value
                        .orEmpty()
                        .map { it.toPickerUiModel() },
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActiveWorkoutUiState(),
    )

    fun onAction(action: ActiveWorkoutAction) {
        when (action) {
            is ActiveWorkoutAction.WeightChanged -> updateDraft(action.setId) {
                copy(
                    weight = action.value.filterNumeric(),
                    weightUnit = currentWeightUnit,
                )
            }
            is ActiveWorkoutAction.RepetitionsChanged -> updateDraft(action.setId) {
                copy(repetitions = action.value.filter(Char::isDigit))
            }
            is ActiveWorkoutAction.CompletionChanged -> persistSet(action.setId, action.completed)
            is ActiveWorkoutAction.AddSet -> addSet(action.sessionExerciseId)
            is ActiveWorkoutAction.SetTypeChanged -> updateSetType(action.setId, action.type)
            is ActiveWorkoutAction.DeleteSet -> deleteSet(action.setId)
            is ActiveWorkoutAction.ExerciseNotesChanged -> updateExerciseNotes(
                action.sessionExerciseId,
                action.notes,
            )
            is ActiveWorkoutAction.AddExercise -> addExercise(action.exerciseId)
            is ActiveWorkoutAction.ReplaceExercise -> replaceExercise(
                action.sessionExerciseId,
                action.exerciseId,
            )
            is ActiveWorkoutAction.DeleteExercise -> deleteExercise(action.sessionExerciseId)
            is ActiveWorkoutAction.MoveExerciseToPosition -> moveExerciseToPosition(
                action.sessionExerciseId,
                action.targetPosition,
            )
            is ActiveWorkoutAction.Finish -> finishWorkout(
                includeLocation = action.includeLocation,
                locationLabel = action.locationLabel,
                routineAction = action.routineAction,
            )
            ActiveWorkoutAction.Discard -> discardWorkout()
        }
    }

    private fun updateDraft(setId: String, transform: SetDraft.() -> SetDraft) {
        val fallback = currentWorkout
            ?.exercises
            ?.flatMap { it.sets }
            ?.firstOrNull { it.id.value == setId }
            ?.let {
                SetDraft(
                    weight = it.weight.asDisplayValue(currentWeightUnit),
                    repetitions = it.repetitions.asInputValue(),
                    weightUnit = currentWeightUnit,
                )
            }
            ?: SetDraft()
        drafts.update { values ->
            values + (setId to (values[setId] ?: fallback).transform())
        }
    }

    private fun persistSet(setId: String, completed: Boolean) {
        val set = currentWorkout
            ?.exercises
            ?.flatMap { it.sets }
            ?.firstOrNull { it.id.value == setId }
            ?: return
        val draft = drafts.value[setId] ?: SetDraft(
            weight = set.weight.asDisplayValue(currentWeightUnit),
            repetitions = set.repetitions.asInputValue(),
            weightUnit = currentWeightUnit,
        )
        viewModelScope.launch {
            repository.updateSet(
                setId = WorkoutSetId(setId),
                weight = draft.asWeight(),
                repetitions = Repetitions(draft.repetitions.toIntOrNull() ?: 0),
                completed = completed,
            )
        }
    }

    private fun addSet(sessionExerciseId: String) {
        viewModelScope.launch {
            repository.addSet(SessionExerciseId(sessionExerciseId))
        }
    }

    private fun updateSetType(setId: String, type: WorkoutSetType) {
        viewModelScope.launch {
            repository.updateSetType(WorkoutSetId(setId), type)
        }
    }

    private fun deleteSet(setId: String) {
        drafts.update { it - setId }
        viewModelScope.launch {
            repository.deleteSet(WorkoutSetId(setId))
        }
    }

    private fun updateExerciseNotes(sessionExerciseId: String, notes: String) {
        exerciseNotes.update { it + (sessionExerciseId to notes) }
        noteSaveJobs.remove(sessionExerciseId)?.cancel()
        noteSaveJobs[sessionExerciseId] = viewModelScope.launch {
            delay(NOTE_SAVE_DELAY_MILLIS)
            repository.updateExerciseNotes(SessionExerciseId(sessionExerciseId), notes)
        }
    }

    private fun addExercise(exerciseId: String) {
        val workout = currentWorkout ?: return
        viewModelScope.launch {
            repository.addExercise(workout.session.id, ExerciseId(exerciseId))
        }
    }

    private fun replaceExercise(sessionExerciseId: String, exerciseId: String) {
        viewModelScope.launch {
            repository.replaceExercise(
                SessionExerciseId(sessionExerciseId),
                ExerciseId(exerciseId),
            )
        }
    }

    private fun deleteExercise(sessionExerciseId: String) {
        exerciseNotes.update { it - sessionExerciseId }
        viewModelScope.launch {
            repository.deleteExercise(SessionExerciseId(sessionExerciseId))
        }
    }

    private fun moveExerciseToPosition(sessionExerciseId: String, targetPosition: Int) {
        viewModelScope.launch {
            repository.moveExerciseToPosition(
                SessionExerciseId(sessionExerciseId),
                targetPosition,
            )
        }
    }

    private fun finishWorkout(
        includeLocation: Boolean,
        locationLabel: String,
        routineAction: RoutineFinishAction,
    ) {
        val workout = currentWorkout ?: return
        viewModelScope.launch {
            operation.update { it.copy(isCapturingLocation = includeLocation) }
            drafts.value.forEach { (setId, draft) ->
                val original = workout.exercises.flatMap { it.sets }
                    .firstOrNull { it.id.value == setId }
                    ?: return@forEach
                repository.updateSet(
                    setId = WorkoutSetId(setId),
                    weight = draft.asWeight(),
                    repetitions = Repetitions(draft.repetitions.toIntOrNull() ?: 0),
                    completed = original.isCompleted,
                )
            }
            exerciseNotes.value.forEach { (exerciseId, notes) ->
                noteSaveJobs.remove(exerciseId)?.cancel()
                repository.updateExerciseNotes(SessionExerciseId(exerciseId), notes)
            }
            if (routineAction != RoutineFinishAction.KEEP_ORIGINAL) {
                saveWorkoutAsRoutine(workout, routineAction)
            }
            val location = if (includeLocation) {
                currentLocationProvider.captureCurrentLocation()?.copy(
                    label = locationLabel.trim().ifBlank { null },
                )
            } else {
                null
            }
            if (repository.finishWorkout(workout.session.id, location) is DataResult.Success) {
                if (healthConnectSyncEnabled) {
                    val completedAt = Instant.now()
                    withTimeoutOrNull(HEALTH_CONNECT_SYNC_TIMEOUT_MILLIS) {
                        healthConnectManager.syncWorkout(
                            workout = workout.asCompletedSnapshot(
                                completedAt = completedAt,
                                currentDrafts = drafts.value,
                                location = location,
                            ),
                            weightUnit = currentWeightUnit,
                        )
                    }
                }
                events.emit(ActiveWorkoutEvent.Close)
            }
            operation.update { it.copy(isCapturingLocation = false) }
        }
    }

    private fun discardWorkout() {
        viewModelScope.launch {
            if (repository.discardActiveWorkout() is DataResult.Success) {
                events.emit(ActiveWorkoutEvent.Close)
            }
        }
    }

    private fun WorkoutDetails.toUiModel(
        currentDrafts: Map<String, SetDraft>,
        currentNotes: Map<String, String>,
        now: Instant,
        weightUnit: WeightUnit,
        history: List<WorkoutDetails>,
    ): ActiveWorkoutUiModel {
        val uiExercises = exercises.map { details ->
            val exerciseId = details.sessionExercise.exerciseId
            val historicalExerciseDetails = history.flatMap { historicalWorkout ->
                historicalWorkout.exercises.filter { historicalExercise ->
                    exerciseId != null &&
                        historicalExercise.sessionExercise.exerciseId == exerciseId
                }
            }
            val historicalSets = historicalExerciseDetails.flatMap { it.sets }
            val latestSets = historicalExerciseDetails.firstOrNull()
                ?.sets
                ?.filter { it.isCompleted && it.repetitions.count > 0 }
                .orEmpty()
            val currentComparisonSets = historicalSets.toMutableList()
            val uiSets = details.sets.mapIndexed { index, set ->
                val draft = currentDrafts[set.id.value]
                val displaySet = set.copy(
                    weight = draft?.asWeight() ?: set.weight,
                    repetitions = Repetitions(
                        draft?.repetitionCount() ?: set.repetitions.count,
                    ),
                )
                val personalRecordTypes = displaySet.personalRecordsAgainst(currentComparisonSets)
                if (displaySet.isCompleted) {
                    currentComparisonSets += displaySet
                }
                ActiveSetUiModel(
                    id = set.id.value,
                    number = index + 1,
                    weight = draft?.asDisplayValue(weightUnit)
                        ?: set.weight.asDisplayValue(weightUnit),
                    repetitions = draft?.repetitions ?: set.repetitions.asInputValue(),
                    completed = set.isCompleted,
                    type = set.setType,
                    previous = latestSets.getOrNull(index)?.asCompactPerformance(weightUnit),
                    personalRecordTypes = personalRecordTypes,
                )
            }
            ActiveExerciseUiModel(
                id = details.sessionExercise.id.value,
                exerciseId = details.sessionExercise.exerciseId?.value,
                name = details.sessionExercise.exerciseNameSnapshot,
                muscleGroup = details.sessionExercise.muscleGroupSnapshot,
                mediaUri = details.sessionExercise.mediaUriSnapshot
                    ?: details.exercise?.media?.uri,
                mediaType = details.sessionExercise.mediaTypeSnapshot
                    ?: details.exercise?.media?.type,
                mediaThumbnailUri = details.sessionExercise.mediaThumbnailUriSnapshot
                    ?: details.exercise?.media?.thumbnailUri,
                lastPerformance = latestSets
                    .maxByOrNull { it.weight.grams }
                    ?.asCompactPerformance(weightUnit),
                notes = currentNotes[details.sessionExercise.id.value]
                    ?: details.sessionExercise.notes,
                sets = uiSets,
            )
        }
        val completedUiSets = uiExercises.flatMap(ActiveExerciseUiModel::sets)
            .filter { set ->
                set.completed && (set.repetitions.toIntOrNull() ?: 0) > 0
            }
        val volumeGrams = exercises.sumOf { details ->
            details.sets.sumOf { set ->
                val draft = currentDrafts[set.id.value]
                if (set.isCompleted) {
                    (draft?.asWeight()?.grams ?: set.weight.grams) *
                        (draft?.repetitionCount() ?: set.repetitions.count)
                } else {
                    0L
                }
            }
        }
        return ActiveWorkoutUiModel(
            id = session.id.value,
            routineId = session.routineId?.value,
            name = session.name,
            elapsedSeconds = Duration.between(session.startedAt, now).seconds.coerceAtLeast(0),
            completedSets = completedUiSets.size,
            totalSets = totalSetCount,
            totalVolume = volumeGrams.gramsIn(weightUnit).toCleanString(),
            personalRecordCount = completedUiSets.sumOf { it.personalRecordTypes.size },
            weightUnit = weightUnit,
            exercises = uiExercises,
        )
    }

    private fun WorkoutDetails.asCompletedSnapshot(
        completedAt: Instant,
        currentDrafts: Map<String, SetDraft>,
        location: com.forgeflow.core.model.WorkoutLocation?,
    ): WorkoutDetails = copy(
        session = session.copy(
            finishedAt = completedAt,
            status = WorkoutSessionStatus.COMPLETED,
            location = location,
            updatedAt = completedAt,
        ),
        exercises = exercises.map { details ->
            details.copy(
                sets = details.sets.map { set ->
                    val draft = currentDrafts[set.id.value]
                    if (draft == null) {
                        set
                    } else {
                        set.copy(
                            weight = draft.asWeight(),
                            repetitions = Repetitions(draft.repetitionCount()),
                            updatedAt = completedAt,
                        )
                    }
                },
            )
        },
    )

    private suspend fun saveWorkoutAsRoutine(
        workout: WorkoutDetails,
        action: RoutineFinishAction,
    ) {
        val sourceId = workout.session.routineId ?: return
        val source = (routineRepository.getRoutine(sourceId) as? DataResult.Success)?.value ?: return
        routineRepository.saveRoutine(
            RoutineDraft(
                id = sourceId.takeIf { action == RoutineFinishAction.UPDATE_ORIGINAL },
                folderId = source.routine.folderId,
                name = if (action == RoutineFinishAction.SAVE_COPY) {
                    "${source.routine.name} - cópia"
                } else {
                    source.routine.name
                },
                description = source.routine.description,
                compareHistoryWithinFolder = source.routine.compareHistoryWithinFolder,
                exercises = workout.exercises.mapNotNull { details ->
                    val exerciseId = details.sessionExercise.exerciseId ?: return@mapNotNull null
                    val original = source.exercises.firstOrNull {
                        it.exercise.id == exerciseId
                    }?.routineExercise
                    RoutineExerciseDraft(
                        exerciseId = exerciseId,
                        plannedSets = details.sets.count {
                            it.setType == WorkoutSetType.NORMAL
                        }.coerceAtLeast(1),
                        plannedWarmUpSets = details.sets.count {
                            it.setType == WorkoutSetType.WARM_UP
                        },
                        plannedRepetitions = original?.plannedRepetitions
                            ?: RepetitionRange(8, 12),
                        restSeconds = original?.defaultRestSeconds ?: 90,
                        notes = exerciseNotes.value[details.sessionExercise.id.value]
                            ?: details.sessionExercise.notes,
                    )
                },
            ),
        )
    }

    private fun List<WorkoutDetails>.scopedFor(
        active: WorkoutDetails?,
        routines: List<RoutineDetails>,
    ): List<WorkoutDetails> {
        val routineId = active?.session?.routineId ?: return this
        val source = routines.firstOrNull { it.routine.id == routineId } ?: return this
        if (!source.routine.compareHistoryWithinFolder) return this
        val comparableRoutineIds = routines
            .filter { it.routine.folderId == source.routine.folderId }
            .map { it.routine.id }
            .toSet()
        return filter { it.session.routineId in comparableRoutineIds }
    }

    private fun Exercise.toPickerUiModel(): ActiveExercisePickerUiModel =
        ActiveExercisePickerUiModel(
            id = id.value,
            name = name,
            muscleGroup = primaryMuscleGroup,
            equipment = equipment,
            mediaUri = media?.uri,
            mediaThumbnailUri = media?.thumbnailUri,
            searchTerms = searchTerms(),
        )

    private fun Weight.asDisplayValue(unit: WeightUnit): String =
        if (grams == 0L) "" else valueIn(unit).toCleanString()

    private fun WorkoutSet.asCompactPerformance(unit: WeightUnit): String =
        "${weight.valueIn(unit).toCleanString()} × ${repetitions.count}"

    private fun SetDraft.asDisplayValue(unit: WeightUnit): String {
        if (weightUnit == unit) return weight
        return asWeight().asDisplayValue(unit)
    }

    private fun SetDraft.asWeight(): Weight = Weight.from(
        value = weight.replace(',', '.').toDoubleOrNull() ?: 0.0,
        unit = weightUnit,
    )

    private fun SetDraft.repetitionCount(): Int = repetitions.toIntOrNull() ?: 0

    private fun String.filterNumeric(): String {
        var separatorFound = false
        return filter { character ->
            character.isDigit() || ((character == ',' || character == '.') && !separatorFound).also {
                if (it) separatorFound = true
            }
        }
    }

    private fun Double.toCleanString(): String =
        BigDecimal.valueOf(this)
            .setScale(1, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

    private data class SetDraft(
        val weight: String = "",
        val repetitions: String = "",
        val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    )

    private data class WorkoutOperationState(
        val isCapturingLocation: Boolean = false,
    )

    private data class WorkoutEditorState(
        val setDrafts: Map<String, SetDraft>,
        val exerciseNotes: Map<String, String>,
        val operation: WorkoutOperationState,
    )

    private data class WorkoutSources(
        val active: DataResult<WorkoutDetails?>,
        val history: DataResult<List<WorkoutDetails>>,
        val exercises: DataResult<List<Exercise>>,
        val routines: DataResult<List<RoutineDetails>>,
    )

    private companion object {
        const val HEALTH_CONNECT_SYNC_TIMEOUT_MILLIS = 8_000L
        const val NOTE_SAVE_DELAY_MILLIS = 600L
    }
}

internal fun Repetitions.asInputValue(): String =
    if (count == 0) "" else count.toString()
