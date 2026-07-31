package com.forgeflow.feature.workout.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.workout.R

@Composable
internal fun ActiveExerciseCard(
    exercise: ActiveExerciseUiModel,
    weightUnit: WeightUnit,
    onAction: (ActiveWorkoutAction) -> Unit,
    onOpenExercise: (String) -> Unit,
    onReplaceExercise: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var accumulatedDrag by remember { mutableStateOf(0f) }
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = stringResource(R.string.drag_exercise),
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
                            onAction(
                                ActiveWorkoutAction.MoveExercise(
                                    exercise.id,
                                    if (accumulatedDrag > 0) 1 else -1,
                                ),
                            )
                            accumulatedDrag = 0f
                        }
                    }
                },
            )
            ForgeFlowExerciseMedia(
                mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
                contentDescription = exercise.name,
                modifier = Modifier
                    .size(60.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                        shape = CircleShape,
                    ),
                contentScale = ContentScale.Fit,
                shape = CircleShape,
                containerColor = Color.White,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                ForgeFlowEyebrow(
                    text = stringResource(exercise.muscleGroup.labelResource()),
                )
                Text(
                    text = exercise.name,
                    modifier = Modifier.clickable {
                        exercise.exerciseId?.let(onOpenExercise)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = exercise.lastPerformance?.let {
                        stringResource(
                            R.string.last_performance,
                            it,
                            stringResource(
                                if (weightUnit == WeightUnit.KILOGRAM) {
                                    R.string.weight_header_kg
                                } else {
                                    R.string.weight_header_lb
                                },
                            ),
                        )
                    } ?: stringResource(R.string.no_previous_performance),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.exercise_options),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.open_exercise_history)) },
                    onClick = {
                        menuExpanded = false
                        exercise.exerciseId?.let(onOpenExercise)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.replace_exercise)) },
                    leadingIcon = { Icon(Icons.Outlined.SwapHoriz, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onReplaceExercise()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.remove_exercise)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    },
                    onClick = {
                        menuExpanded = false
                        onAction(ActiveWorkoutAction.DeleteExercise(exercise.id))
                    },
                )
            }
        }
        ForgeFlowTextField(
            value = exercise.notes,
            onValueChange = {
                onAction(ActiveWorkoutAction.ExerciseNotesChanged(exercise.id, it))
            },
            label = stringResource(R.string.exercise_notes),
            placeholder = stringResource(R.string.exercise_notes_hint),
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        SetTable(
            sets = exercise.sets,
            weightUnit = weightUnit,
            onAction = onAction,
        )
        ForgeFlowOutlinedButton(
            text = stringResource(R.string.add_set),
            onClick = { onAction(ActiveWorkoutAction.AddSet(exercise.id)) },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Add,
            iconContentDescription = null,
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
