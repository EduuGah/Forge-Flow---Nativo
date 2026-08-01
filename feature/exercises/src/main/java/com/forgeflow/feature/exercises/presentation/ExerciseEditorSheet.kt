package com.forgeflow.feature.exercises.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.feature.exercises.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseEditorSheet(
    editor: ExerciseEditorUiState,
    isSaving: Boolean,
    onSelectPhoto: () -> Unit,
    onAction: (ExercisesAction) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = { onAction(ExercisesAction.CloseEditor) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(
                    if (editor.id == null) R.string.create_exercise_title
                    else R.string.edit_exercise_title,
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            ForgeFlowTextField(
                value = editor.name,
                onValueChange = { onAction(ExercisesAction.EditorNameChanged(it)) },
                label = stringResource(R.string.exercise_name),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.exercise_photo),
                style = MaterialTheme.typography.labelLarge,
            )
            ForgeFlowExerciseMedia(
                mediaUri = editor.mediaUri,
                contentDescription = stringResource(R.string.exercise_photo_preview),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Fit,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ForgeFlowOutlinedButton(
                    text = stringResource(
                        if (editor.mediaUri == null) R.string.exercise_photo_add
                        else R.string.exercise_photo_change,
                    ),
                    onClick = onSelectPhoto,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.PhotoLibrary,
                    iconContentDescription = null,
                )
                if (editor.mediaUri != null) {
                    IconButton(onClick = { onAction(ExercisesAction.RemoveEditorPhoto) }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.exercise_photo_remove),
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.muscle_group_title),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MuscleGroup.entries) { group ->
                    FilterChip(
                        selected = editor.muscleGroup == group,
                        onClick = { onAction(ExercisesAction.EditorMuscleChanged(group)) },
                        label = { Text(stringResource(group.labelResource())) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.equipment_title),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Equipment.entries) { equipment ->
                    FilterChip(
                        selected = editor.equipment == equipment,
                        onClick = {
                            onAction(ExercisesAction.EditorEquipmentChanged(equipment))
                        },
                        label = { Text(stringResource(equipment.labelResource())) },
                    )
                }
            }
            ForgeFlowTextField(
                value = editor.instructions,
                onValueChange = { onAction(ExercisesAction.EditorInstructionsChanged(it)) },
                label = stringResource(R.string.instructions),
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
            )
            ForgeFlowButton(
                text = stringResource(R.string.save_exercise),
                onClick = { onAction(ExercisesAction.SaveExercise) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && editor.name.isNotBlank(),
            )
        }
    }
}

internal fun MuscleGroup.labelResource(): Int = when (this) {
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

internal fun Equipment.labelResource(): Int = when (this) {
    Equipment.BARBELL -> R.string.equipment_barbell
    Equipment.DUMBBELL -> R.string.equipment_dumbbell
    Equipment.MACHINE -> R.string.equipment_machine
    Equipment.CABLE -> R.string.equipment_cable
    Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
    Equipment.OTHER -> R.string.equipment_other
}
