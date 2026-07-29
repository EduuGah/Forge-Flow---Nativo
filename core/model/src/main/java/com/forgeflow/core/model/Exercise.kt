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

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: Set<MuscleGroup>,
    val equipment: Equipment,
    val instructions: String,
    val isCustom: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
