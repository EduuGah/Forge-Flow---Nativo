package com.forgeflow.feature.workout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.workout.R

@Composable
internal fun WorkoutHeader(
    workout: ActiveWorkoutUiModel,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
        ) {
            ForgeFlowEyebrow(text = stringResource(R.string.workout_in_progress))
            Text(text = workout.name, style = MaterialTheme.typography.headlineSmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = workout.elapsedSeconds.asClock(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.elapsed_time),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
internal fun WorkoutProgress(workout: ActiveWorkoutUiModel) {
    val progress = if (workout.totalSets == 0) {
        0f
    } else {
        workout.completedSets.toFloat() / workout.totalSets
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(ForgeFlowDesign.spacing.card),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.session_progress),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(
                    R.string.set_progress,
                    workout.completedSets,
                    workout.totalSets,
                ),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            WorkoutMetric(
                value = workout.totalVolume,
                label = stringResource(
                    R.string.total_volume_metric,
                    stringResource(
                        if (workout.weightUnit == com.forgeflow.core.model.WeightUnit.KILOGRAM) {
                            R.string.weight_header_kg
                        } else {
                            R.string.weight_header_lb
                        },
                    ),
                ),
            )
            WorkoutMetric(
                value = workout.completedSets.toString(),
                label = stringResource(R.string.completed_sets_metric),
            )
            WorkoutMetric(
                value = workout.personalRecordCount.toString(),
                label = stringResource(R.string.personal_records_metric),
            )
        }
    }
}

@Composable
private fun WorkoutMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun Long.asClock(): String {
    val hours = this / 3_600
    val minutes = (this % 3_600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
