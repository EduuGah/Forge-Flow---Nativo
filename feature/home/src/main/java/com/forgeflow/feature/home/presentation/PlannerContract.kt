package com.forgeflow.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.TrainingDay

@Immutable
data class PlannerUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val monthLabel: String = "",
    val firstDayOffset: Int = 0,
    val calendarDays: List<PlannerCalendarDayUiModel> = emptyList(),
    val selectedDateLabel: String = "",
    val selectedDayWorkouts: List<PlannerWorkoutUiModel> = emptyList(),
    val isSelectedDayScheduled: Boolean = false,
    val weeklyWorkoutGoal: Int = 3,
    val currentWeekWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val trainingDays: Set<TrainingDay> = emptySet(),
    val preferredWorkoutTimeMinutes: Int = 18 * 60,
    val nextScheduledWorkout: String? = null,
)

@Immutable
data class PlannerCalendarDayUiModel(
    val epochDay: Long,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isScheduled: Boolean,
    val completedWorkoutCount: Int,
)

@Immutable
data class PlannerWorkoutUiModel(
    val name: String,
    val time: String,
    val completedSets: Int,
)

sealed interface PlannerAction {
    data object PreviousMonth : PlannerAction
    data object NextMonth : PlannerAction
    data class SelectDate(val epochDay: Long) : PlannerAction
    data class WeeklyGoalChanged(val goal: Int) : PlannerAction
    data class TrainingDayToggled(val day: TrainingDay) : PlannerAction
    data class PreferredTimeChanged(val minutesFromMidnight: Int) : PlannerAction
}
