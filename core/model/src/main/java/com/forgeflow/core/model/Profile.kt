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

data class UserProfile(
    val displayName: String = "",
    val birthYear: Int? = null,
    val heightCentimeters: Int? = null,
    val bodyWeight: Weight? = null,
    val trainingGoal: TrainingGoal = TrainingGoal.HYPERTROPHY,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val progressPhotos: List<ProgressPhoto> = emptyList(),
)
