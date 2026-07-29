package com.forgeflow.feature.exercises.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.ExerciseDetailsRoute
import com.forgeflow.core.navigation.ExercisesRoute
import com.forgeflow.feature.exercises.presentation.ExerciseDetailsScreen
import com.forgeflow.feature.exercises.presentation.ExerciseDetailsViewModel
import com.forgeflow.feature.exercises.presentation.ExercisesScreen
import com.forgeflow.feature.exercises.presentation.ExercisesViewModel

fun NavController.navigateToExercises() {
    navigate(ExercisesRoute)
}

fun NavController.navigateToExerciseDetails(exerciseId: String) {
    navigate(ExerciseDetailsRoute(exerciseId))
}

fun NavGraphBuilder.exercisesScreen(
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    composable<ExercisesRoute> {
        val viewModel: ExercisesViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        ExercisesScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
            onOpenExercise = onOpenExercise,
        )
    }
}

fun NavGraphBuilder.exerciseDetailsScreen(onBack: () -> Unit) {
    composable<ExerciseDetailsRoute> {
        val viewModel: ExerciseDetailsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        ExerciseDetailsScreen(state = state, onBack = onBack)
    }
}
