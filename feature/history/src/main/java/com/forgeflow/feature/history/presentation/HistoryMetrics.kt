package com.forgeflow.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.history.R

@Composable
internal fun HistoryMetricGrid(state: HistoryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_workouts),
                value = state.workouts.size.toString(),
                helper = stringResource(R.string.metric_finished),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_sets),
                value = state.totalSets.toString(),
                helper = stringResource(R.string.metric_registered),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_volume),
                value = state.totalVolume.asDisplayValue(),
                helper = state.weightUnit.symbol,
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_time),
                value = state.totalDurationMinutes.asDuration(),
                helper = stringResource(R.string.metric_training_time),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
