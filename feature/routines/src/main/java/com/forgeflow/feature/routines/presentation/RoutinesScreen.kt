package com.forgeflow.feature.routines.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowErrorState
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.routines.R

@Composable
fun RoutinesScreen(
    state: RoutinesUiState,
    onAction: (RoutinesAction) -> Unit,
    onOpenExercises: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        when {
            state.isLoading -> ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.routines_loading),
            )
            state.error == RoutinesError.LOAD_FAILED -> ForgeFlowErrorState(
                title = stringResource(R.string.routines_error_title),
                message = stringResource(R.string.routines_error_message),
                retryLabel = stringResource(R.string.dismiss),
                onRetry = { onAction(RoutinesAction.DismissError) },
            )
            else -> RoutinesContent(
                state = state,
                onAction = onAction,
                onOpenExercises = onOpenExercises,
                contentPadding = innerPadding,
            )
        }
    }
    state.editor?.let { editor ->
        RoutineEditorSheet(
            editor = editor,
            exercises = state.exercises,
            isSaving = state.isSaving,
            onAction = onAction,
        )
    }
}

@Composable
private fun RoutinesContent(
    state: RoutinesUiState,
    onAction: (RoutinesAction) -> Unit,
    onOpenExercises: () -> Unit,
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
                eyebrow = stringResource(R.string.routines_eyebrow),
                title = stringResource(R.string.routines_title),
                description = stringResource(R.string.routines_description),
            )
        }
        item {
            ForgeFlowButton(
                text = stringResource(R.string.new_routine),
                onClick = { onAction(RoutinesAction.CreateRoutine) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.Add,
                iconContentDescription = null,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                ForgeFlowMetric(
                    label = stringResource(R.string.metric_routines),
                    value = state.routines.size.toString(),
                    helper = stringResource(R.string.metric_saved),
                    modifier = Modifier.weight(1f),
                )
                ForgeFlowMetric(
                    label = stringResource(R.string.metric_exercises),
                    value = state.exercises.size.toString(),
                    helper = stringResource(R.string.metric_library),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (state.routines.isEmpty()) {
            item {
                ForgeFlowEmptyState(
                    title = stringResource(R.string.routines_empty_title),
                    message = stringResource(R.string.routines_empty_message),
                )
            }
        } else {
            items(state.routines, key = RoutineUiModel::id) { routine ->
                RoutineCard(routine = routine, onAction = onAction)
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
