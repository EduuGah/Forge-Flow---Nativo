package com.forgeflow.feature.home.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.HomeRoute
import com.forgeflow.core.navigation.PlannerRoute
import com.forgeflow.feature.home.presentation.HomeScreen
import com.forgeflow.feature.home.presentation.HomeViewModel
import com.forgeflow.feature.home.presentation.PlannerScreen
import com.forgeflow.feature.home.presentation.PlannerViewModel

fun NavGraphBuilder.homeScreen(
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
    onOpenPlanner: () -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HomeScreen(
            state = state,
            onOpenExercises = onOpenExercises,
            onOpenRoutines = onOpenRoutines,
            onOpenActiveWorkout = onOpenActiveWorkout,
            onOpenPlanner = onOpenPlanner,
        )
    }
}

fun NavController.navigateToPlanner() {
    navigate(PlannerRoute)
}

fun NavGraphBuilder.plannerScreen(onBack: () -> Unit) {
    composable<PlannerRoute> {
        val viewModel: PlannerViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        PlannerScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
        )
    }
}
