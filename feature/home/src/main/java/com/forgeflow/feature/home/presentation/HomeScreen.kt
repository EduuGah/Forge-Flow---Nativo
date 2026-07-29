package com.forgeflow.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.home.R

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.home_loading),
            )
        } else {
            HomeContent(
                state = state,
                onOpenExercises = onOpenExercises,
                onOpenRoutines = onOpenRoutines,
                onOpenActiveWorkout = onOpenActiveWorkout,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
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
        item { DashboardHero(state = state) }
        item {
            state.activeWorkout?.let { active ->
                ActiveWorkoutCard(active = active, onOpen = onOpenActiveWorkout)
            } ?: NextActionCard(onOpenRoutines = onOpenRoutines)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                ForgeFlowMetric(
                    label = stringResource(R.string.metric_routines),
                    value = state.routineCount.toString(),
                    helper = stringResource(R.string.metric_saved),
                    modifier = Modifier.weight(1f),
                )
                ForgeFlowMetric(
                    label = stringResource(R.string.metric_week),
                    value = state.workoutsLastSevenDays.toString(),
                    helper = stringResource(R.string.metric_workouts),
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
        item {
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.open_exercise_library),
                onClick = onOpenExercises,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.FitnessCenter,
                iconContentDescription = null,
            )
        }
    }
}

@Composable
private fun DashboardHero(state: HomeUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large,
            )
            .padding(ForgeFlowDesign.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        ForgeFlowEyebrow(text = stringResource(R.string.home_eyebrow))
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = state.latestWorkoutName?.let {
                stringResource(R.string.last_workout, it)
            } ?: stringResource(R.string.first_workout_message),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ActiveWorkoutCard(
    active: HomeActiveWorkoutUiModel,
    onOpen: () -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.workout_in_progress))
        Text(text = active.name, style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(
                R.string.active_progress,
                active.completedSets,
                active.totalSets,
            ),
            color = ForgeFlowDesign.colors.textSecondary,
        )
        ForgeFlowButton(
            text = stringResource(R.string.continue_workout),
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PlayArrow,
            iconContentDescription = null,
        )
    }
}

@Composable
private fun NextActionCard(onOpenRoutines: () -> Unit) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.next_action))
        Text(
            text = stringResource(R.string.create_routine_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.create_routine_message),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        ForgeFlowButton(
            text = stringResource(R.string.open_routines),
            onClick = onOpenRoutines,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.ArrowForward,
            iconContentDescription = null,
        )
    }
}
