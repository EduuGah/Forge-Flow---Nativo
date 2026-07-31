package com.forgeflow.feature.workout.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.ActiveWorkoutRoute
import com.forgeflow.feature.workout.presentation.ActiveWorkoutEvent
import com.forgeflow.feature.workout.presentation.ActiveWorkoutScreen
import com.forgeflow.feature.workout.presentation.ActiveWorkoutViewModel

fun NavController.navigateToActiveWorkout() {
    navigate(ActiveWorkoutRoute) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.activeWorkoutScreen(
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    composable<ActiveWorkoutRoute> {
        val viewModel: ActiveWorkoutViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(viewModel) {
            viewModel.events.collect { event ->
                if (event == ActiveWorkoutEvent.Close) onBack()
            }
        }
        ActiveWorkoutScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
            onOpenExercise = onOpenExercise,
        )
    }
}
