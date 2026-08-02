package com.forgeflow.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forgeflow.app.ui.AppViewModel
import com.forgeflow.app.ui.ForgeFlowApp
import com.forgeflow.app.navigation.AppLaunchRequest
import com.forgeflow.app.navigation.toAppLaunchRequest
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.core.model.ThemePreference
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var launchRequest by mutableStateOf<AppLaunchRequest?>(null)
    private var launchRequestId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRequest = intent.toAppLaunchRequest(++launchRequestId)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = hiltViewModel()
            val appState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val credentialManager = remember(context) { CredentialManager.create(context) }
            val googleWebClientId = remember(context) { context.googleWebClientId() }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                viewModel.onNotificationPermissionResult(granted)
            }
            LaunchedEffect(
                appState.activeWorkout?.id,
                appState.hasRequestedNotificationPermission,
            ) {
                if (
                    appState.auth.isSignedIn &&
                    !appState.showProfileSetup &&
                    appState.activeWorkout != null &&
                    !appState.hasRequestedNotificationPermission &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.onNotificationPermissionRequested()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val systemDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = when (appState.themePreference) {
                ThemePreference.SYSTEM -> systemDarkTheme
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            ForgeFlowTheme(
                darkTheme = useDarkTheme,
                accentColor = appState.accentColor,
                compactMode = appState.compactMode,
            ) {
                ForgeFlowApp(
                    state = appState,
                    launchRequest = launchRequest,
                    onGoogleSignIn = googleSignIn@{
                        if (!viewModel.onGoogleSignInStarted()) return@googleSignIn
                        val clientId = googleWebClientId
                        if (clientId == null) {
                            viewModel.onGoogleSignInFailed()
                        } else {
                            scope.launch {
                                try {
                                    val googleOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(clientId)
                                        .setAutoSelectEnabled(false)
                                        .build()
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleOption)
                                        .build()
                                    val credential = credentialManager
                                        .getCredential(context, request)
                                        .credential
                                    val googleCredential = if (
                                        credential is CustomCredential &&
                                        credential.type ==
                                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    ) {
                                        GoogleIdTokenCredential.createFrom(credential.data)
                                    } else {
                                        null
                                    }
                                    googleCredential?.let {
                                        viewModel.onGoogleIdTokenReceived(it.idToken)
                                    } ?: viewModel.onGoogleSignInFailed()
                                } catch (_: NoCredentialException) {
                                    viewModel.onGoogleSignInFailed()
                                } catch (_: Exception) {
                                    viewModel.onGoogleSignInFailed()
                                }
                            }
                        }
                    },
                    onEmailSignIn = viewModel::onEmailSignIn,
                    onCreateAccount = viewModel::onCreateAccount,
                    onPasswordReset = viewModel::onPasswordReset,
                    onDismissAuthFeedback = viewModel::onAuthFeedbackDismissed,
                    onCompleteProfile = viewModel::onProfileCompleted,
                    onOpenTutorial = viewModel::onTutorialRequested,
                    onCompleteTutorial = viewModel::onTutorialCompleted,
                    onOpenGuidedWorkoutTutorial =
                        viewModel::onGuidedWorkoutTutorialRequested,
                    onDismissGuidedWorkoutTutorial =
                        viewModel::onGuidedWorkoutTutorialDismissed,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchRequest = intent.toAppLaunchRequest(++launchRequestId)
    }
}

@SuppressLint("DiscouragedApi")
private fun android.content.Context.googleWebClientId(): String? =
    resources.getIdentifier(
        "default_web_client_id",
        "string",
        packageName,
    ).takeIf { it != 0 }?.let(::getString)
