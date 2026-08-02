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
    onOpenPlanner: () -> Unit,
    onOpenEvolution: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenGuidedWorkoutTutorial: () -> Unit,
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
                onOpenPlanner = onOpenPlanner,
                onOpenEvolution = onOpenEvolution,
                onOpenTutorial = onOpenTutorial,
                onOpenGuidedWorkoutTutorial = onOpenGuidedWorkoutTutorial,
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
    onOpenPlanner: () -> Unit,
    onOpenEvolution: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenGuidedWorkoutTutorial: () -> Unit,
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
        item {
            DashboardOverviewPanel(
                state = state,
                onOpenPlanner = onOpenPlanner,
                onOpenEvolution = onOpenEvolution,
            )
        }
        state.latestWorkout?.let { workout ->
            item {
                LatestWorkoutPanel(
                    workout = workout,
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
        item {
            TutorialAccessPanel(
                onOpenTutorial = onOpenTutorial,
                onOpenGuidedWorkoutTutorial = onOpenGuidedWorkoutTutorial,
            )
        }
    }
}
