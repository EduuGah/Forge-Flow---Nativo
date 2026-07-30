package com.forgeflow.feature.history.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowTextField
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.history.R

@Composable
internal fun HistoryFilters(
    state: HistoryUiState,
    onAction: (HistoryAction) -> Unit,
) {
    val hasActiveFilters = state.searchQuery.isNotBlank() ||
        state.dateFilter != HistoryDateFilter.ALL ||
        state.onlyWithLocation
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.history_filters_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.history_results_count,
                        state.workouts.size,
                        state.workouts.size,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (hasActiveFilters) {
                TextButton(onClick = { onAction(HistoryAction.ClearFilters) }) {
                    Text(stringResource(R.string.clear_filters))
                }
            }
        }
        ForgeFlowTextField(
            value = state.searchQuery,
            onValueChange = { onAction(HistoryAction.SearchChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.history_search_label),
            placeholder = stringResource(R.string.history_search_placeholder),
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { onAction(HistoryAction.SearchChanged("")) },
                    ) {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            HistoryDateFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.dateFilter == filter,
                    onClick = { onAction(HistoryAction.DateFilterChanged(filter)) },
                    label = { Text(stringResource(filter.labelResource())) },
                )
            }
            FilterChip(
                selected = state.onlyWithLocation,
                onClick = {
                    onAction(
                        HistoryAction.OnlyWithLocationChanged(!state.onlyWithLocation),
                    )
                },
                label = { Text(stringResource(R.string.filter_with_location)) },
                leadingIcon = {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                },
            )
        }
    }
}

@Composable
internal fun DeleteHistoryDialog(
    workout: HistoryWorkoutUiModel,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_history_title)) },
        text = {
            Text(stringResource(R.string.delete_history_message, workout.name))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
            ) {
                Text(
                    text = stringResource(
                        if (isDeleting) {
                            R.string.deleting_history
                        } else {
                            R.string.delete_history_confirm
                        },
                    ),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
            ) {
                Text(stringResource(R.string.delete_history_cancel))
            }
        },
    )
}

private fun HistoryDateFilter.labelResource(): Int = when (this) {
    HistoryDateFilter.ALL -> R.string.filter_all_dates
    HistoryDateFilter.LAST_7_DAYS -> R.string.filter_last_7_days
    HistoryDateFilter.LAST_30_DAYS -> R.string.filter_last_30_days
    HistoryDateFilter.LAST_90_DAYS -> R.string.filter_last_90_days
}
