package com.forgeflow.feature.workout.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.workout.presentation.ActiveWorkoutScreen

@Preview(showBackground = true)
@Composable
private fun ActiveWorkoutScreenPreview() {
    ForgeFlowTheme {
        ActiveWorkoutScreen(onBack = {})
    }
}
