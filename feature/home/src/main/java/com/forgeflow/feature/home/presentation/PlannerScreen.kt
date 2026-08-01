package com.forgeflow.feature.home.presentation

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.TrainingDay
import com.forgeflow.feature.home.R

@Composable
fun PlannerScreen(
    state: PlannerUiState,
    onAction: (PlannerAction) -> Unit,
    onBack: () -> Unit,
    onOpenGoals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.planner_loading),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = ForgeFlowDesign.spacing.screenHorizontal,
                    top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
                    end = ForgeFlowDesign.spacing.screenHorizontal,
                    bottom = innerPadding.calculateBottomPadding() +
                        ForgeFlowDesign.spacing.extraLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
            ) {
                item {
                    PlannerHeader(onBack = onBack)
                }
                if (state.hasError) {
                    item {
                        Text(
                            text = stringResource(R.string.planner_history_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                item {
                    PlannerGoalPanel(
                        state = state,
                        onGoalChanged = {
                            onAction(PlannerAction.WeeklyGoalChanged(it))
                        },
                    )
                }
                item {
                    PlannerAdvancedGoalsPanel(onOpenGoals = onOpenGoals)
                }
                item {
                    PlannerCalendar(
                        state = state,
                        onPreviousMonth = { onAction(PlannerAction.PreviousMonth) },
                        onNextMonth = { onAction(PlannerAction.NextMonth) },
                        onSelectDate = {
                            onAction(PlannerAction.SelectDate(it))
                        },
                    )
                }
                item {
                    PlannerSchedulePanel(
                        state = state,
                        onToggleDay = {
                            onAction(PlannerAction.TrainingDayToggled(it))
                        },
                        onTimeChanged = {
                            onAction(PlannerAction.PreferredTimeChanged(it))
                        },
                    )
                }
                item {
                    SelectedDayPanel(state = state)
                }
            }
        }
    }
}

@Composable
private fun PlannerAdvancedGoalsPanel(onOpenGoals: () -> Unit) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.planner_goals_eyebrow))
        Text(
            text = stringResource(R.string.planner_goals_title),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.planner_goals_description),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        ForgeFlowOutlinedButton(
            text = stringResource(R.string.planner_goals_open),
            onClick = onOpenGoals,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Flag,
        )
    }
}

@Composable
private fun PlannerHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.planner_back),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
        ) {
            ForgeFlowEyebrow(text = stringResource(R.string.planner_eyebrow))
            Text(
                text = stringResource(R.string.planner_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.planner_description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PlannerGoalPanel(
    state: PlannerUiState,
    onGoalChanged: (Int) -> Unit,
) {
    val progress = (
        state.currentWeekWorkouts.toFloat() / state.weeklyWorkoutGoal.coerceAtLeast(1)
        ).coerceIn(0f, 1f)
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall)) {
                ForgeFlowEyebrow(text = stringResource(R.string.planner_weekly_goal))
                Text(
                    text = stringResource(
                        R.string.planner_goal_progress,
                        state.currentWeekWorkouts,
                        state.weeklyWorkoutGoal,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onGoalChanged(state.weeklyWorkoutGoal - 1) },
                    enabled = state.weeklyWorkoutGoal > 1,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = stringResource(R.string.planner_decrease_goal),
                    )
                }
                Text(
                    text = state.weeklyWorkoutGoal.toString(),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(
                    onClick = { onGoalChanged(state.weeklyWorkoutGoal + 1) },
                    enabled = state.weeklyWorkoutGoal < 7,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.planner_increase_goal),
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            PlannerStat(
                label = stringResource(R.string.planner_current_streak),
                value = pluralStringResource(
                    R.plurals.planner_days_value,
                    state.currentStreak,
                    state.currentStreak,
                ),
                modifier = Modifier.weight(1f),
            )
            PlannerStat(
                label = stringResource(R.string.planner_best_streak),
                value = pluralStringResource(
                    R.plurals.planner_days_value,
                    state.bestStreak,
                    state.bestStreak,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlannerStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
    ) {
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun PlannerCalendar(
    state: PlannerUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (Long) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = stringResource(R.string.planner_previous_month),
                )
            }
            Text(
                text = state.monthLabel,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.planner_next_month),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            WEEKDAY_HEADERS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = ForgeFlowDesign.colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        val totalCells = state.firstDayOffset + state.calendarDays.size
        repeat((totalCells + DAYS_PER_WEEK - 1) / DAYS_PER_WEEK) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                repeat(DAYS_PER_WEEK) { weekdayIndex ->
                    val cellIndex = weekIndex * DAYS_PER_WEEK + weekdayIndex
                    val dayIndex = cellIndex - state.firstDayOffset
                    val day = state.calendarDays.getOrNull(dayIndex)
                    if (day == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        PlannerCalendarDay(
                            day = day,
                            onClick = { onSelectDate(day.epochDay) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.large),
        ) {
            CalendarLegend(
                color = MaterialTheme.colorScheme.primary,
                label = stringResource(R.string.planner_completed),
            )
            CalendarLegend(
                color = MaterialTheme.colorScheme.outline,
                label = stringResource(R.string.planner_scheduled),
            )
        }
    }
}

@Composable
private fun PlannerCalendarDay(
    day: PlannerCalendarDayUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        day.isSelected -> MaterialTheme.colorScheme.primary
        day.completedWorkoutCount > 0 -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val contentColor = when {
        day.isSelected -> MaterialTheme.colorScheme.onPrimary
        day.completedWorkoutCount > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val border = when {
        day.isToday -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        day.isScheduled -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else -> null
    }
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f).padding(2.dp),
        color = background,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        border = border,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.dayOfMonth.toString(),
                fontWeight = if (day.isToday || day.isSelected) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.Medium
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (day.completedWorkoutCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 5.dp)
                        .size(4.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (day.isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape,
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend(
    color: Color,
    label: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(7.dp), color = color, shape = CircleShape) {}
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun PlannerSchedulePanel(
    state: PlannerUiState,
    onToggleDay: (TrainingDay) -> Unit,
    onTimeChanged: (Int) -> Unit,
) {
    val context = LocalContext.current
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall)) {
                ForgeFlowEyebrow(text = stringResource(R.string.planner_schedule))
                Text(
                    text = stringResource(R.string.planner_schedule_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TrainingDay.entries.forEach { day ->
                val selected = day in state.trainingDays
                Surface(
                    onClick = { onToggleDay(day) },
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = day.shortLabel(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        Surface(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onTimeChanged(hour * 60 + minute) },
                    state.preferredWorkoutTimeMinutes / 60,
                    state.preferredWorkoutTimeMinutes % 60,
                    true,
                ).show()
            },
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = MaterialTheme.shapes.small,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(ForgeFlowDesign.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.planner_preferred_time),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = state.preferredWorkoutTimeMinutes.asTimeLabel(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.planner_change_time),
                )
            }
        }
        Text(
            text = state.nextScheduledWorkout?.let {
                stringResource(R.string.planner_next_scheduled, it)
            } ?: stringResource(R.string.planner_no_schedule),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SelectedDayPanel(state: PlannerUiState) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = stringResource(R.string.planner_selected_day))
                Text(
                    text = state.selectedDateLabel,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        if (state.isSelectedDayScheduled) {
            Text(
                text = stringResource(
                    R.string.planner_planned_at,
                    state.preferredWorkoutTimeMinutes.asTimeLabel(),
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state.selectedDayWorkouts.isEmpty()) {
            Text(
                text = stringResource(R.string.planner_no_workout_on_day),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            state.selectedDayWorkouts.forEachIndexed { index, workout ->
                if (index > 0) HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workout.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = pluralStringResource(
                                R.plurals.planner_workout_metadata,
                                workout.completedSets,
                                workout.time,
                                workout.completedSets,
                            ),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun TrainingDay.shortLabel(): String = when (this) {
    TrainingDay.MONDAY -> "SEG"
    TrainingDay.TUESDAY -> "TER"
    TrainingDay.WEDNESDAY -> "QUA"
    TrainingDay.THURSDAY -> "QUI"
    TrainingDay.FRIDAY -> "SEX"
    TrainingDay.SATURDAY -> "SÁB"
    TrainingDay.SUNDAY -> "DOM"
}

private fun Int.asTimeLabel(): String = "%02d:%02d".format(this / 60, this % 60)

private val WEEKDAY_HEADERS = listOf("S", "T", "Q", "Q", "S", "S", "D")
private const val DAYS_PER_WEEK = 7
