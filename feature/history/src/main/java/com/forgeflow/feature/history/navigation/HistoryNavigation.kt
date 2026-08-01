package com.forgeflow.feature.history.navigation

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgeflow.core.navigation.HistoryRoute
import com.forgeflow.core.navigation.TrainingMapRoute
import com.forgeflow.feature.history.R
import com.forgeflow.feature.history.presentation.HistoryScreen
import com.forgeflow.feature.history.presentation.HistoryViewModel
import com.forgeflow.feature.history.presentation.TrainingMapScreen
import com.forgeflow.feature.history.presentation.TrainingMapViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NavGraphBuilder.historyScreen(
    onOpenExercise: (String) -> Unit,
    onOpenTrainingMap: () -> Unit,
) {
    composable<HistoryRoute> {
        val viewModel: HistoryViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        HistoryScreen(
            state = state,
            onOpenExercise = onOpenExercise,
            onOpenTrainingMap = onOpenTrainingMap,
            onShareStory = { image ->
                scope.launch { shareWorkoutStory(context, image) }
            },
            onAction = viewModel::onAction,
        )
    }
}

private suspend fun shareWorkoutStory(context: Context, image: ImageBitmap) {
    val uri = runCatching { withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "workout_shares")
        check(directory.exists() || directory.mkdirs())
        directory.listFiles()
            .orEmpty()
            .filter { it.lastModified() < System.currentTimeMillis() - SHARE_CACHE_TTL_MILLIS }
            .forEach(File::delete)
        val file = File(directory, "forgeflow_${System.currentTimeMillis()}.png")
        file.outputStream().use { output ->
            check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output))
        }
        FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    } }.getOrNull() ?: return
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newUri(context.contentResolver, "ForgeFlow", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(sendIntent, context.getString(R.string.share_workout_chooser))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private const val SHARE_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1_000L

fun NavController.navigateToTrainingMap() {
    navigate(TrainingMapRoute)
}

fun NavGraphBuilder.trainingMapScreen(onBack: () -> Unit) {
    composable<TrainingMapRoute> {
        val viewModel: TrainingMapViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        TrainingMapScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = onBack,
        )
    }
}
