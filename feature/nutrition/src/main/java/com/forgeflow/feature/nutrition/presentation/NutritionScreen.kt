package com.forgeflow.feature.nutrition.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.NutritionMealType
import com.forgeflow.feature.nutrition.R
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    state: NutritionUiState,
    onAction: (NutritionAction) -> Unit,
    onBack: () -> Unit,
    onPickMealPhoto: () -> Unit,
    onTakeMealPhoto: () -> Unit,
    onWellnessReminderChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeletion by remember { mutableStateOf<String?>(null) }
    var selectedMeal by remember { mutableStateOf<NutritionMealUiModel?>(null) }
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
                NutritionHeader(onBack = onBack)
            }
            item {
                NutritionDateSelector(state = state, onAction = onAction)
            }
            item {
                NutritionDailySummary(state = state, onEditGoals = {
                    onAction(NutritionAction.OpenGoalsEditor)
                })
            }
            item {
                HydrationSection(
                    state = state,
                    onAction = onAction,
                    onReminderChanged = onWellnessReminderChanged,
                )
            }
            item {
                ForgeFlowButton(
                    text = stringResource(R.string.nutrition_add_meal),
                    onClick = { onAction(NutritionAction.OpenMealEditor) },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.Add,
                    iconContentDescription = null,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
                    ForgeFlowEyebrow(text = stringResource(R.string.nutrition_diary_eyebrow))
                    Text(
                        text = stringResource(R.string.nutrition_diary_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            if (state.meals.isEmpty()) {
                item {
                    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.nutrition_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.nutrition_empty_message),
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(state.meals.size, key = { index -> state.meals[index].id }) { index ->
                    NutritionMealCard(
                        meal = state.meals[index],
                        onOpen = { selectedMeal = state.meals[index] },
                        onDelete = { pendingDeletion = state.meals[index].id },
                    )
                }
            }
        }
    }

    state.mealEditor?.let { editor ->
        NutritionMealEditorDialog(
            editor = editor,
            onAction = onAction,
            onPickPhoto = onPickMealPhoto,
            onTakePhoto = onTakeMealPhoto,
        )
    }
    state.goalsEditor?.let { editor ->
        NutritionGoalsDialog(editor = editor, onAction = onAction)
    }
    pendingDeletion?.let { mealId ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text(stringResource(R.string.nutrition_delete_title)) },
            text = { Text(stringResource(R.string.nutrition_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeletion = null
                        onAction(NutritionAction.DeleteMeal(mealId))
                    },
                ) {
                    Text(stringResource(R.string.nutrition_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) {
                    Text(stringResource(R.string.nutrition_cancel))
                }
            },
        )
    }
    selectedMeal?.let { meal ->
        NutritionMealDetailsDialog(
            meal = meal,
            onDismiss = { selectedMeal = null },
        )
    }
}

@Composable
private fun NutritionHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.nutrition_back),
            )
        }
        ForgeFlowPageHeader(
            eyebrow = stringResource(R.string.nutrition_eyebrow),
            title = stringResource(R.string.nutrition_title),
            description = stringResource(R.string.nutrition_description),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NutritionDateSelector(
    state: NutritionUiState,
    onAction: (NutritionAction) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onAction(NutritionAction.PreviousDay) }) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = stringResource(R.string.nutrition_previous_day),
            )
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.dateLabel.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.nutrition_choose_date),
                    modifier = Modifier.padding(start = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            if (!state.isToday) {
                Text(
                    text = stringResource(R.string.nutrition_today),
                    modifier = Modifier.clickable { onAction(NutritionAction.Today) },
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        IconButton(
            onClick = { onAction(NutritionAction.NextDay) },
            enabled = !state.isToday,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = stringResource(R.string.nutrition_next_day),
            )
        }
    }
    if (showDatePicker) {
        val maximumDateMillis = LocalDate.now().toEpochDay() * MILLIS_PER_DAY
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDateEpochDay * MILLIS_PER_DAY,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= maximumDateMillis
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selectedMillis ->
                            onAction(
                                NutritionAction.DateSelected(
                                    epochDay = selectedMillis / MILLIS_PER_DAY,
                                ),
                            )
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.nutrition_apply_date))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.nutrition_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun NutritionDailySummary(
    state: NutritionUiState,
    onEditGoals: () -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ForgeFlowEyebrow(text = stringResource(R.string.nutrition_daily_goal))
            IconButton(onClick = onEditGoals) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.nutrition_edit_goals),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CircularNutritionGoal(
                label = stringResource(R.string.nutrition_calories),
                value = state.calories,
                goal = state.calorieGoal,
                unit = "kcal",
                icon = Icons.Outlined.LocalFireDepartment,
            )
            CircularNutritionGoal(
                label = stringResource(R.string.nutrition_water),
                value = state.waterMilliliters,
                goal = state.waterGoalMilliliters,
                unit = "ml",
                icon = Icons.Outlined.WaterDrop,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        ) {
            NutritionMacro(
                label = stringResource(R.string.nutrition_protein),
                value = state.proteinGrams,
                goal = state.proteinGoalGrams,
                modifier = Modifier.weight(1f),
            )
            NutritionMacro(
                label = stringResource(R.string.nutrition_carbohydrate),
                value = state.carbohydrateGrams,
                goal = state.carbohydrateGoalGrams,
                modifier = Modifier.weight(1f),
            )
            NutritionMacro(
                label = stringResource(R.string.nutrition_fat),
                value = state.fatGrams,
                goal = state.fatGoalGrams,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CircularNutritionGoal(
    label: String,
    value: Int,
    goal: Int,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val progress = (value.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val progressColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(126.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx()),
                )
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx()),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = progressColor)
                Text(
                    text = NumberFormat.getIntegerInstance().format(value),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "$unit / ${NumberFormat.getIntegerInstance().format(goal)}",
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun HydrationSection(
    state: NutritionUiState,
    onAction: (NutritionAction) -> Unit,
    onReminderChanged: (Boolean) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nutrition_hydration_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(
                        R.string.nutrition_hydration_value,
                        state.waterMilliliters,
                        state.waterGoalMilliliters,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ForgeFlowOutlinedButton(
                text = "+250 ml",
                onClick = { onAction(NutritionAction.AddWater(250)) },
                modifier = Modifier.weight(1f),
            )
            ForgeFlowOutlinedButton(
                text = "+500 ml",
                onClick = { onAction(NutritionAction.AddWater(500)) },
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onAction(NutritionAction.RemoveLastWater) },
                enabled = state.lastHydrationEntryId != null,
            ) {
                Icon(
                    Icons.Outlined.Undo,
                    contentDescription = stringResource(R.string.nutrition_water_undo),
                )
            }
        }
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nutrition_reminders_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.nutrition_reminders_description),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = state.wellnessRemindersEnabled,
                onCheckedChange = onReminderChanged,
            )
        }
        if (state.wellnessRemindersEnabled) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(4) { index ->
                    val hours = index + 1
                    FilterChip(
                        selected = state.wellnessReminderIntervalHours == hours,
                        onClick = {
                            onAction(NutritionAction.WellnessReminderIntervalChanged(hours))
                        },
                        label = {
                            Text(stringResource(R.string.nutrition_reminder_interval, hours))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionMacro(
    label: String,
    value: Double,
    goal: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = stringResource(
                R.string.nutrition_macro_value,
                value.asNumber(),
                goal.asNumber(),
            ),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
        )
        LinearProgressIndicator(
            progress = { (value / goal.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NutritionMealCard(
    meal: NutritionMealUiModel,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    ForgeFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (meal.photoPath != null) {
                AsyncImage(
                    model = File(meal.photoPath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = meal.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(
                        R.string.nutrition_meal_metadata,
                        meal.typeLabel,
                        meal.timeLabel,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = stringResource(R.string.nutrition_meal_calories, meal.calories),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = meal.macrosLabel,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.nutrition_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (meal.notes.isNotBlank()) {
            HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            Text(
                text = meal.notes,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun NutritionMealDetailsDialog(
    meal: NutritionMealUiModel,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        ForgeFlowEyebrow(text = meal.typeLabel)
                        Text(
                            text = meal.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.nutrition_close_details),
                        )
                    }
                }
                meal.photoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = stringResource(R.string.nutrition_meal_photo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NutritionDetailMetric(
                        label = stringResource(R.string.nutrition_time),
                        value = meal.timeLabel,
                        modifier = Modifier.weight(1f),
                    )
                    NutritionDetailMetric(
                        label = stringResource(R.string.nutrition_calories),
                        value = stringResource(R.string.nutrition_meal_calories, meal.calories),
                        modifier = Modifier.weight(1f),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ForgeFlowEyebrow(text = stringResource(R.string.nutrition_macros))
                    Text(
                        text = meal.macrosLabel,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (meal.notes.isNotBlank()) {
                    HorizontalDivider(color = ForgeFlowDesign.colors.divider)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ForgeFlowEyebrow(text = stringResource(R.string.nutrition_notes))
                        Text(
                            text = meal.notes,
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionDetailMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun NutritionMealEditorDialog(
    editor: NutritionMealEditorUiState,
    onAction: (NutritionAction) -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(NutritionAction.CloseMealEditor) },
        title = { Text(stringResource(R.string.nutrition_meal_editor_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onAction(NutritionAction.MealNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.nutrition_meal_name)) },
                    singleLine = true,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NutritionMealType.entries.size) { index ->
                        val type = NutritionMealType.entries[index]
                        FilterChip(
                            selected = editor.type == type,
                            onClick = { onAction(NutritionAction.MealTypeChanged(type)) },
                            label = { Text(type.label()) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NutritionNumberField(
                        value = editor.proteinGrams,
                        onValueChange = { onAction(NutritionAction.MealProteinChanged(it)) },
                        label = stringResource(R.string.nutrition_protein_short),
                        modifier = Modifier.weight(1f),
                    )
                    NutritionNumberField(
                        value = editor.carbohydrateGrams,
                        onValueChange = {
                            onAction(NutritionAction.MealCarbohydrateChanged(it))
                        },
                        label = stringResource(R.string.nutrition_carbohydrate_short),
                        modifier = Modifier.weight(1f),
                    )
                    NutritionNumberField(
                        value = editor.fatGrams,
                        onValueChange = { onAction(NutritionAction.MealFatChanged(it)) },
                        label = stringResource(R.string.nutrition_fat_short),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = editor.calories,
                        onValueChange = { onAction(NutritionAction.MealCaloriesChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.nutrition_calories)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    ForgeFlowOutlinedButton(
                        text = stringResource(R.string.nutrition_calculate),
                        onClick = { onAction(NutritionAction.CalculateCaloriesFromMacros) },
                    )
                }
                editor.sourcePhotoUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = stringResource(R.string.nutrition_photo_preview),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ForgeFlowOutlinedButton(
                        text = stringResource(R.string.nutrition_take_photo),
                        onClick = onTakePhoto,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CameraAlt,
                        iconContentDescription = null,
                    )
                    ForgeFlowOutlinedButton(
                        text = stringResource(R.string.nutrition_pick_photo),
                        onClick = onPickPhoto,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.PhotoLibrary,
                        iconContentDescription = null,
                    )
                }
                Text(
                    text = stringResource(R.string.nutrition_photo_analysis_hint),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onAction(NutritionAction.MealNotesChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.nutrition_notes)) },
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(NutritionAction.SaveMeal) },
                enabled = editor.name.isNotBlank() && !editor.isSaving,
            ) {
                Text(stringResource(R.string.nutrition_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(NutritionAction.CloseMealEditor) }) {
                Text(stringResource(R.string.nutrition_cancel))
            }
        },
    )
}

@Composable
private fun NutritionNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label, maxLines = 1) },
        suffix = { Text("g") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}

@Composable
private fun NutritionGoalsDialog(
    editor: NutritionGoalsEditorUiState,
    onAction: (NutritionAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(NutritionAction.CloseGoalsEditor) },
        title = { Text(stringResource(R.string.nutrition_goals_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NutritionGoalField(
                    value = editor.calories,
                    onValueChange = { onAction(NutritionAction.GoalCaloriesChanged(it)) },
                    label = stringResource(R.string.nutrition_calories_goal),
                    suffix = "kcal",
                )
                NutritionGoalField(
                    value = editor.proteinGrams,
                    onValueChange = { onAction(NutritionAction.GoalProteinChanged(it)) },
                    label = stringResource(R.string.nutrition_protein),
                    suffix = "g",
                )
                NutritionGoalField(
                    value = editor.carbohydrateGrams,
                    onValueChange = { onAction(NutritionAction.GoalCarbohydrateChanged(it)) },
                    label = stringResource(R.string.nutrition_carbohydrate),
                    suffix = "g",
                )
                NutritionGoalField(
                    value = editor.fatGrams,
                    onValueChange = { onAction(NutritionAction.GoalFatChanged(it)) },
                    label = stringResource(R.string.nutrition_fat),
                    suffix = "g",
                )
                NutritionGoalField(
                    value = editor.waterMilliliters,
                    onValueChange = { onAction(NutritionAction.GoalWaterChanged(it)) },
                    label = stringResource(R.string.nutrition_water_goal),
                    suffix = "ml",
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(NutritionAction.SaveGoals) },
                enabled = !editor.isSaving,
            ) {
                Text(stringResource(R.string.nutrition_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(NutritionAction.CloseGoalsEditor) }) {
                Text(stringResource(R.string.nutrition_cancel))
            }
        },
    )
}

@Composable
private fun NutritionGoalField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}

@Composable
private fun NutritionMealType.label(): String = stringResource(
    when (this) {
        NutritionMealType.BREAKFAST -> R.string.nutrition_type_breakfast
        NutritionMealType.LUNCH -> R.string.nutrition_type_lunch
        NutritionMealType.SNACK -> R.string.nutrition_type_snack
        NutritionMealType.DINNER -> R.string.nutrition_type_dinner
        NutritionMealType.PRE_WORKOUT -> R.string.nutrition_type_pre_workout
        NutritionMealType.POST_WORKOUT -> R.string.nutrition_type_post_workout
        NutritionMealType.OTHER -> R.string.nutrition_type_other
    },
)

private fun Double.asNumber(): String = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 1
}.format(this)

private const val MILLIS_PER_DAY = 86_400_000L
