package com.forgeflow.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.SessionExerciseId
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSetId
import com.forgeflow.core.platform.location.CurrentLocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    private val currentLocationProvider: CurrentLocationProvider,
) : ViewModel() {
    private val drafts = MutableStateFlow<Map<String, SetDraft>>(emptyMap())
    private val operation = MutableStateFlow(WorkoutOperationState())
    private var currentWorkout: WorkoutDetails? = null
    private var currentWeightUnit = WeightUnit.KILOGRAM

    val events = MutableSharedFlow<ActiveWorkoutEvent>()

    private val ticker = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    val uiState = combine(
        repository.observeActiveWorkout(),
        drafts,
        ticker,
        settingsRepository.observeSettings(),
        operation,
    ) { result, currentDrafts, now, settings, currentOperation ->
        currentWeightUnit = settings.weightUnit
        when (result) {
            is DataResult.Failure -> ActiveWorkoutUiState(
                isLoading = false,
                includeLocation = currentOperation.includeLocation,
                isCapturingLocation = currentOperation.isCapturingLocation,
                error = true,
            )
            is DataResult.Success -> {
                currentWorkout = result.value
                ActiveWorkoutUiState(
                    isLoading = false,
                    includeLocation = currentOperation.includeLocation,
                    isCapturingLocation = currentOperation.isCapturingLocation,
                    workout = result.value?.toUiModel(
                        currentDrafts = currentDrafts,
                        now = now,
                        weightUnit = settings.weightUnit,
                    ),
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
            is ActiveWorkoutAction.IncludeLocationChanged -> operation.update {
                it.copy(includeLocation = action.enabled)
            }
            ActiveWorkoutAction.Finish -> finishWorkout()
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
                    repetitions = it.repetitions.count.toString(),
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
            repetitions = set.repetitions.count.toString(),
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

    private fun finishWorkout() {
        val workout = currentWorkout ?: return
        viewModelScope.launch {
            operation.update { it.copy(isCapturingLocation = it.includeLocation) }
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
            val location = if (operation.value.includeLocation) {
                currentLocationProvider.captureCurrentLocation()
            } else {
                null
            }
            if (repository.finishWorkout(workout.session.id, location) is DataResult.Success) {
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
        now: Instant,
        weightUnit: WeightUnit,
    ): ActiveWorkoutUiModel = ActiveWorkoutUiModel(
        id = session.id.value,
        name = session.name,
        elapsedSeconds = Duration.between(session.startedAt, now).seconds.coerceAtLeast(0),
        completedSets = completedSetCount,
        totalSets = totalSetCount,
        weightUnit = weightUnit,
        exercises = exercises.map { details ->
            ActiveExerciseUiModel(
                id = details.sessionExercise.id.value,
                name = details.sessionExercise.exerciseNameSnapshot,
                muscleGroup = details.sessionExercise.muscleGroupSnapshot,
                mediaUri = details.sessionExercise.mediaUriSnapshot
                    ?: details.exercise?.media?.uri,
                mediaType = details.sessionExercise.mediaTypeSnapshot
                    ?: details.exercise?.media?.type,
                mediaThumbnailUri = details.sessionExercise.mediaThumbnailUriSnapshot
                    ?: details.exercise?.media?.thumbnailUri,
                sets = details.sets.mapIndexed { index, set ->
                    val draft = currentDrafts[set.id.value]
                    ActiveSetUiModel(
                        id = set.id.value,
                        number = index + 1,
                        weight = draft?.asDisplayValue(weightUnit)
                            ?: set.weight.asDisplayValue(weightUnit),
                        repetitions = draft?.repetitions ?: set.repetitions.count.toString(),
                        completed = set.isCompleted,
                    )
                },
            )
        },
    )

    private fun Weight.asDisplayValue(unit: WeightUnit): String =
        if (grams == 0L) "" else valueIn(unit).toCleanString()

    private fun SetDraft.asDisplayValue(unit: WeightUnit): String {
        if (weightUnit == unit) return weight
        return asWeight().asDisplayValue(unit)
    }

    private fun SetDraft.asWeight(): Weight = Weight.from(
        value = weight.replace(',', '.').toDoubleOrNull() ?: 0.0,
        unit = weightUnit,
    )

    private fun String.filterNumeric(): String {
        var separatorFound = false
        return filter { character ->
            character.isDigit() || ((character == ',' || character == '.') && !separatorFound).also {
                if (it) separatorFound = true
            }
        }
    }

    private fun Double.toCleanString(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()

    private data class SetDraft(
        val weight: String = "",
        val repetitions: String = "",
        val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    )

    private data class WorkoutOperationState(
        val includeLocation: Boolean = false,
        val isCapturingLocation: Boolean = false,
    )
}
