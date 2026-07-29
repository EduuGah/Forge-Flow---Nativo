package com.forgeflow.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.forgeflow.core.common.result.AppError
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
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
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATION_PERMISSION_REQUESTED =
            booleanPreferencesKey("notification_permission_requested")
    }
}
