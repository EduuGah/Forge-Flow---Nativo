package com.forgeflow.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.feature.home.R
import java.text.NumberFormat

@Composable
fun EvolutionScreen(
    state: EvolutionUiState,
    onAction: (EvolutionAction) -> Unit,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.evolution_loading),
            )
        } else {
            EvolutionContent(
                state = state,
                onPeriodChanged = {
                    onAction(EvolutionAction.PeriodChanged(it))
                },
                onBack = onBack,
                onOpenExercise = onOpenExercise,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun EvolutionContent(
    state: EvolutionUiState,
    onPeriodChanged: (EvolutionPeriod) -> Unit,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.screenHorizontal,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
            end = ForgeFlowDesign.spacing.screenHorizontal,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
    ) {
        item { EvolutionHeader(onBack = onBack) }
        item {
            EvolutionPeriodSelector(
                selected = state.period,
                onSelected = onPeriodChanged,
            )
        }
        if (state.hasError) {
            item {
                Text(
                    text = stringResource(R.string.evolution_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (state.workoutCount == 0) {
            item { EvolutionEmptyState() }
        } else {
            item { EvolutionMetricGrid(state) }
            item { EvolutionOverviewPanel(state) }
            item { EvolutionVolumePanel(state) }
            item { EvolutionFrequencyPanel(state.frequency) }
            if (state.muscleDistribution.isNotEmpty()) {
                item { EvolutionMusclePanel(state.muscleDistribution) }
            }
            if (state.topExercises.isNotEmpty()) {
                item {
                    EvolutionTopExercisesPanel(
                        exercises = state.topExercises,
                        weightUnitLabel = state.weightUnit.symbol,
                        onOpenExercise = onOpenExercise,
                    )
                }
            }
        }
    }
}

@Composable
private fun EvolutionHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.evolution_back),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
        ) {
            ForgeFlowEyebrow(text = stringResource(R.string.evolution_eyebrow))
            Text(
                text = stringResource(R.string.evolution_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.evolution_description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EvolutionPeriodSelector(
    selected: EvolutionPeriod,
    onSelected: (EvolutionPeriod) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        EvolutionPeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selected == period,
                onClick = { onSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index, EvolutionPeriod.entries.size),
                label = {
                    Text(text = stringResource(period.labelResource()))
                },
            )
        }
    }
}

@Composable
private fun EvolutionMetricGrid(state: EvolutionUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_workouts),
                value = state.workoutCount.toString(),
                helper = state.workoutChangePercent.asComparisonLabel(),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.evolution_metric_total_volume),
                value = state.totalVolume.asWeightLabel(state.weightUnit),
                helper = stringResource(
                    R.string.evolution_volume_helper,
                    state.weightUnit.symbol,
                    state.volumeChangePercent.asComparisonLabel(),
                ),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_sets),
                value = state.completedSets.toString(),
                helper = stringResource(R.string.evolution_sets_helper),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_personal_records),
                value = state.personalRecordCount.toString(),
                helper = stringResource(R.string.evolution_records_helper),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Int?.asComparisonLabel(): String = when {
    this == null -> stringResource(R.string.evolution_no_comparison)
    this > 0 -> stringResource(R.string.evolution_comparison_up, this)
    this < 0 -> stringResource(R.string.evolution_comparison_down, -this)
    else -> stringResource(R.string.evolution_comparison_equal)
}

@Composable
private fun EvolutionOverviewPanel(state: EvolutionUiState) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.evolution_rhythm))
        Text(
            text = stringResource(R.string.evolution_rhythm_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        ) {
            EvolutionSummaryMetric(
                label = stringResource(R.string.evolution_weekly_average),
                value = stringResource(
                    R.string.evolution_weekly_value,
                    state.averageWorkoutsPerWeek.asOneDecimal(),
                ),
                modifier = Modifier.weight(1f),
            )
            EvolutionSummaryMetric(
                label = stringResource(R.string.evolution_average_duration),
                value = state.averageDurationMinutes.asDashboardDuration(),
                modifier = Modifier.weight(1f),
            )
            EvolutionSummaryMetric(
                label = stringResource(R.string.evolution_active_days),
                value = state.activeDays.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        Text(
            text = stringResource(
                R.string.evolution_total_time,
                state.durationMinutes.asDashboardDuration(),
            ),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EvolutionSummaryMetric(
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
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun EvolutionVolumePanel(state: EvolutionUiState) {
    val points = state.volumeChart
    var selectedIndex by remember(points) {
        mutableIntStateOf(points.lastIndex.coerceAtLeast(0))
    }
    val selectedPoint = points.getOrNull(selectedIndex)
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                ForgeFlowEyebrow(text = stringResource(R.string.evolution_volume_eyebrow))
                Text(
                    text = stringResource(R.string.evolution_volume_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            selectedPoint?.let { point ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${point.value.asWeightLabel(state.weightUnit)} " +
                            state.weightUnit.symbol,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = point.label,
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        EvolutionLineChart(
            points = points,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
        )
        Text(
            text = stringResource(R.string.evolution_chart_hint),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun EvolutionLineChart(
    points: List<EvolutionChartPointUiModel>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = ForgeFlowDesign.colors.divider
    val haloColor = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(points) {
                detectTapGestures { tap ->
                    nearestEvolutionChartPointIndex(
                        tapX = tap.x,
                        chartWidth = size.width.toFloat(),
                        pointCount = points.size,
                    )?.let(onSelect)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(4) { line ->
                val y = size.height * line / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                )
            }
            if (points.isEmpty()) return@Canvas
            val maximum = points.maxOf { it.value }.coerceAtLeast(1.0)
            val offsets = points.mapIndexed { index, point ->
                val x = if (points.size == 1) {
                    size.width / 2f
                } else {
                    size.width * index / (points.size - 1)
                }
                val y = size.height -
                    (point.value / maximum * size.height * 0.8f).toFloat() -
                    size.height * 0.1f
                Offset(x, y)
            }
            val path = Path().apply {
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            offsets.forEachIndexed { index, offset ->
                val selected = index == selectedIndex
                if (selected) {
                    drawLine(
                        color = lineColor.copy(alpha = 0.35f),
                        start = Offset(offset.x, 0f),
                        end = Offset(offset.x, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                    drawCircle(
                        color = haloColor,
                        radius = 9.dp.toPx(),
                        center = offset,
                    )
                }
                drawCircle(
                    color = lineColor,
                    radius = if (selected) 6.dp.toPx() else 4.dp.toPx(),
                    center = offset,
                )
            }
        }
    }
}

@Composable
private fun EvolutionFrequencyPanel(items: List<EvolutionFrequencyUiModel>) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.evolution_consistency))
        Text(
            text = stringResource(R.string.evolution_frequency_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            items.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.workoutCount.toString(),
                        color = if (item.workoutCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            ForgeFlowDesign.colors.textSecondary
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .width(18.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((72 * item.share.coerceAtLeast(0.04f)).dp),
                            color = if (item.workoutCount > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {}
                    }
                    Text(
                        text = item.label,
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EvolutionMusclePanel(items: List<EvolutionMuscleUiModel>) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.evolution_balance))
        Text(
            text = stringResource(R.string.evolution_muscle_title),
            style = MaterialTheme.typography.titleLarge,
        )
        items.take(6).forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(item.muscleGroup.labelResource()),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.muscle_set_count,
                            item.completedSets,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                LinearProgressIndicator(
                    progress = { item.share },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EvolutionTopExercisesPanel(
    exercises: List<EvolutionExerciseUiModel>,
    weightUnitLabel: String,
    onOpenExercise: (String) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = stringResource(R.string.evolution_exercises_eyebrow))
                Text(
                    text = stringResource(R.string.evolution_exercises_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        exercises.forEachIndexed { index, exercise ->
            if (index > 0) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (exercise.exerciseId != null) {
                            Modifier.clickable {
                                onOpenExercise(exercise.exerciseId)
                            }
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = ForgeFlowDesign.spacing.small),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (index + 1).toString(),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.evolution_exercise_metadata,
                            exercise.workoutCount,
                            exercise.completedSets,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = exercise.totalVolume.asWeightLabelValue(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = weightUnitLabel,
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (exercise.exerciseId != null) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = stringResource(R.string.evolution_open_exercise),
                        tint = ForgeFlowDesign.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EvolutionEmptyState() {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Outlined.Insights,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.evolution_empty_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.evolution_empty_message),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@StringRes
private fun EvolutionPeriod.labelResource(): Int = when (this) {
    EvolutionPeriod.THIRTY_DAYS -> R.string.evolution_period_30_days
    EvolutionPeriod.NINETY_DAYS -> R.string.evolution_period_90_days
    EvolutionPeriod.ONE_YEAR -> R.string.evolution_period_one_year
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

private fun Double.asOneDecimal(): String =
    NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 1
    }.format(this)

private fun Double.asWeightLabelValue(): String =
    NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = if (this@asWeightLabelValue < 1_000) 1 else 0
    }.format(this)
