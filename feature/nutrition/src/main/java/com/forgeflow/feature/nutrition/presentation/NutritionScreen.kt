package com.forgeflow.feature.nutrition.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun NutritionScreen(
    state: NutritionUiState,
    onAction: (NutritionAction) -> Unit,
    onBack: () -> Unit,
    onPickMealPhoto: () -> Unit,
    onTakeMealPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeletion by remember { mutableStateOf<String?>(null) }
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
private fun NutritionDateSelector(
    state: NutritionUiState,
    onAction: (NutritionAction) -> Unit,
) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.dateLabel.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            if (!state.isToday) {
                TextButton(onClick = { onAction(NutritionAction.Today) }) {
                    Text(stringResource(R.string.nutrition_today))
                }
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
            Column {
                ForgeFlowEyebrow(text = stringResource(R.string.nutrition_daily_goal))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(
                            R.string.nutrition_calorie_value,
                            state.calories,
                            state.calorieGoal,
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
            IconButton(onClick = onEditGoals) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.nutrition_edit_goals),
                )
            }
        }
        LinearProgressIndicator(
            progress = {
                (state.calories.toFloat() / state.calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth(),
        )
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
    onDelete: () -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
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
                        contentScale = ContentScale.Crop,
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
        label = { Text(label) },
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
