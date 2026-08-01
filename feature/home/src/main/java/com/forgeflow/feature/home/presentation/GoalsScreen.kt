package com.forgeflow.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowPill
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.GoalCadence
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.core.model.PerformanceGoalType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.normalizedSearchText
import com.forgeflow.feature.home.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun GoalsScreen(
    state: GoalsUiState,
    onAction: (GoalsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<GoalUiModel?>(null) }
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
                end = ForgeFlowDesign.spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.goals_eyebrow),
                    title = stringResource(R.string.goals_title),
                    description = stringResource(R.string.goals_description),
                    actions = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.goals_back),
                            )
                        }
                        IconButton(onClick = { onAction(GoalsAction.OpenEditor()) }) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.goals_create),
                            )
                        }
                    },
                )
            }
            if (state.hasHistoryError) {
                item {
                    Text(
                        text = stringResource(R.string.goals_history_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item { GoalSummary(state) }
            item { GoalFilters(state.filter, onAction) }
            if (state.goals.isEmpty()) {
                item {
                    ForgeFlowEmptyState(
                        title = stringResource(R.string.goals_empty_title),
                        message = stringResource(R.string.goals_empty_message),
                    )
                    ForgeFlowButton(
                        text = stringResource(R.string.goals_create_first),
                        onClick = { onAction(GoalsAction.OpenEditor()) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.Add,
                    )
                }
            } else {
                items(state.goals, key = GoalUiModel::id) { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = { onAction(GoalsAction.OpenEditor(goal.id)) },
                        onDelete = { pendingDelete = goal },
                    )
                }
            }
        }
    }
    state.editor?.let { editor ->
        GoalEditorSheet(
            editor = editor,
            exercises = state.exercises,
            weightUnit = state.weightUnit,
            onAction = onAction,
        )
    }
    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
            title = { Text(stringResource(R.string.goals_delete_title)) },
            text = { Text(stringResource(R.string.goals_delete_message, goal.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(GoalsAction.Delete(goal.id))
                        pendingDelete = null
                    },
                ) { Text(stringResource(R.string.goals_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.goals_cancel))
                }
            },
        )
    }
    if (state.writeFailed) {
        AlertDialog(
            onDismissRequest = { onAction(GoalsAction.DismissWriteError) },
            title = { Text(stringResource(R.string.goals_save_error_title)) },
            text = { Text(stringResource(R.string.goals_save_error_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(GoalsAction.DismissWriteError) }) {
                    Text(stringResource(R.string.goals_ok))
                }
            },
        )
    }
}

@Composable
private fun GoalSummary(state: GoalsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        GoalSummaryItem(
            value = state.activeCount,
            label = stringResource(R.string.goals_active),
            modifier = Modifier.weight(1f),
        )
        GoalSummaryItem(
            value = state.completedCount,
            label = stringResource(R.string.goals_completed),
            modifier = Modifier.weight(1f),
        )
        GoalSummaryItem(
            value = state.overdueCount,
            label = stringResource(R.string.goals_overdue),
            modifier = Modifier.weight(1f),
            isAlert = state.overdueCount > 0,
        )
    }
}

@Composable
private fun GoalSummaryItem(
    value: Int,
    label: String,
    modifier: Modifier,
    isAlert: Boolean = false,
) {
    ForgeFlowCard(
        modifier = modifier,
        contentPadding = PaddingValues(ForgeFlowDesign.spacing.medium),
    ) {
        Text(
            text = value.toString(),
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun GoalFilters(filter: GoalsFilter, onAction: (GoalsAction) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        items(GoalsFilter.entries, key = GoalsFilter::name) { item ->
            FilterChip(
                selected = filter == item,
                onClick = { onAction(GoalsAction.FilterChanged(item)) },
                label = { Text(stringResource(item.labelResource())) },
            )
        }
    }
}

@Composable
private fun GoalCard(goal: GoalUiModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    val progress by animateFloatAsState(goal.progress, label = "goalProgress")
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (goal.exerciseMediaUri != null) {
                ForgeFlowExerciseMedia(
                    mediaUri = goal.exerciseMediaUri,
                    contentDescription = goal.title,
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Fit,
                    shape = CircleShape,
                    containerColor = Color.White,
                )
            } else {
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (goal.isCompleted) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.Flag
                        },
                        contentDescription = null,
                        tint = if (goal.isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = goal.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = goal.cadenceLabel,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.goals_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.goals_delete),
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = when {
                goal.isCompleted -> MaterialTheme.colorScheme.tertiary
                goal.isOverdue -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = goal.progressLabel,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
            when {
                goal.isCompleted -> ForgeFlowPill(stringResource(R.string.goals_status_completed))
                goal.isOverdue -> ForgeFlowPill(stringResource(R.string.goals_status_overdue))
                goal.deadlineLabel != null -> Text(
                    text = stringResource(R.string.goals_until, goal.deadlineLabel),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditorSheet(
    editor: GoalEditorUiState,
    exercises: List<GoalExerciseUiModel>,
    weightUnit: WeightUnit,
    onAction: (GoalsAction) -> Unit,
) {
    var choosingExercise by remember(editor.id) { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = { onAction(GoalsAction.CloseEditor) }) {
        if (choosingExercise) {
            GoalExercisePicker(
                exercises = exercises,
                onSelect = {
                    onAction(GoalsAction.ExerciseSelected(it))
                    choosingExercise = false
                },
                onBack = { choosingExercise = false },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .imePadding(),
                contentPadding = PaddingValues(
                    start = ForgeFlowDesign.spacing.screenHorizontal,
                    end = ForgeFlowDesign.spacing.screenHorizontal,
                    bottom = ForgeFlowDesign.spacing.extraLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            ) {
                item {
                    Text(
                        text = stringResource(
                            if (editor.id == null) R.string.goals_new_title else R.string.goals_edit_title,
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.goals_type).uppercase(),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PerformanceGoalType.entries, key = PerformanceGoalType::name) { type ->
                            FilterChip(
                                selected = editor.type == type,
                                onClick = { onAction(GoalsAction.TypeChanged(type)) },
                                label = { Text(stringResource(type.labelResource())) },
                            )
                        }
                    }
                }
                if (editor.type == PerformanceGoalType.EXERCISE_WEIGHT) {
                    item {
                        ForgeFlowOutlinedButton(
                            text = editor.exerciseName ?: stringResource(R.string.goals_choose_exercise),
                            onClick = { choosingExercise = true },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Outlined.Search,
                        )
                    }
                    item {
                        ForgeFlowTextField(
                            value = editor.minimumRepetitions,
                            onValueChange = {
                                onAction(GoalsAction.MinimumRepetitionsChanged(it))
                            },
                            label = stringResource(R.string.goals_minimum_reps),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
                item {
                    ForgeFlowTextField(
                        value = editor.target,
                        onValueChange = { onAction(GoalsAction.TargetChanged(it)) },
                        label = editor.type.targetLabel(weightUnit),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
                if (editor.type != PerformanceGoalType.EXERCISE_WEIGHT) {
                    item {
                        Text(
                            text = stringResource(R.string.goals_frequency).uppercase(),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(GoalCadence.entries, key = GoalCadence::name) { cadence ->
                                FilterChip(
                                    selected = editor.cadence == cadence,
                                    onClick = { onAction(GoalsAction.CadenceChanged(cadence)) },
                                    label = { Text(stringResource(cadence.labelResource())) },
                                )
                            }
                        }
                    }
                }
                item {
                    ForgeFlowTextField(
                        value = editor.title,
                        onValueChange = { onAction(GoalsAction.TitleChanged(it)) },
                        label = stringResource(R.string.goals_custom_title),
                        placeholder = stringResource(R.string.goals_custom_title_hint),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAction(GoalsAction.DeadlineEnabledChanged(!editor.hasDeadline))
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.goals_deadline),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(R.string.goals_deadline_hint),
                                color = ForgeFlowDesign.colors.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = editor.hasDeadline,
                            onCheckedChange = {
                                onAction(GoalsAction.DeadlineEnabledChanged(it))
                            },
                        )
                    }
                }
                if (editor.hasDeadline) {
                    item {
                        ForgeFlowOutlinedButton(
                            text = LocalDate.ofEpochDay(editor.deadlineEpochDay).format(DATE_FORMATTER),
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Outlined.CalendarMonth,
                        )
                    }
                }
                if (editor.validationFailed) {
                    item {
                        Text(
                            text = stringResource(R.string.goals_validation_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item {
                    ForgeFlowButton(
                        text = stringResource(R.string.goals_save),
                        onClick = { onAction(GoalsAction.Save) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !editor.isSaving,
                        icon = Icons.Outlined.Flag,
                    )
                }
                item {
                    ForgeFlowOutlinedButton(
                        text = stringResource(R.string.goals_cancel),
                        onClick = { onAction(GoalsAction.CloseEditor) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !editor.isSaving,
                    )
                }
            }
        }
    }
    if (showDatePicker) {
        val today = LocalDate.now()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = editor.deadlineEpochDay * MILLIS_PER_DAY,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= today.toEpochDay() * MILLIS_PER_DAY
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            onAction(GoalsAction.DeadlineChanged(it / MILLIS_PER_DAY))
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.goals_apply)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.goals_cancel))
                }
            },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun GoalExercisePicker(
    exercises: List<GoalExerciseUiModel>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.normalizedSearchText()
    val visible = remember(exercises, normalizedQuery) {
        exercises.filter { normalizedQuery.isBlank() || it.searchTerms.contains(normalizedQuery) }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .imePadding()
            .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.goals_back),
                )
            }
            Text(
                text = stringResource(R.string.goals_choose_exercise),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        ForgeFlowTextField(
            value = query,
            onValueChange = { query = it },
            label = stringResource(R.string.goals_search_exercise),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        )
        Text(
            text = stringResource(R.string.goals_result_count, visible.size),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = ForgeFlowDesign.spacing.extraLarge),
        ) {
            items(visible, key = GoalExerciseUiModel::id) { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(exercise.id) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ForgeFlowExerciseMedia(
                        mediaUri = exercise.mediaUri,
                        contentDescription = exercise.name,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                        shape = CircleShape,
                        containerColor = Color.White,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(
                                R.string.goals_exercise_metadata,
                                stringResource(exercise.muscleGroup.labelResource()),
                                stringResource(exercise.equipment.labelResource()),
                            ),
                            color = ForgeFlowDesign.colors.textSecondary,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun GoalsFilter.labelResource(): Int = when (this) {
    GoalsFilter.ACTIVE -> R.string.goals_filter_active
    GoalsFilter.ALL -> R.string.goals_filter_all
    GoalsFilter.COMPLETED -> R.string.goals_filter_completed
}

private fun PerformanceGoalType.labelResource(): Int = when (this) {
    PerformanceGoalType.EXERCISE_WEIGHT -> R.string.goals_type_exercise
    PerformanceGoalType.WORKOUT_COUNT -> R.string.goals_type_workouts
    PerformanceGoalType.TOTAL_VOLUME -> R.string.goals_type_volume
    PerformanceGoalType.TRAINING_DURATION -> R.string.goals_type_duration
}

@Composable
private fun PerformanceGoalType.targetLabel(unit: WeightUnit): String = when (this) {
    PerformanceGoalType.EXERCISE_WEIGHT -> stringResource(
        R.string.goals_target_weight,
        unit.shortLabel(),
    )
    PerformanceGoalType.WORKOUT_COUNT -> stringResource(R.string.goals_target_workouts)
    PerformanceGoalType.TOTAL_VOLUME -> stringResource(
        R.string.goals_target_volume,
        unit.shortLabel(),
    )
    PerformanceGoalType.TRAINING_DURATION -> stringResource(R.string.goals_target_duration)
}

private fun GoalCadence.labelResource(): Int = when (this) {
    GoalCadence.ONCE -> R.string.goals_cadence_once
    GoalCadence.DAILY -> R.string.goals_cadence_daily
    GoalCadence.WEEKLY -> R.string.goals_cadence_weekly
    GoalCadence.MONTHLY -> R.string.goals_cadence_monthly
}

private fun WeightUnit.shortLabel(): String = when (this) {
    WeightUnit.KILOGRAM -> "kg"
    WeightUnit.POUND -> "lb"
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
private fun Equipment.labelResource(): Int = when (this) {
    Equipment.BARBELL -> R.string.equipment_barbell
    Equipment.DUMBBELL -> R.string.equipment_dumbbell
    Equipment.MACHINE -> R.string.equipment_machine
    Equipment.CABLE -> R.string.equipment_cable
    Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
    Equipment.OTHER -> R.string.equipment_other
}

private const val MILLIS_PER_DAY = 86_400_000L
private val DATE_FORMATTER = DateTimeFormatter.ofPattern(
    "dd 'de' MMMM 'de' yyyy",
    Locale.forLanguageTag("pt-BR"),
)
