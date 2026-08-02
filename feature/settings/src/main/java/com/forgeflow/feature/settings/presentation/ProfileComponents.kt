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
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WorkspacePremium
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.SupporterTier
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
internal fun DataProtectionSection(
    state: SettingsUiState,
    onExport: () -> Unit,
    onRestore: () -> Unit,
    onRestart: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    onDismissResult: () -> Unit,
) {
    var confirmCloudRestore by remember { mutableStateOf(false) }
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
        HorizontalDivider(color = ForgeFlowDesign.colors.divider)
        ForgeFlowButton(
            text = if (state.isExportingData) {
                stringResource(R.string.data_exporting)
            } else {
                stringResource(R.string.data_export_action)
            },
            onClick = onExport,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isExportingData,
            icon = Icons.Outlined.Download,
            iconContentDescription = null,
        )
        ForgeFlowOutlinedButton(
            text = if (state.isRestoringData) {
                stringResource(R.string.data_restoring)
            } else {
                stringResource(R.string.data_restore_action)
            },
            onClick = onRestore,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isRestoringData,
            icon = Icons.Outlined.Restore,
            iconContentDescription = null,
        )
        if (state.account.isSignedIn) {
            HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            Text(
                text = stringResource(R.string.data_cloud_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.data_cloud_description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            ForgeFlowButton(
                text = if (state.isCloudBackupRunning) {
                    stringResource(R.string.data_cloud_working)
                } else {
                    stringResource(R.string.data_cloud_save)
                },
                onClick = { onAction(SettingsAction.SaveCloudBackup) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isCloudBackupRunning,
                icon = Icons.Outlined.CloudUpload,
                iconContentDescription = null,
            )
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.data_cloud_restore),
                onClick = { confirmCloudRestore = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isCloudBackupRunning,
                icon = Icons.Outlined.Restore,
                iconContentDescription = null,
            )
        } else {
            Text(
                text = stringResource(R.string.data_cloud_requires_account),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        state.dataExportResult?.let { result ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (result == DataExportUiResult.SUCCESS) {
                            R.string.data_export_success
                        } else {
                            R.string.data_export_failed
                        },
                    ),
                    modifier = Modifier.weight(1f),
                    color = if (result == DataExportUiResult.SUCCESS) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                IconButton(onClick = onDismissResult) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.data_export_dismiss),
                    )
                }
            }
        }
        val restoreReady = state.dataRestoreResult == DataRestoreUiResult.READY_TO_RESTART ||
            state.cloudBackupResult == CloudBackupUiResult.RESTORE_READY_TO_RESTART
        val operationMessage = when {
            restoreReady -> R.string.data_restore_ready
            state.dataRestoreResult == DataRestoreUiResult.FAILED -> R.string.data_restore_failed
            state.cloudBackupResult == CloudBackupUiResult.BACKUP_SAVED ->
                R.string.data_cloud_saved
            state.cloudBackupResult == CloudBackupUiResult.FAILED -> R.string.data_cloud_failed
            else -> null
        }
        operationMessage?.let { message ->
            Text(
                text = stringResource(message),
                color = if (restoreReady || state.cloudBackupResult == CloudBackupUiResult.BACKUP_SAVED) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (restoreReady) {
            ForgeFlowButton(
                text = stringResource(R.string.data_restart_now),
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.Restore,
                iconContentDescription = null,
            )
        }
    }
    if (confirmCloudRestore) {
        AlertDialog(
            onDismissRequest = { confirmCloudRestore = false },
            title = { Text(stringResource(R.string.data_restore_confirm_title)) },
            text = { Text(stringResource(R.string.data_restore_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmCloudRestore = false
                        onAction(SettingsAction.RestoreCloudBackup)
                    },
                ) {
                    Text(stringResource(R.string.data_restore_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCloudRestore = false }) {
                    Text(stringResource(R.string.data_restore_cancel))
                }
            },
        )
    }
}

@Composable
internal fun AccountSection(
    state: SettingsUiState,
    onGoogleSignIn: () -> Unit,
    onAction: (SettingsAction) -> Unit,
) {
    var showDonationOptions by remember { mutableStateOf(false) }
    SettingsSection(
        eyebrow = stringResource(R.string.account_eyebrow),
        title = stringResource(R.string.account_title),
        description = stringResource(R.string.account_description),
        icon = Icons.Outlined.AccountCircle,
    ) {
        if (!state.account.isLoaded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text(
                    text = stringResource(R.string.account_loading),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else if (state.account.isSignedIn) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.account.photoUrl != null) {
                    AsyncImage(
                        model = state.account.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.account.displayName?.takeIf(String::isNotBlank)
                            ?: state.profile.displayName.takeIf(String::isNotBlank)
                            ?: stringResource(R.string.account_google_user),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    state.account.email?.let { email ->
                        Text(
                            text = email,
                            color = ForgeFlowDesign.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        text = stringResource(R.string.account_connected),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            state.account.supporterTier?.let { tier ->
                SupporterBenefitsPanel(tier)
            }
            DonationInvitation(onOpen = { showDonationOptions = true })
            if (!state.account.hasPassword && state.account.email != null) {
                ForgeFlowOutlinedButton(
                    text = stringResource(R.string.account_create_password),
                    onClick = {
                        onAction(SettingsAction.OpenAccountAuth(AccountAuthMode.CREATE_PASSWORD))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.Lock,
                    iconContentDescription = null,
                )
            }
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.account_sign_out),
                onClick = { onAction(SettingsAction.SignOut) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = stringResource(
                    if (state.account.isConfigured) {
                        R.string.account_sign_in_explanation
                    } else {
                        R.string.account_configuration_pending
                    },
                ),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            ForgeFlowButton(
                text = if (state.account.isSigningIn) {
                    stringResource(R.string.account_signing_in)
                } else {
                    stringResource(R.string.account_sign_in_google)
                },
                onClick = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.account.isConfigured && !state.account.isSigningIn,
                icon = Icons.Outlined.AccountCircle,
                iconContentDescription = null,
            )
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.account_sign_in_email),
                onClick = { onAction(SettingsAction.OpenAccountAuth(AccountAuthMode.SIGN_IN)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.account.isConfigured && !state.account.isSigningIn,
                icon = Icons.Outlined.Lock,
                iconContentDescription = null,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = {
                        onAction(SettingsAction.OpenAccountAuth(AccountAuthMode.CREATE_ACCOUNT))
                    },
                ) {
                    Text(stringResource(R.string.account_create_account))
                }
                TextButton(
                    onClick = {
                        onAction(SettingsAction.OpenAccountAuth(AccountAuthMode.RESET_PASSWORD))
                    },
                ) {
                    Text(stringResource(R.string.account_forgot_password))
                }
            }
        }
        state.account.notice?.let { notice ->
            Text(
                text = stringResource(notice.messageResource()),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (state.account.operationFailed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.account_operation_failed),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(onClick = { onAction(SettingsAction.DismissAccountError) }) {
                    Text(stringResource(R.string.account_error_dismiss))
                }
            }
        }
    }
    if (showDonationOptions) {
        DonationOptionsDialog(onDismiss = { showDonationOptions = false })
    }
    state.account.editor?.let { editor ->
        AccountAuthDialog(editor = editor, onAction = onAction)
    }
}

@Composable
private fun AccountAuthDialog(
    editor: AccountAuthEditorUiState,
    onAction: (SettingsAction) -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmationVisible by remember { mutableStateOf(false) }
    val needsPassword = editor.mode != AccountAuthMode.RESET_PASSWORD
    val needsConfirmation = editor.mode in setOf(
        AccountAuthMode.CREATE_ACCOUNT,
        AccountAuthMode.CREATE_PASSWORD,
    )
    AlertDialog(
        onDismissRequest = { if (!editor.isSaving) onAction(SettingsAction.CloseAccountAuth) },
        title = { Text(stringResource(editor.mode.titleResource())) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
                if (editor.mode != AccountAuthMode.CREATE_PASSWORD) {
                    ForgeFlowTextField(
                        value = editor.email,
                        onValueChange = { onAction(SettingsAction.AccountEmailChanged(it)) },
                        label = stringResource(R.string.account_email_label),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.account_create_password_explanation),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (needsPassword) {
                    ForgeFlowTextField(
                        value = editor.password,
                        onValueChange = { onAction(SettingsAction.AccountPasswordChanged(it)) },
                        label = stringResource(R.string.account_password_label),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            PasswordVisibilityButton(
                                visible = passwordVisible,
                                onClick = { passwordVisible = !passwordVisible },
                            )
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    )
                }
                if (needsConfirmation) {
                    ForgeFlowTextField(
                        value = editor.passwordConfirmation,
                        onValueChange = {
                            onAction(SettingsAction.AccountPasswordConfirmationChanged(it))
                        },
                        label = stringResource(R.string.account_password_confirmation_label),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            PasswordVisibilityButton(
                                visible = confirmationVisible,
                                onClick = { confirmationVisible = !confirmationVisible },
                            )
                        },
                        visualTransformation = if (confirmationVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    )
                }
                if (editor.validationFailed) {
                    Text(
                        text = stringResource(R.string.account_validation_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(SettingsAction.SubmitAccountAuth) },
                enabled = !editor.isSaving,
            ) {
                Text(
                    if (editor.isSaving) {
                        stringResource(R.string.account_saving)
                    } else {
                        stringResource(editor.mode.actionResource())
                    },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(SettingsAction.CloseAccountAuth) },
                enabled = !editor.isSaving,
            ) {
                Text(stringResource(R.string.data_restore_cancel))
            }
        },
    )
}

@Composable
private fun PasswordVisibilityButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = stringResource(
                if (visible) R.string.account_hide_password else R.string.account_show_password,
            ),
        )
    }
}

@Composable
private fun SupporterBenefitsPanel(tier: SupporterTier) {
    val containerColor = when (tier) {
        SupporterTier.SUPPORTER -> MaterialTheme.colorScheme.secondaryContainer
        SupporterTier.PRO -> MaterialTheme.colorScheme.primaryContainer
        SupporterTier.FOUNDER -> MaterialTheme.colorScheme.tertiaryContainer
        SupporterTier.LIFETIME -> MaterialTheme.colorScheme.inverseSurface
    }
    val contentColor = if (tier == SupporterTier.LIFETIME) {
        MaterialTheme.colorScheme.inverseOnSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = contentColor,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.account_supporter_status),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = stringResource(tier.labelResource()),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            Text(
                text = stringResource(R.string.account_supporter_benefits_title),
                style = MaterialTheme.typography.labelMedium,
            )
            SupporterBenefitRow(stringResource(R.string.account_supporter_benefit_badge))
            SupporterBenefitRow(stringResource(R.string.account_supporter_benefit_account))
            SupporterBenefitRow(stringResource(R.string.account_supporter_benefit_future))
        }
    }
}

@Composable
private fun DonationInvitation(onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.account_donation_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.account_donation_description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            ForgeFlowOutlinedButton(
                text = stringResource(R.string.account_donation_action),
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.WorkspacePremium,
                iconContentDescription = null,
            )
        }
    }
}

@Composable
private fun DonationOptionsDialog(onDismiss: () -> Unit) {
    val options = listOf(
        DonationOption(
            tag = R.string.account_supporter,
            amount = R.string.account_donation_supporter_amount,
            description = R.string.account_donation_supporter_description,
        ),
        DonationOption(
            tag = R.string.account_supporter_pro,
            amount = R.string.account_donation_pro_amount,
            description = R.string.account_donation_pro_description,
        ),
        DonationOption(
            tag = R.string.account_supporter_founder,
            amount = R.string.account_donation_founder_amount,
            description = R.string.account_donation_founder_description,
        ),
        DonationOption(
            tag = R.string.account_supporter_lifetime,
            amount = R.string.account_donation_lifetime_amount,
            description = R.string.account_donation_lifetime_description,
        ),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_donation_options_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.account_donation_options_description),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                options.forEach { option -> DonationOptionRow(option) }
                Text(
                    text = stringResource(R.string.account_donation_payment_pending),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.account_donation_close))
            }
        },
    )
}

@Composable
private fun DonationOptionRow(option: DonationOption) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(option.tag),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(option.amount),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = stringResource(option.description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class DonationOption(
    @param:StringRes val tag: Int,
    @param:StringRes val amount: Int,
    @param:StringRes val description: Int,
)

@Composable
private fun SupporterBenefitRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun AccountAuthMode.titleResource(): Int = when (this) {
    AccountAuthMode.SIGN_IN -> R.string.account_email_sign_in_title
    AccountAuthMode.CREATE_ACCOUNT -> R.string.account_create_title
    AccountAuthMode.RESET_PASSWORD -> R.string.account_reset_title
    AccountAuthMode.CREATE_PASSWORD -> R.string.account_create_password_title
}

private fun AccountAuthMode.actionResource(): Int = when (this) {
    AccountAuthMode.SIGN_IN -> R.string.account_sign_in_action
    AccountAuthMode.CREATE_ACCOUNT -> R.string.account_create_action
    AccountAuthMode.RESET_PASSWORD -> R.string.account_reset_action
    AccountAuthMode.CREATE_PASSWORD -> R.string.account_create_password_action
}

private fun AccountNoticeUi.messageResource(): Int = when (this) {
    AccountNoticeUi.ACCOUNT_CREATED -> R.string.account_created_notice
    AccountNoticeUi.PASSWORD_RESET_SENT -> R.string.account_reset_sent
    AccountNoticeUi.PASSWORD_CREATED -> R.string.account_password_created
}

private fun SupporterTier.labelResource(): Int = when (this) {
    SupporterTier.SUPPORTER -> R.string.account_supporter
    SupporterTier.PRO -> R.string.account_supporter_pro
    SupporterTier.FOUNDER -> R.string.account_supporter_founder
    SupporterTier.LIFETIME -> R.string.account_supporter_lifetime
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
