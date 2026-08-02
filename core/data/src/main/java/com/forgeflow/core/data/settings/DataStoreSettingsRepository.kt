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
import com.forgeflow.core.data.auth.AuthRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.HealthConnectDataType
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.TrainingDay
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WeightUnit
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository,
) : SettingsRepository {
    override fun observeSettings(): Flow<UserSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .combine(authRepository.observeSession()) { preferences, session ->
            val completedUserIds = preferences[PROFILE_COMPLETED_USER_IDS].orEmpty()
            val currentUserId = session?.userId
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
                healthConnectSyncEnabled =
                    preferences[HEALTH_CONNECT_SYNC_ENABLED] ?: false,
                healthConnectReadDataTypes = preferences[HEALTH_CONNECT_READ_DATA_TYPES]
                    ?.mapNotNullTo(mutableSetOf()) { storedValue ->
                        HealthConnectDataType.entries.firstOrNull { it.name == storedValue }
                    }
                    ?: DEFAULT_HEALTH_CONNECT_READ_DATA_TYPES,
                profileCompletedForUserId = currentUserId?.takeIf { userId ->
                    userId in completedUserIds ||
                        preferences[PROFILE_COMPLETED_FOR_USER_ID] == userId
                },
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

    override suspend fun setHealthConnectSyncEnabled(
        enabled: Boolean,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[HEALTH_CONNECT_SYNC_ENABLED] = enabled
    }

    override suspend fun setHealthConnectReadDataTypes(
        dataTypes: Set<HealthConnectDataType>,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[HEALTH_CONNECT_READ_DATA_TYPES] =
            dataTypes.mapTo(mutableSetOf(), HealthConnectDataType::name)
    }

    override suspend fun setProfileCompletedForUserId(
        userId: String,
    ): DataResult<Unit> = updatePreferences { preferences ->
        preferences[PROFILE_COMPLETED_USER_IDS] =
            preferences[PROFILE_COMPLETED_USER_IDS].orEmpty() + userId
        preferences[PROFILE_COMPLETED_FOR_USER_ID] = userId
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
        val HEALTH_CONNECT_SYNC_ENABLED =
            booleanPreferencesKey("health_connect_sync_enabled")
        val HEALTH_CONNECT_READ_DATA_TYPES =
            stringSetPreferencesKey("health_connect_read_data_types")
        val PROFILE_COMPLETED_FOR_USER_ID =
            stringPreferencesKey("profile_completed_for_user_id")
        val PROFILE_COMPLETED_USER_IDS =
            stringSetPreferencesKey("profile_completed_user_ids")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATION_PERMISSION_REQUESTED =
            booleanPreferencesKey("notification_permission_requested")
        const val MIN_WEEKLY_GOAL = 1
        const val MAX_WEEKLY_GOAL = 7
        const val DEFAULT_WORKOUT_TIME_MINUTES = 18 * 60
        const val MIN_TIME_MINUTES = 0
        const val MAX_TIME_MINUTES = 24 * 60 - 1
        val DEFAULT_HEALTH_CONNECT_READ_DATA_TYPES = setOf(
            HealthConnectDataType.BODY_WEIGHT,
            HealthConnectDataType.STEPS,
            HealthConnectDataType.EXERCISE_SESSIONS,
        )
    }
}
