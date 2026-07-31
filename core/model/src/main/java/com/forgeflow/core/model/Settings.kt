package com.forgeflow.core.model

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AccentColor {
    BLUE,
    CYAN,
    TEAL,
    GREEN,
    LIME,
    AMBER,
    ORANGE,
    RED,
    ROSE,
    PINK,
    PURPLE,
    INDIGO,
}

enum class WeightUnit {
    KILOGRAM,
    POUND,
}

enum class HealthConnectDataType {
    BODY_WEIGHT,
    STEPS,
    DISTANCE,
    CALORIES,
    HEART_RATE,
    EXERCISE_SESSIONS,
    SLEEP,
}

enum class TrainingDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
}

data class UserSettings(
    val themePreference: ThemePreference = ThemePreference.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val compactMode: Boolean = false,
    val weeklyWorkoutGoal: Int = 3,
    val trainingDays: Set<TrainingDay> = emptySet(),
    val preferredWorkoutTimeMinutes: Int = 18 * 60,
    val healthConnectSyncEnabled: Boolean = false,
    val healthConnectReadDataTypes: Set<HealthConnectDataType> = setOf(
        HealthConnectDataType.BODY_WEIGHT,
        HealthConnectDataType.STEPS,
        HealthConnectDataType.EXERCISE_SESSIONS,
    ),
    val hasCompletedOnboarding: Boolean = false,
    val hasRequestedNotificationPermission: Boolean = false,
)
