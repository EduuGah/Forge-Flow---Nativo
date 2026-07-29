package com.forgeflow.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowPill
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.history.R

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.history_loading),
            )
        } else {
            HistoryContent(state = state, contentPadding = innerPadding)
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.medium,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
            end = ForgeFlowDesign.spacing.medium,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
    ) {
        item {
            ForgeFlowPageHeader(
                eyebrow = stringResource(R.string.history_eyebrow),
                title = stringResource(R.string.history_title),
                description = stringResource(R.string.history_description),
            )
        }
        item {
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
                ForgeFlowMetric(
                    label = stringResource(R.string.metric_volume),
                    value = stringResource(R.string.volume_value, state.totalVolumeKg),
                    helper = stringResource(R.string.metric_total),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (state.workouts.isEmpty()) {
            item {
                ForgeFlowEmptyState(
                    title = stringResource(R.string.history_empty_title),
                    message = stringResource(R.string.history_empty_message),
                )
            }
        } else {
            items(state.workouts, key = HistoryWorkoutUiModel::id) { workout ->
                HistoryWorkoutCard(workout)
            }
        }
    }
}

@Composable
private fun HistoryWorkoutCard(workout: HistoryWorkoutUiModel) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = workout.date)
        Text(text = workout.name, style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForgeFlowPill(
                text = stringResource(R.string.exercise_count, workout.exerciseCount),
            )
            ForgeFlowPill(text = stringResource(R.string.duration_value, workout.duration))
            ForgeFlowPill(text = stringResource(R.string.volume_value, workout.volumeKg))
        }
        workout.exercises.forEach { exercise ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = if (workout.volumeKg > 0) {
                        Icons.Outlined.LocalFireDepartment
                    } else {
                        Icons.Outlined.FitnessCenter
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = exercise.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = exercise.summary,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
