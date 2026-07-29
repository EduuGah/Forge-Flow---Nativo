package com.forgeflow.feature.workout.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.workout.R

@Composable
internal fun SetTable(
    sets: List<ActiveSetUiModel>,
    weightUnit: WeightUnit,
    onAction: (ActiveWorkoutAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(ForgeFlowDesign.spacing.small),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        SetTableHeader(weightUnit = weightUnit)
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        sets.forEach { set ->
            ActiveSetRow(set = set, onAction = onAction)
        }
    }
}

@Composable
private fun SetTableHeader(weightUnit: WeightUnit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableLabel(
            text = stringResource(R.string.set_header),
            modifier = Modifier.width(36.dp),
        )
        TableLabel(
            text = stringResource(
                if (weightUnit == WeightUnit.KILOGRAM) {
                    R.string.weight_header_kg
                } else {
                    R.string.weight_header_lb
                },
            ),
            modifier = Modifier.weight(1f),
        )
        TableLabel(
            text = stringResource(R.string.repetitions_header),
            modifier = Modifier.weight(1f),
        )
        TableLabel(
            text = stringResource(R.string.done_header),
            modifier = Modifier.width(44.dp),
        )
    }
}

@Composable
private fun TableLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = ForgeFlowDesign.colors.textSecondary,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun ActiveSetRow(
    set: ActiveSetUiModel,
    onAction: (ActiveWorkoutAction) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val backgroundColor by animateColorAsState(
        targetValue = if (set.completed) {
            ForgeFlowDesign.colors.success.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "set-background",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(
                start = ForgeFlowDesign.spacing.small,
                top = ForgeFlowDesign.spacing.extraSmall,
                end = ForgeFlowDesign.spacing.extraSmall,
                bottom = ForgeFlowDesign.spacing.extraSmall,
            ),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = set.number.toString(),
            modifier = Modifier.width(28.dp),
            color = if (set.completed) {
                MaterialTheme.colorScheme.primary
            } else {
                ForgeFlowDesign.colors.textSecondary
            },
            style = MaterialTheme.typography.titleMedium,
        )
        SetInput(
            value = set.weight,
            onValueChange = { onAction(ActiveWorkoutAction.WeightChanged(set.id, it)) },
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )
        SetInput(
            value = set.repetitions,
            onValueChange = { onAction(ActiveWorkoutAction.RepetitionsChanged(set.id, it)) },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = set.completed,
            onCheckedChange = {
                if (it) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onAction(ActiveWorkoutAction.CompletionChanged(set.id, it))
            },
            modifier = Modifier.width(44.dp),
        )
    }
}

@Composable
private fun SetInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 52.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.titleMedium,
        placeholder = { Text(text = "0") },
        shape = MaterialTheme.shapes.small,
    )
}
