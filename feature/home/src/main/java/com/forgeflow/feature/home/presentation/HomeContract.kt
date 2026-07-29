package com.forgeflow.feature.home.presentation

import androidx.compose.runtime.Immutable
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
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val latestWorkout: HomeWorkoutSummaryUiModel? = null,
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
    val name: String,
    val date: String,
    val durationMinutes: Long,
    val exerciseCount: Int,
    val completedSets: Int,
    val volume: Double,
)
