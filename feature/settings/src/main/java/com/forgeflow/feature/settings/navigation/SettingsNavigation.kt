package com.forgeflow.feature.settings.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.SettingsRoute
import com.forgeflow.feature.settings.presentation.SettingsScreen
import com.forgeflow.feature.settings.presentation.SettingsViewModel

fun NavGraphBuilder.settingsScreen(onOpenExercises: () -> Unit) {
    composable<SettingsRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        SettingsScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenExercises = onOpenExercises,
        )
    }
}
