package com.forgeflow.feature.exercises.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowErrorState
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.exercises.R

@Composable
fun ExercisesScreen(
    state: ExercisesUiState,
    onAction: (ExercisesAction) -> Unit,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.medium,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
                end = ForgeFlowDesign.spacing.medium,
                bottom = innerPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.medium,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.exercises_eyebrow),
                    title = stringResource(R.string.exercises_title),
                    description = stringResource(R.string.exercises_description),
                    actions = {
                        ExerciseHeaderActions(
                            onBack = onBack,
                            onCreate = { onAction(ExercisesAction.CreateExercise) },
                        )
                    },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                ) {
                    ForgeFlowMetric(
                        label = stringResource(R.string.metric_total),
                        value = state.totalCount.toString(),
                        helper = stringResource(R.string.metric_library),
                        modifier = Modifier.weight(1f),
                    )
                    ForgeFlowMetric(
                        label = stringResource(R.string.metric_found),
                        value = state.exercises.size.toString(),
                        helper = stringResource(R.string.metric_filtered),
                        modifier = Modifier.weight(1f),
                    )
                    ForgeFlowMetric(
                        label = stringResource(R.string.metric_custom),
                        value = state.customCount.toString(),
                        helper = stringResource(R.string.metric_created),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                ExerciseFilters(state = state, onAction = onAction)
            }
            when {
                state.isLoading -> item {
                    ForgeFlowLoadingState(
                        contentDescription = stringResource(R.string.exercises_loading),
                    )
                }
                state.error == ExercisesError.LOAD_FAILED -> item {
                    ForgeFlowErrorState(
                        title = stringResource(R.string.exercises_error_title),
                        message = stringResource(R.string.exercises_error_message),
                        retryLabel = stringResource(R.string.retry),
                        onRetry = { onAction(ExercisesAction.Retry) },
                    )
                }
                state.exercises.isEmpty() -> item {
                    ForgeFlowEmptyState(
                        title = stringResource(R.string.exercises_empty_title),
                        message = stringResource(R.string.exercises_search_empty_message),
                    )
                }
                else -> items(state.exercises, key = ExerciseUiModel::id) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        onAction = onAction,
                        onOpenExercise = onOpenExercise,
                    )
                }
            }
        }
    }
    state.editor?.let { editor ->
        ExerciseEditorSheet(
            editor = editor,
            isSaving = state.isSaving,
            onAction = onAction,
        )
    }
}
