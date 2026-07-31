package com.forgeflow.feature.nutrition.navigation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
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
            if (saved) {
                pendingCameraUri?.let { uri ->
                    viewModel.onAction(NutritionAction.MealPhotoSelected(uri.toString()))
                }
            }
        }
        NutritionScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
            onPickMealPhoto = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onTakeMealPhoto = {
                pendingCameraUri = context.createNutritionCameraUri()
                camera.launch(pendingCameraUri!!)
            },
        )
    }
}

private fun Context.createNutritionCameraUri(): Uri {
    val directory = File(cacheDir, "nutrition_camera").apply { mkdirs() }
    val photo = File.createTempFile("meal_", ".jpg", directory)
    return FileProvider.getUriForFile(this, "$packageName.files", photo)
}
