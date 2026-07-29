package com.forgeflow.feature.routines.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.feature.routines.R

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
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (exercise.id in editor.selectedExerciseIds) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            )
                            .clickable {
                                onAction(RoutinesAction.ExerciseToggled(exercise.id))
                            }
                            .padding(
                                horizontal = ForgeFlowDesign.spacing.small,
                                vertical = ForgeFlowDesign.spacing.extraSmall,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = exercise.id in editor.selectedExerciseIds,
                            onCheckedChange = null,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(
                                    R.string.exercise_picker_metadata,
                                    stringResource(exercise.muscleGroup.labelResource()),
                                    stringResource(exercise.equipment.labelResource()),
                                ),
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
