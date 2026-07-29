package com.forgeflow.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowErrorState
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.workout.R

@Composable
fun ActiveWorkoutScreen(
    state: ActiveWorkoutUiState,
    onAction: (ActiveWorkoutAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            state.workout?.let {
                WorkoutBottomActions(onAction = onAction)
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.active_workout_loading),
            )
            state.error -> ForgeFlowErrorState(
                title = stringResource(R.string.active_workout_error_title),
                message = stringResource(R.string.active_workout_error_message),
                retryLabel = stringResource(R.string.navigate_back),
                onRetry = onBack,
            )
            state.workout == null -> ForgeFlowEmptyState(
                title = stringResource(R.string.active_workout_unavailable_title),
                message = stringResource(R.string.active_workout_unavailable_message),
                modifier = Modifier.padding(innerPadding),
            )
            else -> WorkoutContent(
                workout = state.workout,
                onAction = onAction,
                onBack = onBack,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun WorkoutContent(
    workout: ActiveWorkoutUiModel,
    onAction: (ActiveWorkoutAction) -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.medium,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.small,
            end = ForgeFlowDesign.spacing.medium,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    ForgeFlowEyebrow(text = stringResource(R.string.workout_in_progress))
                    Text(text = workout.name, style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = workout.elapsedSeconds.asClock(),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.set_progress,
                            workout.completedSets,
                            workout.totalSets,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        items(workout.exercises, key = ActiveExerciseUiModel::id) { exercise ->
            ActiveExerciseCard(exercise = exercise, onAction = onAction)
        }
    }
}

@Composable
private fun WorkoutBottomActions(onAction: (ActiveWorkoutAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ForgeFlowDesign.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        ForgeFlowOutlinedButton(
            text = stringResource(R.string.discard_workout),
            onClick = { onAction(ActiveWorkoutAction.Discard) },
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.DeleteOutline,
            iconContentDescription = null,
        )
        ForgeFlowButton(
            text = stringResource(R.string.finish_workout),
            onClick = { onAction(ActiveWorkoutAction.Finish) },
            modifier = Modifier.weight(1f),
        )
    }
}

private fun Long.asClock(): String {
    val hours = this / 3_600
    val minutes = (this % 3_600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
