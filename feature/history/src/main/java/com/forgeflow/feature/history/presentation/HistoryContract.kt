package com.forgeflow.feature.history.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class HistoryUiState(
    val isLoading: Boolean = true,
    val workouts: List<HistoryWorkoutUiModel> = emptyList(),
    val totalSets: Int = 0,
    val totalVolumeKg: Long = 0,
    val error: Boolean = false,
)

@Immutable
data class HistoryWorkoutUiModel(
    val id: String,
    val name: String,
    val date: String,
    val duration: String,
    val volumeKg: Long,
    val exerciseCount: Int,
    val completedSets: Int,
    val exercises: List<HistoryExerciseUiModel>,
)

@Immutable
data class HistoryExerciseUiModel(
    val name: String,
    val summary: String,
)
