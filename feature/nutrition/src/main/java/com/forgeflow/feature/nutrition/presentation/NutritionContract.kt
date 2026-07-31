package com.forgeflow.feature.nutrition.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.NutritionMealType

@Immutable
data class NutritionUiState(
    val isLoading: Boolean = true,
    val dateLabel: String = "",
    val isToday: Boolean = true,
    val calories: Int = 0,
    val calorieGoal: Int = 2_200,
    val proteinGrams: Double = 0.0,
    val proteinGoalGrams: Double = 160.0,
    val carbohydrateGrams: Double = 0.0,
    val carbohydrateGoalGrams: Double = 250.0,
    val fatGrams: Double = 0.0,
    val fatGoalGrams: Double = 70.0,
    val meals: List<NutritionMealUiModel> = emptyList(),
    val mealEditor: NutritionMealEditorUiState? = null,
    val goalsEditor: NutritionGoalsEditorUiState? = null,
    val writeFailed: Boolean = false,
)

@Immutable
data class NutritionMealUiModel(
    val id: String,
    val name: String,
    val type: NutritionMealType,
    val typeLabel: String,
    val timeLabel: String,
    val calories: Int,
    val macrosLabel: String,
    val notes: String,
    val photoPath: String?,
)

@Immutable
data class NutritionMealEditorUiState(
    val name: String = "",
    val type: NutritionMealType = NutritionMealType.LUNCH,
    val calories: String = "",
    val proteinGrams: String = "",
    val carbohydrateGrams: String = "",
    val fatGrams: String = "",
    val notes: String = "",
    val sourcePhotoUri: String? = null,
    val isSaving: Boolean = false,
)

@Immutable
data class NutritionGoalsEditorUiState(
    val calories: String = "",
    val proteinGrams: String = "",
    val carbohydrateGrams: String = "",
    val fatGrams: String = "",
    val isSaving: Boolean = false,
)

sealed interface NutritionAction {
    data object PreviousDay : NutritionAction
    data object NextDay : NutritionAction
    data object Today : NutritionAction
    data object OpenMealEditor : NutritionAction
    data object CloseMealEditor : NutritionAction
    data class MealNameChanged(val value: String) : NutritionAction
    data class MealTypeChanged(val value: NutritionMealType) : NutritionAction
    data class MealCaloriesChanged(val value: String) : NutritionAction
    data class MealProteinChanged(val value: String) : NutritionAction
    data class MealCarbohydrateChanged(val value: String) : NutritionAction
    data class MealFatChanged(val value: String) : NutritionAction
    data class MealNotesChanged(val value: String) : NutritionAction
    data class MealPhotoSelected(val sourceUri: String?) : NutritionAction
    data object CalculateCaloriesFromMacros : NutritionAction
    data object SaveMeal : NutritionAction
    data class DeleteMeal(val mealId: String) : NutritionAction
    data object OpenGoalsEditor : NutritionAction
    data object CloseGoalsEditor : NutritionAction
    data class GoalCaloriesChanged(val value: String) : NutritionAction
    data class GoalProteinChanged(val value: String) : NutritionAction
    data class GoalCarbohydrateChanged(val value: String) : NutritionAction
    data class GoalFatChanged(val value: String) : NutritionAction
    data object SaveGoals : NutritionAction
    data object DismissError : NutritionAction
}
