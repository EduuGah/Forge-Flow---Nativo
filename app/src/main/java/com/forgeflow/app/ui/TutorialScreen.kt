package com.forgeflow.app.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forgeflow.app.R
import com.forgeflow.core.designsystem.component.ForgeFlowButton
import com.forgeflow.core.designsystem.theme.ForgeFlowDesign
import com.forgeflow.core.model.WeightUnit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TutorialScreen(
    initialWeightUnit: WeightUnit,
    initialWeeklyWorkoutGoal: Int,
    onComplete: (WeightUnit, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = remember { tutorialPages() }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var weightUnit by rememberSaveable { mutableStateOf(initialWeightUnit) }
    var weeklyWorkoutGoal by rememberSaveable {
        mutableIntStateOf(initialWeeklyWorkoutGoal.coerceIn(1, 7))
    }
    val completeTutorial = { onComplete(weightUnit, weeklyWorkoutGoal) }

    BackHandler {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            TutorialTopBar(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                onSkip = completeTutorial,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
            ) { pageIndex ->
                TutorialPageContent(
                    page = pages[pageIndex],
                    pageIndex = pageIndex,
                    weightUnit = weightUnit,
                    weeklyWorkoutGoal = weeklyWorkoutGoal,
                    onWeightUnitChanged = { weightUnit = it },
                    onWeeklyWorkoutGoalChanged = { weeklyWorkoutGoal = it },
                )
            }
            TutorialPageIndicator(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
            )
            TutorialNavigation(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                onPrevious = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                onNext = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                onComplete = completeTutorial,
            )
        }
    }
}

@Composable
private fun TutorialTopBar(
    currentPage: Int,
    pageCount: Int,
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.tutorial_brand),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(
                    R.string.tutorial_page_progress,
                    currentPage + 1,
                    pageCount,
                ),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        TextButton(onClick = onSkip) {
            Text(stringResource(R.string.tutorial_skip))
        }
    }
}

@Composable
private fun TutorialPageContent(
    page: TutorialPage,
    pageIndex: Int,
    weightUnit: WeightUnit,
    weeklyWorkoutGoal: Int,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    onWeeklyWorkoutGoalChanged: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TutorialArtwork(page = page, pageIndex = pageIndex)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(page.eyebrow).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = stringResource(page.title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(page.description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (pageIndex == TUTORIAL_SETUP_PAGE) {
            TutorialSetupControls(
                weightUnit = weightUnit,
                weeklyWorkoutGoal = weeklyWorkoutGoal,
                onWeightUnitChanged = onWeightUnitChanged,
                onWeeklyWorkoutGoalChanged = onWeeklyWorkoutGoalChanged,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                page.features.forEach { feature ->
                    TutorialFeatureRow(feature)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun TutorialArtwork(page: TutorialPage, pageIndex: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(206.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = page.heroIcon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = stringResource(page.artworkLabel),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                page.artworkMetrics.forEachIndexed { index, metric ->
                    ArtworkMetric(
                        label = stringResource(metric.label),
                        value = stringResource(metric.value),
                        modifier = Modifier.weight(1f),
                    )
                    if (index < page.artworkMetrics.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(42.dp)
                                .background(ForgeFlowDesign.colors.divider),
                        )
                    }
                }
            }
            if (pageIndex == 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .background(
                                    color = if (index < 3) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtworkMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = label,
            color = ForgeFlowDesign.colors.textSecondary,
            maxLines = 2,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun TutorialFeatureRow(feature: TutorialFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(feature.title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(feature.description),
                color = ForgeFlowDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorialSetupControls(
    weightUnit: WeightUnit,
    weeklyWorkoutGoal: Int,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    onWeeklyWorkoutGoalChanged: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.tutorial_weight_unit),
                style = MaterialTheme.typography.titleSmall,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WeightUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = weightUnit == unit,
                        onClick = { onWeightUnitChanged(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                        label = {
                            Text(
                                stringResource(
                                    if (unit == WeightUnit.KILOGRAM) {
                                        R.string.tutorial_weight_kg
                                    } else {
                                        R.string.tutorial_weight_lb
                                    },
                                ),
                            )
                        },
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.tutorial_weekly_goal),
                style = MaterialTheme.typography.titleSmall,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            onWeeklyWorkoutGoalChanged((weeklyWorkoutGoal - 1).coerceAtLeast(1))
                        },
                        enabled = weeklyWorkoutGoal > 1,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = stringResource(R.string.tutorial_goal_decrease),
                        )
                    }
                    Text(
                        text = pluralStringResource(
                            R.plurals.tutorial_weekly_goal_value,
                            weeklyWorkoutGoal,
                            weeklyWorkoutGoal,
                        ),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(
                        onClick = {
                            onWeeklyWorkoutGoalChanged((weeklyWorkoutGoal + 1).coerceAtMost(7))
                        },
                        enabled = weeklyWorkoutGoal < 7,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.tutorial_goal_increase),
                        )
                    }
                }
            }
        }
        TutorialFeatureRow(
            TutorialFeature(
                icon = Icons.Outlined.Backup,
                title = R.string.tutorial_setup_backup_title,
                description = R.string.tutorial_setup_backup_description,
            ),
        )
    }
}

@Composable
private fun TutorialPageIndicator(currentPage: Int, pageCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier.width(26.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(if (index == currentPage) 20.dp else 7.dp)
                        .height(7.dp)
                        .background(
                            color = if (index == currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun TutorialNavigation(
    currentPage: Int,
    pageCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (currentPage > 0) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.tutorial_previous),
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        if (currentPage == pageCount - 1) {
            ForgeFlowButton(
                text = stringResource(R.string.tutorial_start),
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                iconContentDescription = null,
            )
        } else {
            ForgeFlowButton(
                text = stringResource(R.string.tutorial_next),
                onClick = onNext,
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                iconContentDescription = null,
            )
        }
    }
}

private fun tutorialPages(): List<TutorialPage> = listOf(
    TutorialPage(
        eyebrow = R.string.tutorial_welcome_eyebrow,
        title = R.string.tutorial_welcome_title,
        description = R.string.tutorial_welcome_description,
        heroIcon = Icons.Outlined.FitnessCenter,
        artworkLabel = R.string.tutorial_artwork_dashboard,
        artworkMetrics = listOf(
            TutorialMetric(R.string.tutorial_metric_week, R.string.tutorial_metric_week_value),
            TutorialMetric(R.string.tutorial_metric_prs, R.string.tutorial_metric_prs_value),
            TutorialMetric(R.string.tutorial_metric_sets, R.string.tutorial_metric_sets_value),
        ),
        features = listOf(
            TutorialFeature(
                Icons.Outlined.Insights,
                R.string.tutorial_dashboard_title,
                R.string.tutorial_dashboard_description,
            ),
            TutorialFeature(
                Icons.Outlined.FolderOpen,
                R.string.tutorial_routines_title,
                R.string.tutorial_routines_description,
            ),
            TutorialFeature(
                Icons.Outlined.History,
                R.string.tutorial_history_title,
                R.string.tutorial_history_description,
            ),
        ),
    ),
    TutorialPage(
        eyebrow = R.string.tutorial_workout_eyebrow,
        title = R.string.tutorial_workout_title,
        description = R.string.tutorial_workout_description,
        heroIcon = Icons.Outlined.Timer,
        artworkLabel = R.string.tutorial_artwork_active,
        artworkMetrics = listOf(
            TutorialMetric(R.string.tutorial_metric_time, R.string.tutorial_metric_time_value),
            TutorialMetric(
                R.string.tutorial_metric_progress,
                R.string.tutorial_metric_progress_value,
            ),
            TutorialMetric(R.string.tutorial_metric_volume, R.string.tutorial_metric_volume_value),
        ),
        features = listOf(
            TutorialFeature(
                Icons.Outlined.CheckCircle,
                R.string.tutorial_sets_title,
                R.string.tutorial_sets_description,
            ),
            TutorialFeature(
                Icons.Outlined.WorkspacePremium,
                R.string.tutorial_pr_title,
                R.string.tutorial_pr_description,
            ),
            TutorialFeature(
                Icons.Outlined.DragHandle,
                R.string.tutorial_reorder_title,
                R.string.tutorial_reorder_description,
            ),
        ),
    ),
    TutorialPage(
        eyebrow = R.string.tutorial_progress_eyebrow,
        title = R.string.tutorial_progress_title,
        description = R.string.tutorial_progress_description,
        heroIcon = Icons.Outlined.Insights,
        artworkLabel = R.string.tutorial_artwork_progress,
        artworkMetrics = listOf(
            TutorialMetric(
                R.string.tutorial_metric_workouts,
                R.string.tutorial_metric_workouts_value,
            ),
            TutorialMetric(
                R.string.tutorial_metric_evolution,
                R.string.tutorial_metric_evolution_value,
            ),
            TutorialMetric(
                R.string.tutorial_metric_places,
                R.string.tutorial_metric_places_value,
            ),
        ),
        features = listOf(
            TutorialFeature(
                Icons.Outlined.History,
                R.string.tutorial_filters_title,
                R.string.tutorial_filters_description,
            ),
            TutorialFeature(
                Icons.Outlined.PhotoLibrary,
                R.string.tutorial_photos_title,
                R.string.tutorial_photos_description,
            ),
            TutorialFeature(
                Icons.Outlined.Map,
                R.string.tutorial_map_title,
                R.string.tutorial_map_description,
            ),
        ),
    ),
    TutorialPage(
        eyebrow = R.string.tutorial_wellness_eyebrow,
        title = R.string.tutorial_wellness_title,
        description = R.string.tutorial_wellness_description,
        heroIcon = Icons.Outlined.HealthAndSafety,
        artworkLabel = R.string.tutorial_artwork_health,
        artworkMetrics = listOf(
            TutorialMetric(R.string.tutorial_metric_steps, R.string.tutorial_metric_steps_value),
            TutorialMetric(R.string.tutorial_metric_water, R.string.tutorial_metric_water_value),
            TutorialMetric(
                R.string.tutorial_metric_calories,
                R.string.tutorial_metric_calories_value,
            ),
        ),
        features = listOf(
            TutorialFeature(
                Icons.Outlined.HealthAndSafety,
                R.string.tutorial_health_title,
                R.string.tutorial_health_description,
            ),
            TutorialFeature(
                Icons.Outlined.Restaurant,
                R.string.tutorial_nutrition_title,
                R.string.tutorial_nutrition_description,
            ),
            TutorialFeature(
                Icons.Outlined.Person,
                R.string.tutorial_profile_title,
                R.string.tutorial_profile_description,
            ),
        ),
    ),
    TutorialPage(
        eyebrow = R.string.tutorial_setup_eyebrow,
        title = R.string.tutorial_setup_title,
        description = R.string.tutorial_setup_description,
        heroIcon = Icons.Outlined.Tune,
        artworkLabel = R.string.tutorial_artwork_setup,
        artworkMetrics = listOf(
            TutorialMetric(R.string.tutorial_metric_unit, R.string.tutorial_metric_unit_value),
            TutorialMetric(R.string.tutorial_metric_goal, R.string.tutorial_metric_goal_value),
            TutorialMetric(R.string.tutorial_metric_sync, R.string.tutorial_metric_sync_value),
        ),
        features = emptyList(),
    ),
)

private data class TutorialPage(
    @param:StringRes val eyebrow: Int,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val heroIcon: ImageVector,
    @param:StringRes val artworkLabel: Int,
    val artworkMetrics: List<TutorialMetric>,
    val features: List<TutorialFeature>,
)

private data class TutorialMetric(
    @param:StringRes val label: Int,
    @param:StringRes val value: Int,
)

private data class TutorialFeature(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
)

private const val TUTORIAL_SETUP_PAGE = 4
