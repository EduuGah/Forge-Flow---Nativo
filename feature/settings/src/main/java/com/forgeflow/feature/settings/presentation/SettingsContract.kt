package com.forgeflow.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.platform.health.HealthConnectAvailability

@Immutable
data class SettingsUiState(
    val theme: ThemePreference = ThemePreference.DARK,
    val accent: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val compactMode: Boolean = false,
    val profile: SettingsProfileUiModel = SettingsProfileUiModel(),
    val profileEditor: ProfileEditorUiState? = null,
    val isImportingPhoto: Boolean = false,
    val profileWriteFailed: Boolean = false,
    val healthConnectAvailability: HealthConnectAvailability =
        HealthConnectAvailability.UNAVAILABLE,
    val isCheckingHealthConnect: Boolean = true,
    val healthConnectHasPermissions: Boolean = false,
    val healthConnectSyncEnabled: Boolean = false,
    val isSyncingHealthHistory: Boolean = false,
    val healthSyncResult: HealthSyncUiResult? = null,
)

@Immutable
data class SettingsProfileUiModel(
    val displayName: String = "",
    val birthYear: Int? = null,
    val heightCentimeters: Int? = null,
    val bodyWeightLabel: String? = null,
    val trainingGoal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val photos: List<ProgressPhotoUiModel> = emptyList(),
)

@Immutable
data class ProgressPhotoUiModel(
    val id: String,
    val filePath: String,
    val dateLabel: String,
)

@Immutable
data class ProfileEditorUiState(
    val displayName: String = "",
    val birthYear: String = "",
    val heightCentimeters: String = "",
    val bodyWeight: String = "",
    val trainingGoal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val isSaving: Boolean = false,
)

@Immutable
data class HealthSyncUiResult(
    val syncedCount: Int,
    val failedCount: Int,
)

sealed interface SettingsAction {
    data object OpenProfileEditor : SettingsAction
    data object CloseProfileEditor : SettingsAction
    data class ProfileNameChanged(val value: String) : SettingsAction
    data class ProfileBirthYearChanged(val value: String) : SettingsAction
    data class ProfileHeightChanged(val value: String) : SettingsAction
    data class ProfileBodyWeightChanged(val value: String) : SettingsAction
    data class ProfileGoalChanged(val value: TrainingGoal) : SettingsAction
    data class ProfileExperienceChanged(val value: ExperienceLevel) : SettingsAction
    data object SaveProfile : SettingsAction
    data class ImportProgressPhoto(val sourceUri: String) : SettingsAction
    data class DeleteProgressPhoto(val photoId: String) : SettingsAction
    data object DismissProfileError : SettingsAction
    data class ThemeChanged(val value: ThemePreference) : SettingsAction
    data class AccentChanged(val value: AccentColor) : SettingsAction
    data class WeightUnitChanged(val value: WeightUnit) : SettingsAction
    data class CompactModeChanged(val enabled: Boolean) : SettingsAction
    data class HealthConnectSyncChanged(val enabled: Boolean) : SettingsAction
    data class HealthConnectPermissionsResult(
        val grantedPermissions: Set<String>,
    ) : SettingsAction
    data object HealthConnectRefresh : SettingsAction
    data object HealthConnectSyncHistory : SettingsAction
    data object OpenHealthConnect : SettingsAction
    data object InstallHealthConnect : SettingsAction
}
