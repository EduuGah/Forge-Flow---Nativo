package com.forgeflow.feature.history.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.WeightUnit

@Immutable
data class HistoryUiState(
    val isLoading: Boolean = true,
    val workouts: List<HistoryWorkoutUiModel> = emptyList(),
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,
    val totalDurationMinutes: Long = 0,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val error: Boolean = false,
)

@Immutable
data class HistoryWorkoutUiModel(
    val id: String,
    val name: String,
    val day: String,
    val month: String,
    val time: String,
    val monthGroup: String,
    val durationMinutes: Long,
    val volume: Double,
    val exerciseCount: Int,
    val completedSets: Int,
    val hasLocation: Boolean,
    val locationLabel: String?,
    val exercises: List<HistoryExerciseUiModel>,
)

@Immutable
data class HistoryExerciseUiModel(
    val name: String,
    val completedSets: Int,
    val bestWeight: Double?,
    val bestRepetitions: Int?,
)
