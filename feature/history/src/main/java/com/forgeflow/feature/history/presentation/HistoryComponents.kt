package com.forgeflow.feature.history.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowLocationMap
import com.forgeflow.core.designsystem.component.ForgeFlowMapPoint
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.history.R
import java.text.NumberFormat

@Composable
internal fun HistoryWorkoutCard(
    workout: HistoryWorkoutUiModel,
    weightUnit: WeightUnit,
    onOpenExercise: (String) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable(workout.id) { mutableStateOf(false) }
    ForgeFlowCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateBlock(day = workout.day, month = workout.month)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
            ) {
                Text(
                    text = workout.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.finished_at, workout.time),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (workout.hasLocation) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            ForgeFlowDesign.spacing.extraSmall,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = workout.locationLabel
                                ?: stringResource(R.string.location_registered),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Icon(
                imageVector = if (expanded) {
                    Icons.Outlined.ExpandLess
                } else {
                    Icons.Outlined.ExpandMore
                },
                contentDescription = stringResource(
                    if (expanded) R.string.collapse_workout else R.string.expand_workout,
                ),
                tint = ForgeFlowDesign.colors.textSecondary,
            )
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.share_workout_action),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.delete_history_action),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        SessionSummary(workout = workout, weightUnit = weightUnit)
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium)) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
                if (workout.latitude != null && workout.longitude != null) {
                    Text(
                        text = stringResource(R.string.workout_location_map),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    ForgeFlowLocationMap(
                        points = listOf(
                            ForgeFlowMapPoint(
                                latitude = workout.latitude,
                                longitude = workout.longitude,
                                label = workout.locationLabel ?: workout.name,
                            ),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                }
                workout.exercises.forEach { exercise ->
                    HistoryExerciseRow(
                        exercise = exercise,
                        weightUnit = weightUnit,
                        onOpenExercise = onOpenExercise,
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBlock(day: String, month: String) {
    Column(
        modifier = Modifier
            .size(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = month,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun SessionSummary(
    workout: HistoryWorkoutUiModel,
    weightUnit: WeightUnit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(ForgeFlowDesign.spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HistoryStat(
            value = workout.durationMinutes.asDuration(),
            label = stringResource(R.string.summary_duration),
        )
        HistoryStat(
            value = workout.completedSets.toString(),
            label = stringResource(R.string.summary_sets),
        )
        HistoryStat(
            value = workout.exerciseCount.toString(),
            label = stringResource(R.string.summary_exercises),
        )
        HistoryStat(
            value = workout.volume.asDisplayValue(),
            label = stringResource(R.string.summary_total_volume, weightUnit.symbol),
        )
    }
}

@Composable
private fun HistoryStat(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = MaterialTheme.typography.labelLarge)
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

internal val WeightUnit.symbol: String
    get() = when (this) {
        WeightUnit.KILOGRAM -> "kg"
        WeightUnit.POUND -> "lb"
    }

internal fun Double.asDisplayValue(): String = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = if (this@asDisplayValue < 1_000) 1 else 0
}.format(this)

internal fun Long.asDuration(): String =
    if (this >= 60) "${this / 60}h ${this % 60}min" else "${this}min"
