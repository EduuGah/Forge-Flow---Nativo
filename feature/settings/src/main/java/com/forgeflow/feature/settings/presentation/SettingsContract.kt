package com.forgeflow.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.HealthConnectDataType
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
    val weightEditor: BodyWeightEditorUiState? = null,
    val hevyImport: HevyImportUiState = HevyImportUiState(),
    val healthConnectAvailability: HealthConnectAvailability =
        HealthConnectAvailability.UNAVAILABLE,
    val isCheckingHealthConnect: Boolean = true,
    val healthConnectHasPermissions: Boolean = false,
    val healthConnectSyncEnabled: Boolean = false,
    val healthConnectReadDataTypes: Set<HealthConnectDataType> = emptySet(),
    val isSyncingHealthHistory: Boolean = false,
    val healthSyncResult: HealthSyncUiResult? = null,
    val isReadingHealthData: Boolean = false,
    val healthReadResult: HealthReadUiResult? = null,
    val isExportingData: Boolean = false,
    val dataExportResult: DataExportUiResult? = null,
    val account: AccountUiModel = AccountUiModel(),
)

@Immutable
data class AccountUiModel(
    val isConfigured: Boolean = false,
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isSigningIn: Boolean = false,
    val operationFailed: Boolean = false,
)

enum class DataExportUiResult {
    SUCCESS,
    FAILED,
}

@Immutable
data class SettingsProfileUiModel(
    val displayName: String = "",
    val birthYear: Int? = null,
    val heightCentimeters: Int? = null,
    val bodyWeightLabel: String? = null,
    val trainingGoal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val profilePhotoPath: String? = null,
    val bodyWeightHistory: List<BodyWeightEntryUiModel> = emptyList(),
    val photos: List<ProgressPhotoUiModel> = emptyList(),
    val completedWorkoutCount: Int = 0,
    val totalVolumeLabel: String = "0 kg",
)

@Immutable
data class BodyWeightEntryUiModel(
    val id: String,
    val value: Double,
    val valueLabel: String,
    val dateLabel: String,
    val measuredAtEpochMillis: Long,
)

@Immutable
data class ProgressPhotoUiModel(
    val id: String,
    val filePath: String,
    val dateLabel: String,
    val capturedAtEpochMillis: Long,
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

@Immutable
data class HealthReadUiResult(
    val steps: Long? = null,
    val distanceKilometers: Double? = null,
    val caloriesKilocalories: Double? = null,
    val averageHeartRate: Long? = null,
    val exerciseSessionCount: Int? = null,
    val exerciseDurationMinutes: Long? = null,
    val sleepMinutes: Long? = null,
    val importedWeightCount: Int = 0,
)

@Immutable
data class BodyWeightEditorUiState(
    val value: String = "",
    val measuredAtEpochMillis: Long = 0L,
    val isSaving: Boolean = false,
)

@Immutable
data class HevyFileUiModel(
    val fileName: String,
    val recordCount: Int,
    val dateRangeLabel: String,
    val skippedRows: Int,
)

@Immutable
data class HevyImportUiResult(
    val workoutsImported: Int,
    val workoutsAlreadyImported: Int,
    val measurementsImported: Int,
    val measurementsAlreadyImported: Int,
    val skippedRows: Int,
)

@Immutable
data class HevyImportUiState(
    val workoutFile: HevyFileUiModel? = null,
    val measurementFile: HevyFileUiModel? = null,
    val isInspectingFile: Boolean = false,
    val isImporting: Boolean = false,
    val failed: Boolean = false,
    val result: HevyImportUiResult? = null,
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
    data class ImportProfilePhoto(
        val sourceUri: String,
        val zoom: Float,
        val horizontalOffset: Float,
        val verticalOffset: Float,
        val rotationDegrees: Float,
    ) : SettingsAction
    data object OpenBodyWeightEditor : SettingsAction
    data object CloseBodyWeightEditor : SettingsAction
    data class BodyWeightChanged(val value: String) : SettingsAction
    data class BodyWeightDateChanged(val epochMillis: Long) : SettingsAction
    data object SaveBodyWeight : SettingsAction
    data class ImportProgressPhoto(val sourceUri: String) : SettingsAction
    data class DeleteProgressPhoto(val photoId: String) : SettingsAction
    data object DismissProfileError : SettingsAction
    data class ThemeChanged(val value: ThemePreference) : SettingsAction
    data class AccentChanged(val value: AccentColor) : SettingsAction
    data class WeightUnitChanged(val value: WeightUnit) : SettingsAction
    data class CompactModeChanged(val enabled: Boolean) : SettingsAction
    data class HealthConnectSyncChanged(val enabled: Boolean) : SettingsAction
    data class HealthConnectDataTypeChanged(
        val dataType: HealthConnectDataType,
        val enabled: Boolean,
    ) : SettingsAction
    data class HealthConnectPermissionsResult(
        val grantedPermissions: Set<String>,
    ) : SettingsAction
    data object HealthConnectRefresh : SettingsAction
    data object HealthConnectSyncHistory : SettingsAction
    data object HealthConnectReadData : SettingsAction
    data object OpenHealthConnect : SettingsAction
    data object InstallHealthConnect : SettingsAction
    data class HevyWorkoutFileSelected(val sourceUri: String) : SettingsAction
    data class HevyMeasurementFileSelected(val sourceUri: String) : SettingsAction
    data object ImportHevyData : SettingsAction
    data class ExportData(val targetUri: String) : SettingsAction
    data object DismissDataExportResult : SettingsAction
    data class GoogleIdTokenReceived(val idToken: String) : SettingsAction
    data object GoogleSignInFailed : SettingsAction
    data object SignOut : SettingsAction
    data object DismissAccountError : SettingsAction
}
