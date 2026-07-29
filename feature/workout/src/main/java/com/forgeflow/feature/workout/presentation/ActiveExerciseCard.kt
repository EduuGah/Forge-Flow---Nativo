package com.forgeflow.feature.workout.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.workout.R

@Composable
internal fun ActiveExerciseCard(
    exercise: ActiveExerciseUiModel,
    weightUnit: WeightUnit,
    onAction: (ActiveWorkoutAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExerciseMediaPlaceholder(exercise)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                ForgeFlowEyebrow(
                    text = stringResource(exercise.muscleGroup.labelResource()),
                )
                Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.exercise_set_count, exercise.sets.size),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
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

@Composable
private fun ExerciseMediaPlaceholder(exercise: ActiveExerciseUiModel) {
    val icon = when {
        exercise.mediaType == ExerciseMediaType.ANIMATED_IMAGE -> Icons.Outlined.GifBox
        exercise.mediaUri != null -> Icons.Outlined.Image
        else -> Icons.Outlined.FitnessCenter
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
