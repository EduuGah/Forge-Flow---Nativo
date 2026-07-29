package com.forgeflow.feature.routines.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.feature.routines.R

@Composable
internal fun RoutineFolderEditorDialog(
    editor: RoutineFolderEditorUiState,
    onAction: (RoutinesAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(RoutinesAction.CloseFolderEditor) },
        title = {
            Text(
                stringResource(
                    if (editor.id == null) R.string.create_folder else R.string.rename_folder,
                ),
            )
        },
        text = {
            ForgeFlowTextField(
                value = editor.name,
                onValueChange = { onAction(RoutinesAction.FolderNameChanged(it)) },
                label = stringResource(R.string.folder_name),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(RoutinesAction.SaveFolder) },
                enabled = editor.name.isNotBlank(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(RoutinesAction.CloseFolderEditor) }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
internal fun ConfirmRoutineDeletionDialog(
    routineName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDeletionDialog(
        title = stringResource(R.string.delete_routine_title),
        message = stringResource(R.string.delete_routine_message, routineName),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun ConfirmFolderDeletionDialog(
    folderName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDeletionDialog(
        title = stringResource(R.string.delete_folder_title),
        message = stringResource(R.string.delete_folder_message, folderName),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ConfirmDeletionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
