package com.forgeflow.feature.routines.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowPill
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.routines.R

@Composable
internal fun RoutineBrowserControls(
    state: RoutinesUiState,
    onAction: (RoutinesAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.saved_workouts_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.saved_workouts_description,
                        state.routines.size,
                        state.routines.size,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        ForgeFlowTextField(
            value = state.searchQuery,
            onValueChange = { onAction(RoutinesAction.BrowseSearchChanged(it)) },
            label = stringResource(R.string.search_saved_routines),
            placeholder = stringResource(R.string.search_saved_routines_hint),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onAction(RoutinesAction.BrowseSearchChanged(""))
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(R.string.clear_routine_search),
                        )
                    }
                }
            },
        )
    }
}

@Composable
internal fun RoutineFolderSection(
    folder: RoutineFolderUiModel?,
    routines: List<RoutineUiModel>,
    onAction: (RoutinesAction) -> Unit,
    onDeleteRoutine: (RoutineUiModel) -> Unit,
    onDeleteFolder: (RoutineFolderUiModel) -> Unit,
    reorderTarget: RoutineReorderTarget?,
    onReorderStarted: (RoutineReorderTarget) -> Unit,
    onReorderStopped: () -> Unit,
) {
    var expanded by rememberSaveable(folder?.id ?: "unfiled") { mutableStateOf(true) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(
                    if ((reorderTarget as? RoutineReorderTarget.Folder)?.id == folder?.id) {
                        0.58f
                    } else {
                        1f
                    },
                )
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (folder != null) {
                ReorderHandle(
                    onDragStarted = {
                        onReorderStarted(RoutineReorderTarget.Folder(folder.id))
                    },
                    onDragStopped = onReorderStopped,
                    onMove = { direction ->
                        onAction(RoutinesAction.MoveFolder(folder.id, direction))
                    },
                )
            }
            Icon(
                imageVector = if (expanded) {
                    Icons.Outlined.ExpandLess
                } else {
                    Icons.Outlined.ExpandMore
                },
                contentDescription = null,
                tint = ForgeFlowDesign.colors.textSecondary,
            )
            Text(
                text = folder?.name ?: stringResource(R.string.unfiled_routines),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            ForgeFlowPill(
                text = stringResource(R.string.folder_routine_count, routines.size),
            )
            if (folder != null) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.folder_options),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.copy_folder)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onAction(RoutinesAction.CopyFolder(folder.id))
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.rename_folder)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAction(RoutinesAction.EditFolder(folder.id))
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_folder)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteFolder(folder)
                            },
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = expanded && reorderTarget !is RoutineReorderTarget.Folder,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
                routines.forEach { routine ->
                    RoutineCard(
                        routine = routine,
                        onAction = onAction,
                        onDelete = { onDeleteRoutine(routine) },
                        compact = reorderTarget is RoutineReorderTarget.Routine,
                        isDragging = (reorderTarget as? RoutineReorderTarget.Routine)?.id == routine.id,
                        onDragStarted = {
                            onReorderStarted(RoutineReorderTarget.Routine(routine.id))
                        },
                        onDragStopped = onReorderStopped,
                    )
                }
                if (routines.isEmpty()) {
                    Text(
                        text = stringResource(R.string.folder_empty),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineUiModel,
    onAction: (RoutinesAction) -> Unit,
    onDelete: () -> Unit,
    compact: Boolean,
    isDragging: Boolean,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    ForgeFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDragging) 0.58f else 1f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReorderHandle(
                onDragStarted = onDragStarted,
                onDragStopped = onDragStopped,
                onMove = { direction ->
                    onAction(
                        RoutinesAction.MoveRoutine(
                            id = routine.id,
                            folderId = routine.folderId,
                            direction = direction,
                        ),
                    )
                },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                Text(
                    text = routine.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = routine.exerciseNames.joinToString(),
                    color = ForgeFlowDesign.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.routine_options),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.copy_routine)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onAction(RoutinesAction.CopyRoutine(routine.id))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_routine)) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onAction(RoutinesAction.EditRoutine(routine.id))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_routine)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
        if (!compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
                ForgeFlowPill(
                    text = stringResource(
                        R.string.compact_exercise_count,
                        routine.exerciseNames.size,
                    ),
                )
                ForgeFlowPill(text = stringResource(R.string.compact_set_count, routine.totalSets))
            }
            ForgeFlowButton(
                text = stringResource(R.string.start_routine),
                onClick = { onAction(RoutinesAction.StartRoutine(routine.id)) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.PlayArrow,
                iconContentDescription = null,
            )
        }
    }
}

@Composable
private fun ReorderHandle(
    onMove: (Int) -> Unit,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }
    IconButton(
        onClick = {},
        modifier = Modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    accumulatedDrag = 0f
                    onDragStarted()
                },
                onDragEnd = {
                    accumulatedDrag = 0f
                    onDragStopped()
                },
                onDragCancel = {
                    accumulatedDrag = 0f
                    onDragStopped()
                },
            ) { change, dragAmount ->
                change.consume()
                accumulatedDrag += dragAmount.y
                if (kotlin.math.abs(accumulatedDrag) >= 52.dp.toPx()) {
                    onMove(if (accumulatedDrag > 0) 1 else -1)
                    accumulatedDrag = 0f
                }
            }
        },
    ) {
        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = stringResource(R.string.drag_to_reorder),
            tint = ForgeFlowDesign.colors.textSecondary,
        )
    }
}
