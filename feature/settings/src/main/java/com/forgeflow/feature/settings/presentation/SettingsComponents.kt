package com.forgeflow.feature.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.platform.health.HealthConnectAvailability
import com.forgeflow.feature.settings.R

@Composable
internal fun AppearanceSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.appearance_eyebrow),
        title = stringResource(R.string.appearance_title),
        description = stringResource(R.string.appearance_description),
        icon = Icons.Outlined.Palette,
    ) {
        PreferenceLabel(
            title = stringResource(R.string.theme_title),
            description = stringResource(R.string.theme_description),
        )
        ThemeSelector(
            selected = state.theme,
            onSelected = { onAction(SettingsAction.ThemeChanged(it)) },
        )
        PreferenceLabel(
            title = stringResource(R.string.accent_title),
            description = stringResource(R.string.accent_description),
        )
        AccentPicker(
            selected = state.accent,
            onSelected = { onAction(SettingsAction.AccentChanged(it)) },
        )
    }
}

@Composable
internal fun LayoutSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.layout_eyebrow),
        title = stringResource(R.string.layout_title),
        description = stringResource(R.string.layout_description),
        icon = Icons.Outlined.ViewCompact,
    ) {
        PreferenceToggle(
            title = stringResource(R.string.compact_mode_title),
            description = stringResource(R.string.compact_mode_description),
            checked = state.compactMode,
            onCheckedChange = { onAction(SettingsAction.CompactModeChanged(it)) },
        )
    }
}

@Composable
internal fun TrainingSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.training_eyebrow),
        title = stringResource(R.string.training_title),
        description = stringResource(R.string.training_description),
        icon = Icons.Outlined.FitnessCenter,
    ) {
        PreferenceLabel(
            title = stringResource(R.string.weight_unit_title),
            description = stringResource(R.string.weight_unit_description),
        )
        WeightUnitSelector(
            selected = state.weightUnit,
            onSelected = { onAction(SettingsAction.WeightUnitChanged(it)) },
        )
    }
}

@Composable
internal fun HealthConnectSection(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onRequestPermissions: () -> Unit,
) {
    SettingsSection(
        eyebrow = stringResource(R.string.health_connect_eyebrow),
        title = stringResource(R.string.health_connect_title),
        description = stringResource(R.string.health_connect_description),
        icon = Icons.Outlined.HealthAndSafety,
    ) {
        HealthConnectStatus(state = state)
        when {
            state.isCheckingHealthConnect -> Unit
            state.healthConnectAvailability == HealthConnectAvailability.UPDATE_REQUIRED -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(SettingsAction.InstallHealthConnect) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.health_connect_install),
                        modifier = Modifier.padding(start = ForgeFlowDesign.spacing.small),
                    )
                }
            }
            state.healthConnectAvailability == HealthConnectAvailability.UNAVAILABLE -> {
                Text(
                    text = stringResource(R.string.health_connect_unavailable_description),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            !state.healthConnectHasPermissions -> {
                Text(
                    text = stringResource(R.string.health_connect_permission_description),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestPermissions,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HealthAndSafety,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.health_connect_connect),
                        modifier = Modifier.padding(start = ForgeFlowDesign.spacing.small),
                    )
                }
            }
            else -> {
                PreferenceToggle(
                    title = stringResource(R.string.health_connect_sync_title),
                    description = stringResource(R.string.health_connect_sync_description),
                    checked = state.healthConnectSyncEnabled,
                    onCheckedChange = {
                        onAction(SettingsAction.HealthConnectSyncChanged(it))
                    },
                )
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSyncingHealthHistory,
                    onClick = { onAction(SettingsAction.HealthConnectSyncHistory) },
                ) {
                    if (state.isSyncingHealthHistory) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = stringResource(R.string.health_connect_sync_history),
                        modifier = Modifier.padding(start = ForgeFlowDesign.spacing.small),
                    )
                }
                state.healthSyncResult?.let { result ->
                    val message = if (result.failedCount == 0) {
                        stringResource(
                            R.string.health_connect_sync_success,
                            result.syncedCount,
                        )
                    } else {
                        stringResource(
                            R.string.health_connect_sync_partial,
                            result.syncedCount,
                            result.failedCount,
                        )
                    }
                    Text(
                        text = message,
                        color = if (result.failedCount == 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(SettingsAction.OpenHealthConnect) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.health_connect_manage),
                        modifier = Modifier.padding(start = ForgeFlowDesign.spacing.small),
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthConnectStatus(state: SettingsUiState) {
    val status = when {
        state.isCheckingHealthConnect -> stringResource(R.string.health_connect_checking)
        state.healthConnectAvailability == HealthConnectAvailability.UPDATE_REQUIRED ->
            stringResource(R.string.health_connect_update_required)
        state.healthConnectAvailability == HealthConnectAvailability.UNAVAILABLE ->
            stringResource(R.string.health_connect_unavailable)
        state.healthConnectHasPermissions ->
            stringResource(R.string.health_connect_connected)
        else -> stringResource(R.string.health_connect_ready)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isCheckingHealthConnect) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = when {
                            state.healthConnectHasPermissions ->
                                MaterialTheme.colorScheme.primary
                            state.healthConnectAvailability ==
                                HealthConnectAvailability.UNAVAILABLE ->
                                MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                        shape = CircleShape,
                    ),
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun SettingsSection(
    eyebrow: String,
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = eyebrow)
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = description,
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        ForgeFlowCard(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}
