package com.forgeflow.app.ui

import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.TrainingGoal
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountEntryStateTest {
    @Test
    fun signIn_requiresValidEmailAndPassword() {
        assertTrue(
            isValidAuthForm(
                mode = AccountEntryMode.SIGN_IN,
                email = "atleta@forgeflow.app",
                password = "senha-forte",
                confirmation = "",
            ),
        )
        assertFalse(
            isValidAuthForm(
                mode = AccountEntryMode.SIGN_IN,
                email = "atleta",
                password = "1234567",
                confirmation = "",
            ),
        )
    }

    @Test
    fun accountCreation_requiresMatchingPasswords() {
        assertFalse(
            isValidAuthForm(
                mode = AccountEntryMode.CREATE_ACCOUNT,
                email = "atleta@forgeflow.app",
                password = "senha-forte",
                confirmation = "outra-senha",
            ),
        )
    }

    @Test
    fun profileSetup_rejectsImplausibleValues() {
        val valid = ProfileSetupSubmission(
            displayName = "Atleta ForgeFlow",
            birthYear = 1995,
            heightCentimeters = 178,
            bodyWeight = 82.5,
            trainingGoal = TrainingGoal.HYPERTROPHY,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
        )

        assertTrue(valid.isValid(currentYear = 2026))
        assertFalse(valid.copy(birthYear = 2027).isValid(currentYear = 2026))
        assertFalse(valid.copy(bodyWeight = 0.0).isValid(currentYear = 2026))
    }

    @Test
    fun authenticationFailure_isHiddenWhenFirebaseAlreadyHasAnActiveSession() {
        assertTrue(shouldReportAuthFailure(requestFailed = true, hasActiveSession = false))
        assertFalse(shouldReportAuthFailure(requestFailed = true, hasActiveSession = true))
        assertFalse(shouldReportAuthFailure(requestFailed = false, hasActiveSession = false))
    }
}
