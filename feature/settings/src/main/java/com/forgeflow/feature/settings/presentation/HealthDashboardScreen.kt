package com.forgeflow.feature.settings.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.component.ForgeFlowCard
import com.forgeflow.core.designsystem.component.ForgeFlowEyebrow
import com.forgeflow.core.designsystem.component.ForgeFlowLoadingState
import com.forgeflow.core.designsystem.component.ForgeFlowMetric
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.component.ForgeFlowTopAppBar
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.platform.health.HealthConnectAvailability
import com.forgeflow.feature.settings.R
import java.text.NumberFormat
import kotlin.math.abs

@Composable
fun HealthDashboardScreen(
    state: HealthDashboardUiState,
    onAction: (HealthDashboardAction) -> Unit,
    onBack: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForgeFlowScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ForgeFlowTopAppBar(
                title = stringResource(R.string.health_dashboard_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { onAction(HealthDashboardAction.Refresh) }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.health_dashboard_refresh),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            ForgeFlowLoadingState(
                contentDescription = stringResource(R.string.health_dashboard_loading),
            )
        } else {
            HealthDashboardContent(
                state = state,
                onAction = onAction,
                onRequestPermissions = onRequestPermissions,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun HealthDashboardContent(
    state: HealthDashboardUiState,
    onAction: (HealthDashboardAction) -> Unit,
    onRequestPermissions: () -> Unit,
    contentPadding: PaddingValues,
) {
    val today = state.daily.lastOrNull()
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
        item {
            Column(verticalArrangement = Arrangement.spacedBy(ForgeFlowDesign.spacing.small)) {
                ForgeFlowEyebrow(text = stringResource(R.string.health_dashboard_eyebrow))
                Text(
                    text = stringResource(R.string.health_dashboard_heading),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.health_dashboard_subtitle),
                    color = ForgeFlowDesign.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (
            state.availability != HealthConnectAvailability.AVAILABLE ||
            !state.hasPermissions
        ) {
            item {
                ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Outlined.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.health_dashboard_connect_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.health_dashboard_connect_message),
                        color = ForgeFlowDesign.colors.textSecondary,
                    )
                    ForgeFlowButton(
                        text = stringResource(R.string.health_connect_connect),
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Outlined.HealthAndSafety,
                        iconContentDescription = null,
                    )
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HealthDashboardPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = state.period == period,
                            onClick = {
                                onAction(HealthDashboardAction.PeriodChanged(period))
                            },
                            label = {
                                Text(
                                    stringResource(
                                        if (period == HealthDashboardPeriod.SEVEN_DAYS) {
                                            R.string.health_period_seven
                                        } else {
                                            R.string.health_period_thirty
                                        },
                                    ),
                                )
                            },
                        )
                    }
                }
            }
            item {
                ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
                    ForgeFlowEyebrow(text = stringResource(R.string.health_sources))
                    Text(
                        text = if (state.sources.isEmpty()) {
                            stringResource(R.string.health_sources_empty)
                        } else {
                            state.sources.joinToString(" • ")
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    AssistChip(
                        onClick = { onAction(HealthDashboardAction.OpenHealthConnect) },
                        label = { Text(stringResource(R.string.health_connect_manage)) },
                        leadingIcon = { Icon(Icons.Outlined.OpenInNew, contentDescription = null) },
                    )
                }
            }
            today?.let { current ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ForgeFlowMetric(
                                label = stringResource(R.string.health_steps),
                                value = current.steps.formatted(),
                                helper = stringResource(R.string.health_today),
                                modifier = Modifier.weight(1f),
                            )
                            ForgeFlowMetric(
                                label = stringResource(R.string.health_distance),
                                value = "%.1f km".format(current.distanceKilometers),
                                helper = stringResource(R.string.health_today),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ForgeFlowMetric(
                                label = stringResource(R.string.health_calories),
                                value = "%.0f kcal".format(current.caloriesKilocalories),
                                helper = stringResource(R.string.health_burned),
                                modifier = Modifier.weight(1f),
                            )
                            ForgeFlowMetric(
                                label = stringResource(R.string.health_sleep),
                                value = current.sleepMinutes.asDuration(),
                                helper = stringResource(R.string.health_last_night),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            item { StepsHistoryChart(state.daily) }
            item { HealthDailyHistory(state.daily.asReversed()) }
            item { HealthDataUsagePanel() }
        }
    }
}

@Composable
private fun StepsHistoryChart(points: List<HealthDailyUiModel>) {
    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex.coerceAtLeast(0)) }
    val selected = points.getOrNull(selectedIndex)
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                ForgeFlowEyebrow(text = stringResource(R.string.health_steps_history))
                Text(
                    text = stringResource(R.string.health_daily_steps),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            selected?.let {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = it.steps.formatted(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = it.fullDateLabel,
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        val lineColor = MaterialTheme.colorScheme.primary
        val gridColor = ForgeFlowDesign.colors.divider
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(points) {
                    detectTapGestures { tap ->
                        if (points.isNotEmpty()) {
                            val step = size.width.toFloat() / (points.size - 1).coerceAtLeast(1)
                            selectedIndex = points.indices.minByOrNull { index ->
                                abs(tap.x - step * index)
                            } ?: selectedIndex
                        }
                    }
                },
        ) {
            repeat(4) { line ->
                val y = size.height * line / 3f
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y))
            }
            if (points.isEmpty()) return@Canvas
            val maximum = points.maxOf { it.steps }.coerceAtLeast(1)
            val offsets = points.mapIndexed { index, point ->
                val x = if (points.size == 1) size.width / 2 else {
                    size.width * index / (points.size - 1)
                }
                val y = size.height - point.steps.toFloat() / maximum * size.height * 0.8f -
                    size.height * 0.1f
                Offset(x, y)
            }
            val path = Path().apply {
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }
            drawPath(path, lineColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            offsets.forEachIndexed { index, offset ->
                drawCircle(
                    color = lineColor,
                    radius = if (index == selectedIndex) 6.dp.toPx() else 4.dp.toPx(),
                    center = offset,
                )
            }
        }
        if (points.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(points.first().dateLabel, color = ForgeFlowDesign.colors.textSecondary)
                Text(points.last().dateLabel, color = ForgeFlowDesign.colors.textSecondary)
            }
        }
    }
}

@Composable
private fun HealthDailyHistory(days: List<HealthDailyUiModel>) {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.health_history))
        days.take(14).forEach { day ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(day.fullDateLabel, fontWeight = FontWeight.Bold)
                    Text(
                        text = stringResource(
                            R.string.health_daily_secondary,
                            day.distanceKilometers,
                            day.caloriesKilocalories,
                        ),
                        color = ForgeFlowDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(day.steps.formatted(), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun HealthDataUsagePanel() {
    ForgeFlowCard(modifier = Modifier.fillMaxWidth()) {
        ForgeFlowEyebrow(text = stringResource(R.string.health_used_by_forgeflow))
        Text(
            text = stringResource(R.string.health_usage_weight),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.health_usage_activity),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.health_usage_training),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun Long.formatted(): String = NumberFormat.getIntegerInstance().format(this)

private fun Long.asDuration(): String = "%dh %02dmin".format(this / 60, this % 60)
