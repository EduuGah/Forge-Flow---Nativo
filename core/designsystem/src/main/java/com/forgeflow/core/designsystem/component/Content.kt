package com.forgeflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign

@Composable
fun ForgeFlowEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    StateContainer(modifier) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = message,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun ForgeFlowLoadingState(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    StateContainer(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ForgeFlowErrorState(
    title: String,
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateContainer(modifier) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = message,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        ForgeFlowOutlinedButton(
            text = retryLabel,
            onClick = onRetry,
        )
    }
}

@Composable
fun ForgeFlowSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun StateContainer(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ForgeFlowDesign.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
    ) {
        content()
    }
}
