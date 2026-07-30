package com.forgeflow.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.profile.ProfileRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.platform.health.HealthConnectAvailability
import com.forgeflow.core.platform.health.HealthConnectManager
import com.forgeflow.core.platform.health.HealthConnectStatus
import com.forgeflow.core.model.Weight
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val workoutRepository: WorkoutRepository,
    private val healthConnectManager: HealthConnectManager,
) : ViewModel() {
    private val healthStatus = MutableStateFlow(
        HealthConnectStatus(HealthConnectAvailability.UNAVAILABLE),
    )
    private val operation = MutableStateFlow(SettingsOperationState())

    val uiState = combine(
        settingsRepository.observeSettings(),
        profileRepository.observeProfile(),
        healthStatus,
        operation,
    ) { settings, profile, currentHealthStatus, currentOperation ->
            SettingsUiState(
                theme = settings.themePreference,
                accent = settings.accentColor,
                weightUnit = settings.weightUnit,
                compactMode = settings.compactMode,
                profile = SettingsProfileUiModel(
                    displayName = profile.displayName,
                    birthYear = profile.birthYear,
                    heightCentimeters = profile.heightCentimeters,
                    bodyWeightLabel = profile.bodyWeight?.let { weight ->
                        "${weight.valueIn(settings.weightUnit).toInputValue()} " +
                            settings.weightUnit.shortLabel()
                    },
                    trainingGoal = profile.trainingGoal,
                    experienceLevel = profile.experienceLevel,
                    photos = profile.progressPhotos.map { photo ->
                        ProgressPhotoUiModel(
                            id = photo.id,
                            filePath = photo.filePath,
                            dateLabel = PHOTO_DATE_FORMATTER.format(
                                photo.capturedAt.atZone(ZoneId.systemDefault()),
                            ),
                        )
                    },
                ),
                profileEditor = currentOperation.profileEditor,
                isImportingPhoto = currentOperation.isImportingPhoto,
                profileWriteFailed = currentOperation.profileWriteFailed,
                healthConnectAvailability = currentHealthStatus.availability,
                isCheckingHealthConnect = currentOperation.isCheckingHealthConnect,
                healthConnectHasPermissions = currentHealthStatus.hasPermissions,
                healthConnectSyncEnabled = settings.healthConnectSyncEnabled,
                isSyncingHealthHistory = currentOperation.isSyncingHealthHistory,
                healthSyncResult = currentOperation.healthSyncResult,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    init {
        refreshHealthConnect()
    }

    fun createHealthPermissionContract() =
        healthConnectManager.createPermissionRequestContract()

    val requiredHealthPermissions: Set<String>
        get() = healthConnectManager.requiredPermissions

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OpenProfileEditor -> openProfileEditor()
            SettingsAction.CloseProfileEditor -> operation.update {
                it.copy(profileEditor = null)
            }
            is SettingsAction.ProfileNameChanged -> updateProfileEditor {
                copy(displayName = action.value.take(MAX_PROFILE_NAME_LENGTH))
            }
            is SettingsAction.ProfileBirthYearChanged -> updateProfileEditor {
                copy(birthYear = action.value.filter(Char::isDigit).take(4))
            }
            is SettingsAction.ProfileHeightChanged -> updateProfileEditor {
                copy(heightCentimeters = action.value.filter(Char::isDigit).take(3))
            }
            is SettingsAction.ProfileBodyWeightChanged -> updateProfileEditor {
                copy(bodyWeight = action.value.toDecimalInput())
            }
            is SettingsAction.ProfileGoalChanged -> updateProfileEditor {
                copy(trainingGoal = action.value)
            }
            is SettingsAction.ProfileExperienceChanged -> updateProfileEditor {
                copy(experienceLevel = action.value)
            }
            SettingsAction.SaveProfile -> saveProfile()
            is SettingsAction.ImportProgressPhoto -> importProgressPhoto(action.sourceUri)
            is SettingsAction.DeleteProgressPhoto -> deleteProgressPhoto(action.photoId)
            SettingsAction.DismissProfileError -> operation.update {
                it.copy(profileWriteFailed = false)
            }
            is SettingsAction.AccentChanged -> updateSetting {
                settingsRepository.setAccentColor(action.value)
            }
            is SettingsAction.CompactModeChanged -> updateSetting {
                settingsRepository.setCompactMode(action.enabled)
            }
            is SettingsAction.ThemeChanged -> updateSetting {
                settingsRepository.setThemePreference(action.value)
            }
            is SettingsAction.WeightUnitChanged -> updateSetting {
                settingsRepository.setWeightUnit(action.value)
            }
            is SettingsAction.HealthConnectSyncChanged -> updateSetting {
                settingsRepository.setHealthConnectSyncEnabled(
                    action.enabled && healthStatus.value.hasPermissions,
                )
            }
            is SettingsAction.HealthConnectPermissionsResult -> {
                val granted = action.grantedPermissions.containsAll(
                    healthConnectManager.requiredPermissions,
                )
                viewModelScope.launch {
                    if (granted) {
                        settingsRepository.setHealthConnectSyncEnabled(true)
                    }
                    refreshHealthConnect()
                }
            }
            SettingsAction.HealthConnectRefresh -> refreshHealthConnect()
            SettingsAction.HealthConnectSyncHistory -> syncHealthHistory()
            SettingsAction.OpenHealthConnect -> healthConnectManager.openHealthConnect()
            SettingsAction.InstallHealthConnect -> healthConnectManager.openInstallOrUpdate()
        }
    }

    private fun openProfileEditor() {
        viewModelScope.launch {
            val profile = profileRepository.observeProfile().first()
            val settings = settingsRepository.observeSettings().first()
            operation.update {
                it.copy(
                    profileWriteFailed = false,
                    profileEditor = ProfileEditorUiState(
                        displayName = profile.displayName,
                        birthYear = profile.birthYear?.toString().orEmpty(),
                        heightCentimeters = profile.heightCentimeters?.toString().orEmpty(),
                        bodyWeight = profile.bodyWeight
                            ?.valueIn(settings.weightUnit)
                            ?.toInputValue()
                            .orEmpty(),
                        trainingGoal = profile.trainingGoal,
                        experienceLevel = profile.experienceLevel,
                    ),
                )
            }
        }
    }

    private fun updateProfileEditor(
        transform: ProfileEditorUiState.() -> ProfileEditorUiState,
    ) {
        operation.update { current ->
            current.copy(profileEditor = current.profileEditor?.transform())
        }
    }

    private fun saveProfile() {
        val editor = operation.value.profileEditor ?: return
        if (editor.isSaving) return
        viewModelScope.launch {
            updateProfileEditor { copy(isSaving = true) }
            val currentProfile = profileRepository.observeProfile().first()
            val settings = settingsRepository.observeSettings().first()
            val birthYear = editor.birthYear.toIntOrNull()
                ?.takeIf { it in MIN_BIRTH_YEAR..LocalDate.now().year }
            val height = editor.heightCentimeters.toIntOrNull()
                ?.takeIf { it in MIN_HEIGHT_CENTIMETERS..MAX_HEIGHT_CENTIMETERS }
            val bodyWeight = editor.bodyWeight.replace(',', '.').toDoubleOrNull()
                ?.takeIf { it in MIN_BODY_WEIGHT..MAX_BODY_WEIGHT }
                ?.let { Weight.from(it, settings.weightUnit) }
            val result = profileRepository.saveProfile(
                currentProfile.copy(
                    displayName = editor.displayName.trim(),
                    birthYear = birthYear,
                    heightCentimeters = height,
                    bodyWeight = bodyWeight,
                    trainingGoal = editor.trainingGoal,
                    experienceLevel = editor.experienceLevel,
                ),
            )
            operation.update { current ->
                current.copy(
                    profileEditor = if (result is DataResult.Success) {
                        null
                    } else {
                        current.profileEditor?.copy(isSaving = false)
                    },
                    profileWriteFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun importProgressPhoto(sourceUri: String) {
        if (operation.value.isImportingPhoto) return
        viewModelScope.launch {
            operation.update {
                it.copy(isImportingPhoto = true, profileWriteFailed = false)
            }
            val result = profileRepository.importProgressPhoto(sourceUri)
            operation.update {
                it.copy(
                    isImportingPhoto = false,
                    profileWriteFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun deleteProgressPhoto(photoId: String) {
        viewModelScope.launch {
            val result = profileRepository.deleteProgressPhoto(photoId)
            operation.update {
                it.copy(profileWriteFailed = result is DataResult.Failure)
            }
        }
    }

    private fun updateSetting(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private fun refreshHealthConnect() {
        viewModelScope.launch {
            operation.update { it.copy(isCheckingHealthConnect = true) }
            val currentStatus = healthConnectManager.status()
            healthStatus.value = currentStatus
            if (!currentStatus.hasPermissions) {
                settingsRepository.setHealthConnectSyncEnabled(false)
            }
            operation.update { it.copy(isCheckingHealthConnect = false) }
        }
    }

    private fun syncHealthHistory() {
        if (
            operation.value.isSyncingHealthHistory ||
            !healthStatus.value.hasPermissions
        ) {
            return
        }
        viewModelScope.launch {
            operation.update {
                it.copy(
                    isSyncingHealthHistory = true,
                    healthSyncResult = null,
                )
            }
            val settings = settingsRepository.observeSettings().first()
            val history = workoutRepository.observeHistory().first()
            val result = when (history) {
                is DataResult.Failure -> HealthSyncUiResult(
                    syncedCount = 0,
                    failedCount = 1,
                )
                is DataResult.Success -> {
                    val syncResult = healthConnectManager.syncWorkouts(
                        workouts = history.value,
                        weightUnit = settings.weightUnit,
                    )
                    HealthSyncUiResult(
                        syncedCount = syncResult.syncedCount,
                        failedCount = syncResult.failedCount,
                    )
                }
            }
            operation.update {
                it.copy(
                    isSyncingHealthHistory = false,
                    healthSyncResult = result,
                )
            }
        }
    }

    private data class SettingsOperationState(
        val isCheckingHealthConnect: Boolean = true,
        val isSyncingHealthHistory: Boolean = false,
        val healthSyncResult: HealthSyncUiResult? = null,
        val profileEditor: ProfileEditorUiState? = null,
        val isImportingPhoto: Boolean = false,
        val profileWriteFailed: Boolean = false,
    )

    private fun Double.toInputValue(): String =
        if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

    private fun String.toDecimalInput(): String {
        var hasDecimalSeparator = false
        return replace(',', '.')
            .filterIndexed { index, character ->
                when {
                    character.isDigit() -> true
                    character == '.' && index > 0 && !hasDecimalSeparator -> {
                        hasDecimalSeparator = true
                        true
                    }
                    else -> false
                }
            }
            .take(MAX_WEIGHT_INPUT_LENGTH)
    }

    private fun com.forgeflow.core.model.WeightUnit.shortLabel(): String =
        if (this == com.forgeflow.core.model.WeightUnit.KILOGRAM) "kg" else "lb"

    private companion object {
        const val MAX_PROFILE_NAME_LENGTH = 32
        const val MAX_WEIGHT_INPUT_LENGTH = 6
        const val MIN_BIRTH_YEAR = 1900
        const val MIN_HEIGHT_CENTIMETERS = 100
        const val MAX_HEIGHT_CENTIMETERS = 250
        const val MIN_BODY_WEIGHT = 20.0
        const val MAX_BODY_WEIGHT = 500.0
        val PHOTO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
