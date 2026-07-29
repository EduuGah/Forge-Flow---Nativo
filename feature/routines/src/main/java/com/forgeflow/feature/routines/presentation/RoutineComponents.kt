package com.forgeflow.feature.routines.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowPill
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.routines.R

@Composable
internal fun RoutineCard(
    routine: RoutineUiModel,
    onAction: (RoutinesAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = stringResource(R.string.routine_label))
                Text(text = routine.name, style = MaterialTheme.typography.titleLarge)
                if (routine.description.isNotBlank()) {
                    Text(
                        text = routine.description,
                        color = ForgeFlowDesign.colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            IconButton(onClick = { onAction(RoutinesAction.EditRoutine(routine.id)) }) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_routine))
            }
            IconButton(onClick = { onAction(RoutinesAction.ArchiveRoutine(routine.id)) }) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.archive_routine),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForgeFlowPill(
                text = stringResource(R.string.exercise_count, routine.exerciseNames.size),
            )
            ForgeFlowPill(text = stringResource(R.string.set_count, routine.totalSets))
        }
        Text(
            text = routine.exerciseNames.joinToString(),
            color = ForgeFlowDesign.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
        ForgeFlowButton(
            text = stringResource(R.string.start_routine),
            onClick = { onAction(RoutinesAction.StartRoutine(routine.id)) },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PlayArrow,
            iconContentDescription = null,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineEditorSheet(
    editor: RoutineEditorUiState,
    exercises: List<RoutineExercisePickerModel>,
    isSaving: Boolean,
    onAction: (RoutinesAction) -> Unit,
) {
    val visibleExercises = exercises.filter {
        editor.query.isBlank() || it.name.contains(editor.query, ignoreCase = true)
    }
    ModalBottomSheet(onDismissRequest = { onAction(RoutinesAction.CloseEditor) }) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(
                    if (editor.routineId == null) R.string.create_routine_title
                    else R.string.edit_routine_title,
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
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
            ) {
                items(visibleExercises, key = RoutineExercisePickerModel::id) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = exercise.id in editor.selectedExerciseIds,
                            onCheckedChange = {
                                onAction(RoutinesAction.ExerciseToggled(exercise.id))
                            },
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${exercise.muscleGroup.name} • ${exercise.equipment.name}",
                                color = ForgeFlowDesign.colors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
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
