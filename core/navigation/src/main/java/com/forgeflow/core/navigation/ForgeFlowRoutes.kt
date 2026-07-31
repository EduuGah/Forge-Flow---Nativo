package com.forgeflow.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object PlannerRoute

@Serializable
data object EvolutionRoute

@Serializable
data object RoutinesRoute

@Serializable
data object ActiveWorkoutRoute

@Serializable
data object HistoryRoute

@Serializable
data object TrainingMapRoute

@Serializable
data object ExercisesRoute

@Serializable
data class ExerciseDetailsRoute(val exerciseId: String)

@Serializable
data object ProfileRoute

@Serializable
data object ProgressPhotosRoute

@Serializable
data object SettingsRoute

@Serializable
data object NutritionRoute

@Serializable
data object HealthDashboardRoute
