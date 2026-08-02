package com.forgeflow.app.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgeflow.app.icon.LauncherIconManager
import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.data.auth.AuthRepository
import com.forgeflow.core.data.profile.ProfileRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import com.forgeflow.core.model.AccentColor
import com.forgeflow.core.model.AccountSession
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.ThemePreference
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.UserProfile
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.gramsIn
import com.forgeflow.core.platform.notification.ActiveWorkoutNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AppUiState(
    val isSettingsLoaded: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val weeklyWorkoutGoal: Int = 3,
    val compactMode: Boolean = false,
    val auth: AppAuthUiState = AppAuthUiState(),
    val profileSetup: AppProfileSetupUiState = AppProfileSetupUiState(),
    val showProfileSetup: Boolean = false,
    val showTutorial: Boolean = false,
    val showGuidedWorkoutTutorial: Boolean = false,
    val hasRequestedNotificationPermission: Boolean = false,
    val activeWorkout: AppActiveWorkoutUiModel? = null,
)

@Immutable
data class AppAuthUiState(
    val isConfigured: Boolean = false,
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isWorking: Boolean = false,
    val operationFailed: Boolean = false,
    val notice: AppAuthNotice? = null,
)

enum class AppAuthNotice {
    PASSWORD_RESET_SENT,
}

@Immutable
data class AppProfileSetupUiState(
    val displayName: String = "",
    val email: String? = null,
    val birthYear: Int? = null,
    val heightCentimeters: Int? = null,
    val bodyWeight: Double? = null,
    val trainingGoal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
)

data class ProfileSetupSubmission(
    val displayName: String,
    val birthYear: Int,
    val heightCentimeters: Int,
    val bodyWeight: Double,
    val trainingGoal: TrainingGoal,
    val experienceLevel: ExperienceLevel,
)

@Immutable
data class AppActiveWorkoutUiModel(
    val id: String,
    val name: String,
    val completedSets: Int,
    val totalSets: Int,
    val exerciseCount: Int,
    val totalVolumeLabel: String,
    val startedAtEpochMillis: Long,
)

private data class AppOperationState(
    val tutorialRequested: Boolean = false,
    val tutorialDismissedForSession: Boolean = false,
    val guidedWorkoutTutorialRequested: Boolean = false,
    val isRequestingGoogleCredential: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authOperationFailed: Boolean = false,
    val authNotice: AppAuthNotice? = null,
    val isSavingProfile: Boolean = false,
    val profileSaveFailed: Boolean = false,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    workoutRepository: WorkoutRepository,
    private val activeWorkoutNotifier: ActiveWorkoutNotifier,
    private val launcherIconManager: LauncherIconManager,
) : ViewModel() {
    private val operation = MutableStateFlow(AppOperationState())

    val uiState = combine(
        settingsRepository.observeSettings(),
        workoutRepository.observeActiveWorkout(),
        authRepository.observeSession(),
        profileRepository.observeProfile(),
        operation,
    ) { settings, workoutResult, session, profile, currentOperation ->
        val activeWorkout = (workoutResult as? DataResult.Success)
            ?.value
            ?.let { workout ->
                AppActiveWorkoutUiModel(
                    id = workout.session.id.value,
                    name = workout.session.name,
                    completedSets = workout.completedSetCount,
                    totalSets = workout.totalSetCount,
                    exerciseCount = workout.exercises.size,
                    totalVolumeLabel = "${
                        workout.totalVolumeGrams
                            .gramsIn(settings.weightUnit)
                            .asNotificationValue()
                    } ${settings.weightUnit.shortLabel()}",
                    startedAtEpochMillis = workout.session.startedAt.toEpochMilli(),
                )
            }
        val profileCompletedForAccount = session != null &&
            settings.profileCompletedForUserId == session.userId
        val showTutorial = profileCompletedForAccount && shouldShowTutorial(
            hasCompletedOnboarding = settings.hasCompletedOnboarding,
            requested = currentOperation.tutorialRequested,
            dismissedForSession = currentOperation.tutorialDismissedForSession,
        )
        AppUiState(
            isSettingsLoaded = true,
            themePreference = settings.themePreference,
            accentColor = settings.accentColor,
            weightUnit = settings.weightUnit,
            weeklyWorkoutGoal = settings.weeklyWorkoutGoal,
            compactMode = settings.compactMode,
            auth = session.asUiState(
                isConfigured = authRepository.isConfigured(),
                operation = currentOperation,
            ),
            profileSetup = profile.asSetupUiState(
                session = session,
                unit = settings.weightUnit,
                operation = currentOperation,
            ),
            showProfileSetup = session != null && !profileCompletedForAccount,
            showTutorial = showTutorial,
            showGuidedWorkoutTutorial = profileCompletedForAccount &&
                currentOperation.guidedWorkoutTutorialRequested &&
                !showTutorial,
            hasRequestedNotificationPermission = settings.hasRequestedNotificationPermission,
            activeWorkout = activeWorkout,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AppUiState(),
    )

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings()
                .map { it.accentColor }
                .distinctUntilChanged()
                .collect(launcherIconManager::scheduleUpdate)
        }
        viewModelScope.launch {
            uiState.collect { state ->
                state.activeWorkout?.let { activeWorkoutNotifier.show(it) }
                    ?: activeWorkoutNotifier.cancel()
            }
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        operation.update { it.copy(isRequestingGoogleCredential = false) }
        authenticate { authRepository.signInWithGoogleIdToken(idToken) }
    }

    fun onGoogleSignInStarted(): Boolean {
        val current = operation.value
        if (current.isRequestingGoogleCredential || current.isAuthenticating) return false
        operation.update {
            it.copy(
                isRequestingGoogleCredential = true,
                authOperationFailed = false,
                authNotice = null,
            )
        }
        return true
    }

    fun onGoogleSignInFailed() {
        operation.update {
            it.copy(
                isRequestingGoogleCredential = false,
                isAuthenticating = false,
                authOperationFailed = true,
                authNotice = null,
            )
        }
    }

    fun onEmailSignIn(email: String, password: String) {
        authenticate { authRepository.signInWithEmail(email, password) }
    }

    fun onCreateAccount(email: String, password: String) {
        authenticate { authRepository.createAccountWithEmail(email, password) }
    }

    fun onPasswordReset(email: String) {
        if (operation.value.isAuthenticating || operation.value.isRequestingGoogleCredential) return
        operation.update {
            it.copy(isAuthenticating = true, authOperationFailed = false, authNotice = null)
        }
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            operation.update {
                it.copy(
                    isAuthenticating = false,
                    authOperationFailed = result is DataResult.Failure,
                    authNotice = if (result is DataResult.Success) {
                        AppAuthNotice.PASSWORD_RESET_SENT
                    } else {
                        null
                    },
                )
            }
        }
    }

    fun onAuthFeedbackDismissed() {
        operation.update { it.copy(authOperationFailed = false, authNotice = null) }
    }

    fun onProfileCompleted(submission: ProfileSetupSubmission) {
        if (operation.value.isSavingProfile || !submission.isValid()) return
        viewModelScope.launch {
            operation.update { it.copy(isSavingProfile = true, profileSaveFailed = false) }
            val session = authRepository.observeSession().first()
            val currentProfile = profileRepository.observeProfile().first()
            if (session == null) {
                operation.update { it.copy(isSavingProfile = false, profileSaveFailed = true) }
                return@launch
            }
            val unit = uiState.value.weightUnit
            val newWeight = Weight.from(submission.bodyWeight, unit)
            val saveResult = profileRepository.saveProfile(
                currentProfile.copy(
                    displayName = submission.displayName.trim(),
                    birthYear = submission.birthYear,
                    heightCentimeters = submission.heightCentimeters,
                    bodyWeight = newWeight,
                    trainingGoal = submission.trainingGoal,
                    experienceLevel = submission.experienceLevel,
                ),
            )
            if (saveResult is DataResult.Success && currentProfile.bodyWeight != newWeight) {
                profileRepository.addBodyWeight(
                    weight = newWeight,
                    measuredAt = Instant.now(),
                    bodyFatPercent = null,
                )
            }
            val completionFailed = saveResult !is DataResult.Success ||
                settingsRepository.setProfileCompletedForUserId(session.userId) is DataResult.Failure
            operation.update {
                it.copy(
                    isSavingProfile = false,
                    profileSaveFailed = completionFailed,
                )
            }
        }
    }

    fun onNotificationPermissionRequested() {
        viewModelScope.launch {
            settingsRepository.setNotificationPermissionRequested(true)
        }
    }

    fun onTutorialRequested() {
        operation.update {
            it.copy(tutorialDismissedForSession = false, tutorialRequested = true)
        }
    }

    fun onTutorialCompleted(weightUnit: WeightUnit, weeklyWorkoutGoal: Int) {
        operation.update {
            it.copy(tutorialDismissedForSession = true, tutorialRequested = false)
        }
        viewModelScope.launch {
            settingsRepository.setWeightUnit(weightUnit)
            settingsRepository.setWeeklyWorkoutGoal(weeklyWorkoutGoal)
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun onGuidedWorkoutTutorialRequested() {
        operation.update { it.copy(guidedWorkoutTutorialRequested = true) }
    }

    fun onGuidedWorkoutTutorialDismissed() {
        operation.update { it.copy(guidedWorkoutTutorialRequested = false) }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (!granted) return
        uiState.value.activeWorkout?.let { activeWorkoutNotifier.show(it) }
    }

    private fun authenticate(
        request: suspend () -> DataResult<AccountSession>,
    ) {
        if (operation.value.isAuthenticating) return
        operation.update {
            it.copy(
                isRequestingGoogleCredential = false,
                isAuthenticating = true,
                authOperationFailed = false,
                authNotice = null,
            )
        }
        viewModelScope.launch {
            val result = request()
            val hasActiveSession = authRepository.observeSession().first() != null
            operation.update {
                it.copy(
                    isAuthenticating = false,
                    authOperationFailed = shouldReportAuthFailure(
                        requestFailed = result is DataResult.Failure,
                        hasActiveSession = hasActiveSession,
                    ),
                )
            }
        }
    }

    private fun ActiveWorkoutNotifier.show(workout: AppActiveWorkoutUiModel) {
        show(
            workoutName = workout.name,
            completedSets = workout.completedSets,
            totalSets = workout.totalSets,
            exerciseCount = workout.exerciseCount,
            totalVolumeLabel = workout.totalVolumeLabel,
            startedAtEpochMillis = workout.startedAtEpochMillis,
        )
    }

    private fun Double.asNotificationValue(): String =
        NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 1
        }.format(this)

    private fun WeightUnit.shortLabel(): String = when (this) {
        WeightUnit.KILOGRAM -> "kg"
        WeightUnit.POUND -> "lb"
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun AccountSession?.asUiState(
    isConfigured: Boolean,
    operation: AppOperationState,
): AppAuthUiState =
    AppAuthUiState(
        isConfigured = isConfigured,
        isSignedIn = this != null,
        displayName = this?.displayName,
        email = this?.email,
        photoUrl = this?.photoUrl,
        isWorking = operation.isRequestingGoogleCredential || operation.isAuthenticating,
        operationFailed = this == null && operation.authOperationFailed,
        notice = operation.authNotice,
    )

internal fun shouldReportAuthFailure(
    requestFailed: Boolean,
    hasActiveSession: Boolean,
): Boolean = requestFailed && !hasActiveSession

private fun UserProfile.asSetupUiState(
    session: AccountSession?,
    unit: WeightUnit,
    operation: AppOperationState,
): AppProfileSetupUiState = AppProfileSetupUiState(
    displayName = displayName.ifBlank { session?.displayName.orEmpty() },
    email = session?.email,
    birthYear = birthYear,
    heightCentimeters = heightCentimeters,
    bodyWeight = bodyWeight?.valueIn(unit),
    trainingGoal = trainingGoal,
    experienceLevel = experienceLevel,
    isSaving = operation.isSavingProfile,
    saveFailed = operation.profileSaveFailed,
)

internal fun ProfileSetupSubmission.isValid(currentYear: Int = LocalDate.now().year): Boolean =
    displayName.trim().length >= 2 &&
        birthYear in 1900..currentYear &&
        heightCentimeters in 100..250 &&
        bodyWeight in 20.0..500.0

internal fun shouldShowTutorial(
    hasCompletedOnboarding: Boolean,
    requested: Boolean,
    dismissedForSession: Boolean,
): Boolean = requested || (!hasCompletedOnboarding && !dismissedForSession)
