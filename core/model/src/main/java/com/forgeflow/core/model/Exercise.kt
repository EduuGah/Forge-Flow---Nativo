package com.forgeflow.core.model

import java.time.Instant

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    BICEPS,
    TRICEPS,
    CALVES,
    CORE,
    FULL_BODY,
}

enum class Equipment {
    BARBELL,
    DUMBBELL,
    MACHINE,
    CABLE,
    BODYWEIGHT,
    OTHER,
}

enum class ExerciseMediaType {
    IMAGE,
    ANIMATED_IMAGE,
}

object ExerciseMediaUris {
    const val BENCH_PRESS = "forgeflow://exercise/bench-press"
    const val BACK_SQUAT = "forgeflow://exercise/back-squat"
    const val DEADLIFT = "forgeflow://exercise/deadlift"
    const val BARBELL_ROW = "forgeflow://exercise/barbell-row"
    const val OVERHEAD_PRESS = "forgeflow://exercise/overhead-press"
}

data class ExerciseMedia(
    val uri: String,
    val type: ExerciseMediaType,
    val thumbnailUri: String? = null,
) {
    init {
        require(uri.isNotBlank()) { "Exercise media URI cannot be blank" }
    }
}

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: Set<MuscleGroup>,
    val equipment: Equipment,
    val instructions: String,
    val media: ExerciseMedia? = null,
    val isCustom: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
