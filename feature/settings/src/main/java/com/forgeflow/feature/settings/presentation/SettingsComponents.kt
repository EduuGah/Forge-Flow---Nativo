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
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ViewCompact
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
