package com.forgeflow.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.Repetitions
import com.forgeflow.core.model.SessionExerciseId
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSetId
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
) : ViewModel() {
    private val drafts = MutableStateFlow<Map<String, SetDraft>>(emptyMap())
    private var currentWorkout: WorkoutDetails? = null

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
    ) { result, currentDrafts, now ->
        when (result) {
            is DataResult.Failure -> ActiveWorkoutUiState(isLoading = false, error = true)
            is DataResult.Success -> {
                currentWorkout = result.value
                ActiveWorkoutUiState(
                    isLoading = false,
                    workout = result.value?.toUiModel(currentDrafts, now),
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
                copy(weight = action.value.filterNumeric())
            }
            is ActiveWorkoutAction.RepetitionsChanged -> updateDraft(action.setId) {
                copy(repetitions = action.value.filter(Char::isDigit))
            }
            is ActiveWorkoutAction.CompletionChanged -> persistSet(action.setId, action.completed)
            is ActiveWorkoutAction.AddSet -> addSet(action.sessionExerciseId)
            ActiveWorkoutAction.Finish -> finishWorkout()
            ActiveWorkoutAction.Discard -> discardWorkout()
        }
    }

    private fun updateDraft(setId: String, transform: SetDraft.() -> SetDraft) {
        val fallback = currentWorkout
            ?.exercises
            ?.flatMap { it.sets }
            ?.firstOrNull { it.id.value == setId }
            ?.let { SetDraft(it.weight.asKilograms(), it.repetitions.count.toString()) }
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
            weight = set.weight.asKilograms(),
            repetitions = set.repetitions.count.toString(),
        )
        viewModelScope.launch {
            repository.updateSet(
                setId = WorkoutSetId(setId),
                weight = Weight.fromGrams(draft.weight.toGrams()),
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
            drafts.value.forEach { (setId, draft) ->
                val original = workout.exercises.flatMap { it.sets }
                    .firstOrNull { it.id.value == setId }
                    ?: return@forEach
                repository.updateSet(
                    setId = WorkoutSetId(setId),
                    weight = Weight.fromGrams(draft.weight.toGrams()),
                    repetitions = Repetitions(draft.repetitions.toIntOrNull() ?: 0),
                    completed = original.isCompleted,
                )
            }
            if (repository.finishWorkout(workout.session.id) is DataResult.Success) {
                events.emit(ActiveWorkoutEvent.Close)
            }
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
    ): ActiveWorkoutUiModel = ActiveWorkoutUiModel(
        id = session.id.value,
        name = session.name,
        elapsedSeconds = Duration.between(session.startedAt, now).seconds.coerceAtLeast(0),
        completedSets = completedSetCount,
        totalSets = totalSetCount,
        exercises = exercises.map { details ->
            ActiveExerciseUiModel(
                id = details.sessionExercise.id.value,
                name = details.sessionExercise.exerciseNameSnapshot,
                muscleGroup = details.sessionExercise.muscleGroupSnapshot.name,
                sets = details.sets.mapIndexed { index, set ->
                    val draft = currentDrafts[set.id.value]
                    ActiveSetUiModel(
                        id = set.id.value,
                        number = index + 1,
                        weight = draft?.weight ?: set.weight.asKilograms(),
                        repetitions = draft?.repetitions ?: set.repetitions.count.toString(),
                        completed = set.isCompleted,
                    )
                },
            )
        },
    )

    private fun Weight.asKilograms(): String =
        if (grams == 0L) "" else (grams / 1_000.0).toCleanString()

    private fun String.toGrams(): Long =
        ((replace(',', '.').toDoubleOrNull() ?: 0.0) * 1_000).toLong().coerceAtLeast(0)

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
    )
}
