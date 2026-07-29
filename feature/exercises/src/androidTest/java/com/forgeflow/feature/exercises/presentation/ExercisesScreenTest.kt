package com.forgeflow.feature.exercises.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.feature.exercises.R
import org.junit.Rule
import org.junit.Test

class ExercisesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_isDisplayed() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val expectedTitle = context.getString(R.string.exercises_empty_title)

        composeRule.setContent {
            ForgeFlowTheme {
                ExercisesScreen(
                    state = ExercisesUiState(isLoading = false),
                    onAction = {},
                    onBack = {},
                    onOpenExercise = {},
                )
            }
        }

        composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    @Test
    fun exerciseList_displaysExerciseName() {
        val exerciseName = "Supino com barra"

        composeRule.setContent {
            ForgeFlowTheme {
                ExercisesScreen(
                    state = ExercisesUiState(
                        isLoading = false,
                        exercises = listOf(
                            ExerciseUiModel(
                                id = "04f35c8f-e525-469e-8238-25e31087e07a",
                                name = exerciseName,
                                primaryMuscleGroup = MuscleGroup.CHEST,
                                equipment = Equipment.BARBELL,
                            ),
                        ),
                    ),
                    onAction = {},
                    onBack = {},
                    onOpenExercise = {},
                )
            }
        }

        composeRule.onNodeWithText(exerciseName).assertIsDisplayed()
    }
}
