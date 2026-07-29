package com.forgeflow.feature.routines.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowPill
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
        RoutineSummary(routine)
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        RoutineExerciseList(names = routine.exerciseNames)
        ForgeFlowButton(
            text = stringResource(R.string.start_routine),
            onClick = { onAction(RoutinesAction.StartRoutine(routine.id)) },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PlayArrow,
            iconContentDescription = null,
        )
    }
}

@Composable
private fun RoutineSummary(routine: RoutineUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(ForgeFlowDesign.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraLarge),
    ) {
        RoutineStat(
            value = routine.exerciseNames.size.toString(),
            label = stringResource(R.string.summary_exercises),
        )
        RoutineStat(
            value = routine.totalSets.toString(),
            label = stringResource(R.string.summary_sets),
        )
    }
}

@Composable
private fun RoutineStat(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun RoutineExerciseList(names: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        Text(
            text = stringResource(R.string.routine_composition),
            style = MaterialTheme.typography.labelLarge,
        )
        names.take(4).forEachIndexed { index, name ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (names.size > 4) {
            Text(
                text = stringResource(R.string.more_exercises, names.size - 4),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
