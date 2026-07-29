package com.forgeflow.core.model

import java.time.Instant

data class WorkoutLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val capturedAt: Instant,
    val label: String? = null,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude is out of range" }
        require(longitude in -180.0..180.0) { "Longitude is out of range" }
        require(accuracyMeters == null || accuracyMeters >= 0f) {
            "Location accuracy cannot be negative"
        }
    }
}
