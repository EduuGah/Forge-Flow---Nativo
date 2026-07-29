package com.forgeflow.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.workout.R

@Composable
internal fun WorkoutBottomActions(
    finishEnabled: Boolean,
    onFinish: () -> Unit,
    onDiscard: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = ForgeFlowDesign.spacing.extraSmall,
    ) {
        Column {
            HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(ForgeFlowDesign.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
            ) {
                ForgeFlowOutlinedButton(
                    text = stringResource(R.string.discard_workout),
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.DeleteOutline,
                    iconContentDescription = null,
                )
                ForgeFlowButton(
                    text = stringResource(R.string.finish_workout),
                    onClick = onFinish,
                    modifier = Modifier.weight(1f),
                    enabled = finishEnabled,
                )
            }
        }
    }
}

@Composable
internal fun InvalidWorkoutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.invalid_workout_title)) },
        text = { Text(stringResource(R.string.invalid_workout_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.invalid_workout_confirm))
            }
        },
    )
}

@Composable
internal fun DiscardWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.discard_dialog_title)) },
        text = { Text(stringResource(R.string.discard_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.discard_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.discard_cancel))
            }
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun FinishWorkoutSheet(
    includeLocation: Boolean,
    locationLabel: String,
    locationPermissionDenied: Boolean,
    isFinishing: Boolean,
    onIncludeLocationChanged: (Boolean) -> Unit,
    onLocationLabelChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(
                    start = ForgeFlowDesign.spacing.screenHorizontal,
                    end = ForgeFlowDesign.spacing.screenHorizontal,
                    bottom = ForgeFlowDesign.spacing.section,
                ),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        ) {
            Text(
                text = stringResource(R.string.finish_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.finish_sheet_description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.save_workout_location),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.workout_location_description),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = includeLocation,
                    onCheckedChange = onIncludeLocationChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
            if (includeLocation) {
                OutlinedTextField(
                    value = locationLabel,
                    onValueChange = onLocationLabelChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.location_name_label)) },
                    placeholder = { Text(stringResource(R.string.location_name_placeholder)) },
                    supportingText = {
                        Text(
                            text = if (locationPermissionDenied) {
                                stringResource(R.string.location_permission_denied)
                            } else {
                                stringResource(R.string.location_name_support)
                            },
                            color = if (locationPermissionDenied) {
                                MaterialTheme.colorScheme.error
                            } else {
                                ForgeFlowDesign.colors.textSecondary
                            },
                        )
                    },
                    singleLine = true,
                )
            }
            ForgeFlowButton(
                text = if (isFinishing) {
                    stringResource(R.string.capturing_workout_location)
                } else {
                    stringResource(R.string.save_workout)
                },
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isFinishing,
            )
        }
    }
}
