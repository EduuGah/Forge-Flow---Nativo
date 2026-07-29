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

data class UserSettings(
    val themePreference: ThemePreference = ThemePreference.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val compactMode: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
)
