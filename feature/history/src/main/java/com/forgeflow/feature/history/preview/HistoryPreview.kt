package com.forgeflow.feature.history.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.history.presentation.HistoryScreen
import com.forgeflow.feature.history.presentation.HistoryUiState

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    ForgeFlowTheme {
        HistoryScreen(
            state = HistoryUiState(isLoading = false),
            onOpenExercise = {},
            onOpenTrainingMap = {},
            onAction = {},
        )
    }
}
