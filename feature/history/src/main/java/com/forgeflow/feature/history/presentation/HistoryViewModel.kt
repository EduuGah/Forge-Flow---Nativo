package com.forgeflow.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.WorkoutDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: WorkoutRepository,
) : ViewModel() {
    val uiState = repository.observeHistory()
        .map { result ->
            when (result) {
                is DataResult.Failure -> HistoryUiState(isLoading = false, error = true)
                is DataResult.Success -> result.value.toUiState()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    private fun List<WorkoutDetails>.toUiState(): HistoryUiState {
        val items = map(WorkoutDetails::toUiModel)
        return HistoryUiState(
            isLoading = false,
            workouts = items,
            totalSets = sumOf(WorkoutDetails::completedSetCount),
            totalVolumeKg = sumOf(WorkoutDetails::totalVolumeGrams) / 1_000,
        )
    }

    private fun WorkoutDetails.toUiModel(): HistoryWorkoutUiModel {
        val duration = session.finishedAt?.let { Duration.between(session.startedAt, it) }
            ?: Duration.ZERO
        return HistoryWorkoutUiModel(
            id = session.id.value,
            name = session.name,
            date = DATE_FORMATTER.format(session.startedAt.atZone(ZoneId.systemDefault())),
            duration = duration.asLabel(),
            volumeKg = totalVolumeGrams / 1_000,
            exerciseCount = exercises.size,
            completedSets = completedSetCount,
            exercises = exercises.map { exercise ->
                val completed = exercise.sets.filter { it.isCompleted }
                val best = completed.maxByOrNull { it.weight.grams }
                HistoryExerciseUiModel(
                    name = exercise.sessionExercise.exerciseNameSnapshot,
                    summary = if (best == null) {
                        "${completed.size} séries"
                    } else {
                        "${completed.size} séries • ${best.weight.grams / 1_000.0} kg × " +
                            best.repetitions.count
                    },
                )
            },
        )
    }

    private fun Duration.asLabel(): String {
        val minutes = toMinutes()
        return if (minutes >= 60) "${minutes / 60}h ${minutes % 60}min" else "${minutes}min"
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
    }
}
