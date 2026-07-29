package com.forgeflow.core.platform.location

import com.forgeflow.core.model.WorkoutLocation

interface CurrentLocationProvider {
    suspend fun captureCurrentLocation(): WorkoutLocation?
}
