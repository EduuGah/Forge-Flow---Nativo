package com.forgeflow.feature.workout.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowErrorState
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.workout.R

@Composable
fun ActiveWorkoutScreen(
    state: ActiveWorkoutUiState,
    onAction: (ActiveWorkoutAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showFinishSheet by remember { mutableStateOf(false) }
    var includeLocation by remember { mutableStateOf(false) }
    var locationLabel by remember { mutableStateOf("") }
    var locationPermissionDenied by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionDenied = !granted
        if (granted) {
            showFinishSheet = false
            onAction(
                ActiveWorkoutAction.Finish(
                    includeLocation = true,
                    locationLabel = locationLabel,
                ),
            )
        }
    }
    ForgeFlowScaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            state.workout?.let { workout ->
                WorkoutBottomActions(
                    canFinish = workout.completedSets > 0 && !state.isCapturingLocation,
                    onFinish = { showFinishSheet = true },
                    onDiscard = { showDiscardDialog = true },
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.active_workout_loading),
            )
            state.error -> ForgeFlowErrorState(
                title = stringResource(R.string.active_workout_error_title),
                message = stringResource(R.string.active_workout_error_message),
                retryLabel = stringResource(R.string.navigate_back),
                onRetry = onBack,
            )
            state.workout == null -> ForgeFlowEmptyState(
                title = stringResource(R.string.active_workout_unavailable_title),
                message = stringResource(R.string.active_workout_unavailable_message),
                modifier = Modifier.padding(innerPadding),
            )
            else -> WorkoutContent(
                workout = state.workout,
                onAction = onAction,
                onBack = onBack,
                contentPadding = innerPadding,
            )
        }
    }
    if (showDiscardDialog) {
        DiscardWorkoutDialog(
            onConfirm = {
                showDiscardDialog = false
                onAction(ActiveWorkoutAction.Discard)
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
    if (showFinishSheet) {
        FinishWorkoutSheet(
            includeLocation = includeLocation,
            locationLabel = locationLabel,
            locationPermissionDenied = locationPermissionDenied,
            isFinishing = state.isCapturingLocation,
            onIncludeLocationChanged = {
                includeLocation = it
                locationPermissionDenied = false
            },
            onLocationLabelChanged = { locationLabel = it },
            onConfirm = {
                when {
                    !includeLocation -> {
                        showFinishSheet = false
                        onAction(
                            ActiveWorkoutAction.Finish(
                                includeLocation = false,
                                locationLabel = "",
                            ),
                        )
                    }
                    context.hasLocationPermission() -> {
                        showFinishSheet = false
                        onAction(
                            ActiveWorkoutAction.Finish(
                                includeLocation = true,
                                locationLabel = locationLabel,
                            ),
                        )
                    }
                    else -> locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            onDismiss = { showFinishSheet = false },
        )
    }
}

@Composable
private fun WorkoutContent(
    workout: ActiveWorkoutUiModel,
    onAction: (ActiveWorkoutAction) -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.screenHorizontal,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
            end = ForgeFlowDesign.spacing.screenHorizontal,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
    ) {
        item {
            WorkoutHeader(workout = workout, onBack = onBack)
        }
        item {
            WorkoutProgress(workout = workout)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall)) {
                ForgeFlowEyebrow(text = stringResource(R.string.exercise_section))
                Text(
                    text = stringResource(
                        R.string.exercise_section_count,
                        workout.exercises.size,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(workout.exercises, key = ActiveExerciseUiModel::id) { exercise ->
            ActiveExerciseCard(
                exercise = exercise,
                weightUnit = workout.weightUnit,
                onAction = onAction,
            )
        }
    }
}

private fun android.content.Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}
