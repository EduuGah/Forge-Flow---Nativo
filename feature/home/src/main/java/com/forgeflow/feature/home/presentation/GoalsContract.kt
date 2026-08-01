package com.forgeflow.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.GoalCadence
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PerformanceGoalType
import com.forgeflow.core.model.WeightUnit

enum class GoalsFilter {
    ACTIVE,
    ALL,
    COMPLETED,
}

@Immutable
data class GoalsUiState(
    val isLoading: Boolean = true,
    val hasHistoryError: Boolean = false,
    val filter: GoalsFilter = GoalsFilter.ACTIVE,
    val goals: List<GoalUiModel> = emptyList(),
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0,
    val editor: GoalEditorUiState? = null,
    val exercises: List<GoalExerciseUiModel> = emptyList(),
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val writeFailed: Boolean = false,
)

@Immutable
data class GoalUiModel(
    val id: String,
    val title: String,
    val type: PerformanceGoalType,
    val cadence: GoalCadence,
    val progress: Float,
    val progressLabel: String,
    val cadenceLabel: String,
    val deadlineLabel: String?,
    val exerciseMediaUri: String?,
    val isCompleted: Boolean,
    val isOverdue: Boolean,
)

@Immutable
data class GoalExerciseUiModel(
    val id: String,
    val name: String,
    val searchTerms: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val mediaUri: String?,
)

@Immutable
data class GoalEditorUiState(
    val id: String? = null,
    val type: PerformanceGoalType = PerformanceGoalType.EXERCISE_WEIGHT,
    val cadence: GoalCadence = GoalCadence.ONCE,
    val title: String = "",
    val target: String = "",
    val exerciseId: String? = null,
    val exerciseName: String? = null,
    val minimumRepetitions: String = "1",
    val hasDeadline: Boolean = false,
    val deadlineEpochDay: Long = 0L,
    val isSaving: Boolean = false,
    val validationFailed: Boolean = false,
)

sealed interface GoalsAction {
    data class FilterChanged(val filter: GoalsFilter) : GoalsAction
    data class OpenEditor(val goalId: String? = null) : GoalsAction
    data object CloseEditor : GoalsAction
    data class TypeChanged(val type: PerformanceGoalType) : GoalsAction
    data class CadenceChanged(val cadence: GoalCadence) : GoalsAction
    data class TitleChanged(val value: String) : GoalsAction
    data class TargetChanged(val value: String) : GoalsAction
    data class MinimumRepetitionsChanged(val value: String) : GoalsAction
    data class ExerciseSelected(val exerciseId: String) : GoalsAction
    data class DeadlineEnabledChanged(val enabled: Boolean) : GoalsAction
    data class DeadlineChanged(val epochDay: Long) : GoalsAction
    data object Save : GoalsAction
    data class Delete(val goalId: String) : GoalsAction
    data object DismissWriteError : GoalsAction
}
