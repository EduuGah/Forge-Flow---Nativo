package com.forgeflow.feature.home.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.home.presentation.HomeScreen

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ForgeFlowTheme {
        HomeScreen(onOpenExercises = {})
    }
}
