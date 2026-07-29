package com.forgeflow.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowLocationMap
import com.forgeflow.core.designsystem.component.ForgeFlowMapPoint
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.history.R

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onOpenExercise: (String) -> Unit,
    onAction: (HistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.history_loading),
            )
        } else {
            HistoryContent(
                state = state,
                contentPadding = innerPadding,
                onOpenExercise = onOpenExercise,
                onAction = onAction,
            )
        }
    }
    state.pendingDeleteWorkout?.let { workout ->
        DeleteHistoryDialog(
            workout = workout,
            isDeleting = state.isDeleting,
            onConfirm = { onAction(HistoryAction.DeleteConfirmed) },
            onDismiss = { onAction(HistoryAction.DeleteDismissed) },
        )
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onOpenExercise: (String) -> Unit,
    onAction: (HistoryAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.screenHorizontal,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.large,
            end = ForgeFlowDesign.spacing.screenHorizontal,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
    ) {
        item {
            ForgeFlowPageHeader(
                eyebrow = stringResource(R.string.history_eyebrow),
                title = stringResource(R.string.history_title),
                description = stringResource(R.string.history_description),
            )
        }
        item {
            HistoryMetricGrid(state = state)
        }
        item {
            HistoryFilters(state = state, onAction = onAction)
        }
        if (state.mapPoints.isNotEmpty()) {
            item {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(
                        ForgeFlowDesign.spacing.small,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.training_map_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.training_map_description,
                            state.mapPoints.size,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ForgeFlowLocationMap(
                        points = state.mapPoints.map {
                            ForgeFlowMapPoint(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                label = it.label,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                    )
                }
            }
        }
        if (state.workouts.isEmpty()) {
            item {
                ForgeFlowEmptyState(
                    title = if (state.error) {
                        stringResource(R.string.history_error_title)
                    } else {
                        if (
                            state.searchQuery.isNotBlank() ||
                            state.dateFilter != HistoryDateFilter.ALL ||
                            state.onlyWithLocation
                        ) {
                            stringResource(R.string.history_filter_empty_title)
                        } else {
                            stringResource(R.string.history_empty_title)
                        }
                    },
                    message = if (state.error) {
                        stringResource(R.string.history_error_message)
                    } else {
                        if (
                            state.searchQuery.isNotBlank() ||
                            state.dateFilter != HistoryDateFilter.ALL ||
                            state.onlyWithLocation
                        ) {
                            stringResource(R.string.history_filter_empty_message)
                        } else {
                            stringResource(R.string.history_empty_message)
                        }
                    },
                )
            }
        } else {
            itemsIndexed(
                items = state.workouts,
                key = { _, workout -> workout.id },
            ) { index, workout ->
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                ) {
                    val previousMonth = state.workouts.getOrNull(index - 1)?.monthGroup
                    if (workout.monthGroup != previousMonth) {
                        Text(
                            text = workout.monthGroup.replaceFirstChar(Char::uppercase),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    HistoryWorkoutCard(
                        workout = workout,
                        weightUnit = state.weightUnit,
                        expandedByDefault = index == 0,
                        onOpenExercise = onOpenExercise,
                        onDelete = {
                            onAction(HistoryAction.DeleteRequested(workout.id))
                        },
                    )
                }
            }
        }
    }
}
