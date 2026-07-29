package com.forgeflow.feature.home.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.HomeRoute
import com.forgeflow.feature.home.presentation.HomeScreen
import com.forgeflow.feature.home.presentation.HomeViewModel

fun NavGraphBuilder.homeScreen(
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HomeScreen(
            state = state,
            onOpenExercises = onOpenExercises,
            onOpenRoutines = onOpenRoutines,
            onOpenActiveWorkout = onOpenActiveWorkout,
        )
    }
}
