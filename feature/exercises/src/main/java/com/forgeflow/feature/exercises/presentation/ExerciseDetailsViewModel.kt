package com.forgeflow.feature.exercises.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSet
import com.forgeflow.core.model.estimatedOneRepMaxGrams
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.model.personalRecordsAgainst
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    exerciseRepository: ExerciseRepository,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val exerciseId = savedStateHandle.toRoute<
        com.forgeflow.core.navigation.ExerciseDetailsRoute
    >().exerciseId

    val uiState = combine(
        exerciseRepository.observeExercises(),
        workoutRepository.observeHistory(),
        settingsRepository.observeSettings(),
    ) { exercisesResult, historyResult, settings ->
        if (exercisesResult is DataResult.Failure || historyResult is DataResult.Failure) {
            ExerciseDetailsUiState(isLoading = false, error = true)
        } else {
            val exercise = (
                exercisesResult as DataResult.Success<List<Exercise>>
                ).value
                .firstOrNull { it.id.value == exerciseId }
            if (exercise == null) {
                ExerciseDetailsUiState(isLoading = false, error = true)
            } else {
                ExerciseDetailsUiState(
                    isLoading = false,
                    exercise = exercise.toDetails(
                        history = (
                            historyResult as DataResult.Success<List<WorkoutDetails>>
                            ).value,
                        settings = settings,
                    ),
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseDetailsUiState(),
    )

    private fun Exercise.toDetails(
        history: List<WorkoutDetails>,
        settings: UserSettings,
    ): ExerciseDetailsUiModel {
        val matchingSessions = history.mapNotNull { workout ->
            workout.exercises
                .firstOrNull { it.sessionExercise.exerciseId == id }
                ?.let { workout to it }
        }
        val personalRecordsBySet = mutableMapOf<String, Set<com.forgeflow.core.model.PersonalRecordType>>()
        val previousSets = mutableListOf<WorkoutSet>()
        matchingSessions.asReversed().forEach { (_, exerciseDetails) ->
            exerciseDetails.sets
                .filter(WorkoutSet::isCompleted)
                .sortedBy(WorkoutSet::position)
                .forEach { set ->
                    val records = set.personalRecordsAgainst(previousSets)
                    if (records.isNotEmpty()) {
                        personalRecordsBySet[set.id.value] = records
                    }
                    previousSets += set
                }
        }
        val completedSets = matchingSessions
            .flatMap { it.second.sets }
            .filter { it.isCompleted && it.repetitions.count > 0 }
        val bestWeight = completedSets.maxByOrNull { it.weight.grams }
        val bestOneRepMax = completedSets.maxOfOrNull(WorkoutSet::estimatedOneRepMaxGrams)
            ?.toLong()
            ?: 0L
        val bestSet = completedSets.maxByOrNull {
            it.weight.grams * it.repetitions.count
        }
        val chart = matchingSessions.asReversed().mapNotNull { (workout, details) ->
            details.sets
                .filter(WorkoutSet::isCompleted)
                .maxOfOrNull { it.weight.valueIn(settings.weightUnit) }
                ?.let { value ->
                    ExerciseChartPointUiModel(
                        value = value.toFloat(),
                        label = DATE_FORMATTER.format(
                            workout.session.startedAt.atZone(ZoneId.systemDefault()),
                        ),
                    )
                }
        }
        val personalRecordTimeline = matchingSessions.flatMap { (workout, details) ->
            details.sets
                .filter { it.isCompleted && it.repetitions.count > 0 }
                .mapNotNull { set ->
                    val recordTypes = personalRecordsBySet[set.id.value].orEmpty()
                    if (recordTypes.isEmpty()) {
                        null
                    } else {
                        ExercisePersonalRecordUiModel(
                            types = recordTypes,
                            workoutName = workout.session.name,
                            date = DATE_FORMATTER.format(
                                workout.session.startedAt.atZone(
                                    ZoneId.systemDefault(),
                                ),
                            ),
                            performance = "${
                                set.weight.valueIn(settings.weightUnit).toCleanString()
                            } × ${set.repetitions.count} ${settings.weightUnit.shortLabel()}",
                        )
                    }
                }
        }.take(3)
        return ExerciseDetailsUiModel(
            id = id.value,
            name = name,
            muscleGroup = primaryMuscleGroup,
            secondaryMuscles = secondaryMuscleGroups.toList(),
            equipment = equipment,
            instructions = instructions,
            mediaUri = media?.uri,
            mediaType = media?.type,
            mediaThumbnailUri = media?.thumbnailUri,
            weightUnit = settings.weightUnit,
            sessionCount = matchingSessions.size,
            completedSetCount = completedSets.size,
            totalVolume = completedSets
                .sumOf { it.weight.grams * it.repetitions.count }
                .gramsIn(settings.weightUnit)
                .toCleanString(),
            maxWeight = bestWeight?.weight
                ?.valueIn(settings.weightUnit)
                ?.toCleanString()
                ?: "0",
            estimatedOneRepMax = bestOneRepMax
                .toDouble()
                .roundToLong()
                .gramsIn(settings.weightUnit)
                .toCleanString(),
            bestSet = bestSet?.let {
                "${it.weight.valueIn(settings.weightUnit).toCleanString()} × " +
                    it.repetitions.count
            } ?: "—",
            personalRecordCount = personalRecordsBySet.values
                .flatten()
                .distinct()
                .size,
            personalRecords = personalRecordTimeline,
            chartPoints = chart,
            sessions = matchingSessions.map { (workout, details) ->
                ExerciseSessionUiModel(
                    id = workout.session.id.value,
                    workoutName = workout.session.name,
                    date = DATE_FORMATTER.format(
                        workout.session.startedAt.atZone(ZoneId.systemDefault()),
                    ),
                    sets = details.sets
                        .filter { it.isCompleted && it.repetitions.count > 0 }
                        .mapIndexed { index, set ->
                            ExerciseSessionSetUiModel(
                                number = index + 1,
                                performance = "${
                                    set.weight.valueIn(settings.weightUnit).toCleanString()
                                } × ${set.repetitions.count}",
                                type = set.setType,
                                personalRecordTypes =
                                    personalRecordsBySet[set.id.value].orEmpty(),
                            )
                        },
                )
            },
        )
    }

    private fun Double.toCleanString(): String =
        if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

    private fun com.forgeflow.core.model.WeightUnit.shortLabel(): String =
        if (this == com.forgeflow.core.model.WeightUnit.KILOGRAM) "kg" else "lb"

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
