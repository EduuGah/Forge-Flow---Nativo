package com.forgeflow.feature.routines.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.normalizedSearchText
import com.forgeflow.feature.routines.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineEditorSheet(
    editor: RoutineEditorUiState,
    folders: List<RoutineFolderUiModel>,
    exercises: List<RoutineExercisePickerModel>,
    isSaving: Boolean,
    onAction: (RoutinesAction) -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    val visibleExercises = exercises.filter {
        (editor.query.isBlank() ||
            it.searchTerms.contains(editor.query.normalizedSearchText())) &&
            (editor.selectedMuscleGroup == null ||
                it.muscleGroup == editor.selectedMuscleGroup)
    }.sortedWith(
        compareBy<RoutineExercisePickerModel> {
            editor.selectedExerciseIds.indexOf(it.id).let { index ->
                if (index < 0) Int.MAX_VALUE else index
            }
        }.thenBy(RoutineExercisePickerModel::name),
    )
    ModalBottomSheet(onDismissRequest = { onAction(RoutinesAction.CloseEditor) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
                .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal)
                .padding(bottom = ForgeFlowDesign.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            Text(
                text = stringResource(
                    if (editor.routineId == null) {
                        R.string.create_routine_title
                    } else {
                        R.string.edit_routine_title
                    },
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            ForgeFlowTextField(
                value = editor.name,
                onValueChange = { onAction(RoutinesAction.NameChanged(it)) },
                label = stringResource(R.string.routine_name),
                modifier = Modifier.fillMaxWidth(),
            )
            ForgeFlowTextField(
                value = editor.description,
                onValueChange = { onAction(RoutinesAction.DescriptionChanged(it)) },
                label = stringResource(R.string.routine_description),
                modifier = Modifier.fillMaxWidth(),
            )
            FolderSelector(
                selectedFolderId = editor.folderId,
                folders = folders,
                onFolderSelected = { onAction(RoutinesAction.FolderChanged(it)) },
                onCreateFolder = { onAction(RoutinesAction.CreateFolderInEditor) },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.folder_history_scope),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.folder_history_scope_description),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = editor.compareHistoryWithinFolder,
                    onCheckedChange = {
                        onAction(RoutinesAction.CompareHistoryWithinFolderChanged(it))
                    },
                )
            }
            ForgeFlowTextField(
                value = editor.query,
                onValueChange = { onAction(RoutinesAction.SearchChanged(it)) },
                label = stringResource(R.string.search_exercises),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = editor.selectedMuscleGroup == null,
                        onClick = {
                            onAction(RoutinesAction.MuscleGroupChanged(null))
                        },
                        label = { Text(stringResource(R.string.filter_all_muscles)) },
                    )
                }
                items(MuscleGroup.entries) { group ->
                    FilterChip(
                        selected = editor.selectedMuscleGroup == group,
                        onClick = {
                            onAction(RoutinesAction.MuscleGroupChanged(group))
                        },
                        label = { Text(stringResource(group.labelResource())) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        R.string.selected_exercises,
                        editor.selectedExerciseIds.size,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(
                        R.string.exercise_results,
                        visibleExercises.size,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    bottom = ForgeFlowDesign.spacing.extraSmall,
                ),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                items(visibleExercises, key = RoutineExercisePickerModel::id) { exercise ->
                    ExercisePickerRow(
                        exercise = exercise,
                        selected = exercise.id in editor.selectedExerciseIds,
                        plan = editor.exercisePlans.firstOrNull {
                            it.exerciseId == exercise.id
                        },
                        onToggle = {
                            onAction(RoutinesAction.ExerciseToggled(exercise.id))
                        },
                        onOpenDetails = { onOpenExercise(exercise.id) },
                        onMove = { direction ->
                            onAction(RoutinesAction.SelectedExerciseMoved(exercise.id, direction))
                        },
                        onPlannedSetsChanged = {
                            onAction(RoutinesAction.PlannedSetsChanged(exercise.id, it))
                        },
                        onWarmUpSetsChanged = {
                            onAction(RoutinesAction.WarmUpSetsChanged(exercise.id, it))
                        },
                        onNotesChanged = {
                            onAction(RoutinesAction.ExerciseNotesChanged(exercise.id, it))
                        },
                    )
                }
            }
            ForgeFlowButton(
                text = stringResource(R.string.save_routine),
                onClick = { onAction(RoutinesAction.SaveRoutine) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving &&
                    editor.name.isNotBlank() &&
                    editor.selectedExerciseIds.isNotEmpty(),
            )
        }
    }
}

@Composable
private fun FolderSelector(
    selectedFolderId: String?,
    folders: List<RoutineFolderUiModel>,
    onFolderSelected: (String?) -> Unit,
    onCreateFolder: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = folders.firstOrNull { it.id == selectedFolderId }?.name
        ?: stringResource(R.string.unfiled_routines)
    Box(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowOutlinedButton(
            text = stringResource(R.string.selected_folder, label),
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Folder,
            iconContentDescription = null,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.unfiled_routines)) },
                onClick = {
                    expanded = false
                    onFolderSelected(null)
                },
            )
            folders.forEach { folder ->
                DropdownMenuItem(
                    text = { Text(folder.name) },
                    onClick = {
                        expanded = false
                        onFolderSelected(folder.id)
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.create_new_folder)) },
                leadingIcon = {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = null)
                },
                onClick = {
                    expanded = false
                    onCreateFolder()
                },
            )
        }
    }
}

@Composable
private fun ExercisePickerRow(
    exercise: RoutineExercisePickerModel,
    selected: Boolean,
    plan: RoutineExercisePlanUiModel?,
    onToggle: () -> Unit,
    onOpenDetails: () -> Unit,
    onMove: (Int) -> Unit,
    onPlannedSetsChanged: (Int) -> Unit,
    onWarmUpSetsChanged: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
) {
    var accumulatedDrag by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
            )
            .clickable(onClick = onToggle)
            .padding(ForgeFlowDesign.spacing.small),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = stringResource(R.string.drag_to_reorder),
                tint = ForgeFlowDesign.colors.textSecondary,
                modifier = Modifier.pointerInput(exercise.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { accumulatedDrag = 0f },
                        onDragEnd = { accumulatedDrag = 0f },
                        onDragCancel = { accumulatedDrag = 0f },
                    ) { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount.y
                        if (kotlin.math.abs(accumulatedDrag) >= 52.dp.toPx()) {
                            onMove(if (accumulatedDrag > 0) 1 else -1)
                            accumulatedDrag = 0f
                        }
                    }
                },
            )
        }
        ForgeFlowExerciseMedia(
            mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
            contentDescription = exercise.name,
            modifier = Modifier.size(52.dp),
            contentScale = ContentScale.Fit,
            shape = CircleShape,
            containerColor = Color.White,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = exercise.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(
                    R.string.exercise_picker_metadata,
                    stringResource(exercise.muscleGroup.labelResource()),
                    stringResource(exercise.equipment.labelResource()),
                ),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        IconButton(onClick = onOpenDetails) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = stringResource(R.string.open_exercise_details),
            )
        }
        Checkbox(
            checked = selected,
            onCheckedChange = null,
        )
        }
        if (selected && plan != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoutinePlanStepper(
                    label = stringResource(R.string.normal_sets),
                    value = plan.plannedSets,
                    minimum = 1,
                    maximum = 12,
                    onValueChanged = onPlannedSetsChanged,
                    modifier = Modifier.weight(1f),
                )
                RoutinePlanStepper(
                    label = stringResource(R.string.warm_up_sets),
                    value = plan.warmUpSets,
                    minimum = 0,
                    maximum = 6,
                    onValueChanged = onWarmUpSetsChanged,
                    modifier = Modifier.weight(1f),
                )
            }
            ForgeFlowTextField(
                value = plan.notes,
                onValueChange = onNotesChanged,
                label = stringResource(R.string.exercise_notes_next_workout),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RoutinePlanStepper(
    label: String,
    value: Int,
    minimum: Int,
    maximum: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChanged(value - 1) },
                enabled = value > minimum,
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = null)
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = { onValueChanged(value + 1) },
                enabled = value < maximum,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }
    }
}

@StringRes
private fun MuscleGroup.labelResource(): Int = when (this) {
    MuscleGroup.CHEST -> R.string.muscle_chest
    MuscleGroup.BACK -> R.string.muscle_back
    MuscleGroup.SHOULDERS -> R.string.muscle_shoulders
    MuscleGroup.QUADRICEPS -> R.string.muscle_quadriceps
    MuscleGroup.HAMSTRINGS -> R.string.muscle_hamstrings
    MuscleGroup.GLUTES -> R.string.muscle_glutes
    MuscleGroup.BICEPS -> R.string.muscle_biceps
    MuscleGroup.TRICEPS -> R.string.muscle_triceps
    MuscleGroup.CALVES -> R.string.muscle_calves
    MuscleGroup.CORE -> R.string.muscle_core
    MuscleGroup.FULL_BODY -> R.string.muscle_full_body
}

@StringRes
private fun Equipment.labelResource(): Int = when (this) {
    Equipment.BARBELL -> R.string.equipment_barbell
    Equipment.DUMBBELL -> R.string.equipment_dumbbell
    Equipment.MACHINE -> R.string.equipment_machine
    Equipment.CABLE -> R.string.equipment_cable
    Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
    Equipment.OTHER -> R.string.equipment_other
}
