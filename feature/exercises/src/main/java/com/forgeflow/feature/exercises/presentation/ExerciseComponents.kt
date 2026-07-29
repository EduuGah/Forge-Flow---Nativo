package com.forgeflow.feature.exercises.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowPill
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.MuscleGroup
import com.forgeflow.feature.exercises.R

@Composable
internal fun ExerciseHeaderActions(
    onBack: () -> Unit,
    onCreate: () -> Unit,
) {
    IconButton(onClick = onBack) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.navigate_back),
        )
    }
    IconButton(onClick = onCreate) {
        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.new_exercise))
    }
}

@Composable
internal fun ExerciseFilters(
    state: ExercisesUiState,
    onAction: (ExercisesAction) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowTextField(
            value = state.query,
            onValueChange = { onAction(ExercisesAction.SearchChanged(it)) },
            label = stringResource(R.string.search_exercises),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = state.selectedMuscleGroup == null,
                    onClick = { onAction(ExercisesAction.MuscleGroupChanged(null)) },
                    label = { Text(stringResource(R.string.filter_all)) },
                )
            }
            items(MuscleGroup.entries) { group ->
                FilterChip(
                    selected = state.selectedMuscleGroup == group,
                    onClick = { onAction(ExercisesAction.MuscleGroupChanged(group)) },
                    label = { Text(stringResource(group.labelResource())) },
                )
            }
        }
    }
}

@Composable
internal fun ExerciseListItem(
    exercise: ExerciseUiModel,
    onAction: (ExercisesAction) -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    ForgeFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenExercise(exercise.id) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ForgeFlowExerciseMedia(
                mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
                contentDescription = exercise.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit,
                shape = CircleShape,
                containerColor = Color.White,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(exercise.primaryMuscleGroup.labelResource()),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (exercise.isCustom) {
                IconButton(onClick = { onAction(ExercisesAction.EditExercise(exercise.id)) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = { onAction(ExercisesAction.DeleteExercise(exercise.id)) }) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.delete),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForgeFlowPill(text = stringResource(exercise.primaryMuscleGroup.labelResource()))
            ForgeFlowPill(text = stringResource(exercise.equipment.labelResource()))
            if (exercise.isCustom) ForgeFlowPill(text = stringResource(R.string.custom))
        }
    }
}
