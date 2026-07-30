package com.forgeflow.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PlayArrow
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
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
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
            text = if (state.displayName.isBlank()) {
                stringResource(R.string.home_title)
            } else {
                stringResource(R.string.home_title_named, state.displayName)
            },
            style = MaterialTheme.typography.headlineLarge,
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
internal fun DashboardOverviewPanel(
    state: HomeUiState,
    onOpenPlanner: () -> Unit,
    onOpenEvolution: () -> Unit,
) {
    val progress = (
        state.currentWeekWorkouts.toFloat() / state.weeklyWorkoutGoal.coerceAtLeast(1)
        ).coerceIn(0f, 1f)
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardIcon {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            }
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = stringResource(R.string.planner_dashboard_eyebrow))
                Text(
                    text = stringResource(
                        R.string.planner_dashboard_progress,
                        state.currentWeekWorkouts,
                        state.weeklyWorkoutGoal,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = state.nextScheduledWorkout?.let {
                        stringResource(R.string.planner_dashboard_next, it)
                    } ?: stringResource(R.string.planner_dashboard_no_schedule),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DashboardStat(
                value = state.currentStreak.toString(),
                label = stringResource(R.string.metric_streak),
            )
            DashboardStat(
                value = state.weeklyVolume.asWeightLabel(state.weightUnit),
                label = stringResource(R.string.dashboard_week_volume, state.weightUnit.symbol),
            )
            DashboardStat(
                value = state.personalRecordCount.toString(),
                label = stringResource(R.string.metric_personal_records),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.dashboard_calendar),
                onClick = onOpenPlanner,
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CalendarMonth,
                iconContentDescription = null,
            )
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.dashboard_evolution_short),
                onClick = onOpenEvolution,
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Insights,
                iconContentDescription = null,
            )
        }
    }
}

@Composable
private fun DashboardStat(
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
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
internal fun PlannerOverviewPanel(
    state: HomeUiState,
    onOpenPlanner: () -> Unit,
) {
    val progress = (
        state.currentWeekWorkouts.toFloat() / state.weeklyWorkoutGoal.coerceAtLeast(1)
        ).coerceIn(0f, 1f)
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardIcon {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                ForgeFlowEyebrow(text = stringResource(R.string.planner_dashboard_eyebrow))
                Text(
                    text = stringResource(
                        R.string.planner_dashboard_progress,
                        state.currentWeekWorkouts,
                        state.weeklyWorkoutGoal,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = state.nextScheduledWorkout?.let {
                        stringResource(R.string.planner_dashboard_next, it)
                    } ?: stringResource(R.string.planner_dashboard_no_schedule),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small),
        )
        ForgeFlowOutlinedButton(
            text = stringResource(R.string.planner_dashboard_open),
            onClick = onOpenPlanner,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.CalendarMonth,
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

internal fun Long.asDashboardDuration(): String =
    if (this >= 60) "${this / 60}h ${this % 60}min" else "${this}min"
