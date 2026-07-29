package com.forgeflow.feature.routines.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
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
        editor.query.isBlank() || it.name.contains(editor.query, ignoreCase = true)
    }
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
            )
            ForgeFlowTextField(
                value = editor.query,
                onValueChange = { onAction(RoutinesAction.SearchChanged(it)) },
                label = stringResource(R.string.search_exercises),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            )
            Text(
                text = stringResource(
                    R.string.selected_exercises,
                    editor.selectedExerciseIds.size,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                items(visibleExercises, key = RoutineExercisePickerModel::id) { exercise ->
                    ExercisePickerRow(
                        exercise = exercise,
                        selected = exercise.id in editor.selectedExerciseIds,
                        onToggle = {
                            onAction(RoutinesAction.ExerciseToggled(exercise.id))
                        },
                        onOpenDetails = { onOpenExercise(exercise.id) },
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
        }
    }
}

@Composable
private fun ExercisePickerRow(
    exercise: RoutineExercisePickerModel,
    selected: Boolean,
    onToggle: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    Row(
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
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForgeFlowExerciseMedia(
            mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
            contentDescription = exercise.name,
            modifier = Modifier
                .size(52.dp)
                .clip(MaterialTheme.shapes.small),
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
