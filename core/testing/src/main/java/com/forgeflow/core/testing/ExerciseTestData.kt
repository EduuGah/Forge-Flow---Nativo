package com.forgeflow.core.testing

import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.MuscleGroup
import java.time.Instant

fun exerciseTestData(
    id: String = "04f35c8f-e525-469e-8238-25e31087e07a",
    name: String = "Supino com barra",
    primaryMuscleGroup: MuscleGroup = MuscleGroup.CHEST,
    equipment: Equipment = Equipment.BARBELL,
): Exercise = Exercise(
    id = ExerciseId(id),
    name = name,
    primaryMuscleGroup = primaryMuscleGroup,
    secondaryMuscleGroups = emptySet(),
    equipment = equipment,
    instructions = "",
    isCustom = false,
    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
)
