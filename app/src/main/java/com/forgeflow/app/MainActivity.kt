package com.forgeflow.app

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forgeflow.app.ui.AppViewModel
import com.forgeflow.app.ui.ForgeFlowApp
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.core.model.ThemePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = hiltViewModel()
            val appState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
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
                ForgeFlowApp(state = appState)
            }
        }
    }
}
