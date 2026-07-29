package com.forgeflow.feature.routines.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.routines.presentation.RoutinesScreen
import com.forgeflow.feature.routines.presentation.RoutinesUiState

@Preview(showBackground = true)
@Composable
private fun RoutinesScreenPreview() {
    ForgeFlowTheme {
        RoutinesScreen(
            state = RoutinesUiState(isLoading = false),
            onAction = {},
            onOpenExercises = {},
            onOpenExercise = {},
        )
    }
}
