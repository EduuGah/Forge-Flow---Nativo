package com.forgeflow.feature.workout.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutSetType
import com.forgeflow.feature.workout.R

@Composable
internal fun SetTable(
    sets: List<ActiveSetUiModel>,
    weightUnit: WeightUnit,
    onAction: (ActiveWorkoutAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableLabel(stringResource(R.string.set_header), Modifier.width(36.dp))
        TableLabel(stringResource(R.string.previous_header), Modifier.width(72.dp))
        TableLabel(
            stringResource(
                if (weightUnit == WeightUnit.KILOGRAM) {
                    R.string.weight_header_kg
                } else {
                    R.string.weight_header_lb
                },
            ),
            Modifier.weight(1f),
        )
        TableLabel(stringResource(R.string.repetitions_header), Modifier.width(48.dp))
        TableLabel(stringResource(R.string.done_header), Modifier.width(40.dp))
    }
}

@Composable
private fun TableLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = ForgeFlowDesign.colors.textSecondary,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
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
            ForgeFlowDesign.colors.success.copy(alpha = 0.13f)
        } else {
            Color.Transparent
        },
        label = "set-background",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SetTypeMenu(
            set = set,
            onTypeChanged = {
                onAction(ActiveWorkoutAction.SetTypeChanged(set.id, it))
            },
            onDelete = { onAction(ActiveWorkoutAction.DeleteSet(set.id)) },
        )
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = set.previous ?: "—",
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
        }
        SetInput(
            value = set.weight,
            onValueChange = { onAction(ActiveWorkoutAction.WeightChanged(set.id, it)) },
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )
        SetInput(
            value = set.repetitions,
            onValueChange = {
                onAction(ActiveWorkoutAction.RepetitionsChanged(set.id, it))
            },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.width(48.dp),
        )
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (set.isPersonalRecord && set.completed) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = stringResource(R.string.personal_record),
                    tint = ForgeFlowDesign.colors.warning,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .width(14.dp),
                )
            }
            IconButton(
                onClick = {
                    val canComplete = set.completed ||
                        (set.repetitions.toIntOrNull() ?: 0) > 0
                    if (!set.completed && canComplete) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (canComplete) {
                        onAction(
                            ActiveWorkoutAction.CompletionChanged(
                                set.id,
                                !set.completed,
                            ),
                        )
                    }
                },
            ) {
                Icon(
                    imageVector = if (set.completed) {
                        Icons.Outlined.CheckCircle
                    } else {
                        Icons.Outlined.RadioButtonUnchecked
                    },
                    contentDescription = stringResource(R.string.toggle_set_completion),
                    tint = if (set.completed) {
                        ForgeFlowDesign.colors.success
                    } else {
                        ForgeFlowDesign.colors.textSecondary
                    },
                )
            }
        }
    }
}

@Composable
private fun SetTypeMenu(
    set: ActiveSetUiModel,
    onTypeChanged: (WorkoutSetType) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val typeColor = when (set.type) {
        WorkoutSetType.WARM_UP -> ForgeFlowDesign.colors.warning
        WorkoutSetType.NORMAL -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier.width(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = set.type.shortLabel(set.number),
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(horizontal = 6.dp, vertical = 8.dp),
            color = typeColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            WorkoutSetType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(stringResource(type.labelResource())) },
                    onClick = {
                        expanded = false
                        onTypeChanged(type)
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.delete_set),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun SetInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(42.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isBlank()) {
                    Text(
                        text = "0",
                        color = ForgeFlowDesign.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                innerTextField()
            }
        },
    )
}

private fun WorkoutSetType.shortLabel(number: Int): String = when (this) {
    WorkoutSetType.WARM_UP -> "A"
    WorkoutSetType.NORMAL -> number.toString()
}

private fun WorkoutSetType.labelResource(): Int = when (this) {
    WorkoutSetType.WARM_UP -> R.string.set_type_warm_up
    WorkoutSetType.NORMAL -> R.string.set_type_normal
}
