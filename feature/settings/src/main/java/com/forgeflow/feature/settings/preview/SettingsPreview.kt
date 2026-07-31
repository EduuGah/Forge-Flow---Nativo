package com.forgeflow.feature.settings.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.settings.presentation.SettingsScreen
import com.forgeflow.feature.settings.presentation.SettingsUiState

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ForgeFlowTheme {
        SettingsScreen(
            state = SettingsUiState(),
            onAction = {},
            onRequestHealthPermissions = {},
            onSelectWorkoutCsv = {},
            onSelectMeasurementCsv = {},
        )
    }
}
