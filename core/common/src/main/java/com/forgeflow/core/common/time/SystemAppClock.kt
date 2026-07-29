package com.forgeflow.core.common.time

import java.time.Instant
import javax.inject.Inject

class SystemAppClock @Inject constructor() : AppClock {
    override fun now(): Instant = Instant.now()
}
