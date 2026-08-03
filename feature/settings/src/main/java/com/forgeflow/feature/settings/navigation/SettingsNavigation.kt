package com.forgeflow.feature.settings.navigation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Process
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import java.time.LocalDate
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.ProfileRoute
import com.forgeflow.core.navigation.HealthDashboardRoute
import com.forgeflow.core.navigation.ProgressPhotosRoute
import com.forgeflow.core.navigation.SettingsRoute
import com.forgeflow.core.navigation.SupportRoute
import com.forgeflow.feature.settings.presentation.ProfileScreen
import com.forgeflow.feature.settings.presentation.HealthDashboardAction
import com.forgeflow.feature.settings.presentation.HealthDashboardScreen
import com.forgeflow.feature.settings.presentation.HealthDashboardViewModel
import com.forgeflow.feature.settings.presentation.ProgressPhotosScreen
import com.forgeflow.feature.settings.presentation.SettingsAction
import com.forgeflow.feature.settings.presentation.SettingsScreen
import com.forgeflow.feature.settings.presentation.SettingsViewModel
import com.forgeflow.feature.settings.presentation.SupportScreen
import com.forgeflow.feature.settings.R

fun NavController.navigateToProgressPhotos() {
    navigate(ProgressPhotosRoute)
}

fun NavGraphBuilder.supportScreen(
    onBack: () -> Unit,
) {
    composable<SupportRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        SupportScreen(
            state = state,
            onBack = onBack,
        )
    }
}

fun NavGraphBuilder.settingsScreen(
    onOpenHealthDashboard: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenGuidedWorkoutTutorial: () -> Unit,
) {
    composable<SettingsRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
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
        val dataExportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip"),
        ) { uri ->
            uri?.let { viewModel.onAction(SettingsAction.ExportData(it.toString())) }
        }
        var pendingRestoreUri by remember { mutableStateOf<String?>(null) }
        val dataRestoreLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            pendingRestoreUri = uri?.toString()
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
            onOpenHealthDashboard = onOpenHealthDashboard,
            onOpenTutorial = onOpenTutorial,
            onOpenGuidedWorkoutTutorial = onOpenGuidedWorkoutTutorial,
            onCreateDataExport = {
                dataExportLauncher.launch("forgeflow-backup-${LocalDate.now()}.zip")
            },
            onSelectDataRestore = {
                dataRestoreLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
            },
            onRestartApp = context::restartApplication,
        )
        pendingRestoreUri?.let { sourceUri ->
            AlertDialog(
                onDismissRequest = { pendingRestoreUri = null },
                title = { Text(stringResource(R.string.data_restore_confirm_title)) },
                text = { Text(stringResource(R.string.data_restore_confirm_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingRestoreUri = null
                            viewModel.onAction(SettingsAction.RestoreData(sourceUri))
                        },
                    ) {
                        Text(stringResource(R.string.data_restore_confirm_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRestoreUri = null }) {
                        Text(stringResource(R.string.data_restore_cancel))
                    }
                },
            )
        }
    }
}

fun NavGraphBuilder.healthDashboardScreen(
    onBack: () -> Unit,
) {
    composable<HealthDashboardRoute> {
        val viewModel: HealthDashboardViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val permissionContract = remember(viewModel) {
            viewModel.createPermissionContract()
        }
        val permissionLauncher = rememberLauncherForActivityResult(permissionContract) { granted ->
            viewModel.onAction(HealthDashboardAction.PermissionsResult(granted))
        }
        LifecycleResumeEffect(viewModel) {
            viewModel.onAction(HealthDashboardAction.Refresh)
            onPauseOrDispose {}
        }
        HealthDashboardScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
            onRequestPermissions = {
                permissionLauncher.launch(viewModel.requiredPermissions)
            },
        )
    }
}

fun NavGraphBuilder.profileScreen(
    onOpenProgressPhotos: () -> Unit,
) {
    composable<ProfileRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val credentialManager = remember(context) { CredentialManager.create(context) }
        val googleWebClientId = remember(context) {
            context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName,
            ).takeIf { it != 0 }?.let(context::getString)
        }
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
            onConfirmAvatarCrop = { zoom, horizontalOffset, verticalOffset, rotationDegrees ->
                pendingAvatarUri?.let { sourceUri ->
                    viewModel.onAction(
                        SettingsAction.ImportProfilePhoto(
                            sourceUri = sourceUri,
                            zoom = zoom,
                            horizontalOffset = horizontalOffset,
                            verticalOffset = verticalOffset,
                            rotationDegrees = rotationDegrees,
                        ),
                    )
                    pendingAvatarUri = null
                }
            },
            onDismissAvatarCrop = { pendingAvatarUri = null },
            onOpenProgressPhotos = onOpenProgressPhotos,
            onGoogleSignIn = {
                val clientId = googleWebClientId
                if (clientId == null) {
                    viewModel.onAction(SettingsAction.GoogleSignInFailed)
                } else {
                    scope.launch {
                        runCatching {
                            val googleOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(clientId)
                                .setAutoSelectEnabled(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleOption)
                                .build()
                            credentialManager.getCredential(context, request).credential
                        }.onSuccess { credential ->
                            val googleCredential = if (
                                credential is CustomCredential &&
                                credential.type ==
                                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                GoogleIdTokenCredential.createFrom(credential.data)
                            } else {
                                null
                            }
                            if (googleCredential != null) {
                                viewModel.onAction(
                                    SettingsAction.GoogleIdTokenReceived(
                                        googleCredential.idToken,
                                    ),
                                )
                            } else {
                                viewModel.onAction(SettingsAction.GoogleSignInFailed)
                            }
                        }.onFailure {
                            viewModel.onAction(SettingsAction.GoogleSignInFailed)
                        }
                    }
                }
            },
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

private fun Context.restartApplication() {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    val restartIntent = PendingIntent.getActivity(
        this,
        41_401,
        launchIntent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    getSystemService(AlarmManager::class.java).set(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 400L,
        restartIntent,
    )
    Process.killProcess(Process.myPid())
}
