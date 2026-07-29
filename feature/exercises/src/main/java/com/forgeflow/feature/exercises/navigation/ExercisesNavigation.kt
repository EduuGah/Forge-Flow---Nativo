package com.forgeflow.feature.exercises.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.ExercisesRoute
import com.forgeflow.feature.exercises.presentation.ExercisesScreen
import com.forgeflow.feature.exercises.presentation.ExercisesViewModel

fun NavController.navigateToExercises() {
    navigate(ExercisesRoute)
}

fun NavGraphBuilder.exercisesScreen(onBack: () -> Unit) {
    composable<ExercisesRoute> {
        val viewModel: ExercisesViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        ExercisesScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
        )
    }
}
