package com.forgeflow.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.forgeflow.core.designsystem.R
import com.forgeflow.core.model.ExerciseMediaUris

@Composable
fun ForgeFlowExerciseMedia(
    mediaUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape? = null,
    containerColor: Color? = null,
) {
    val resolvedShape = shape ?: MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .clip(resolvedShape)
            .background(containerColor ?: MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val builtInResource = mediaUri.builtInResource()
        val resolvedUri = mediaUri.resolvedUri()
        when {
            !resolvedUri.isNullOrBlank() -> AsyncImage(
                model = resolvedUri,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
            builtInResource != null -> Image(
                painter = painterResource(builtInResource),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }
    }
}

@DrawableRes
private fun String?.builtInResource(): Int? = when (this) {
    ExerciseMediaUris.BENCH_PRESS -> R.drawable.exercise_bench_press
    ExerciseMediaUris.BACK_SQUAT -> R.drawable.exercise_back_squat
    ExerciseMediaUris.DEADLIFT -> R.drawable.exercise_deadlift
    ExerciseMediaUris.BARBELL_ROW -> R.drawable.exercise_barbell_row
    ExerciseMediaUris.OVERHEAD_PRESS -> R.drawable.exercise_overhead_press
    else -> null
}

private fun String?.resolvedUri(): String? = when (this) {
    ExerciseMediaUris.BENCH_PRESS ->
        "file:///android_asset/exercise-media/chest/bench-press-barbell.gif"
    ExerciseMediaUris.BACK_SQUAT ->
        "file:///android_asset/exercise-media/legs/barbell-squat.gif"
    ExerciseMediaUris.DEADLIFT ->
        "file:///android_asset/exercise-media/back/deadlift.gif"
    ExerciseMediaUris.BARBELL_ROW ->
        "file:///android_asset/exercise-media/back/barbell-bent-over-row.gif"
    ExerciseMediaUris.OVERHEAD_PRESS ->
        "file:///android_asset/exercise-media/shoulders/barbell-shoulder-press.gif"
    else -> this
}
