package com.forgeflow.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppTutorialStateTest {
    @Test
    fun firstLaunch_showsTutorialUntilDismissed() {
        assertTrue(
            shouldShowTutorial(
                hasCompletedOnboarding = false,
                requested = false,
                dismissedForSession = false,
            ),
        )
        assertFalse(
            shouldShowTutorial(
                hasCompletedOnboarding = false,
                requested = false,
                dismissedForSession = true,
            ),
        )
    }

    @Test
    fun completedTutorial_onlyShowsWhenReplayIsRequested() {
        assertFalse(
            shouldShowTutorial(
                hasCompletedOnboarding = true,
                requested = false,
                dismissedForSession = false,
            ),
        )
        assertTrue(
            shouldShowTutorial(
                hasCompletedOnboarding = true,
                requested = true,
                dismissedForSession = false,
            ),
        )
    }
}
