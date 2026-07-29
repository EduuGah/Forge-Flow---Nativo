package com.forgeflow.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.core.model.WeightUnit

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val routineCount: Int = 0,
    val workoutsLastSevenDays: Int = 0,
    val totalWorkoutCount: Int = 0,
    val totalCompletedSets: Int = 0,
    val totalVolume: Double = 0.0,
    val weeklyVolume: Double = 0.0,
    val totalDurationMinutes: Long = 0,
    val averageDurationMinutes: Long = 0,
    val currentStreak: Int = 0,
    val personalRecordCount: Int = 0,
    val weightRecordCount: Int = 0,
    val volumeRecordCount: Int = 0,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val latestWorkout: HomeWorkoutSummaryUiModel? = null,
    val recentWorkouts: List<HomeWorkoutSummaryUiModel> = emptyList(),
    val volumeChart: List<HomeChartPointUiModel> = emptyList(),
    val muscleDistribution: List<HomeMuscleDistributionUiModel> = emptyList(),
    val recentRecords: List<HomePersonalRecordUiModel> = emptyList(),
    val activeWorkout: HomeActiveWorkoutUiModel? = null,
)

@Immutable
data class HomeActiveWorkoutUiModel(
    val name: String,
    val completedSets: Int,
    val totalSets: Int,
)

@Immutable
data class HomeWorkoutSummaryUiModel(
    val id: String,
    val name: String,
    val date: String,
    val durationMinutes: Long,
    val exerciseCount: Int,
    val completedSets: Int,
    val volume: Double,
)

@Immutable
data class HomeChartPointUiModel(
    val label: String,
    val value: Float,
)

@Immutable
data class HomeMuscleDistributionUiModel(
    val muscleGroup: MuscleGroup,
    val completedSets: Int,
    val share: Float,
)

@Immutable
data class HomePersonalRecordUiModel(
    val type: PersonalRecordType,
    val exerciseName: String,
    val workoutName: String,
    val performance: String,
    val date: String,
)
