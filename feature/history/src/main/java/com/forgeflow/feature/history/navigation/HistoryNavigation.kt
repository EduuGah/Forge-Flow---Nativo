package com.forgeflow.feature.history.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.HistoryRoute
import com.forgeflow.core.navigation.TrainingMapRoute
import com.forgeflow.feature.history.presentation.HistoryScreen
import com.forgeflow.feature.history.presentation.HistoryViewModel
import com.forgeflow.feature.history.presentation.TrainingMapScreen
import com.forgeflow.feature.history.presentation.TrainingMapViewModel

fun NavGraphBuilder.historyScreen(
    onOpenExercise: (String) -> Unit,
    onOpenTrainingMap: () -> Unit,
) {
    composable<HistoryRoute> {
        val viewModel: HistoryViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HistoryScreen(
            state = state,
            onOpenExercise = onOpenExercise,
            onOpenTrainingMap = onOpenTrainingMap,
            onAction = viewModel::onAction,
        )
    }
}

fun NavController.navigateToTrainingMap() {
    navigate(TrainingMapRoute)
}

fun NavGraphBuilder.trainingMapScreen(onBack: () -> Unit) {
    composable<TrainingMapRoute> {
        val viewModel: TrainingMapViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        TrainingMapScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
        )
    }
}
