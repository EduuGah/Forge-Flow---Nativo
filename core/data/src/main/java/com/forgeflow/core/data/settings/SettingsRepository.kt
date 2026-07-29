package com.forgeflow.core.data.settings

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.UserSettings
import com.forgeflow.core.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>

    suspend fun setThemePreference(preference: ThemePreference): DataResult<Unit>

    suspend fun setAccentColor(color: AccentColor): DataResult<Unit>

    suspend fun setWeightUnit(unit: WeightUnit): DataResult<Unit>

    suspend fun setCompactMode(enabled: Boolean): DataResult<Unit>

    suspend fun setOnboardingCompleted(completed: Boolean): DataResult<Unit>
}
