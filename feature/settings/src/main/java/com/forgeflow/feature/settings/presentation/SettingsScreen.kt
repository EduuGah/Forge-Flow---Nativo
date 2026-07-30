package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowPageHeader
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.settings.R

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onRequestHealthPermissions: () -> Unit,
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
                    eyebrow = stringResource(R.string.settings_eyebrow),
                    title = stringResource(R.string.settings_title),
                    description = stringResource(R.string.settings_description),
                )
            }
            item {
                AppearanceSection(state = state, onAction = onAction)
            }
            item {
                LayoutSection(state = state, onAction = onAction)
            }
            item {
                TrainingSection(state = state, onAction = onAction)
            }
            item {
                HealthConnectSection(
                    state = state,
                    onAction = onAction,
                    onRequestPermissions = onRequestHealthPermissions,
                )
            }
        }
    }
}
