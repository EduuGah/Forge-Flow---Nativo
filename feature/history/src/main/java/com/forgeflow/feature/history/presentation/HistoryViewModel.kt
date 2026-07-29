package com.forgeflow.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.gramsIn
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = kotlinx.coroutines.flow.combine(
        repository.observeHistory(),
        settingsRepository.observeSettings(),
    ) { result, settings ->
            when (result) {
                is DataResult.Failure -> HistoryUiState(isLoading = false, error = true)
                is DataResult.Success -> result.value.toUiState(settings)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    private fun List<WorkoutDetails>.toUiState(settings: UserSettings): HistoryUiState {
        val items = map { workout -> workout.toUiModel(settings) }
        return HistoryUiState(
            isLoading = false,
            workouts = items,
            totalSets = sumOf(WorkoutDetails::completedSetCount),
            totalVolume = sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            totalDurationMinutes = sumOf { workout -> workout.duration().toMinutes() },
            weightUnit = settings.weightUnit,
        )
    }

    private fun WorkoutDetails.toUiModel(settings: UserSettings): HistoryWorkoutUiModel {
        val date = session.startedAt.atZone(ZoneId.systemDefault())
        return HistoryWorkoutUiModel(
            id = session.id.value,
            name = session.name,
            day = DAY_FORMATTER.format(date),
            month = MONTH_FORMATTER.format(date).uppercase(),
            time = TIME_FORMATTER.format(date),
            monthGroup = MONTH_GROUP_FORMATTER.format(date),
            durationMinutes = duration().toMinutes(),
            volume = totalVolumeGrams.gramsIn(settings.weightUnit),
            exerciseCount = exercises.size,
            completedSets = completedSetCount,
            hasLocation = session.location != null,
            locationLabel = session.location?.label,
            exercises = exercises.map { exercise ->
                val completed = exercise.sets.filter { it.isCompleted }
                val best = completed.maxByOrNull { it.weight.grams }
                HistoryExerciseUiModel(
                    name = exercise.sessionExercise.exerciseNameSnapshot,
                    completedSets = completed.size,
                    bestWeight = best?.weight?.valueIn(settings.weightUnit),
                    bestRepetitions = best?.repetitions?.count,
                )
            },
        )
    }

    private fun WorkoutDetails.duration(): Duration =
        session.finishedAt?.let { Duration.between(session.startedAt, it) }
            ?: Duration.ZERO

    private companion object {
        val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd")
        val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "MMM",
            Locale.forLanguageTag("pt-BR"),
        )
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val MONTH_GROUP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "MMMM 'de' yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
