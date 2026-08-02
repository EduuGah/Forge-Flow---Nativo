package com.forgeflow.core.model

import java.time.Instant

enum class TrainingGoal {
    STRENGTH,
    HYPERTROPHY,
    GENERAL_FITNESS,
}

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

data class ProgressPhoto(
    val id: String,
    val filePath: String,
    val capturedAt: Instant,
)

enum class BodyWeightSource {
    MANUAL,
    HEVY,
    HEALTH_CONNECT,
}

data class BodyWeightEntry(
    val id: String,
    val weight: Weight,
    val measuredAt: Instant,
    val bodyFatPercent: Double? = null,
    val source: BodyWeightSource = BodyWeightSource.MANUAL,
)

data class UserProfile(
    val ownerUserId: String? = null,
    val displayName: String = "",
    val birthYear: Int? = null,
    val heightCentimeters: Int? = null,
    val bodyWeight: Weight? = null,
    val trainingGoal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val profilePhotoPath: String? = null,
    val bodyWeightHistory: List<BodyWeightEntry> = emptyList(),
    val progressPhotos: List<ProgressPhoto> = emptyList(),
)
