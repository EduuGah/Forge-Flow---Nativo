package com.forgeflow.feature.home.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val routineCount: Int = 0,
    val workoutsLastSevenDays: Int = 0,
    val totalVolumeKg: Long = 0,
    val latestWorkoutName: String? = null,
    val activeWorkout: HomeActiveWorkoutUiModel? = null,
)

@Immutable
data class HomeActiveWorkoutUiModel(
    val name: String,
    val completedSets: Int,
    val totalSets: Int,
)
