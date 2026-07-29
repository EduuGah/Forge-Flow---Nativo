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
import androidx.compose.ui.layout.ContentScale
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
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val builtInResource = mediaUri.builtInResource()
        when {
            builtInResource != null -> Image(
                painter = painterResource(builtInResource),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
            !mediaUri.isNullOrBlank() -> AsyncImage(
                model = mediaUri,
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
