package com.forgeflow.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.feature.home.R

@Composable
internal fun DashboardEvolutionPanels(
    state: HomeUiState,
    onOpenEvolution: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
    ) {
        VolumeEvolutionCard(
            state = state,
            onOpenEvolution = onOpenEvolution,
        )
        if (state.muscleDistribution.isNotEmpty()) {
            MuscleDistributionCard(state.muscleDistribution)
        }
        if (state.recentRecords.isNotEmpty()) {
            RecentRecordsCard(state.recentRecords)
        }
    }
}

@Composable
private fun VolumeEvolutionCard(
    state: HomeUiState,
    onOpenEvolution: () -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                ForgeFlowEyebrow(text = stringResource(R.string.dashboard_evolution))
                Text(
                    text = stringResource(R.string.volume_per_workout),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                text = state.latestWorkout?.let {
                    "${it.volume.asWeightLabel(state.weightUnit)} ${state.weightUnit.symbol}"
                } ?: "—",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.volumeChart.isEmpty()) {
            Text(
                text = stringResource(R.string.volume_chart_empty),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            val barColor = MaterialTheme.colorScheme.primary
            val gridColor = ForgeFlowDesign.colors.divider
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            ) {
                val maxValue = state.volumeChart.maxOf { it.value }.coerceAtLeast(1f)
                val gap = 8.dp.toPx()
                val barWidth = (
                    size.width - gap * (state.volumeChart.size - 1)
                    ) / state.volumeChart.size
                repeat(3) { line ->
                    val y = size.height * line / 2f
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                    )
                }
                state.volumeChart.forEachIndexed { index, point ->
                    val height = size.height * (point.value / maxValue) * 0.9f
                    val x = index * (barWidth + gap)
                    drawRoundRect(
                        color = barColor,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x,
                            size.height - height,
                        ),
                        size = androidx.compose.ui.geometry.Size(barWidth, height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.volumeChart.first().label,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = state.volumeChart.last().label,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        ForgeFlowOutlinedButton(
            text = stringResource(R.string.open_evolution),
            onClick = onOpenEvolution,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Insights,
            iconContentDescription = null,
        )
    }
}

@Composable
private fun MuscleDistributionCard(items: List<HomeMuscleDistributionUiModel>) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.muscle_map))
        Text(
            text = stringResource(R.string.muscle_distribution_title),
            style = MaterialTheme.typography.titleLarge,
        )
        items.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(item.muscleGroup.labelResource()),
                        style = MaterialTheme.typography.labelLarge,
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
private fun RecentRecordsCard(records: List<HomePersonalRecordUiModel>) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.recent_records))
        Text(
            text = stringResource(R.string.personal_best_title),
            style = MaterialTheme.typography.titleLarge,
        )
        records.forEachIndexed { index, record ->
            if (index > 0) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = ForgeFlowDesign.colors.warning,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.exerciseName,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "${record.workoutName} • ${record.date}",
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = record.performance,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(record.type.labelResource()),
                        color = ForgeFlowDesign.colors.warning,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
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

@StringRes
private fun PersonalRecordType.labelResource(): Int = when (this) {
    PersonalRecordType.WEIGHT -> R.string.record_weight
    PersonalRecordType.SET_VOLUME -> R.string.record_volume
}
