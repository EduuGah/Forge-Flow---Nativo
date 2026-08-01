package com.forgeflow.feature.workout.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.workout.presentation.ActiveWorkoutScreen
import com.forgeflow.feature.workout.presentation.ActiveWorkoutUiState

@Preview(showBackground = true)
@Composable
private fun ActiveWorkoutScreenPreview() {
    ForgeFlowTheme {
        ActiveWorkoutScreen(
            state = ActiveWorkoutUiState(isLoading = false),
            onAction = {},
            onBack = {},
            onOpenExercise = {},
        )
    }
}
