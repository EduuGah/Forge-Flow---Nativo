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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeSelector(
    selected: ThemePreference,
    onSelected: (ThemePreference) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ThemePreference.entries.forEachIndexed { index, theme ->
            SegmentedButton(
                selected = selected == theme,
                onClick = { onSelected(theme) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemePreference.entries.size),
                label = { Text(stringResource(theme.labelResource())) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WeightUnitSelector(
    selected: WeightUnit,
    onSelected: (WeightUnit) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        WeightUnit.entries.forEachIndexed { index, unit ->
            SegmentedButton(
                selected = selected == unit,
                onClick = { onSelected(unit) },
                shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                label = { Text(stringResource(unit.labelResource())) },
            )
        }
    }
}

@Composable
internal fun PreferenceLabel(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = description,
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun AccentPicker(
    selected: AccentColor,
    onSelected: (AccentColor) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        AccentColor.entries.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                rowColors.forEach { color ->
                    val label = stringResource(color.labelResource())
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .semantics {
                                contentDescription = label
                                role = Role.RadioButton
                            }
                            .border(
                                BorderStroke(
                                    width = if (selected == color) 3.dp else 1.dp,
                                    color = if (selected == color) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                ),
                                CircleShape,
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
internal fun PreferenceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = ForgeFlowDesign.spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@StringRes
private fun ThemePreference.labelResource(): Int = when (this) {
    ThemePreference.DARK -> R.string.theme_dark
    ThemePreference.LIGHT -> R.string.theme_light
    ThemePreference.SYSTEM -> R.string.theme_system
}

@StringRes
private fun WeightUnit.labelResource(): Int = when (this) {
    WeightUnit.KILOGRAM -> R.string.weight_kg
    WeightUnit.POUND -> R.string.weight_lb
}

@StringRes
private fun AccentColor.labelResource(): Int = when (this) {
    AccentColor.BLUE -> R.string.accent_blue
    AccentColor.CYAN -> R.string.accent_cyan
    AccentColor.TEAL -> R.string.accent_teal
    AccentColor.GREEN -> R.string.accent_green
    AccentColor.LIME -> R.string.accent_lime
    AccentColor.AMBER -> R.string.accent_amber
    AccentColor.ORANGE -> R.string.accent_orange
    AccentColor.RED -> R.string.accent_red
    AccentColor.ROSE -> R.string.accent_rose
    AccentColor.PINK -> R.string.accent_pink
    AccentColor.PURPLE -> R.string.accent_purple
    AccentColor.INDIGO -> R.string.accent_indigo
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
