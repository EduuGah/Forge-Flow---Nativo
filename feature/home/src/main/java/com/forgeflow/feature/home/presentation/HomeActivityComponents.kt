package com.forgeflow.feature.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.home.R

@Composable
internal fun LatestWorkoutPanel(
    workout: HomeWorkoutSummaryUiModel,
    weightUnit: WeightUnit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardIcon {
                Icon(Icons.Outlined.History, contentDescription = null)
            }
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = stringResource(R.string.latest_activity))
                Text(text = workout.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = workout.date,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryValue(
                value = workout.exerciseCount.toString(),
                label = stringResource(R.string.summary_exercises),
            )
            SummaryValue(
                value = workout.completedSets.toString(),
                label = stringResource(R.string.summary_sets),
            )
            SummaryValue(
                value = stringResource(R.string.duration_minutes, workout.durationMinutes),
                label = stringResource(R.string.summary_duration),
            )
            SummaryValue(
                value = workout.volume.asWeightLabel(weightUnit),
                label = weightUnit.symbol,
            )
        }
    }
}

@Composable
internal fun RecentWorkoutsPanel(
    workouts: List<HomeWorkoutSummaryUiModel>,
    weightUnit: WeightUnit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardIcon {
                Icon(Icons.Outlined.History, contentDescription = null)
            }
            Column {
                ForgeFlowEyebrow(text = stringResource(R.string.recent_activity))
                Text(
                    text = stringResource(R.string.recent_workouts_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        workouts.forEachIndexed { index, workout ->
            if (index > 0) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = workout.date,
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${workout.volume.asWeightLabel(weightUnit)} " +
                            weightUnit.symbol,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.recent_workout_metadata,
                            workout.completedSets,
                            workout.durationMinutes,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuickAccessPanel(
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        ForgeFlowEyebrow(text = stringResource(R.string.quick_access))
        QuickAccessRow(
            title = stringResource(R.string.open_exercise_library),
            description = stringResource(R.string.exercise_library_helper),
            onClick = onOpenExercises,
        )
        QuickAccessRow(
            title = stringResource(R.string.manage_routines),
            description = stringResource(R.string.manage_routines_helper),
            onClick = onOpenRoutines,
        )
    }
}

@Composable
private fun QuickAccessRow(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    ForgeFlowCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardIcon {
                Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = ForgeFlowDesign.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SummaryValue(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, style = MaterialTheme.typography.labelLarge)
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
