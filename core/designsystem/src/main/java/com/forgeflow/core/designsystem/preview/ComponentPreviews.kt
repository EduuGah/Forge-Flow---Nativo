package com.forgeflow.core.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.forgeflow.core.designsystem.R
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    ForgeFlowTheme {
        ForgeFlowButton(
            text = stringResource(R.string.preview_action),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    ForgeFlowTheme {
        ForgeFlowEmptyState(
            title = stringResource(R.string.preview_empty_title),
            message = stringResource(R.string.preview_empty_message),
        )
    }
}
