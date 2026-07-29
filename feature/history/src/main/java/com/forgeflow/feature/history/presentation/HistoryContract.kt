package com.forgeflow.feature.history.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.PersonalRecordType
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
    val searchQuery: String = "",
    val dateFilter: HistoryDateFilter = HistoryDateFilter.ALL,
    val onlyWithLocation: Boolean = false,
    val mapPoints: List<HistoryMapPointUiModel> = emptyList(),
    val pendingDeleteWorkout: HistoryWorkoutUiModel? = null,
    val isDeleting: Boolean = false,
    val error: Boolean = false,
)

enum class HistoryDateFilter {
    ALL,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_90_DAYS,
}

@Immutable
data class HistoryMapPointUiModel(
    val latitude: Double,
    val longitude: Double,
    val label: String?,
)

@Immutable
data class HistoryWorkoutUiModel(
    val id: String,
    val name: String,
    val day: String,
    val month: String,
    val time: String,
    val monthGroup: String,
    val startedAtEpochMillis: Long,
    val durationMinutes: Long,
    val volume: Double,
    val exerciseCount: Int,
    val completedSets: Int,
    val hasLocation: Boolean,
    val locationLabel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val searchableDate: String,
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
    val personalRecordTypes: Set<PersonalRecordType>,
)

val HistorySetUiModel.isPersonalRecord: Boolean
    get() = personalRecordTypes.isNotEmpty()

sealed interface HistoryAction {
    data class SearchChanged(val query: String) : HistoryAction
    data class DateFilterChanged(val filter: HistoryDateFilter) : HistoryAction
    data class OnlyWithLocationChanged(val enabled: Boolean) : HistoryAction
    data class DeleteRequested(val workoutId: String) : HistoryAction
    data object DeleteDismissed : HistoryAction
    data object DeleteConfirmed : HistoryAction
}
