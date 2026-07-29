package com.forgeflow.feature.history.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.HistoryRoute
import com.forgeflow.feature.history.presentation.HistoryScreen
import com.forgeflow.feature.history.presentation.HistoryViewModel

fun NavGraphBuilder.historyScreen() {
    composable<HistoryRoute> {
        val viewModel: HistoryViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HistoryScreen(state = state)
    }
}
