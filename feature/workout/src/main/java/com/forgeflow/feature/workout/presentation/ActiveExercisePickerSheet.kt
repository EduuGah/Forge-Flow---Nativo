package com.forgeflow.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowExerciseMedia
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.workout.R
import com.forgeflow.core.model.normalizedSearchText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActiveExercisePickerSheet(
    exercises: List<ActiveExercisePickerUiModel>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.normalizedSearchText()
    val visible = remember(exercises, normalizedQuery) {
        exercises.filter {
            normalizedQuery.isBlank() || it.searchTerms.contains(normalizedQuery)
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .imePadding()
                .padding(horizontal = ForgeFlowDesign.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            Text(
                text = stringResource(R.string.choose_exercise),
                style = MaterialTheme.typography.headlineSmall,
            )
            ForgeFlowTextField(
                value = query,
                onValueChange = { query = it },
                label = stringResource(R.string.search_exercise),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            )
            Text(
                text = stringResource(R.string.exercise_result_count, visible.size),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visible, key = ActiveExercisePickerUiModel::id) { exercise ->
                    OutlinedButton(
                        onClick = { onSelect(exercise.id) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ForgeFlowExerciseMedia(
                                mediaUri = exercise.mediaThumbnailUri ?: exercise.mediaUri,
                                contentDescription = exercise.name,
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Fit,
                                shape = CircleShape,
                                containerColor = Color.White,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    text = exercise.muscleGroup.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    color = ForgeFlowDesign.colors.textSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
