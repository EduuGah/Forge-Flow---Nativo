package com.forgeflow.feature.routines.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onOpenExercise: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingRoutineDeletion by remember { mutableStateOf<RoutineUiModel?>(null) }
    var pendingFolderDeletion by remember { mutableStateOf<RoutineFolderUiModel?>(null) }
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
                onDeleteRoutine = { pendingRoutineDeletion = it },
                onDeleteFolder = { pendingFolderDeletion = it },
                contentPadding = innerPadding,
            )
        }
    }
    state.editor?.let { editor ->
        RoutineEditorSheet(
            editor = editor,
            folders = state.folders,
            exercises = state.exercises,
            isSaving = state.isSaving,
            onAction = onAction,
            onOpenExercise = onOpenExercise,
        )
    }
    state.folderEditor?.let { editor ->
        RoutineFolderEditorDialog(editor = editor, onAction = onAction)
    }
    pendingRoutineDeletion?.let { routine ->
        ConfirmRoutineDeletionDialog(
            routineName = routine.name,
            onConfirm = {
                pendingRoutineDeletion = null
                onAction(RoutinesAction.ArchiveRoutine(routine.id))
            },
            onDismiss = { pendingRoutineDeletion = null },
        )
    }
    pendingFolderDeletion?.let { folder ->
        ConfirmFolderDeletionDialog(
            folderName = folder.name,
            onConfirm = {
                pendingFolderDeletion = null
                onAction(RoutinesAction.DeleteFolder(folder.id))
            },
            onDismiss = { pendingFolderDeletion = null },
        )
    }
}

@Composable
private fun RoutinesContent(
    state: RoutinesUiState,
    onAction: (RoutinesAction) -> Unit,
    onOpenExercises: () -> Unit,
    onDeleteRoutine: (RoutineUiModel) -> Unit,
    onDeleteFolder: (RoutineFolderUiModel) -> Unit,
    contentPadding: PaddingValues,
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
                eyebrow = stringResource(R.string.routines_eyebrow),
                title = stringResource(R.string.routines_title),
                description = stringResource(R.string.routines_description),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                ForgeFlowButton(
                    text = stringResource(R.string.new_routine),
                    onClick = { onAction(RoutinesAction.CreateRoutine) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Add,
                    iconContentDescription = null,
                )
                ForgeFlowOutlinedButton(
                    text = stringResource(R.string.new_folder),
                    onClick = { onAction(RoutinesAction.CreateFolder) },
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
            val unfiled = state.routines.filter { it.folderId == null }
            if (unfiled.isNotEmpty()) {
                item(key = "unfiled") {
                    RoutineFolderSection(
                        folder = null,
                        routines = unfiled,
                        onAction = onAction,
                        onDeleteRoutine = onDeleteRoutine,
                        onDeleteFolder = onDeleteFolder,
                    )
                }
            }
            items(state.folders, key = RoutineFolderUiModel::id) { folder ->
                RoutineFolderSection(
                    folder = folder,
                    routines = state.routines.filter { it.folderId == folder.id },
                    onAction = onAction,
                    onDeleteRoutine = onDeleteRoutine,
                    onDeleteFolder = onDeleteFolder,
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
