package com.forgeflow.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.WeightUnit

@Immutable
data class EvolutionUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val period: EvolutionPeriod = EvolutionPeriod.THIRTY_DAYS,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val workoutCount: Int = 0,
    val completedSets: Int = 0,
    val totalVolume: Double = 0.0,
    val durationMinutes: Long = 0,
    val personalRecordCount: Int = 0,
    val workoutChangePercent: Int? = null,
    val volumeChangePercent: Int? = null,
    val averageWorkoutsPerWeek: Double = 0.0,
    val averageDurationMinutes: Long = 0,
    val activeDays: Int = 0,
    val volumeChart: List<EvolutionChartPointUiModel> = emptyList(),
    val workoutChart: List<EvolutionChartPointUiModel> = emptyList(),
    val bodyWeightChart: List<EvolutionChartPointUiModel> = emptyList(),
    val personalRecordChart: List<EvolutionChartPointUiModel> = emptyList(),
    val frequency: List<EvolutionFrequencyUiModel> = emptyList(),
    val muscleDistribution: List<EvolutionMuscleUiModel> = emptyList(),
    val topExercises: List<EvolutionExerciseUiModel> = emptyList(),
)

enum class EvolutionPeriod(val days: Long) {
    THIRTY_DAYS(30),
    NINETY_DAYS(90),
    ONE_YEAR(365),
}

enum class EvolutionMetric {
    WORKOUTS,
    VOLUME,
    BODY_WEIGHT,
    PERSONAL_RECORDS,
}

@Immutable
data class EvolutionChartPointUiModel(
    val label: String,
    val value: Double,
)

@Immutable
data class EvolutionFrequencyUiModel(
    val label: String,
    val workoutCount: Int,
    val share: Float,
)

@Immutable
data class EvolutionMuscleUiModel(
    val muscleGroup: MuscleGroup,
    val completedSets: Int,
    val share: Float,
)

@Immutable
data class EvolutionExerciseUiModel(
    val exerciseId: String?,
    val name: String,
    val workoutCount: Int,
    val completedSets: Int,
    val totalVolume: Double,
)

sealed interface EvolutionAction {
    data class PeriodChanged(val period: EvolutionPeriod) : EvolutionAction
}
