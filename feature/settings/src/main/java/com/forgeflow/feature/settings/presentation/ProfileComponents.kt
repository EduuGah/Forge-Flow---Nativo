package com.forgeflow.feature.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.feature.settings.R
import java.io.File

@Composable
internal fun ProfileSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.profile_eyebrow),
        title = state.profile.displayName.ifBlank {
            stringResource(R.string.profile_default_name)
        },
        description = stringResource(state.profile.trainingGoal.labelResource()),
        icon = Icons.Outlined.AccountCircle,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ProfileValue(
                label = stringResource(R.string.profile_weight),
                value = state.profile.bodyWeightLabel ?: "-",
            )
            ProfileValue(
                label = stringResource(R.string.profile_height),
                value = state.profile.heightCentimeters?.let {
                    stringResource(R.string.profile_height_value, it)
                } ?: "-",
            )
            ProfileValue(
                label = stringResource(R.string.profile_birth_year),
                value = state.profile.birthYear?.toString() ?: "-",
            )
        }
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_experience),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = stringResource(state.profile.experienceLevel.labelResource()),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.profile_edit),
                onClick = { onAction(SettingsAction.OpenProfileEditor) },
                icon = Icons.Outlined.Edit,
                iconContentDescription = null,
            )
        }
    }
}

@Composable
private fun ProfileValue(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
internal fun ProgressPhotosSection(
    state: SettingsUiState,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.progress_photos_eyebrow),
        title = stringResource(R.string.progress_photos_title),
        description = stringResource(R.string.progress_photos_description),
        icon = Icons.Outlined.PhotoCamera,
    ) {
        when (state.profile.photos.size) {
            0 -> ProgressPhotosEmpty()
            1 -> SingleProgressPhoto(
                photo = state.profile.photos.first(),
                onDelete = onDeletePhoto,
            )
            else -> ProgressPhotoComparison(
                photos = state.profile.photos,
                onDelete = onDeletePhoto,
            )
        }
        ForgeFlowButton(
            text = stringResource(R.string.progress_photos_add),
            onClick = onAddPhoto,
            enabled = !state.isImportingPhoto,
            modifier = Modifier.fillMaxWidth(),
            icon = if (state.isImportingPhoto) null else Icons.Outlined.AddAPhoto,
            iconContentDescription = null,
        )
        if (state.isImportingPhoto) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(
                    text = stringResource(R.string.progress_photos_importing),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (state.profileWriteFailed) {
            Text(
                text = stringResource(R.string.profile_save_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ProgressPhotosEmpty() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.1f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = null,
                tint = ForgeFlowDesign.colors.textSecondary,
            )
            Text(
                text = stringResource(R.string.progress_photos_empty),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SingleProgressPhoto(
    photo: ProgressPhotoUiModel,
    onDelete: (String) -> Unit,
) {
    ProgressPhotoFrame(
        label = stringResource(R.string.progress_photos_first),
        photo = photo,
        onDelete = onDelete,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProgressPhotoComparison(
    photos: List<ProgressPhotoUiModel>,
    onDelete: (String) -> Unit,
) {
    val photoKey = photos.joinToString(separator = "|", transform = ProgressPhotoUiModel::id)
    var beforeIndex by remember(photoKey) { mutableIntStateOf(0) }
    var afterIndex by remember(photoKey) { mutableIntStateOf(photos.lastIndex) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
    ) {
        ProgressPhotoFrame(
            label = stringResource(R.string.progress_photos_before),
            photo = photos[beforeIndex],
            onDelete = onDelete,
            onPrevious = if (beforeIndex > 0) {
                { beforeIndex -= 1 }
            } else {
                null
            },
            onNext = if (beforeIndex < afterIndex - 1) {
                { beforeIndex += 1 }
            } else {
                null
            },
            modifier = Modifier.weight(1f),
        )
        ProgressPhotoFrame(
            label = stringResource(R.string.progress_photos_after),
            photo = photos[afterIndex],
            onDelete = onDelete,
            onPrevious = if (afterIndex > beforeIndex + 1) {
                { afterIndex -= 1 }
            } else {
                null
            },
            onNext = if (afterIndex < photos.lastIndex) {
                { afterIndex += 1 }
            } else {
                null
            },
            modifier = Modifier.weight(1f),
        )
    }
    Text(
        text = pluralStringResource(
            R.plurals.progress_photos_count,
            photos.size,
            photos.size,
        ),
        color = ForgeFlowDesign.colors.textSecondary,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun ProgressPhotoFrame(
    label: String,
    photo: ProgressPhotoUiModel,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label.uppercase(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
            )
            IconButton(
                onClick = { onDelete(photo.id) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.progress_photos_delete),
                    modifier = Modifier.size(18.dp),
                    tint = ForgeFlowDesign.colors.textSecondary,
                )
            }
        }
        AsyncImage(
            model = File(photo.filePath),
            contentDescription = stringResource(R.string.progress_photos_image, label),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.78f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onPrevious?.invoke() },
                enabled = onPrevious != null,
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = stringResource(R.string.progress_photos_previous),
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = photo.dateLabel,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
            IconButton(
                onClick = { onNext?.invoke() },
                enabled = onNext != null,
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.progress_photos_next),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
internal fun DataProtectionSection() {
    SettingsSection(
        eyebrow = stringResource(R.string.data_eyebrow),
        title = stringResource(R.string.data_title),
        description = stringResource(R.string.data_description),
        icon = Icons.Outlined.CloudDone,
    ) {
        DataProtectionRow(
            title = stringResource(R.string.data_local_title),
            description = stringResource(R.string.data_local_description),
        )
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        DataProtectionRow(
            title = stringResource(R.string.data_backup_title),
            description = stringResource(R.string.data_backup_description),
        )
    }
}

@Composable
private fun DataProtectionRow(
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = description,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun ProfileEditorDialog(
    editor: ProfileEditorUiState,
    weightUnitLabel: String,
    onAction: (SettingsAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(SettingsAction.CloseProfileEditor) },
        title = { Text(stringResource(R.string.profile_editor_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                OutlinedTextField(
                    value = editor.displayName,
                    onValueChange = { onAction(SettingsAction.ProfileNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                ) {
                    NumberField(
                        value = editor.birthYear,
                        onValueChange = {
                            onAction(SettingsAction.ProfileBirthYearChanged(it))
                        },
                        label = stringResource(R.string.profile_birth_year),
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        value = editor.heightCentimeters,
                        onValueChange = {
                            onAction(SettingsAction.ProfileHeightChanged(it))
                        },
                        label = stringResource(R.string.profile_height),
                        suffix = "cm",
                        modifier = Modifier.weight(1f),
                    )
                }
                NumberField(
                    value = editor.bodyWeight,
                    onValueChange = {
                        onAction(SettingsAction.ProfileBodyWeightChanged(it))
                    },
                    label = stringResource(R.string.profile_weight),
                    suffix = weightUnitLabel,
                    decimal = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.profile_goal),
                    style = MaterialTheme.typography.labelLarge,
                )
                TrainingGoalSelector(
                    selected = editor.trainingGoal,
                    onSelected = { onAction(SettingsAction.ProfileGoalChanged(it)) },
                )
                Text(
                    text = stringResource(R.string.profile_experience),
                    style = MaterialTheme.typography.labelLarge,
                )
                ExperienceSelector(
                    selected = editor.experienceLevel,
                    onSelected = { onAction(SettingsAction.ProfileExperienceChanged(it)) },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(SettingsAction.SaveProfile) },
                enabled = !editor.isSaving,
            ) {
                Text(
                    stringResource(
                        if (editor.isSaving) R.string.profile_saving else R.string.profile_save,
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(SettingsAction.CloseProfileEditor) },
                enabled = !editor.isSaving,
            ) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    suffix: String? = null,
    decimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        singleLine = true,
    )
}

@Composable
private fun TrainingGoalSelector(
    selected: TrainingGoal,
    onSelected: (TrainingGoal) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TrainingGoal.entries.forEachIndexed { index, goal ->
            SegmentedButton(
                selected = selected == goal,
                onClick = { onSelected(goal) },
                shape = SegmentedButtonDefaults.itemShape(index, TrainingGoal.entries.size),
                label = {
                    Text(
                        text = stringResource(goal.shortLabelResource()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

@Composable
private fun ExperienceSelector(
    selected: ExperienceLevel,
    onSelected: (ExperienceLevel) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ExperienceLevel.entries.forEachIndexed { index, level ->
            SegmentedButton(
                selected = selected == level,
                onClick = { onSelected(level) },
                shape = SegmentedButtonDefaults.itemShape(index, ExperienceLevel.entries.size),
                label = {
                    Text(
                        text = stringResource(level.shortLabelResource()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

@Composable
internal fun DeleteProgressPhotoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.progress_photos_delete_title)) },
        text = { Text(stringResource(R.string.progress_photos_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.progress_photos_delete_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}

@StringRes
private fun TrainingGoal.labelResource(): Int = when (this) {
    TrainingGoal.STRENGTH -> R.string.profile_goal_strength
    TrainingGoal.HYPERTROPHY -> R.string.profile_goal_hypertrophy
    TrainingGoal.GENERAL_FITNESS -> R.string.profile_goal_fitness
}

@StringRes
private fun TrainingGoal.shortLabelResource(): Int = when (this) {
    TrainingGoal.STRENGTH -> R.string.profile_goal_strength_short
    TrainingGoal.HYPERTROPHY -> R.string.profile_goal_hypertrophy_short
    TrainingGoal.GENERAL_FITNESS -> R.string.profile_goal_fitness_short
}

@StringRes
private fun ExperienceLevel.labelResource(): Int = when (this) {
    ExperienceLevel.BEGINNER -> R.string.profile_experience_beginner
    ExperienceLevel.INTERMEDIATE -> R.string.profile_experience_intermediate
    ExperienceLevel.ADVANCED -> R.string.profile_experience_advanced
}

@StringRes
private fun ExperienceLevel.shortLabelResource(): Int = when (this) {
    ExperienceLevel.BEGINNER -> R.string.profile_experience_beginner_short
    ExperienceLevel.INTERMEDIATE -> R.string.profile_experience_intermediate_short
    ExperienceLevel.ADVANCED -> R.string.profile_experience_advanced_short
}
