package com.forgeflow.feature.history.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.history.presentation.HistoryScreen

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    ForgeFlowTheme {
        HistoryScreen()
    }
}
