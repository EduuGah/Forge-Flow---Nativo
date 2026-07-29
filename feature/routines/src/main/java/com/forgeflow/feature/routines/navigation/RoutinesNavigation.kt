package com.forgeflow.feature.routines.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.RoutinesRoute
import com.forgeflow.feature.routines.presentation.RoutinesEvent
import com.forgeflow.feature.routines.presentation.RoutinesScreen
import com.forgeflow.feature.routines.presentation.RoutinesViewModel

fun NavGraphBuilder.routinesScreen(
    onOpenExercises: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    composable<RoutinesRoute> {
        val viewModel: RoutinesViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(viewModel) {
            viewModel.events.collect { event ->
                if (event == RoutinesEvent.OpenActiveWorkout) onOpenActiveWorkout()
            }
        }
        RoutinesScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenExercises = onOpenExercises,
            onOpenExercise = onOpenExercise,
        )
    }
}
