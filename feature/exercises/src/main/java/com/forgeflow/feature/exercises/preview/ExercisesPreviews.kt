package com.forgeflow.feature.exercises.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.feature.exercises.presentation.ExerciseUiModel
import com.forgeflow.feature.exercises.presentation.ExercisesScreen
import com.forgeflow.feature.exercises.presentation.ExercisesUiState

@Preview(showBackground = true)
@Composable
private fun ExerciseListPreview() {
    ForgeFlowTheme {
        ExercisesScreen(
            state = ExercisesUiState(
                isLoading = false,
                exercises = listOf(
                    ExerciseUiModel(
                        id = "04f35c8f-e525-469e-8238-25e31087e07a",
                        name = "Supino com barra",
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        equipment = Equipment.BARBELL,
                    ),
                ),
            ),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseEmptyPreview() {
    ForgeFlowTheme {
        ExercisesScreen(
            state = ExercisesUiState(isLoading = false),
            onAction = {},
            onBack = {},
        )
    }
}
