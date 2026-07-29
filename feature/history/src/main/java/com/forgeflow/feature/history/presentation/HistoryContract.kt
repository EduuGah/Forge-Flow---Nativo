package com.forgeflow.feature.history.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutSetType

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
    val exerciseId: String?,
    val name: String,
    val mediaUri: String?,
    val mediaType: ExerciseMediaType?,
    val mediaThumbnailUri: String?,
    val completedSets: Int,
    val totalVolume: Double,
    val bestWeight: Double?,
    val bestRepetitions: Int?,
    val personalRecordCount: Int,
    val sets: List<HistorySetUiModel>,
)

@Immutable
data class HistorySetUiModel(
    val id: String,
    val number: Int,
    val type: WorkoutSetType,
    val weight: Double,
    val repetitions: Int,
    val isPersonalRecord: Boolean,
)
