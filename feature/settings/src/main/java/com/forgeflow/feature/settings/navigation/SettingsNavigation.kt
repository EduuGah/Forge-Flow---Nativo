package com.forgeflow.feature.settings.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.SettingsRoute
import com.forgeflow.feature.settings.presentation.SettingsAction
import com.forgeflow.feature.settings.presentation.SettingsScreen
import com.forgeflow.feature.settings.presentation.SettingsViewModel

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val permissionContract = remember(viewModel) {
            viewModel.createHealthPermissionContract()
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = permissionContract,
        ) { grantedPermissions ->
            viewModel.onAction(
                SettingsAction.HealthConnectPermissionsResult(grantedPermissions),
            )
        }
        LifecycleResumeEffect(viewModel) {
            viewModel.onAction(
                SettingsAction.HealthConnectRefresh,
            )
            onPauseOrDispose {}
        }
        SettingsScreen(
            state = state,
            onAction = viewModel::onAction,
            onRequestHealthPermissions = {
                permissionLauncher.launch(viewModel.requiredHealthPermissions)
            },
        )
    }
}
