package com.forgeflow.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.home.R
import java.text.NumberFormat

@Composable
internal fun DashboardHeader(state: HomeUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        ForgeFlowEyebrow(text = stringResource(R.string.home_eyebrow))
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = if (state.workoutsLastSevenDays == 0) {
                stringResource(R.string.dashboard_empty_week)
            } else {
                stringResource(
                    R.string.dashboard_week_summary,
                    state.workoutsLastSevenDays,
                    state.weeklyVolume.asWeightLabel(state.weightUnit),
                    state.weightUnit.symbol,
                )
            },
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
internal fun DashboardMetricGrid(state: HomeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_week),
                value = state.workoutsLastSevenDays.toString(),
                helper = stringResource(R.string.metric_workouts),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_total_workouts),
                value = state.totalWorkoutCount.toString(),
                helper = stringResource(R.string.metric_finished),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_sets),
                value = state.totalCompletedSets.toString(),
                helper = stringResource(R.string.metric_completed),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_volume),
                value = state.totalVolume.asWeightLabel(state.weightUnit),
                helper = stringResource(R.string.metric_in_unit, state.weightUnit.symbol),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun ActiveWorkoutPanel(
    active: HomeActiveWorkoutUiModel,
    onOpen: () -> Unit,
) {
    val progress = if (active.totalSets == 0) {
        0f
    } else {
        active.completedSets.toFloat() / active.totalSets
    }
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardIcon {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                ForgeFlowEyebrow(text = stringResource(R.string.workout_in_progress))
                Text(text = active.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(
                        R.string.active_progress,
                        active.completedSets,
                        active.totalSets,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small),
        )
        ForgeFlowButton(
            text = stringResource(R.string.continue_workout),
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PlayArrow,
            iconContentDescription = null,
        )
    }
}

@Composable
internal fun NextWorkoutPanel(
    routineCount: Int,
    onOpenRoutines: () -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.next_workout))
        Text(
            text = if (routineCount == 0) {
                stringResource(R.string.create_routine_title)
            } else {
                stringResource(R.string.ready_to_train_title)
            },
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = if (routineCount == 0) {
                stringResource(R.string.create_routine_message)
            } else {
                stringResource(R.string.ready_to_train_message, routineCount)
            },
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        ForgeFlowButton(
            text = if (routineCount == 0) {
                stringResource(R.string.open_routines)
            } else {
                stringResource(R.string.choose_workout)
            },
            onClick = onOpenRoutines,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PlayArrow,
            iconContentDescription = null,
        )
    }
}

@Composable
internal fun DashboardIcon(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

internal val WeightUnit.symbol: String
    get() = when (this) {
        WeightUnit.KILOGRAM -> "kg"
        WeightUnit.POUND -> "lb"
    }

internal fun Double.asWeightLabel(unit: WeightUnit): String {
    val formatter = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = if (this@asWeightLabel < 1_000) 1 else 0
    }
    return formatter.format(this)
}
