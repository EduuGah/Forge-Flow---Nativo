package com.forgeflow.feature.exercises.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowErrorState
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowPill
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PersonalRecordType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutSetType
import com.forgeflow.feature.exercises.R

@Composable
fun ExerciseDetailsScreen(
    state: ExerciseDetailsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        when {
            state.isLoading -> ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.exercise_details_loading),
            )
            state.error || state.exercise == null -> ForgeFlowErrorState(
                title = stringResource(R.string.exercise_details_error_title),
                message = stringResource(R.string.exercise_details_error_message),
                retryLabel = stringResource(R.string.navigate_back),
                onRetry = onBack,
            )
            else -> {
                val exercise = state.exercise
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() +
                            ForgeFlowDesign.spacing.section,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                ) {
                    item {
                        ExerciseDetailsHeader(exercise = exercise, onBack = onBack)
                    }
                    item {
                        ExerciseDetailsTabs(
                            selectedTab = selectedTab,
                            onSelected = { selectedTab = it },
                        )
                    }
                    when (selectedTab) {
                        0 -> exerciseSummary(exercise)
                        1 -> exerciseHistory(exercise)
                        else -> exerciseInstructions(exercise)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailsHeader(
    exercise: ExerciseDetailsUiModel,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                )
            }
            Text(
                text = exercise.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        ForgeFlowExerciseMedia(
            mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
            contentDescription = exercise.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.65f),
            contentScale = ContentScale.Fit,
            containerColor = Color.White,
        )
        Column(
            modifier = Modifier.padding(
                horizontal = ForgeFlowDesign.spacing.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            Text(exercise.name, style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgeFlowPill(
                    text = stringResource(exercise.muscleGroup.detailsLabelResource()),
                )
                ForgeFlowPill(
                    text = stringResource(exercise.equipment.detailsLabelResource()),
                )
            }
            if (exercise.secondaryMuscles.isNotEmpty()) {
                val context = LocalContext.current
                Text(
                    text = stringResource(
                        R.string.secondary_muscles,
                        exercise.secondaryMuscles.joinToString {
                            context.getString(it.detailsLabelResource())
                        },
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ExerciseDetailsTabs(
    selectedTab: Int,
    onSelected: (Int) -> Unit,
) {
    val labels = listOf(
        stringResource(R.string.details_tab_summary),
        stringResource(R.string.details_tab_history),
        stringResource(R.string.details_tab_instructions),
    )
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelected(index) },
                text = { Text(label, maxLines = 1) },
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.exerciseSummary(
    exercise: ExerciseDetailsUiModel,
) {
    item {
        Column(
            modifier = Modifier.padding(
                horizontal = ForgeFlowDesign.spacing.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                ForgeFlowMetric(
                    label = stringResource(R.string.details_sessions),
                    value = exercise.sessionCount.toString(),
                    helper = stringResource(R.string.details_registered),
                    modifier = Modifier.weight(1f),
                )
                ForgeFlowMetric(
                    label = stringResource(R.string.details_sets),
                    value = exercise.completedSetCount.toString(),
                    helper = stringResource(R.string.details_completed),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                ForgeFlowMetric(
                    label = stringResource(R.string.details_max_weight),
                    value = "${exercise.maxWeight} ${exercise.weightUnit.shortLabel()}",
                    helper = stringResource(R.string.details_best_load),
                    modifier = Modifier.weight(1f),
                )
                ForgeFlowMetric(
                    label = stringResource(R.string.details_estimated_one_rm),
                    value = "${exercise.estimatedOneRepMax} ${exercise.weightUnit.shortLabel()}",
                    helper = stringResource(R.string.details_estimate),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    item {
        ExerciseProgressChart(exercise)
    }
    item {
        ForgeFlowCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = ForgeFlowDesign.colors.warning,
                )
                Text(
                    text = stringResource(R.string.details_personal_records),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = exercise.personalRecordCount.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            DetailValueRow(
                stringResource(R.string.details_best_set),
                "${exercise.bestSet} ${exercise.weightUnit.shortLabel()}",
            )
            DetailValueRow(
                stringResource(R.string.details_total_volume),
                "${exercise.totalVolume} ${exercise.weightUnit.shortLabel()}",
            )
            if (exercise.personalRecords.isNotEmpty()) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
                Text(
                    text = stringResource(R.string.details_recent_records),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                exercise.personalRecords.forEachIndexed { index, record ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = ForgeFlowDesign.colors.divider.copy(alpha = 0.65f),
                        )
                    }
                    PersonalRecordRow(
                        record = record,
                        isLatest = index == 0,
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.exerciseHistory(
    exercise: ExerciseDetailsUiModel,
) {
    if (exercise.sessions.isEmpty()) {
        item {
            Text(
                text = stringResource(R.string.details_no_history),
                modifier = Modifier.padding(ForgeFlowDesign.spacing.screenHorizontal),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        items(exercise.sessions, key = ExerciseSessionUiModel::id) { session ->
            ExerciseSessionCard(
                session = session,
                weightUnit = exercise.weightUnit,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.exerciseInstructions(
    exercise: ExerciseDetailsUiModel,
) {
    item {
        Column(
            modifier = Modifier.padding(
                horizontal = ForgeFlowDesign.spacing.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        ) {
            Text(
                text = stringResource(R.string.details_execution),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = exercise.instructions.ifBlank {
                    stringResource(R.string.details_no_instructions)
                },
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ExerciseProgressChart(exercise: ExerciseDetailsUiModel) {
    val points = exercise.chartPoints
    var selectedPointIndex by remember(points) {
        mutableIntStateOf(points.lastIndex)
    }
    val selectedPoint = points.getOrNull(selectedPointIndex)
    ForgeFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.details_progress),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(
                        R.string.details_max_weight_over_time,
                        exercise.weightUnit.shortLabel(),
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            selectedPoint?.let { point ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${point.value.toDouble().toCleanString()} " +
                            exercise.weightUnit.shortLabel(),
                        color = MaterialTheme.colorScheme.primary,
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
        val lineColor = MaterialTheme.colorScheme.primary
        val gridColor = ForgeFlowDesign.colors.divider
        val selectedHaloColor = MaterialTheme.colorScheme.surface
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .pointerInput(points) {
                    detectTapGestures { tap ->
                        if (points.isNotEmpty()) {
                            nearestChartPointIndex(
                                tapX = tap.x,
                                chartWidth = size.width.toFloat(),
                                pointCount = points.size,
                            )?.let { selectedPointIndex = it }
                        }
                    }
                },
        ) {
            val tooltipWidth = 92.dp
            val selectedFraction = if (points.size <= 1) {
                0.5f
            } else {
                selectedPointIndex.toFloat() / (points.size - 1)
            }
            val tooltipX = (
                maxWidth * selectedFraction - tooltipWidth / 2
                ).coerceIn(0.dp, (maxWidth - tooltipWidth).coerceAtLeast(0.dp))
            Canvas(modifier = Modifier.fillMaxSize()) {
                repeat(4) { line ->
                    val y = size.height * line / 3f
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y))
                }
                if (points.isNotEmpty()) {
                    val min = points.minOf { it.value }
                    val max = points.maxOf { it.value }
                    val range = (max - min).coerceAtLeast(1f)
                    val pointOffsets = points.mapIndexed { index, point ->
                        val x = if (points.size == 1) {
                            size.width / 2f
                        } else {
                            size.width * index / (points.size - 1)
                        }
                        val y = size.height -
                            ((point.value - min) / range * size.height * 0.8f) -
                            size.height * 0.1f
                        Offset(x, y)
                    }
                    val path = Path().apply {
                        pointOffsets.forEachIndexed { index, offset ->
                            if (index == 0) {
                                moveTo(offset.x, offset.y)
                            } else {
                                lineTo(offset.x, offset.y)
                            }
                        }
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    )
                    pointOffsets.forEachIndexed { index, offset ->
                        val isSelected = index == selectedPointIndex
                        if (isSelected) {
                            drawLine(
                                color = lineColor.copy(alpha = 0.35f),
                                start = Offset(offset.x, 0f),
                                end = Offset(offset.x, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                            drawCircle(
                                color = selectedHaloColor,
                                radius = 9.dp.toPx(),
                                center = offset,
                            )
                        }
                        drawCircle(
                            color = lineColor,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = offset,
                        )
                    }
                }
            }
            selectedPoint?.let { point ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = tooltipX, y = 4.dp)
                        .width(tooltipWidth),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small,
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = ForgeFlowDesign.spacing.small,
                            vertical = ForgeFlowDesign.spacing.extraSmall,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${point.value.toDouble().toCleanString()} " +
                                exercise.weightUnit.shortLabel(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        if (points.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = points.first().label,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = points.last().label,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ExerciseSessionCard(
    session: ExerciseSessionUiModel,
    weightUnit: WeightUnit,
) {
    ForgeFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(session.workoutName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = session.date,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        session.sets.forEach { set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = set.type.shortLabel(set.number),
                    modifier = Modifier.weight(0.3f),
                    color = set.type.color(),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${set.performance} ${weightUnit.shortLabel()}",
                    modifier = Modifier.weight(1f),
                )
                if (set.isPersonalRecord) {
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = stringResource(R.string.personal_record),
                            tint = ForgeFlowDesign.colors.warning,
                        )
                        Text(
                            text = set.personalRecordTypes
                                .joinToString(" + ") { it.shortLabel() },
                            color = ForgeFlowDesign.colors.warning,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordRow(
    record: ExercisePersonalRecordUiModel,
    isLatest: Boolean,
) {
    val context = LocalContext.current
    val emphasisColor = if (isLatest) {
        ForgeFlowDesign.colors.warning
    } else {
        ForgeFlowDesign.colors.textSecondary
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = emphasisColor,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.types.joinToString(" + ") { type ->
                    context.getString(type.labelResource())
                },
                color = if (isLatest) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    ForgeFlowDesign.colors.textSecondary
                },
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(
                    if (isLatest) R.string.record_current else R.string.record_previous,
                    record.workoutName,
                    record.date,
                ),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = record.performance,
            color = if (isLatest) {
                MaterialTheme.colorScheme.primary
            } else {
                ForgeFlowDesign.colors.textSecondary
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun DetailValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(text = value, style = MaterialTheme.typography.titleSmall)
    }
}

private fun WeightUnit.shortLabel(): String = if (this == WeightUnit.KILOGRAM) "kg" else "lb"

private fun Double.toCleanString(): String =
    if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

@Composable
private fun WorkoutSetType.color() = when (this) {
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

private fun PersonalRecordType.labelResource(): Int = when (this) {
    PersonalRecordType.WEIGHT -> R.string.record_weight
    PersonalRecordType.SET_VOLUME -> R.string.record_set_volume
}

@StringRes
private fun MuscleGroup.detailsLabelResource(): Int = when (this) {
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
private fun Equipment.detailsLabelResource(): Int = when (this) {
    Equipment.BARBELL -> R.string.equipment_barbell
    Equipment.DUMBBELL -> R.string.equipment_dumbbell
    Equipment.MACHINE -> R.string.equipment_machine
    Equipment.CABLE -> R.string.equipment_cable
    Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
    Equipment.OTHER -> R.string.equipment_other
}
