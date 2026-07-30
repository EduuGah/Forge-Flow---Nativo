package com.forgeflow.feature.home.presentation

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
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class EvolutionViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(EvolutionPeriod.THIRTY_DAYS)

    val uiState = combine(
        workoutRepository.observeHistory(),
        settingsRepository.observeSettings(),
        selectedPeriod,
    ) { historyResult, settings, period ->
        when (historyResult) {
            is DataResult.Failure -> EvolutionUiState(
                isLoading = false,
                hasError = true,
                period = period,
                weightUnit = settings.weightUnit,
            )
            is DataResult.Success -> historyResult.value.toEvolutionUiState(
                period = period,
                settings = settings,
                now = Instant.now(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EvolutionUiState(),
    )

    fun onAction(action: EvolutionAction) {
        when (action) {
            is EvolutionAction.PeriodChanged -> selectedPeriod.value = action.period
        }
    }
}

private fun List<WorkoutDetails>.toEvolutionUiState(
    period: EvolutionPeriod,
    settings: UserSettings,
    now: Instant,
): EvolutionUiState {
    val currentStart = now.minus(period.days, ChronoUnit.DAYS)
    val previousStart = currentStart.minus(period.days, ChronoUnit.DAYS)
    val current = filter { workout ->
        !workout.session.startedAt.isBefore(currentStart) &&
            !workout.session.startedAt.isAfter(now)
    }
    val previous = filter { workout ->
        !workout.session.startedAt.isBefore(previousStart) &&
            workout.session.startedAt.isBefore(currentStart)
    }
    val currentVolumeGrams = current.sumOf(WorkoutDetails::totalVolumeGrams)
    val previousVolumeGrams = previous.sumOf(WorkoutDetails::totalVolumeGrams)
    val durationMinutes = current.sumOf(WorkoutDetails::durationMinutes)
    val recordsByWorkout = personalRecordsByWorkout()
    val exerciseStats = current
        .flatMap { workout ->
            workout.exercises.map { exercise -> workout.session.id.value to exercise }
        }
        .groupBy { (_, exercise) ->
            exercise.sessionExercise.exerciseId?.value
                ?: exercise.sessionExercise.exerciseNameSnapshot
        }
        .values
        .map { occurrences ->
            val firstExercise = occurrences.first().second
            val completedSets = occurrences.flatMap { it.second.sets }
                .filter { it.isCompleted && it.repetitions.count > 0 }
            EvolutionExerciseUiModel(
                exerciseId = firstExercise.sessionExercise.exerciseId?.value,
                name = firstExercise.sessionExercise.exerciseNameSnapshot,
                workoutCount = occurrences.map { it.first }.distinct().size,
                completedSets = completedSets.size,
                totalVolume = completedSets.sumOf {
                    it.weight.grams * it.repetitions.count
                }.gramsIn(settings.weightUnit),
            )
        }
        .sortedWith(
            compareByDescending<EvolutionExerciseUiModel> { it.completedSets }
                .thenByDescending { it.totalVolume },
        )
        .take(5)
    val completedByMuscle = current
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
    val workoutDates = current.map {
        it.session.startedAt.atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val frequencyCounts = workoutDates.groupingBy { it.dayOfWeek }.eachCount()
    val maxFrequency = frequencyCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    return EvolutionUiState(
        isLoading = false,
        period = period,
        weightUnit = settings.weightUnit,
        workoutCount = current.size,
        completedSets = current.sumOf(WorkoutDetails::completedSetCount),
        totalVolume = currentVolumeGrams.gramsIn(settings.weightUnit),
        durationMinutes = durationMinutes,
        personalRecordCount = current.sumOf {
            recordsByWorkout[it.session.id.value] ?: 0
        },
        workoutChangePercent = percentageChange(
            current = current.size.toDouble(),
            previous = previous.size.toDouble(),
        ),
        volumeChangePercent = percentageChange(
            current = currentVolumeGrams.toDouble(),
            previous = previousVolumeGrams.toDouble(),
        ),
        averageWorkoutsPerWeek = current.size / (period.days / 7.0),
        averageDurationMinutes = if (current.isEmpty()) 0 else durationMinutes / current.size,
        activeDays = workoutDates.distinct().size,
        volumeChart = current.volumeChart(
            period = period,
            settings = settings,
            now = now,
        ),
        frequency = DayOfWeek.entries.map { day ->
            val count = frequencyCounts[day] ?: 0
            EvolutionFrequencyUiModel(
                label = day.shortLabel(),
                workoutCount = count,
                share = count.toFloat() / maxFrequency,
            )
        },
        muscleDistribution = completedByMuscle.entries
            .sortedByDescending(Map.Entry<*, Int>::value)
            .map { (muscle, count) ->
                EvolutionMuscleUiModel(
                    muscleGroup = muscle,
                    completedSets = count,
                    share = count.toFloat() / totalMuscleSets,
                )
            },
        topExercises = exerciseStats,
    )
}

private fun List<WorkoutDetails>.volumeChart(
    period: EvolutionPeriod,
    settings: UserSettings,
    now: Instant,
): List<EvolutionChartPointUiModel> {
    val bucketCount = when (period) {
        EvolutionPeriod.THIRTY_DAYS -> 6
        EvolutionPeriod.NINETY_DAYS -> 9
        EvolutionPeriod.ONE_YEAR -> 12
    }
    val bucketSizeDays = (period.days + bucketCount - 1) / bucketCount
    val zone = ZoneId.systemDefault()
    val startDate = now.atZone(zone).toLocalDate().minusDays(period.days - 1)
    val volumeByBucket = DoubleArray(bucketCount)
    forEach { workout ->
        val workoutDate = workout.session.startedAt.atZone(zone).toLocalDate()
        val dayOffset = ChronoUnit.DAYS.between(startDate, workoutDate)
        val index = (dayOffset / bucketSizeDays).toInt()
        if (index in volumeByBucket.indices) {
            volumeByBucket[index] += workout.totalVolumeGrams.gramsIn(settings.weightUnit)
        }
    }
    return volumeByBucket.mapIndexed { index, volume ->
        val bucketStart = startDate.plusDays(index * bucketSizeDays)
        EvolutionChartPointUiModel(
            label = CHART_DATE_FORMATTER.format(bucketStart),
            value = volume,
        )
    }
}

private fun List<WorkoutDetails>.personalRecordsByWorkout(): Map<String, Int> {
    val previousByExercise = mutableMapOf<String, MutableList<WorkoutSet>>()
    val result = mutableMapOf<String, Int>()
    sortedBy { it.session.startedAt }.forEach { workout ->
        var workoutRecordCount = 0
        workout.exercises.forEach { exercise ->
            val exerciseKey = exercise.sessionExercise.exerciseId?.value
                ?: exercise.sessionExercise.exerciseNameSnapshot
            val previous = previousByExercise.getOrPut(exerciseKey) { mutableListOf() }
            exercise.sets
                .filter { it.isCompleted && it.repetitions.count > 0 }
                .sortedBy(WorkoutSet::position)
                .forEach { set ->
                    workoutRecordCount += set.personalRecordsAgainst(previous).size
                    previous += set
                }
        }
        result[workout.session.id.value] = workoutRecordCount
    }
    return result
}

private fun WorkoutDetails.durationMinutes(): Long =
    session.finishedAt
        ?.let { Duration.between(session.startedAt, it).toMinutes().coerceAtLeast(0) }
        ?: 0

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "SEG"
    DayOfWeek.TUESDAY -> "TER"
    DayOfWeek.WEDNESDAY -> "QUA"
    DayOfWeek.THURSDAY -> "QUI"
    DayOfWeek.FRIDAY -> "SEX"
    DayOfWeek.SATURDAY -> "SÁB"
    DayOfWeek.SUNDAY -> "DOM"
}

private val CHART_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "dd/MM",
    Locale.forLanguageTag("pt-BR"),
)
