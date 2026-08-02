package com.forgeflow.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.importer.HevyImportPreview
import com.forgeflow.core.data.backup.LocalDataExportRepository
import com.forgeflow.core.data.backup.CloudBackupRepository
import com.forgeflow.core.data.auth.AuthRepository
import com.forgeflow.core.data.auth.EntitlementRepository
import com.forgeflow.core.data.importer.HevyImportRepository
import com.forgeflow.core.data.importer.HevyImportResult
import com.forgeflow.core.data.profile.ProfileRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.BodyWeightEntry
import com.forgeflow.core.model.BodyWeightSource
import com.forgeflow.core.model.HealthConnectDataType
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.platform.health.HealthConnectAvailability
import com.forgeflow.core.platform.health.HealthConnectManager
import com.forgeflow.core.platform.health.HealthConnectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
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
    private val hevyImportRepository: HevyImportRepository,
    private val healthConnectManager: HealthConnectManager,
    private val localDataExportRepository: LocalDataExportRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
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
        workoutRepository.observeHistory(),
    ) { settings, profile, currentHealthStatus, currentOperation, historyResult ->
        val history = (historyResult as? DataResult.Success)?.value.orEmpty()
        val unitLabel = settings.weightUnit.shortLabel()
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
                    "${weight.valueIn(settings.weightUnit).toInputValue()} $unitLabel"
                },
                trainingGoal = profile.trainingGoal,
                experienceLevel = profile.experienceLevel,
                profilePhotoPath = profile.profilePhotoPath,
                bodyWeightHistory = profile.bodyWeightHistory.map { entry ->
                    val value = entry.weight.valueIn(settings.weightUnit)
                    BodyWeightEntryUiModel(
                        id = entry.id,
                        value = value,
                        valueLabel = "${value.toInputValue()} $unitLabel",
                        dateLabel = PHOTO_DATE_FORMATTER.format(
                            entry.measuredAt.atZone(ZoneId.systemDefault()),
                        ),
                        measuredAtEpochMillis = entry.measuredAt.toEpochMilli(),
                    )
                },
                photos = profile.progressPhotos.map { photo ->
                    ProgressPhotoUiModel(
                        id = photo.id,
                        filePath = photo.filePath,
                        dateLabel = PHOTO_DATE_FORMATTER.format(
                            photo.capturedAt.atZone(ZoneId.systemDefault()),
                        ),
                        capturedAtEpochMillis = photo.capturedAt.toEpochMilli(),
                    )
                },
                completedWorkoutCount = history.size,
                totalVolumeLabel = "${history.sumOf { it.totalVolumeGrams }.gramsIn(
                    settings.weightUnit,
                ).toCompactValue()} $unitLabel",
            ),
            profileEditor = currentOperation.profileEditor,
            isImportingPhoto = currentOperation.isImportingPhoto,
            profileWriteFailed = currentOperation.profileWriteFailed,
            weightEditor = currentOperation.weightEditor,
            hevyImport = HevyImportUiState(
                workoutFile = currentOperation.workoutPreview?.asUiModel(),
                measurementFile = currentOperation.measurementPreview?.asUiModel(),
                isInspectingFile = currentOperation.isInspectingHevyFile,
                isImporting = currentOperation.isImportingHevy,
                failed = currentOperation.hevyImportFailed,
                result = currentOperation.hevyImportResult?.asUiModel(),
            ),
            healthConnectAvailability = currentHealthStatus.availability,
            isCheckingHealthConnect = currentOperation.isCheckingHealthConnect,
            healthConnectHasPermissions = currentHealthStatus.hasPermissions,
            healthConnectSyncEnabled = settings.healthConnectSyncEnabled,
            healthConnectReadDataTypes = settings.healthConnectReadDataTypes,
            isSyncingHealthHistory = currentOperation.isSyncingHealthHistory,
            healthSyncResult = currentOperation.healthSyncResult,
            isReadingHealthData = currentOperation.isReadingHealthData,
            healthReadResult = currentOperation.healthReadResult,
            isExportingData = currentOperation.isExportingData,
            dataExportResult = currentOperation.dataExportResult,
            isRestoringData = currentOperation.isRestoringData,
            isCloudBackupRunning = currentOperation.isCloudBackupRunning,
            dataRestoreResult = currentOperation.dataRestoreResult,
            cloudBackupResult = currentOperation.cloudBackupResult,
            account = AccountUiModel(
                isConfigured = authRepository.isConfigured(),
                isSigningIn = currentOperation.isSigningIn,
                operationFailed = currentOperation.accountOperationFailed,
                editor = currentOperation.accountEditor,
                notice = currentOperation.accountNotice,
            ),
        )
    }.combine(authRepository.observeSession()) { state, session ->
        state.copy(
            account = state.account.copy(
                isLoaded = true,
                isSignedIn = session != null,
                displayName = session?.displayName,
                email = session?.email,
                photoUrl = session?.photoUrl,
                hasPassword = session?.hasPassword == true,
                operationFailed = session == null && state.account.operationFailed,
            ),
        )
    }.combine(entitlementRepository.observeEntitlement()) { state, entitlement ->
        state.copy(
            account = state.account.copy(supporterTier = entitlement?.supporterTier),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(),
    )

    init {
        refreshHealthConnect()
    }

    fun createHealthPermissionContract() =
        healthConnectManager.createPermissionRequestContract()

    val requiredHealthPermissions: Set<String>
        get() = healthConnectManager.permissionsFor(uiState.value.healthConnectReadDataTypes)

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OpenProfileEditor -> openProfileEditor()
            SettingsAction.CloseProfileEditor -> operation.update { it.copy(profileEditor = null) }
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
            is SettingsAction.ImportProfilePhoto -> importProfilePhoto(action)
            SettingsAction.OpenBodyWeightEditor -> openBodyWeightEditor()
            SettingsAction.CloseBodyWeightEditor -> operation.update { it.copy(weightEditor = null) }
            is SettingsAction.BodyWeightChanged -> operation.update { current ->
                current.copy(
                    weightEditor = current.weightEditor?.copy(
                        value = action.value.toDecimalInput(),
                    ),
                )
            }
            is SettingsAction.BodyWeightDateChanged -> operation.update { current ->
                val selectedDate = Instant.ofEpochMilli(action.epochMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                current.copy(
                    weightEditor = current.weightEditor?.copy(
                        measuredAtEpochMillis = if (selectedDate.isAfter(LocalDate.now())) {
                            current.weightEditor.measuredAtEpochMillis
                        } else {
                            action.epochMillis
                        },
                    ),
                )
            }
            SettingsAction.SaveBodyWeight -> saveBodyWeight()
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
            is SettingsAction.HealthConnectDataTypeChanged ->
                updateHealthConnectDataType(action.dataType, action.enabled)
            is SettingsAction.HealthConnectPermissionsResult -> {
                val granted = action.grantedPermissions.containsAll(requiredHealthPermissions)
                viewModelScope.launch {
                    if (granted) settingsRepository.setHealthConnectSyncEnabled(true)
                    refreshHealthConnect()
                }
            }
            SettingsAction.HealthConnectRefresh -> refreshHealthConnect()
            SettingsAction.HealthConnectSyncHistory -> syncHealthHistory()
            SettingsAction.HealthConnectReadData -> readHealthData()
            SettingsAction.OpenHealthConnect -> healthConnectManager.openHealthConnect()
            SettingsAction.InstallHealthConnect -> healthConnectManager.openInstallOrUpdate()
            is SettingsAction.HevyWorkoutFileSelected -> inspectHevyWorkoutFile(action.sourceUri)
            is SettingsAction.HevyMeasurementFileSelected ->
                inspectHevyMeasurementFile(action.sourceUri)
            SettingsAction.ImportHevyData -> importHevyData()
            is SettingsAction.ExportData -> exportData(action.targetUri)
            is SettingsAction.RestoreData -> restoreData(action.sourceUri)
            SettingsAction.SaveCloudBackup -> saveCloudBackup()
            SettingsAction.RestoreCloudBackup -> restoreCloudBackup()
            SettingsAction.DismissDataExportResult -> operation.update {
                it.copy(
                    dataExportResult = null,
                    dataRestoreResult = null,
                    cloudBackupResult = null,
                )
            }
            is SettingsAction.GoogleIdTokenReceived -> signInWithGoogle(action.idToken)
            SettingsAction.GoogleSignInFailed -> operation.update {
                it.copy(isSigningIn = false, accountOperationFailed = true)
            }
            is SettingsAction.OpenAccountAuth -> openAccountAuth(action.mode)
            SettingsAction.CloseAccountAuth -> operation.update { it.copy(accountEditor = null) }
            is SettingsAction.AccountEmailChanged -> updateAccountEditor {
                copy(email = action.value.trimStart().take(MAX_EMAIL_LENGTH), validationFailed = false)
            }
            is SettingsAction.AccountPasswordChanged -> updateAccountEditor {
                copy(password = action.value.take(MAX_PASSWORD_LENGTH), validationFailed = false)
            }
            is SettingsAction.AccountPasswordConfirmationChanged -> updateAccountEditor {
                copy(
                    passwordConfirmation = action.value.take(MAX_PASSWORD_LENGTH),
                    validationFailed = false,
                )
            }
            SettingsAction.SubmitAccountAuth -> submitAccountAuth()
            SettingsAction.SignOut -> signOut()
            SettingsAction.DismissAccountError -> operation.update {
                it.copy(accountOperationFailed = false, accountNotice = null)
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        if (operation.value.isSigningIn) return
        viewModelScope.launch {
            operation.update { it.copy(isSigningIn = true, accountOperationFailed = false) }
            val result = authRepository.signInWithGoogleIdToken(idToken)
            operation.update {
                it.copy(
                    isSigningIn = false,
                    accountOperationFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            val result = authRepository.signOut()
            operation.update {
                it.copy(accountOperationFailed = result is DataResult.Failure)
            }
        }
    }

    private fun openAccountAuth(mode: AccountAuthMode) {
        operation.update {
            it.copy(
                accountEditor = AccountAuthEditorUiState(
                    mode = mode,
                    email = if (mode == AccountAuthMode.CREATE_PASSWORD) {
                        uiState.value.account.email.orEmpty()
                    } else {
                        ""
                    },
                ),
                accountOperationFailed = false,
                accountNotice = null,
            )
        }
    }

    private fun updateAccountEditor(
        transform: AccountAuthEditorUiState.() -> AccountAuthEditorUiState,
    ) {
        operation.update { current ->
            current.copy(accountEditor = current.accountEditor?.transform())
        }
    }

    private fun submitAccountAuth() {
        val editor = operation.value.accountEditor ?: return
        if (editor.isSaving) return
        val needsEmail = editor.mode != AccountAuthMode.CREATE_PASSWORD
        val needsPassword = editor.mode != AccountAuthMode.RESET_PASSWORD
        val needsConfirmation = editor.mode in setOf(
            AccountAuthMode.CREATE_ACCOUNT,
            AccountAuthMode.CREATE_PASSWORD,
        )
        val isValid = (!needsEmail || android.util.Patterns.EMAIL_ADDRESS.matcher(editor.email).matches()) &&
            (!needsPassword || editor.password.length >= MIN_PASSWORD_LENGTH) &&
            (!needsConfirmation || editor.password == editor.passwordConfirmation)
        if (!isValid) {
            updateAccountEditor { copy(validationFailed = true) }
            return
        }
        viewModelScope.launch {
            updateAccountEditor { copy(isSaving = true, validationFailed = false) }
            val result = when (editor.mode) {
                AccountAuthMode.SIGN_IN -> authRepository.signInWithEmail(
                    editor.email,
                    editor.password,
                )
                AccountAuthMode.CREATE_ACCOUNT -> authRepository.createAccountWithEmail(
                    editor.email,
                    editor.password,
                )
                AccountAuthMode.RESET_PASSWORD -> authRepository.sendPasswordReset(editor.email)
                AccountAuthMode.CREATE_PASSWORD -> authRepository.linkPassword(editor.password)
            }
            operation.update { current ->
                if (result is DataResult.Success) {
                    current.copy(
                        accountEditor = null,
                        accountOperationFailed = false,
                        accountNotice = when (editor.mode) {
                            AccountAuthMode.SIGN_IN -> null
                            AccountAuthMode.CREATE_ACCOUNT -> AccountNoticeUi.ACCOUNT_CREATED
                            AccountAuthMode.RESET_PASSWORD -> AccountNoticeUi.PASSWORD_RESET_SENT
                            AccountAuthMode.CREATE_PASSWORD -> AccountNoticeUi.PASSWORD_CREATED
                        },
                    )
                } else {
                    current.copy(
                        accountEditor = current.accountEditor?.copy(isSaving = false),
                        accountOperationFailed = true,
                    )
                }
            }
        }
    }

    private fun exportData(targetUri: String) {
        if (operation.value.isExportingData) return
        viewModelScope.launch {
            operation.update {
                it.copy(isExportingData = true, dataExportResult = null)
            }
            val result = localDataExportRepository.export(targetUri)
            operation.update {
                it.copy(
                    isExportingData = false,
                    dataExportResult = if (result is DataResult.Success) {
                        DataExportUiResult.SUCCESS
                    } else {
                        DataExportUiResult.FAILED
                    },
                )
            }
        }
    }

    private fun restoreData(sourceUri: String) {
        if (operation.value.isRestoringData) return
        viewModelScope.launch {
            operation.update {
                it.copy(isRestoringData = true, dataRestoreResult = null)
            }
            val result = localDataExportRepository.prepareRestore(sourceUri)
            operation.update {
                it.copy(
                    isRestoringData = false,
                    dataRestoreResult = if (result is DataResult.Success) {
                        DataRestoreUiResult.READY_TO_RESTART
                    } else {
                        DataRestoreUiResult.FAILED
                    },
                )
            }
        }
    }

    private fun saveCloudBackup() {
        if (operation.value.isCloudBackupRunning || !cloudBackupRepository.isConfigured()) return
        viewModelScope.launch {
            operation.update {
                it.copy(isCloudBackupRunning = true, cloudBackupResult = null)
            }
            val result = cloudBackupRepository.uploadLatest()
            operation.update {
                it.copy(
                    isCloudBackupRunning = false,
                    cloudBackupResult = if (result is DataResult.Success) {
                        CloudBackupUiResult.BACKUP_SAVED
                    } else {
                        CloudBackupUiResult.FAILED
                    },
                )
            }
        }
    }

    private fun restoreCloudBackup() {
        if (operation.value.isCloudBackupRunning || !cloudBackupRepository.isConfigured()) return
        viewModelScope.launch {
            operation.update {
                it.copy(isCloudBackupRunning = true, cloudBackupResult = null)
            }
            val result = cloudBackupRepository.prepareLatestRestore()
            operation.update {
                it.copy(
                    isCloudBackupRunning = false,
                    cloudBackupResult = if (result is DataResult.Success) {
                        CloudBackupUiResult.RESTORE_READY_TO_RESTART
                    } else {
                        CloudBackupUiResult.FAILED
                    },
                )
            }
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
            if (
                result is DataResult.Success && bodyWeight != null &&
                currentProfile.bodyWeight?.grams != bodyWeight.grams
            ) {
                profileRepository.addBodyWeight(bodyWeight, Instant.now())
            }
            operation.update { current ->
                current.copy(
                    profileEditor = if (result is DataResult.Success) null else {
                        current.profileEditor?.copy(isSaving = false)
                    },
                    profileWriteFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun importProfilePhoto(action: SettingsAction.ImportProfilePhoto) {
        if (operation.value.isImportingPhoto) return
        viewModelScope.launch {
            operation.update { it.copy(isImportingPhoto = true, profileWriteFailed = false) }
            val result = profileRepository.importProfilePhoto(
                sourceUri = action.sourceUri,
                zoom = action.zoom,
                horizontalOffset = action.horizontalOffset,
                verticalOffset = action.verticalOffset,
                rotationDegrees = action.rotationDegrees,
            )
            operation.update {
                it.copy(
                    isImportingPhoto = false,
                    profileWriteFailed = result is DataResult.Failure,
                )
            }
        }
    }

    private fun openBodyWeightEditor() {
        val initialValue = uiState.value.profile.bodyWeightLabel
            ?.substringBefore(' ')
            .orEmpty()
        operation.update {
            it.copy(
                weightEditor = BodyWeightEditorUiState(
                    value = initialValue,
                    measuredAtEpochMillis = LocalDate.now()
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli(),
                ),
            )
        }
    }

    private fun saveBodyWeight() {
        val editor = operation.value.weightEditor ?: return
        val value = editor.value.replace(',', '.').toDoubleOrNull()
            ?.takeIf { it in MIN_BODY_WEIGHT..MAX_BODY_WEIGHT }
            ?: return
        val measuredDate = Instant.ofEpochMilli(editor.measuredAtEpochMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        if (measuredDate.isAfter(LocalDate.now())) return
        val measuredAt = measuredDate.atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
        viewModelScope.launch {
            operation.update { it.copy(weightEditor = editor.copy(isSaving = true)) }
            val settings = settingsRepository.observeSettings().first()
            val weight = Weight.from(value, settings.weightUnit)
            val currentProfile = profileRepository.observeProfile().first()
            val profileResult = profileRepository.saveProfile(currentProfile.copy(bodyWeight = weight))
            val historyResult = if (profileResult is DataResult.Success) {
                profileRepository.addBodyWeight(weight, measuredAt)
            } else {
                profileResult
            }
            operation.update {
                it.copy(
                    weightEditor = if (historyResult is DataResult.Success) null else {
                        editor.copy(isSaving = false)
                    },
                    profileWriteFailed = historyResult is DataResult.Failure,
                )
            }
        }
    }

    private fun importProgressPhoto(sourceUri: String) {
        if (operation.value.isImportingPhoto) return
        viewModelScope.launch {
            operation.update { it.copy(isImportingPhoto = true, profileWriteFailed = false) }
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
            operation.update { it.copy(profileWriteFailed = result is DataResult.Failure) }
        }
    }

    private fun inspectHevyWorkoutFile(sourceUri: String) {
        viewModelScope.launch {
            operation.update {
                it.copy(isInspectingHevyFile = true, hevyImportFailed = false, hevyImportResult = null)
            }
            when (val result = hevyImportRepository.previewWorkouts(sourceUri)) {
                is DataResult.Success -> operation.update {
                    it.copy(
                        workoutSourceUri = sourceUri,
                        workoutPreview = result.value,
                        isInspectingHevyFile = false,
                    )
                }
                is DataResult.Failure -> operation.update {
                    it.copy(isInspectingHevyFile = false, hevyImportFailed = true)
                }
            }
        }
    }

    private fun inspectHevyMeasurementFile(sourceUri: String) {
        viewModelScope.launch {
            operation.update {
                it.copy(isInspectingHevyFile = true, hevyImportFailed = false, hevyImportResult = null)
            }
            when (val result = hevyImportRepository.previewMeasurements(sourceUri)) {
                is DataResult.Success -> operation.update {
                    it.copy(
                        measurementSourceUri = sourceUri,
                        measurementPreview = result.value,
                        isInspectingHevyFile = false,
                    )
                }
                is DataResult.Failure -> operation.update {
                    it.copy(isInspectingHevyFile = false, hevyImportFailed = true)
                }
            }
        }
    }

    private fun importHevyData() {
        val current = operation.value
        if (
            current.isImportingHevy ||
            (current.workoutSourceUri == null && current.measurementSourceUri == null)
        ) {
            return
        }
        viewModelScope.launch {
            operation.update {
                it.copy(isImportingHevy = true, hevyImportFailed = false, hevyImportResult = null)
            }
            when (
                val result = hevyImportRepository.import(
                    workoutSourceUri = current.workoutSourceUri,
                    measurementSourceUri = current.measurementSourceUri,
                )
            ) {
                is DataResult.Success -> operation.update {
                    it.copy(isImportingHevy = false, hevyImportResult = result.value)
                }
                is DataResult.Failure -> operation.update {
                    it.copy(isImportingHevy = false, hevyImportFailed = true)
                }
            }
        }
    }

    private fun updateSetting(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private fun updateHealthConnectDataType(
        dataType: HealthConnectDataType,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            val current = settingsRepository.observeSettings().first().healthConnectReadDataTypes
            val updated = if (enabled) current + dataType else current - dataType
            settingsRepository.setHealthConnectReadDataTypes(updated)
            healthStatus.value = healthConnectManager.status(
                healthConnectManager.permissionsFor(updated),
            )
        }
    }

    private fun refreshHealthConnect() {
        viewModelScope.launch {
            operation.update { it.copy(isCheckingHealthConnect = true) }
            val settings = settingsRepository.observeSettings().first()
            val currentStatus = healthConnectManager.status(
                healthConnectManager.permissionsFor(settings.healthConnectReadDataTypes),
            )
            healthStatus.value = currentStatus
            if (!currentStatus.hasPermissions) {
                settingsRepository.setHealthConnectSyncEnabled(false)
            }
            operation.update { it.copy(isCheckingHealthConnect = false) }
        }
    }

    private fun syncHealthHistory() {
        if (operation.value.isSyncingHealthHistory || !healthStatus.value.hasPermissions) return
        viewModelScope.launch {
            operation.update { it.copy(isSyncingHealthHistory = true, healthSyncResult = null) }
            val settings = settingsRepository.observeSettings().first()
            val history = workoutRepository.observeHistory().first()
            val result = when (history) {
                is DataResult.Failure -> HealthSyncUiResult(syncedCount = 0, failedCount = 1)
                is DataResult.Success -> {
                    val syncResult = healthConnectManager.syncWorkouts(
                        workouts = history.value,
                        weightUnit = settings.weightUnit,
                    )
                    HealthSyncUiResult(syncResult.syncedCount, syncResult.failedCount)
                }
            }
            operation.update {
                it.copy(isSyncingHealthHistory = false, healthSyncResult = result)
            }
        }
    }

    private fun readHealthData() {
        if (operation.value.isReadingHealthData || !healthStatus.value.hasPermissions) return
        viewModelScope.launch {
            operation.update { it.copy(isReadingHealthData = true, healthReadResult = null) }
            val dataTypes = settingsRepository.observeSettings().first().healthConnectReadDataTypes
            val endTime = Instant.now()
            val result = healthConnectManager.readData(
                dataTypes = dataTypes,
                startTime = endTime.minusSeconds(HEALTH_READ_DAYS * SECONDS_PER_DAY),
                endTime = endTime,
            )
            var importedWeightCount = 0
            if (result != null && HealthConnectDataType.BODY_WEIGHT in dataTypes) {
                val entries = result.weightSamples.map { sample ->
                    BodyWeightEntry(
                        id = UUID.nameUUIDFromBytes(
                            "health-connect|${sample.sourcePackage}|${sample.recordId}"
                                .toByteArray(Charsets.UTF_8),
                        ).toString(),
                        weight = Weight.from(sample.kilograms, WeightUnit.KILOGRAM),
                        measuredAt = sample.measuredAt,
                        source = BodyWeightSource.HEALTH_CONNECT,
                    )
                }
                val importResult = profileRepository.mergeBodyWeightEntries(entries)
                if (importResult is DataResult.Success) importedWeightCount = importResult.value
            }
            operation.update {
                it.copy(
                    isReadingHealthData = false,
                    healthReadResult = result?.let { read ->
                        HealthReadUiResult(
                            steps = read.steps,
                            distanceKilometers = read.distanceMeters?.div(1_000.0),
                            caloriesKilocalories = read.caloriesKilocalories,
                            averageHeartRate = read.averageHeartRate,
                            exerciseSessionCount = read.exerciseSessionCount,
                            exerciseDurationMinutes = read.exerciseDurationMinutes,
                            sleepMinutes = read.sleepMinutes,
                            importedWeightCount = importedWeightCount,
                        )
                    },
                )
            }
        }
    }

    private fun HevyImportPreview.asUiModel(): HevyFileUiModel {
        val earliest = earliestAt
        val latest = latestAt
        val range = if (earliest != null && latest != null) {
            "${PHOTO_DATE_FORMATTER.format(earliest.atZone(ZoneId.systemDefault()))} - " +
                PHOTO_DATE_FORMATTER.format(latest.atZone(ZoneId.systemDefault()))
        } else {
            "Sem datas válidas"
        }
        return HevyFileUiModel(
            fileName = fileName,
            recordCount = recordCount,
            dateRangeLabel = range,
            skippedRows = skippedRowCount,
        )
    }

    private fun HevyImportResult.asUiModel(): HevyImportUiResult = HevyImportUiResult(
        workoutsImported = workoutsImported,
        workoutsAlreadyImported = workoutsAlreadyImported,
        measurementsImported = measurementsImported,
        measurementsAlreadyImported = measurementsAlreadyImported,
        skippedRows = skippedRows,
    )

    private data class SettingsOperationState(
        val isCheckingHealthConnect: Boolean = true,
        val isSyncingHealthHistory: Boolean = false,
        val healthSyncResult: HealthSyncUiResult? = null,
        val isReadingHealthData: Boolean = false,
        val healthReadResult: HealthReadUiResult? = null,
        val profileEditor: ProfileEditorUiState? = null,
        val weightEditor: BodyWeightEditorUiState? = null,
        val isImportingPhoto: Boolean = false,
        val profileWriteFailed: Boolean = false,
        val workoutSourceUri: String? = null,
        val measurementSourceUri: String? = null,
        val workoutPreview: HevyImportPreview? = null,
        val measurementPreview: HevyImportPreview? = null,
        val isInspectingHevyFile: Boolean = false,
        val isImportingHevy: Boolean = false,
        val hevyImportFailed: Boolean = false,
        val hevyImportResult: HevyImportResult? = null,
        val isExportingData: Boolean = false,
        val dataExportResult: DataExportUiResult? = null,
        val isRestoringData: Boolean = false,
        val dataRestoreResult: DataRestoreUiResult? = null,
        val isCloudBackupRunning: Boolean = false,
        val cloudBackupResult: CloudBackupUiResult? = null,
        val isSigningIn: Boolean = false,
        val accountOperationFailed: Boolean = false,
        val accountEditor: AccountAuthEditorUiState? = null,
        val accountNotice: AccountNoticeUi? = null,
    )

    private fun Double.toInputValue(): String =
        if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

    private fun Double.toCompactValue(): String = when {
        this >= 1_000_000 -> "%.1f mi".format(this / 1_000_000)
        this >= 1_000 -> "%.1f mil".format(this / 1_000)
        else -> toInputValue()
    }

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

    private fun WeightUnit.shortLabel(): String = if (this == WeightUnit.KILOGRAM) "kg" else "lb"

    private companion object {
        const val MAX_PROFILE_NAME_LENGTH = 80
        const val MAX_EMAIL_LENGTH = 254
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 128
        const val MAX_WEIGHT_INPUT_LENGTH = 6
        const val MIN_BIRTH_YEAR = 1900
        const val MIN_HEIGHT_CENTIMETERS = 100
        const val MAX_HEIGHT_CENTIMETERS = 250
        const val MIN_BODY_WEIGHT = 20.0
        const val MAX_BODY_WEIGHT = 500.0
        const val HEALTH_READ_DAYS = 30L
        const val SECONDS_PER_DAY = 86_400L
        val PHOTO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.forLanguageTag("pt-BR"),
        )
    }
}
