package com.forgeflow.app.icon

import com.forgeflow.core.model.AccentColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LauncherIconUpdateQueueTest {
    @Test
    fun latestScheduledColorWins() {
        val queue = LauncherIconUpdateQueue()

        queue.schedule(AccentColor.CYAN)
        queue.schedule(AccentColor.GREEN)

        assertEquals(AccentColor.GREEN, queue.take())
        assertNull(queue.take())
    }

    @Test
    fun retryDoesNotOverwriteANewerRequest() {
        val queue = LauncherIconUpdateQueue()

        queue.schedule(AccentColor.CYAN)
        val failedColor = queue.take()
        queue.schedule(AccentColor.RED)
        queue.retry(requireNotNull(failedColor))

        assertEquals(AccentColor.RED, queue.take())
    }
}
