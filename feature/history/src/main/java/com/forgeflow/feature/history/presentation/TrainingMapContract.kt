package com.forgeflow.feature.history.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.WeightUnit

@Immutable
data class TrainingMapUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val period: TrainingMapPeriod = TrainingMapPeriod.ALL,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val places: List<TrainingPlaceUiModel> = emptyList(),
    val selectedPlaceId: String? = null,
    val workoutCount: Int = 0,
    val totalVolume: Double = 0.0,
    val totalDurationMinutes: Long = 0,
)

enum class TrainingMapPeriod(val days: Long?) {
    LAST_30_DAYS(30),
    LAST_YEAR(365),
    ALL(null),
}

@Immutable
data class TrainingPlaceUiModel(
    val id: String,
    val label: String?,
    val latitude: Double,
    val longitude: Double,
    val workoutCount: Int,
    val totalVolume: Double,
    val totalDurationMinutes: Long,
    val lastVisited: String,
    val lastVisitedEpochMillis: Long,
    val workouts: List<TrainingMapWorkoutUiModel>,
)

@Immutable
data class TrainingMapWorkoutUiModel(
    val id: String,
    val name: String,
    val date: String,
    val completedSets: Int,
    val volume: Double,
    val durationMinutes: Long,
)

sealed interface TrainingMapAction {
    data class PeriodChanged(val period: TrainingMapPeriod) : TrainingMapAction
    data class PlaceSelected(val placeId: String) : TrainingMapAction
}
