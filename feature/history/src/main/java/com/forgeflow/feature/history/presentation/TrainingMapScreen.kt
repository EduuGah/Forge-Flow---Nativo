package com.forgeflow.feature.history.presentation

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEmptyState
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowLocationMap
import com.forgeflow.core.designsystem.component.ForgeFlowMapPoint
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.feature.history.R

@Composable
fun TrainingMapScreen(
    state: TrainingMapUiState,
    onAction: (TrainingMapAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.training_map_loading),
            )
        } else {
            TrainingMapContent(
                state = state,
                onPeriodChanged = {
                    onAction(TrainingMapAction.PeriodChanged(it))
                },
                onPlaceSelected = {
                    onAction(TrainingMapAction.PlaceSelected(it))
                },
                onBack = onBack,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun TrainingMapContent(
    state: TrainingMapUiState,
    onPeriodChanged: (TrainingMapPeriod) -> Unit,
    onPlaceSelected: (String) -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    val selectedPlace = state.places.firstOrNull { it.id == state.selectedPlaceId }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ForgeFlowDesign.spacing.screenHorizontal,
            top = contentPadding.calculateTopPadding() + ForgeFlowDesign.spacing.medium,
            end = ForgeFlowDesign.spacing.screenHorizontal,
            bottom = contentPadding.calculateBottomPadding() + ForgeFlowDesign.spacing.extraLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.section),
    ) {
        item { TrainingMapHeader(onBack = onBack) }
        item {
            TrainingMapPeriodSelector(
                selected = state.period,
                onSelected = onPeriodChanged,
            )
        }
        if (state.hasError) {
            item {
                Text(
                    text = stringResource(R.string.training_map_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (state.places.isEmpty()) {
            item { TrainingMapEmptyState() }
        } else {
            item { TrainingMapMetricGrid(state) }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
                ) {
                    ForgeFlowEyebrow(text = stringResource(R.string.training_map_overview))
                    Text(
                        text = stringResource(R.string.training_map_all_places),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    ForgeFlowLocationMap(
                        points = state.places.map { place ->
                            ForgeFlowMapPoint(
                                id = place.id,
                                latitude = place.latitude,
                                longitude = place.longitude,
                                label = place.label,
                            )
                        },
                        selectedPointId = state.selectedPlaceId,
                        onPointClick = { point ->
                            point.id?.let(onPlaceSelected)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp),
                    )
                    Text(
                        text = stringResource(R.string.training_map_pin_hint),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            selectedPlace?.let { place ->
                item { SelectedTrainingPlacePanel(place, state.weightUnit.symbol) }
            }
            item {
                TrainingPlacesPanel(
                    places = state.places,
                    selectedPlaceId = state.selectedPlaceId,
                    onPlaceSelected = onPlaceSelected,
                )
            }
        }
    }
}

@Composable
private fun TrainingMapHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.training_map_back),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
        ) {
            ForgeFlowEyebrow(text = stringResource(R.string.training_map_eyebrow))
            Text(
                text = stringResource(R.string.training_map_page_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.training_map_page_description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun TrainingMapPeriodSelector(
    selected: TrainingMapPeriod,
    onSelected: (TrainingMapPeriod) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TrainingMapPeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                selected = period == selected,
                onClick = { onSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = TrainingMapPeriod.entries.size,
                ),
                label = { Text(stringResource(period.labelResource())) },
            )
        }
    }
}

@Composable
private fun TrainingMapMetricGrid(state: TrainingMapUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.training_map_places),
                value = state.places.size.toString(),
                helper = stringResource(R.string.training_map_registered),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_workouts),
                value = state.workoutCount.toString(),
                helper = stringResource(R.string.training_map_with_location),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small),
        ) {
            ForgeFlowMetric(
                label = stringResource(R.string.metric_volume),
                value = state.totalVolume.asDisplayValue(),
                helper = stringResource(
                    R.string.training_map_total_volume_unit,
                    state.weightUnit.symbol,
                ),
                modifier = Modifier.weight(1f),
            )
            ForgeFlowMetric(
                label = stringResource(R.string.metric_time),
                value = state.totalDurationMinutes.asDuration(),
                helper = stringResource(R.string.training_map_total_time),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SelectedTrainingPlacePanel(
    place: TrainingPlaceUiModel,
    weightUnitLabel: String,
) {
    val context = LocalContext.current
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                ForgeFlowEyebrow(text = stringResource(R.string.training_map_selected_place))
                Text(
                    text = place.label ?: stringResource(R.string.training_map_unnamed_place),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(
                        R.string.training_map_last_visited,
                        place.lastVisited,
                    ),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.large),
        ) {
            PlaceSummary(
                label = stringResource(R.string.metric_workouts),
                value = place.workoutCount.toString(),
                modifier = Modifier.weight(1f),
            )
            PlaceSummary(
                label = stringResource(R.string.metric_volume),
                value = "${place.totalVolume.asDisplayValue()} $weightUnitLabel",
                modifier = Modifier.weight(1f),
            )
            PlaceSummary(
                label = stringResource(R.string.metric_time),
                value = place.totalDurationMinutes.asDuration(),
                modifier = Modifier.weight(1f),
            )
        }
        ForgeFlowButton(
            text = stringResource(R.string.training_map_open_maps),
            onClick = { context.openMap(place) },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.OpenInNew,
            iconContentDescription = null,
        )
        Text(
            text = stringResource(R.string.training_map_recent_workouts),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
        )
        place.workouts.take(5).forEachIndexed { index, workout ->
            if (index > 0) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.training_map_workout_metadata,
                            workout.date,
                            workout.completedSets,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${workout.volume.asDisplayValue()} $weightUnitLabel",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = workout.durationMinutes.asDuration(),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceSummary(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.extraSmall),
    ) {
        Text(
            text = label.uppercase(),
            color = ForgeFlowDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun TrainingPlacesPanel(
    places: List<TrainingPlaceUiModel>,
    selectedPlaceId: String?,
    onPlaceSelected: (String) -> Unit,
) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                ForgeFlowEyebrow(text = stringResource(R.string.training_map_places_eyebrow))
                Text(
                    text = stringResource(R.string.training_map_places_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        places.forEachIndexed { index, place ->
            if (index > 0) {
                HorizontalDivider(color = ForgeFlowDesign.colors.divider)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaceSelected(place.id) }
                    .padding(vertical = ForgeFlowDesign.spacing.small),
                horizontalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = if (place.id == selectedPlaceId) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        ForgeFlowDesign.colors.textSecondary
                    },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.label
                            ?: stringResource(R.string.training_map_unnamed_place),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.training_map_place_workouts,
                            place.workoutCount,
                            place.workoutCount,
                            place.lastVisited,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = place.totalVolume.asDisplayValue(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun TrainingMapEmptyState() {
    ForgeFlowEmptyState(
        title = stringResource(R.string.training_map_empty_title),
        message = stringResource(R.string.training_map_empty_message),
    )
}

@StringRes
private fun TrainingMapPeriod.labelResource(): Int = when (this) {
    TrainingMapPeriod.LAST_30_DAYS -> R.string.training_map_period_30_days
    TrainingMapPeriod.LAST_YEAR -> R.string.training_map_period_year
    TrainingMapPeriod.ALL -> R.string.training_map_period_all
}

private fun android.content.Context.openMap(place: TrainingPlaceUiModel) {
    val label = Uri.encode(place.label ?: getString(R.string.training_map_unnamed_place))
    val geoUri = Uri.parse(
        "geo:${place.latitude},${place.longitude}" +
            "?q=${place.latitude},${place.longitude}($label)",
    )
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
    if (geoIntent.resolveActivity(packageManager) != null) {
        startActivity(geoIntent)
    } else {
        val browserUri = Uri.parse(
            "https://www.openstreetmap.org/" +
                "?mlat=${place.latitude}&mlon=${place.longitude}" +
                "#map=17/${place.latitude}/${place.longitude}",
        )
        startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}
