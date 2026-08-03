package com.forgeflow.feature.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.forgeflow.core.designsystem.theme.ForgeFlowTheme
import com.forgeflow.feature.settings.R
import org.junit.Rule
import org.junit.Test

class SettingsScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun supportScreen_opensProCatalogDirectly() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        composeRule.setContent {
            ForgeFlowTheme {
                SupportScreen(
                    state = SettingsUiState(),
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.support_top_bar))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.account_pro_includes))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.account_pro_yearly))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun profile_prioritizesProgressBeforeAccount() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        composeRule.setContent {
            ForgeFlowTheme {
                ProfileScreen(
                    state = SettingsUiState(account = AccountUiModel(isLoaded = true)),
                    onAction = {},
                    onSelectAvatar = {},
                    pendingAvatarUri = null,
                    onConfirmAvatarCrop = { _, _, _, _ -> },
                    onDismissAvatarCrop = {},
                    onOpenProgressPhotos = {},
                    onGoogleSignIn = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.weight_evolution_title))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.account_title))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun settings_keepsVisualAndTrainingPreferencesDiscoverable() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        composeRule.setContent {
            ForgeFlowTheme {
                SettingsScreen(
                    state = SettingsUiState(),
                    onAction = {},
                    onRequestHealthPermissions = {},
                    onSelectWorkoutCsv = {},
                    onSelectMeasurementCsv = {},
                    onOpenHealthDashboard = {},
                    onOpenTutorial = {},
                    onOpenGuidedWorkoutTutorial = {},
                    onCreateDataExport = {},
                    onSelectDataRestore = {},
                    onRestartApp = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.appearance_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.compact_mode_title))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.training_title))
            .performScrollTo()
            .assertIsDisplayed()
    }
}
