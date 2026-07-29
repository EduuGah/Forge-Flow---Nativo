package com.forgeflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign

@Composable
fun ForgeFlowMetric(
    label: String,
    value: String,
    helper: String,
    modifier: Modifier = Modifier,
) {
    ForgeFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
        ) {
            ForgeFlowEyebrow(text = label)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = helper,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
