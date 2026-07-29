package com.forgeflow.feature.workout.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
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
                Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
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
