package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.settings.R
import java.io.File

@Composable
fun ProfileScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onSelectAvatar: () -> Unit,
    onOpenProgressPhotos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ForgeFlowDesign.spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding() + ForgeFlowDesign.spacing.large,
                end = ForgeFlowDesign.spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.profile_page_eyebrow),
                    title = stringResource(R.string.profile_page_title),
                    description = stringResource(R.string.profile_page_description),
                )
            }
            item {
                ProfileHero(
                    state = state,
                    onSelectAvatar = onSelectAvatar,
                    onEdit = { onAction(SettingsAction.OpenProfileEditor) },
                )
            }
            item {
                WeightEvolutionSection(
                    state = state,
                    onAddWeight = { onAction(SettingsAction.OpenBodyWeightEditor) },
                )
            }
            item {
                ProgressPhotosShortcut(
                    state = state,
                    onOpen = onOpenProgressPhotos,
                )
            }
        }
    }
    state.profileEditor?.let { editor ->
        ProfileEditorDialog(
            editor = editor,
            weightUnitLabel = state.weightUnit.shortLabel(),
            onAction = onAction,
        )
    }
    state.weightEditor?.let { editor ->
        BodyWeightEditorDialog(
            editor = editor,
            unitLabel = state.weightUnit.shortLabel(),
            onAction = onAction,
        )
    }
}

@Composable
private fun ProfileHero(
    state: SettingsUiState,
    onSelectAvatar: () -> Unit,
    onEdit: () -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onSelectAvatar),
                contentAlignment = Alignment.Center,
            ) {
                if (state.profile.profilePhotoPath != null) {
                    AsyncImage(
                        model = File(state.profile.profilePhotoPath),
                        contentDescription = stringResource(R.string.profile_avatar_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = stringResource(R.string.profile_change_avatar),
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.profile.displayName.ifBlank {
                        stringResource(R.string.profile_default_name)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(state.profile.trainingGoal.labelResource()),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(state.profile.experienceLevel.labelResource()),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.profile_edit),
                )
            }
        }
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            ProfileMetric(
                value = state.profile.completedWorkoutCount.toString(),
                label = stringResource(R.string.profile_metric_workouts),
            )
            ProfileMetric(
                value = state.profile.totalVolumeLabel,
                label = stringResource(R.string.profile_metric_volume),
            )
            ProfileMetric(
                value = state.profile.photos.size.toString(),
                label = stringResource(R.string.profile_metric_photos),
            )
        }
    }
}

@Composable
private fun ProfileMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun WeightEvolutionSection(
    state: SettingsUiState,
    onAddWeight: () -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.weight_evolution_eyebrow),
        title = stringResource(R.string.weight_evolution_title),
        description = stringResource(R.string.weight_evolution_description),
        icon = Icons.Outlined.Scale,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = state.profile.bodyWeightLabel ?: "-",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.weight_evolution_current),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.weight_evolution_add),
                onClick = onAddWeight,
                icon = Icons.Outlined.Add,
                iconContentDescription = null,
            )
        }
        if (state.profile.bodyWeightHistory.size >= 2) {
            BodyWeightChart(points = state.profile.bodyWeightHistory)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.profile.bodyWeightHistory.first().dateLabel,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = state.profile.bodyWeightHistory.last().dateLabel,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.weight_evolution_empty),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun BodyWeightChart(points: List<BodyWeightEntryUiModel>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = ForgeFlowDesign.colors.divider
    val min = points.minOf(BodyWeightEntryUiModel::value)
    val max = points.maxOf(BodyWeightEntryUiModel::value)
    val range = (max - min).coerceAtLeast(1.0)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = if (points.lastIndex == 0) 0f else size.width * index / points.lastIndex
            val y = size.height - (((point.value - min) / range).toFloat() * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { index, point ->
            val x = if (points.lastIndex == 0) 0f else size.width * index / points.lastIndex
            val y = size.height - (((point.value - min) / range).toFloat() * size.height)
            drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun ProgressPhotosShortcut(
    state: SettingsUiState,
    onOpen: () -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.progress_photos_eyebrow),
        title = stringResource(R.string.progress_photos_title),
        description = stringResource(R.string.profile_photos_shortcut_description),
        icon = Icons.Outlined.PhotoLibrary,
    ) {
        if (state.profile.photos.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                state.profile.photos.takeLast(2).forEach { photo ->
                    Column(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = File(photo.filePath),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            text = photo.dateLabel,
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        ForgeFlowButton(
            text = stringResource(R.string.profile_open_photos),
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.PhotoLibrary,
            iconContentDescription = null,
        )
    }
}

@Composable
private fun BodyWeightEditorDialog(
    editor: BodyWeightEditorUiState,
    unitLabel: String,
    onAction: (SettingsAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(SettingsAction.CloseBodyWeightEditor) },
        title = { Text(stringResource(R.string.weight_editor_title)) },
        text = {
            OutlinedTextField(
                value = editor.value,
                onValueChange = { onAction(SettingsAction.BodyWeightChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_weight)) },
                suffix = { Text(unitLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(SettingsAction.SaveBodyWeight) },
                enabled = !editor.isSaving && editor.value.isNotBlank(),
            ) {
                Text(stringResource(R.string.profile_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(SettingsAction.CloseBodyWeightEditor) }) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}

private fun WeightUnit.shortLabel(): String = if (this == WeightUnit.KILOGRAM) "kg" else "lb"

private fun TrainingGoal.labelResource(): Int = when (this) {
    TrainingGoal.STRENGTH -> R.string.profile_goal_strength
    TrainingGoal.HYPERTROPHY -> R.string.profile_goal_hypertrophy
    TrainingGoal.GENERAL_FITNESS -> R.string.profile_goal_fitness
}

private fun ExperienceLevel.labelResource(): Int = when (this) {
    ExperienceLevel.BEGINNER -> R.string.profile_experience_beginner
    ExperienceLevel.INTERMEDIATE -> R.string.profile_experience_intermediate
    ExperienceLevel.ADVANCED -> R.string.profile_experience_advanced
}
