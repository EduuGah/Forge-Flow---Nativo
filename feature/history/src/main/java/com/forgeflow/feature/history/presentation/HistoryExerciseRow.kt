package com.forgeflow.feature.history.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutSetType
import com.forgeflow.feature.history.R

@Composable
internal fun HistoryExerciseRow(
    exercise: HistoryExerciseUiModel,
    weightUnit: WeightUnit,
    onOpenExercise: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = exercise.exerciseId != null) {
                exercise.exerciseId?.let(onOpenExercise)
            },
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ForgeFlowExerciseMedia(
                mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
                contentDescription = exercise.name,
                modifier = Modifier.size(52.dp),
                contentScale = ContentScale.Fit,
                shape = CircleShape,
                containerColor = Color.White,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = exercise.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(
                        R.string.exercise_volume_summary,
                        exercise.completedSets,
                        exercise.totalVolume.asDisplayValue(),
                        weightUnit.symbol,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (exercise.personalRecordCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = stringResource(R.string.personal_records),
                        tint = ForgeFlowDesign.colors.warning,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = exercise.personalRecordCount.toString(),
                        color = ForgeFlowDesign.colors.warning,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        exercise.sets.forEach { set ->
            HistorySetRow(set = set, weightUnit = weightUnit)
        }
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
    }
}

@Composable
private fun HistorySetRow(
    set: HistorySetUiModel,
    weightUnit: WeightUnit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = set.type.shortLabel(set.number),
            color = set.type.typeColor(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(0.22f),
        )
        Text(
            text = stringResource(
                R.string.history_set_performance,
                set.weight.asDisplayValue(),
                weightUnit.symbol,
                set.repetitions,
            ),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (set.isPersonalRecord) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = stringResource(R.string.personal_record),
                tint = ForgeFlowDesign.colors.warning,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = set.personalRecordTypes
                    .joinToString(" + ") { it.shortLabel() },
                color = ForgeFlowDesign.colors.warning,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun WorkoutSetType.typeColor() = when (this) {
    WorkoutSetType.WARM_UP -> ForgeFlowDesign.colors.warning
    WorkoutSetType.NORMAL -> MaterialTheme.colorScheme.onSurface
}

private fun WorkoutSetType.shortLabel(number: Int): String = when (this) {
    WorkoutSetType.WARM_UP -> "A"
    WorkoutSetType.NORMAL -> number.toString()
}

private fun PersonalRecordType.shortLabel(): String = when (this) {
    PersonalRecordType.WEIGHT -> "PESO"
    PersonalRecordType.SET_VOLUME -> "VOLUME"
}
