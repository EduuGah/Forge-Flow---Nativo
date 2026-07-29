package com.forgeflow.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSet
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.model.personalRecordsAgainst
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
        val personalRecordIds = calculatePersonalRecordIds()
        val items = map { workout -> workout.toUiModel(settings, personalRecordIds) }
        return HistoryUiState(
            isLoading = false,
            workouts = items,
            totalSets = items.sumOf(HistoryWorkoutUiModel::completedSets),
            totalVolume = sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            totalDurationMinutes = sumOf { workout -> workout.duration().toMinutes() },
            weightUnit = settings.weightUnit,
        )
    }

    private fun List<WorkoutDetails>.calculatePersonalRecordIds(): Set<String> {
        val previousByExercise = mutableMapOf<String, MutableList<WorkoutSet>>()
        val recordIds = mutableSetOf<String>()
        asReversed().forEach { workout ->
            workout.exercises.forEach { exercise ->
                val exerciseKey = exercise.sessionExercise.exerciseId?.value
                    ?: exercise.sessionExercise.exerciseNameSnapshot
                val previous = previousByExercise.getOrPut(exerciseKey) { mutableListOf() }
                exercise.sets
                    .filter(WorkoutSet::isCompleted)
                    .sortedBy(WorkoutSet::position)
                    .forEach { set ->
                        if (set.personalRecordsAgainst(previous).isNotEmpty()) {
                            recordIds += set.id.value
                        }
                        previous += set
                    }
            }
        }
        return recordIds
    }

    private fun WorkoutDetails.toUiModel(
        settings: UserSettings,
        personalRecordIds: Set<String>,
    ): HistoryWorkoutUiModel {
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
            completedSets = exercises.sumOf { exercise ->
                exercise.sets.count {
                    it.isCompleted && it.repetitions.count > 0
                }
            },
            hasLocation = session.location != null,
            locationLabel = session.location?.label,
            exercises = exercises.map { exercise ->
                val completed = exercise.sets.filter {
                    it.isCompleted && it.repetitions.count > 0
                }
                val best = completed.maxByOrNull { it.weight.grams }
                HistoryExerciseUiModel(
                    exerciseId = exercise.sessionExercise.exerciseId?.value,
                    name = exercise.sessionExercise.exerciseNameSnapshot,
                    mediaUri = exercise.sessionExercise.mediaUriSnapshot
                        ?: exercise.exercise?.media?.uri,
                    mediaType = exercise.sessionExercise.mediaTypeSnapshot
                        ?: exercise.exercise?.media?.type,
                    mediaThumbnailUri = exercise.sessionExercise.mediaThumbnailUriSnapshot
                        ?: exercise.exercise?.media?.thumbnailUri,
                    completedSets = completed.size,
                    totalVolume = completed.sumOf {
                        it.weight.grams * it.repetitions.count
                    }.gramsIn(settings.weightUnit),
                    bestWeight = best?.weight?.valueIn(settings.weightUnit),
                    bestRepetitions = best?.repetitions?.count,
                    personalRecordCount = completed.count {
                        it.id.value in personalRecordIds
                    },
                    sets = completed.mapIndexed { index, set ->
                        HistorySetUiModel(
                            id = set.id.value,
                            number = index + 1,
                            type = set.setType,
                            weight = set.weight.valueIn(settings.weightUnit),
                            repetitions = set.repetitions.count,
                            isPersonalRecord = set.id.value in personalRecordIds,
                        )
                    },
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
