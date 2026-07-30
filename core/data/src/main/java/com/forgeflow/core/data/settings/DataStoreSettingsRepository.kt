package com.forgeflow.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.TrainingDay
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WeightUnit
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override fun observeSettings(): Flow<UserSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            UserSettings(
                themePreference = preferences[THEME_PREFERENCE]
                    ?.let { storedValue ->
                        ThemePreference.entries.firstOrNull { it.name == storedValue }
                    }
                    ?: ThemePreference.DARK,
                accentColor = preferences[ACCENT_COLOR]
                    ?.let { storedValue ->
                        AccentColor.entries.firstOrNull { it.name == storedValue }
                    }
                    ?: AccentColor.BLUE,
                weightUnit = preferences[WEIGHT_UNIT]
                    ?.let { storedValue ->
                        WeightUnit.entries.firstOrNull { it.name == storedValue }
                    }
                    ?: WeightUnit.KILOGRAM,
                compactMode = preferences[COMPACT_MODE] ?: false,
                weeklyWorkoutGoal = (preferences[WEEKLY_WORKOUT_GOAL] ?: 3)
                    .coerceIn(MIN_WEEKLY_GOAL, MAX_WEEKLY_GOAL),
                trainingDays = preferences[TRAINING_DAYS]
                    .orEmpty()
                    .mapNotNullTo(mutableSetOf()) { storedValue ->
                        TrainingDay.entries.firstOrNull { it.name == storedValue }
                    },
                preferredWorkoutTimeMinutes =
                    (preferences[PREFERRED_WORKOUT_TIME_MINUTES] ?: DEFAULT_WORKOUT_TIME_MINUTES)
                        .coerceIn(MIN_TIME_MINUTES, MAX_TIME_MINUTES),
                hasCompletedOnboarding = preferences[ONBOARDING_COMPLETED] ?: false,
                hasRequestedNotificationPermission =
                    preferences[NOTIFICATION_PERMISSION_REQUESTED] ?: false,
            )
        }

    override suspend fun setThemePreference(
        preference: ThemePreference,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[THEME_PREFERENCE] = preference.name
    }

    override suspend fun setAccentColor(
        color: AccentColor,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[ACCENT_COLOR] = color.name
    }

    override suspend fun setWeightUnit(
        unit: WeightUnit,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[WEIGHT_UNIT] = unit.name
    }

    override suspend fun setCompactMode(
        enabled: Boolean,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[COMPACT_MODE] = enabled
    }

    override suspend fun setWeeklyWorkoutGoal(
        goal: Int,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[WEEKLY_WORKOUT_GOAL] = goal.coerceIn(MIN_WEEKLY_GOAL, MAX_WEEKLY_GOAL)
    }

    override suspend fun setTrainingDays(
        days: Set<TrainingDay>,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[TRAINING_DAYS] = days.mapTo(mutableSetOf(), TrainingDay::name)
    }

    override suspend fun setPreferredWorkoutTime(
        minutesFromMidnight: Int,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[PREFERRED_WORKOUT_TIME_MINUTES] =
            minutesFromMidnight.coerceIn(MIN_TIME_MINUTES, MAX_TIME_MINUTES)
    }

    override suspend fun setOnboardingCompleted(
        completed: Boolean,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[ONBOARDING_COMPLETED] = completed
    }

    override suspend fun setNotificationPermissionRequested(
        requested: Boolean,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[NOTIFICATION_PERMISSION_REQUESTED] = requested
    }

    private suspend fun updatePreferences(
        transform: suspend (MutablePreferences) -> Unit,
    ): DataResult<Unit> = runCatching {
        dataStore.edit(transform)
    }.fold(
        onSuccess = { DataResult.Success(Unit) },
        onFailure = { DataResult.Failure(AppError.WriteFailed) },
    )

    private companion object {
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        val WEEKLY_WORKOUT_GOAL = intPreferencesKey("weekly_workout_goal")
        val TRAINING_DAYS = stringSetPreferencesKey("training_days")
        val PREFERRED_WORKOUT_TIME_MINUTES =
            intPreferencesKey("preferred_workout_time_minutes")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATION_PERMISSION_REQUESTED =
            booleanPreferencesKey("notification_permission_requested")
        const val MIN_WEEKLY_GOAL = 1
        const val MAX_WEEKLY_GOAL = 7
        const val DEFAULT_WORKOUT_TIME_MINUTES = 18 * 60
        const val MIN_TIME_MINUTES = 0
        const val MAX_TIME_MINUTES = 24 * 60 - 1
    }
}
