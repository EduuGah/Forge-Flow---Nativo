package com.forgeflow.feature.settings.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.ProfileRoute
import com.forgeflow.core.navigation.ProgressPhotosRoute
import com.forgeflow.core.navigation.SettingsRoute
import com.forgeflow.feature.settings.presentation.ProfileScreen
import com.forgeflow.feature.settings.presentation.ProgressPhotosScreen
import com.forgeflow.feature.settings.presentation.SettingsAction
import com.forgeflow.feature.settings.presentation.SettingsScreen
import com.forgeflow.feature.settings.presentation.SettingsViewModel

fun NavController.navigateToProgressPhotos() {
    navigate(ProgressPhotosRoute)
}

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val permissionContract = remember(viewModel) {
            viewModel.createHealthPermissionContract()
        }
        val permissionLauncher = rememberLauncherForActivityResult(permissionContract) { granted ->
            viewModel.onAction(SettingsAction.HealthConnectPermissionsResult(granted))
        }
        val workoutCsvPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let { viewModel.onAction(SettingsAction.HevyWorkoutFileSelected(it.toString())) }
        }
        val measurementCsvPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let {
                viewModel.onAction(SettingsAction.HevyMeasurementFileSelected(it.toString()))
            }
        }
        LifecycleResumeEffect(viewModel) {
            viewModel.onAction(SettingsAction.HealthConnectRefresh)
            onPauseOrDispose {}
        }
        SettingsScreen(
            state = state,
            onAction = viewModel::onAction,
            onRequestHealthPermissions = {
                permissionLauncher.launch(viewModel.requiredHealthPermissions)
            },
            onSelectWorkoutCsv = { workoutCsvPicker.launch(CSV_MIME_TYPES) },
            onSelectMeasurementCsv = { measurementCsvPicker.launch(CSV_MIME_TYPES) },
        )
    }
}

fun NavGraphBuilder.profileScreen(
    onOpenProgressPhotos: () -> Unit,
) {
    composable<ProfileRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        var pendingAvatarUri by remember { mutableStateOf<String?>(null) }
        val avatarPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            pendingAvatarUri = uri?.toString()
        }
        ProfileScreen(
            state = state,
            onAction = viewModel::onAction,
            onSelectAvatar = {
                avatarPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            pendingAvatarUri = pendingAvatarUri,
            onConfirmAvatarCrop = { zoom, horizontalOffset, verticalOffset ->
                pendingAvatarUri?.let { sourceUri ->
                    viewModel.onAction(
                        SettingsAction.ImportProfilePhoto(
                            sourceUri = sourceUri,
                            zoom = zoom,
                            horizontalOffset = horizontalOffset,
                            verticalOffset = verticalOffset,
                        ),
                    )
                    pendingAvatarUri = null
                }
            },
            onDismissAvatarCrop = { pendingAvatarUri = null },
            onOpenProgressPhotos = onOpenProgressPhotos,
        )
    }
}

fun NavGraphBuilder.progressPhotosScreen(
    onBack: () -> Unit,
) {
    composable<ProgressPhotosRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val photoPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            uri?.let { viewModel.onAction(SettingsAction.ImportProgressPhoto(it.toString())) }
        }
        ProgressPhotosScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
            onAddProgressPhoto = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        )
    }
}

private val CSV_MIME_TYPES = arrayOf(
    "text/csv",
    "text/comma-separated-values",
    "text/plain",
    "application/csv",
)
