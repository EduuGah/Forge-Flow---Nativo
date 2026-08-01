package com.forgeflow.feature.nutrition.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.NutritionRoute
import com.forgeflow.feature.nutrition.presentation.NutritionAction
import com.forgeflow.feature.nutrition.presentation.NutritionScreen
import com.forgeflow.feature.nutrition.presentation.NutritionViewModel
import java.io.File

fun NavGraphBuilder.nutritionScreen(onBack: () -> Unit) {
    composable<NutritionRoute> {
        val viewModel: NutritionViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
        val photoPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            uri?.let { viewModel.onAction(NutritionAction.MealPhotoSelected(it.toString())) }
        }
        val camera = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { saved ->
            val capturedUri = pendingCameraUri
            pendingCameraUri = null
            if (saved && capturedUri != null) {
                viewModel.onAction(NutritionAction.MealPhotoSelected(capturedUri.toString()))
            }
        }
        val notificationPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            viewModel.onAction(NutritionAction.WellnessRemindersChanged(granted))
        }
        NutritionScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
            onPickMealPhoto = {
                runCatching {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }.onFailure {
                    viewModel.onAction(NutritionAction.PhotoCaptureFailed)
                }
            },
            onTakeMealPhoto = {
                val uri = context.createNutritionCameraUri()
                if (uri == null) {
                    viewModel.onAction(NutritionAction.PhotoCaptureFailed)
                } else {
                    pendingCameraUri = uri
                    runCatching { camera.launch(uri) }.onFailure {
                        pendingCameraUri = null
                        viewModel.onAction(NutritionAction.PhotoCaptureFailed)
                    }
                }
            },
            onWellnessReminderChanged = { enabled ->
                if (
                    enabled &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onAction(NutritionAction.WellnessRemindersChanged(enabled))
                }
            },
        )
    }
}

private fun Context.createNutritionCameraUri(): Uri? = runCatching {
    val directory = File(cacheDir, "nutrition_camera")
    require(directory.exists() || directory.mkdirs())
    val photo = File.createTempFile("meal_", ".jpg", directory)
    FileProvider.getUriForFile(this, "$packageName.files", photo)
}.getOrNull()
