package com.forgeflow.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.home.R

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.home_loading),
            )
        } else {
            HomeContent(
                state = state,
                onOpenExercises = onOpenExercises,
                onOpenRoutines = onOpenRoutines,
                onOpenActiveWorkout = onOpenActiveWorkout,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenExercises: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.screenHorizontal,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.large,
            end = ForgeFlowDesign.spacing.screenHorizontal,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
    ) {
        item { DashboardHeader(state = state) }
        item {
            state.activeWorkout?.let { active ->
                ActiveWorkoutPanel(active = active, onOpen = onOpenActiveWorkout)
            } ?: NextWorkoutPanel(
                routineCount = state.routineCount,
                onOpenRoutines = onOpenRoutines,
            )
        }
        item { DashboardMetricGrid(state = state) }
        item { DashboardEvolutionPanels(state = state) }
        if (state.recentWorkouts.isNotEmpty()) {
            item {
                RecentWorkoutsPanel(
                    workouts = state.recentWorkouts,
                    weightUnit = state.weightUnit,
                )
            }
        }
        item {
            QuickAccessPanel(
                onOpenExercises = onOpenExercises,
                onOpenRoutines = onOpenRoutines,
            )
        }
    }
}
