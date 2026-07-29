package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowOutlinedButton
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.settings.R

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onOpenExercises: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(ForgeFlowDesign.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        ) {
            item {
                ForgeFlowPageHeader(
                    eyebrow = stringResource(R.string.settings_eyebrow),
                    title = stringResource(R.string.settings_title),
                    description = stringResource(R.string.settings_description),
                )
            }
            item {
                AppearanceCard(state = state, onAction = onAction)
            }
            item {
                TrainingPreferencesCard(state = state, onAction = onAction)
            }
            item {
                ForgeFlowOutlinedButton(
                    text = stringResource(R.string.open_exercise_library),
                    onClick = onOpenExercises,
                    icon = Icons.Outlined.FitnessCenter,
                    iconContentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AppearanceCard(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = stringResource(R.string.appearance_title),
            description = stringResource(R.string.appearance_description),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemePreference.entries.forEach { theme ->
                FilterChip(
                    selected = state.theme == theme,
                    onClick = { onAction(SettingsAction.ThemeChanged(theme)) },
                    label = { Text(text = stringResource(theme.labelResource())) },
                )
            }
        }
        Text(
            text = stringResource(R.string.accent_title),
            style = MaterialTheme.typography.titleMedium,
        )
        AccentPicker(
            selected = state.accent,
            onSelected = { onAction(SettingsAction.AccentChanged(it)) },
        )
        PreferenceToggle(
            title = stringResource(R.string.compact_mode_title),
            description = stringResource(R.string.compact_mode_description),
            checked = state.compactMode,
            onCheckedChange = { onAction(SettingsAction.CompactModeChanged(it)) },
        )
    }
}

@Composable
private fun TrainingPreferencesCard(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = stringResource(R.string.training_title),
            description = stringResource(R.string.training_description),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeightUnit.entries.forEach { unit ->
                FilterChip(
                    selected = state.weightUnit == unit,
                    onClick = { onAction(SettingsAction.WeightUnitChanged(unit)) },
                    label = { Text(text = stringResource(unit.labelResource())) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AccentPicker(
    selected: AccentColor,
    onSelected: (AccentColor) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AccentColor.entries.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                rowColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(
                                border = BorderStroke(
                                    width = if (selected == color) 3.dp else 1.dp,
                                    color = if (selected == color) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                ),
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                            .background(color.previewColor(), CircleShape)
                            .clickable { onSelected(color) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun ThemePreference.labelResource(): Int = when (this) {
    ThemePreference.DARK -> R.string.theme_dark
    ThemePreference.LIGHT -> R.string.theme_light
    ThemePreference.SYSTEM -> R.string.theme_system
}

private fun WeightUnit.labelResource(): Int = when (this) {
    WeightUnit.KILOGRAM -> R.string.weight_kg
    WeightUnit.POUND -> R.string.weight_lb
}

private fun AccentColor.previewColor(): Color = Color(
    when (this) {
        AccentColor.BLUE -> 0xFF3B82F6
        AccentColor.CYAN -> 0xFF06B6D4
        AccentColor.TEAL -> 0xFF14B8A6
        AccentColor.GREEN -> 0xFF10B981
        AccentColor.LIME -> 0xFF84CC16
        AccentColor.AMBER -> 0xFFF59E0B
        AccentColor.ORANGE -> 0xFFF97316
        AccentColor.RED -> 0xFFEF4444
        AccentColor.ROSE -> 0xFFF43F5E
        AccentColor.PINK -> 0xFFEC4899
        AccentColor.PURPLE -> 0xFF8B5CF6
        AccentColor.INDIGO -> 0xFF6366F1
    },
)
