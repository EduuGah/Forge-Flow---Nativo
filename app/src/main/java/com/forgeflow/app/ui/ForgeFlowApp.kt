package com.forgeflow.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.forgeflow.app.R
import com.forgeflow.core.designsystem.component.ForgeFlowScaffold
import com.forgeflow.core.designsystem.testing.ForgeFlowTestTags
import com.forgeflow.core.navigation.HistoryRoute
import com.forgeflow.core.navigation.HomeRoute
import com.forgeflow.core.navigation.RoutinesRoute
import com.forgeflow.core.navigation.SettingsRoute
import com.forgeflow.feature.exercises.navigation.exerciseDetailsScreen
import com.forgeflow.feature.exercises.navigation.exercisesScreen
import com.forgeflow.feature.exercises.navigation.navigateToExerciseDetails
import com.forgeflow.feature.exercises.navigation.navigateToExercises
import com.forgeflow.feature.history.navigation.historyScreen
import com.forgeflow.feature.home.navigation.evolutionScreen
import com.forgeflow.feature.home.navigation.homeScreen
import com.forgeflow.feature.home.navigation.navigateToEvolution
import com.forgeflow.feature.home.navigation.navigateToPlanner
import com.forgeflow.feature.home.navigation.plannerScreen
import com.forgeflow.feature.routines.navigation.routinesScreen
import com.forgeflow.feature.settings.navigation.settingsScreen
import com.forgeflow.feature.workout.navigation.activeWorkoutScreen
import com.forgeflow.feature.workout.navigation.navigateToActiveWorkout
import kotlinx.coroutines.delay

@Composable
fun ForgeFlowApp(
    state: AppUiState,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination == null ||
        MainDestination.entries.any(currentDestination::matches)

    ForgeFlowScaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    state.activeWorkout?.let { active ->
                        ActiveWorkoutMiniBar(
                            active = active,
                            onClick = navController::navigateToActiveWorkout,
                        )
                    }
                    ForgeFlowBottomBar(
                        currentDestination = currentDestination,
                        onDestinationSelected = navController::navigateToTopLevel,
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            homeScreen(
                onOpenExercises = navController::navigateToExercises,
                onOpenRoutines = { navController.navigateTopLevelRoute(RoutinesRoute) },
                onOpenActiveWorkout = navController::navigateToActiveWorkout,
                onOpenPlanner = navController::navigateToPlanner,
                onOpenEvolution = navController::navigateToEvolution,
            )
            plannerScreen(onBack = navController::popBackStack)
            evolutionScreen(
                onBack = navController::popBackStack,
                onOpenExercise = navController::navigateToExerciseDetails,
            )
            routinesScreen(
                onOpenExercises = navController::navigateToExercises,
                onOpenActiveWorkout = navController::navigateToActiveWorkout,
                onOpenExercise = navController::navigateToExerciseDetails,
            )
            historyScreen(onOpenExercise = navController::navigateToExerciseDetails)
            settingsScreen()
            exercisesScreen(
                onBack = navController::popBackStack,
                onOpenExercise = navController::navigateToExerciseDetails,
            )
            exerciseDetailsScreen(onBack = navController::popBackStack)
            activeWorkoutScreen(onBack = navController::popBackStack)
        }
    }
}

@Composable
private fun ActiveWorkoutMiniBar(
    active: AppActiveWorkoutUiModel,
    onClick: () -> Unit,
) {
    var nowEpochMillis by remember(active.startedAtEpochMillis) {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(active.startedAtEpochMillis) {
        while (true) {
            nowEpochMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val elapsedSeconds = (
        (nowEpochMillis - active.startedAtEpochMillis) / 1_000
        ).coerceAtLeast(0)
    val progress = if (active.totalSets == 0) {
        0f
    } else {
        active.completedSets.toFloat() / active.totalSets
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.active_workout_bar_label).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = active.name,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.active_workout_bar_details,
                        active.completedSets,
                        active.totalSets,
                        elapsedSeconds.asClock(),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun Long.asClock(): String {
    val hours = this / 3_600
    val minutes = (this % 3_600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun ForgeFlowBottomBar(
    currentDestination: NavDestination?,
    onDestinationSelected: (MainDestination) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.testTag(ForgeFlowTestTags.BottomNavigation),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        MainDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination.matches(destination),
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelResource))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

private fun NavHostController.navigateToTopLevel(destination: MainDestination) {
    when (destination) {
        MainDestination.HOME -> navigateTopLevelRoute(HomeRoute)
        MainDestination.ROUTINES -> navigateTopLevelRoute(RoutinesRoute)
        MainDestination.HISTORY -> navigateTopLevelRoute(HistoryRoute)
        MainDestination.SETTINGS -> navigateTopLevelRoute(SettingsRoute)
    }
}

private fun <T : Any> NavHostController.navigateTopLevelRoute(route: T) {
    navigate(route) {
        popUpTo<HomeRoute> {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.matches(destination: MainDestination): Boolean {
    return this?.route == destination.routeName
}

private enum class MainDestination(
    @param:StringRes val labelResource: Int,
    val icon: ImageVector,
    val routeName: String?,
) {
    HOME(R.string.navigation_home, Icons.Outlined.Home, HomeRoute::class.qualifiedName),
    ROUTINES(
        R.string.navigation_routines,
        Icons.Outlined.FitnessCenter,
        RoutinesRoute::class.qualifiedName,
    ),
    HISTORY(R.string.navigation_history, Icons.Outlined.History, HistoryRoute::class.qualifiedName),
    SETTINGS(
        R.string.navigation_settings,
        Icons.Outlined.Settings,
        SettingsRoute::class.qualifiedName,
    ),
}
