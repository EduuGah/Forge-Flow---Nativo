package com.forgeflow.core.common.time

import java.time.Instant

interface AppClock {
    fun now(): Instant
}
