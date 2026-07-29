package com.forgeflow.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.workout.R

@Composable
internal fun ActiveExerciseCard(
    exercise: ActiveExerciseUiModel,
    onAction: (ActiveWorkoutAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = exercise.muscleGroup)
        Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.set_header),
                modifier = Modifier.width(32.dp),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = stringResource(R.string.weight_header),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = stringResource(R.string.repetitions_header),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = stringResource(R.string.done_header),
                modifier = Modifier.width(48.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        exercise.sets.forEach { set ->
            ActiveSetRow(set = set, onAction = onAction)
        }
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
private fun ActiveSetRow(
    set: ActiveSetUiModel,
    onAction: (ActiveWorkoutAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = set.number.toString(),
            modifier = Modifier.width(32.dp),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = set.weight,
            onValueChange = { onAction(ActiveWorkoutAction.WeightChanged(set.id, it)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            placeholder = { Text(stringResource(R.string.weight_placeholder)) },
        )
        OutlinedTextField(
            value = set.repetitions,
            onValueChange = { onAction(ActiveWorkoutAction.RepetitionsChanged(set.id, it)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string.repetitions_placeholder)) },
        )
        Checkbox(
            checked = set.completed,
            onCheckedChange = {
                onAction(ActiveWorkoutAction.CompletionChanged(set.id, it))
            },
            modifier = Modifier.width(48.dp),
        )
    }
}
