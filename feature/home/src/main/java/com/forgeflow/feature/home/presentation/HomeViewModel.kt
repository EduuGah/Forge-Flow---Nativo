package com.forgeflow.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.routine.RoutineRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSet
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.model.personalRecordsAgainst
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = combine(
        routineRepository.observeRoutines(),
        workoutRepository.observeHistory(),
        workoutRepository.observeActiveWorkout(),
        settingsRepository.observeSettings(),
    ) { routinesResult, historyResult, activeResult, settings ->
        val routines = (routinesResult as? DataResult.Success)?.value.orEmpty()
        val history = (historyResult as? DataResult.Success)?.value.orEmpty()
        val active = (activeResult as? DataResult.Success)?.value
        val weekStart = Instant.now().minus(7, ChronoUnit.DAYS)
        val weeklyWorkouts = history.filter { it.session.startedAt >= weekStart }
        val totalDurationMinutes = history.sumOf { it.durationMinutes() }
        val records = history.personalRecords(settings)
        val completedByMuscle = history
            .flatMap(WorkoutDetails::exercises)
            .groupBy { it.sessionExercise.muscleGroupSnapshot }
            .mapValues { (_, exercises) ->
                exercises.sumOf { exercise ->
                    exercise.sets.count {
                        it.isCompleted && it.repetitions.count > 0
                    }
                }
            }
        val totalMuscleSets = completedByMuscle.values.sum().coerceAtLeast(1)
        HomeUiState(
            isLoading = false,
            routineCount = routines.size,
            workoutsLastSevenDays = weeklyWorkouts.size,
            totalWorkoutCount = history.size,
            totalCompletedSets = history.sumOf(WorkoutDetails::completedSetCount),
            totalVolume = history.sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            weeklyVolume = weeklyWorkouts.sumOf(WorkoutDetails::totalVolumeGrams)
                .gramsIn(settings.weightUnit),
            totalDurationMinutes = totalDurationMinutes,
            averageDurationMinutes = if (history.isEmpty()) {
                0
            } else {
                totalDurationMinutes / history.size
            },
            currentStreak = history.currentStreak(),
            personalRecordCount = records.size,
            weightRecordCount = records.count {
                it.type == com.forgeflow.core.model.PersonalRecordType.WEIGHT
            },
            volumeRecordCount = records.count {
                it.type == com.forgeflow.core.model.PersonalRecordType.SET_VOLUME
            },
            weightUnit = settings.weightUnit,
            latestWorkout = history.firstOrNull()?.toSummary(settings),
            recentWorkouts = history.take(4).map { it.toSummary(settings) },
            volumeChart = history
                .take(8)
                .asReversed()
                .map { workout ->
                    HomeChartPointUiModel(
                        label = CHART_DATE_FORMATTER.format(
                            workout.session.startedAt.atZone(ZoneId.systemDefault()),
                        ),
                        value = workout.totalVolumeGrams
                            .gramsIn(settings.weightUnit)
                            .toFloat(),
                    )
                },
            muscleDistribution = completedByMuscle.entries
                .sortedByDescending(Map.Entry<*, Int>::value)
                .take(5)
                .map { (muscleGroup, count) ->
                    HomeMuscleDistributionUiModel(
                        muscleGroup = muscleGroup,
                        completedSets = count,
                        share = count.toFloat() / totalMuscleSets,
                    )
                },
            recentRecords = records.take(5),
            activeWorkout = active?.let {
                HomeActiveWorkoutUiModel(
                    name = it.session.name,
                    completedSets = it.completedSetCount,
                    totalSets = it.totalSetCount,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private fun WorkoutDetails.toSummary(settings: UserSettings): HomeWorkoutSummaryUiModel {
        val duration = session.finishedAt
            ?.let { Duration.between(session.startedAt, it).toMinutes() }
            ?.coerceAtLeast(0)
            ?: 0
        return HomeWorkoutSummaryUiModel(
            id = session.id.value,
            name = session.name,
            date = DATE_FORMATTER.format(session.startedAt.atZone(ZoneId.systemDefault())),
            durationMinutes = duration,
            exerciseCount = exercises.size,
            completedSets = completedSetCount,
            volume = totalVolumeGrams.gramsIn(settings.weightUnit),
        )
    }

    private fun WorkoutDetails.durationMinutes(): Long =
        session.finishedAt
            ?.let { Duration.between(session.startedAt, it).toMinutes().coerceAtLeast(0) }
            ?: 0

    private fun List<WorkoutDetails>.currentStreak(): Int {
        val workoutDates = map {
            it.session.startedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
        var cursor = LocalDate.now()
        if (cursor !in workoutDates) cursor = cursor.minusDays(1)
        var streak = 0
        while (cursor in workoutDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    private fun List<WorkoutDetails>.personalRecords(
        settings: UserSettings,
    ): List<HomePersonalRecordUiModel> {
        val previousByExercise = mutableMapOf<String, MutableList<WorkoutSet>>()
        val records = mutableListOf<HomePersonalRecordUiModel>()
        asReversed().forEach { workout ->
            workout.exercises.forEach { exercise ->
                val exerciseKey = exercise.sessionExercise.exerciseId?.value
                    ?: exercise.sessionExercise.exerciseNameSnapshot
                val previous = previousByExercise.getOrPut(exerciseKey) { mutableListOf() }
                exercise.sets
                    .filter { it.isCompleted && it.repetitions.count > 0 }
                    .sortedBy(WorkoutSet::position)
                    .forEach { set ->
                        set.personalRecordsAgainst(previous).forEach { type ->
                            records += HomePersonalRecordUiModel(
                                type = type,
                                exerciseName = exercise.sessionExercise.exerciseNameSnapshot,
                                workoutName = workout.session.name,
                                performance = "${
                                    set.weight.valueIn(settings.weightUnit).toCleanString()
                                } × ${set.repetitions.count} ${settings.weightUnit.shortLabel()}",
                                date = DATE_FORMATTER.format(
                                    workout.session.startedAt.atZone(
                                        ZoneId.systemDefault(),
                                    ),
                                ),
                            )
                        }
                        previous += set
                    }
            }
        }
        return records.asReversed()
    }

    private fun Double.toCleanString(): String =
        if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

    private fun com.forgeflow.core.model.WeightUnit.shortLabel(): String =
        if (this == com.forgeflow.core.model.WeightUnit.KILOGRAM) "kg" else "lb"

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd MMM, HH:mm",
            Locale.forLanguageTag("pt-BR"),
        )
        val CHART_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd/MM",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
